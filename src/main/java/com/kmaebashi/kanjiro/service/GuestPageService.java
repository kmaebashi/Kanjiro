package com.kmaebashi.kanjiro.service;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.kmaebashi.jsonparser.ClassMapper;
import com.kmaebashi.kanjiro.controller.data.AnswerInfo;
import com.kmaebashi.kanjiro.controller.data.DateAnswerInfo;
import com.kmaebashi.kanjiro.controller.data.EventInfo;
import com.kmaebashi.kanjiro.controller.data.PossibleDatesTable;
import com.kmaebashi.kanjiro.controller.data.PostAnswerInfoResult;
import com.kmaebashi.kanjiro.controller.data.UserAnswers;
import com.kmaebashi.kanjiro.dbaccess.AnswerDbAccess;
import com.kmaebashi.kanjiro.dbaccess.AuthenticationDbAccess;
import com.kmaebashi.kanjiro.dbaccess.EventDbAccess;
import com.kmaebashi.kanjiro.dbaccess.PossibleDateDbAccess;
import com.kmaebashi.kanjiro.dto.AnswerDto;
import com.kmaebashi.kanjiro.dto.DateAnswerDto;
import com.kmaebashi.kanjiro.dto.EventDto;
import com.kmaebashi.kanjiro.dto.PossibleDateDto;
import com.kmaebashi.kanjiro.dto.UserDto;
import com.kmaebashi.kanjiro.util.CsrfUtil;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.InternalException;
import com.kmaebashi.nctfw.InvokerOption;
import com.kmaebashi.nctfw.JsonResult;
import com.kmaebashi.nctfw.ServiceContext;
import com.kmaebashi.nctfw.ServiceInvoker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GuestPageService {
    private GuestPageService() {}

    public static DocumentResult showPage(ServiceInvoker invoker, String eventId, String userId, String deviceId,
                                          String nextCsrfToken) {
        return invoker.invoke((context) -> {
            Path htmlPath = context.getHtmlTemplateDirectory().resolve("guest.html");
            Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");
            DocumentResult ret = new DocumentResult(doc);

            EventDto eventDto = EventDbAccess.getEvent(context.getDbAccessInvoker(), eventId);
            if (eventDto == null) {
                throw new BadRequestException("そのイベントはありません。");
            }
            HeaderRenderer.renderLinkDevice(doc, eventId);
            renderEventInfo(context, doc, eventDto);
            List<AnswerDto> answerDtoList = renderMessageArea(context, doc, eventId);
            UserDto deviceUser = AuthenticationDbAccess.getUserByDeviceId(context.getDbAccessInvoker(), deviceId);
            PossibleDatesInfo pdi = getPossibleDatesTable(context, eventDto, deviceUser, answerDtoList);
            setPossibleDatesTable(pdi.pdt(), doc);
            renderDateFixedArea(doc, eventDto, pdi);
            renderAnswerArea(context, doc, pdi.pdt(), answerDtoList, userId, deviceUser, pdi.possibleDateDtoList());

            CsrfUtil.addCsrfToken(ret, nextCsrfToken);

            return ret;
        });
    }

    private static DateTimeFormatter deadlineFormmater = DateTimeFormatter.ofPattern("yyyy年MM月dd日(E)", Locale.JAPANESE);
    
    private static void renderEventInfo(ServiceContext context, Document doc, EventDto eventDto) {
        Element eventNameElem = doc.getElementById("event-name");
        eventNameElem.text(eventDto.eventName);
        Element organizerNameElem = doc.getElementById("organizer-name");
        organizerNameElem.text(eventDto.organizerName);
        Element eventDescriptionElem = doc.getElementById("event-description");
        eventDescriptionElem.html(HtmlUtil.escapeHtml2(eventDto.description));
        Element deadLinePElem = doc.getElementById("deadline-line");
        if (eventDto.deadline == null) {
            deadLinePElem.remove();
        } else {
            String deadlineStr = deadlineFormmater.format(eventDto.deadline);
            Element deadLineSpan = deadLinePElem.getElementById("deadline-date");
            deadLineSpan.text(deadlineStr);
        }
    }

    private static List<AnswerDto> renderMessageArea(ServiceContext context, Document doc, String eventId) {
        List<AnswerDto> answerDtoList = AnswerDbAccess.getAnswers(context.getDbAccessInvoker(), eventId);

        Element dlElem = doc.getElementById("message-list");
        dlElem.empty();

        for (AnswerDto dto: answerDtoList) {
            Element dtElem = doc.createElement("dt");
            dtElem.text(dto.userName);
            dlElem.appendChild(dtElem);

            Element ddElem = doc.createElement("dd");
            ddElem.html(HtmlUtil.escapeHtml2(dto.message));
            dlElem.appendChild(ddElem);
        }
        return answerDtoList;
    }

    record PossibleDatesInfo(PossibleDatesTable pdt, List<PossibleDateDto> possibleDateDtoList) {}
    static PossibleDatesInfo getPossibleDatesTable(ServiceContext context, EventDto eventDto, UserDto deviceUser,
                                                   List<AnswerDto> answerDtoList) {
        List<PossibleDateDto> possibleDateDtoList
                = PossibleDateDbAccess.getPossbleDates(context.getDbAccessInvoker(), eventDto.eventId);
        HashMap<String, Integer> possibleDateIdToIndex = new HashMap<>();

        HashMap<String, Integer> userIdToIndex = new HashMap<>();

        PossibleDatesTable pdt = new PossibleDatesTable();
        pdt.possibleDateNames = new String[possibleDateDtoList.size()];
        int pdIdx = 0;
        for (PossibleDateDto pd : possibleDateDtoList) {
            possibleDateIdToIndex.put(pd.possibleDateId, pdIdx);
            pdt.possibleDateNames[pdIdx] = pd.name;
            pdIdx++;
        }
        pdt.userAnswers = new UserAnswers[answerDtoList.size()];
        int ansIdx = 0;
        for (AnswerDto ans : answerDtoList) {
            userIdToIndex.put(ans.userId, ansIdx);
            UserAnswers ua = new UserAnswers();
            ua.userId = ans.userId;
            ua.userName = ans.userName;
            ua.isProtected = ans.isProtected;
            ua.answers = new int[possibleDateDtoList.size()];
            pdt.userAnswers[ansIdx] = ua;
            ansIdx++;
        }
        List<DateAnswerDto> dateAnswerDtoList
                = AnswerDbAccess.getDateAnswers(context.getDbAccessInvoker(), eventDto.eventId);

        for (DateAnswerDto daDto : dateAnswerDtoList) {
            if (possibleDateIdToIndex.containsKey(daDto.possibleDateId)) {
                int userIdx = userIdToIndex.get(daDto.userId);
                int dateIdx = possibleDateIdToIndex.get(daDto.possibleDateId);
                if (!eventDto.isSecretMode
                    || (deviceUser != null
                        && (deviceUser.userId.equals(eventDto.organizierId)
                            || deviceUser.userId.equals(daDto.userId)))) {
                    pdt.userAnswers[userIdx].answers[dateIdx] = daDto.answer;
                } else {
                    pdt.userAnswers[userIdx].answers[dateIdx] = -1;
                }
            }
        }
        return new PossibleDatesInfo(pdt, possibleDateDtoList);
    }

    static void setPossibleDatesTable(PossibleDatesTable pdt, Document doc) {
        String pdtJson = ClassMapper.toJson(pdt);
        Element scriptElem = doc.getElementById("server-side-include-script");
        scriptElem.text("const possibleDatesTable = " + pdtJson);
        Element pdtTableElem = doc.getElementById("possible-dates-table");
        pdtTableElem.empty();
    }

    static void renderDateFixedArea(Document doc, EventDto eventDto, PossibleDatesInfo pdi) {
        Element dateFixedAreaDivElem = doc.getElementById("date-fixed-area");
        if (eventDto.fixedDateId == null) {
            dateFixedAreaDivElem.remove();
            return;
        }
        int possibleDateIdx = 0;
        for (; possibleDateIdx < pdi.possibleDateDtoList().size(); possibleDateIdx++) {
            if (pdi.possibleDateDtoList().get(possibleDateIdx).possibleDateId.equals(eventDto.fixedDateId)) {
                break;
            }
        }
        Element fixedDatePElem = dateFixedAreaDivElem.getElementById("fixed-date-p");
        fixedDatePElem.text(pdi.possibleDateDtoList().get(possibleDateIdx).name);

        Element guestListElem = dateFixedAreaDivElem.getElementById("guest-list");
        Element firstLiElem = guestListElem.getElementsByTag("li").first();
        guestListElem.empty();

        String[] answerStr = {"〇", "△", "×"};
        for (int ansIdx = 1; ansIdx <= 3; ansIdx++) {
            for (int userIdx = 0; userIdx < pdi.pdt().userAnswers.length; userIdx++) {
                int answer = pdi.pdt().userAnswers[userIdx].answers[possibleDateIdx];
                if (answer == ansIdx) {
                    Element liElem = firstLiElem.clone();
                    liElem.text(answerStr[answer - 1] + " " + pdi.pdt().userAnswers[userIdx].userName);
                    guestListElem.appendChild(liElem);
                }
            }
        }
    }

    static void renderAnswerArea(ServiceContext context, Document doc, PossibleDatesTable pdt,
                                 List<AnswerDto> answerDtoList, String queryUserId, UserDto deviceUser,
                                 List<PossibleDateDto> possibleDateDtoList) {
        UserAnswers ua = null;
        if (queryUserId != null) {
            ua = searchUserAnswers(pdt, queryUserId);
            if (queryUserId != null && ua == null) {
                throw new BadRequestException("不正なユーザIDが指定されています。");
            }
            if (ua.isProtected && deviceUser != null && !queryUserId.equals(deviceUser.userId)) {
                throw new BadRequestException("このユーザの回答はロックされています。");
            }
        } else if (deviceUser != null) {
            ua = searchUserAnswers(pdt, deviceUser.userId);
        }
        Element guestNameElem = doc.getElementById("guest-name");
        if (ua != null) {
            guestNameElem.attr("value", ua.userName);
        } else if (deviceUser != null) {
            guestNameElem.attr("value", deviceUser.name);
        }
        Element replyTableElem = doc.getElementById("reply-table");
        Element firstTr = replyTableElem.getElementsByTag("tr").first();
        replyTableElem.empty();
        for (int dateIdx = 0; dateIdx < pdt.possibleDateNames.length; dateIdx++) {
            Element trElem = firstTr.clone();
            trElem.attr("data-possible-date-id", possibleDateDtoList.get(dateIdx).possibleDateId);
            Element dateNameTdElem = trElem.getElementsByClass("possible-date-name").first();
            dateNameTdElem.text(pdt.possibleDateNames[dateIdx]);
            Element radioTdElem = trElem.getElementsByClass("answer-radio").first();
            Elements radioElems = radioTdElem.getElementsByTag("input");
            for (int radioIdx = 0; radioIdx < 3; radioIdx++) {
                Element radioElem = radioElems.get(radioIdx);
                radioElem.attr("name", "date" + dateIdx);
                if (ua != null) {
                    if (radioIdx + 1 == ua.answers[dateIdx]) {
                        radioElem.attr("checked", true);
                    } else {
                        radioElem.removeAttr("checked");
                    }
                }
            }
            replyTableElem.appendChild(trElem);
        }
        if (ua != null) {
            String message = searchMessage(answerDtoList, ua.userId);
            Element messageElem = doc.getElementById("comment-textarea");
            messageElem.text(message);
        }
    }

    static UserAnswers searchUserAnswers(PossibleDatesTable pdt, String userId) {
        for (UserAnswers ua : pdt.userAnswers) {
            if (ua.userId.equals(userId)) {
                return ua;
            }
        }
        return null;
    }

    static String searchMessage(List<AnswerDto> answerDtoList, String userId)
    {
        if (userId == null) {
            return null;
        }
        for (AnswerDto dto : answerDtoList) {
            if (dto.userId.equals(userId)) {
                return dto.message;
            }
        }
        throw new InternalException("回答が見つかりません。");
    }

    public static JsonResult postAnswerInfo(ServiceInvoker invoker, String deviceId, AnswerInfo answerInfo) {
        return invoker.invoke((context) -> {
            String deviceUserId = DbUtil.getOrCreateUser(context, deviceId, answerInfo.userName);
            String userId = deviceUserId;
            if (answerInfo.userId != null) {
                if (!deviceUserId.equals(answerInfo.userId)) {
                    AnswerDto dbAnswer = AnswerDbAccess.getAnswer(context.getDbAccessInvoker(),
                            answerInfo.eventId, answerInfo.userId);
                    if (dbAnswer.isProtected) {
                        throw new BadRequestException("この回答はロックされています。", true);
                    }
                }
                userId = answerInfo.userId;
            }

            AnswerDbAccess.upsertAnswer(context.getDbAccessInvoker(), answerInfo.eventId, userId,
                    answerInfo.userName, answerInfo.message, answerInfo.isProtected);
            AnswerDbAccess.deleteDateAnswer(context.getDbAccessInvoker(), answerInfo.eventId, userId);
            for (DateAnswerInfo dai : answerInfo.answers) {
                AnswerDbAccess.insertDateAnswer(context.getDbAccessInvoker(), answerInfo.eventId, userId,
                                                dai.possibleDateId, dai.answer);
            }
            PostAnswerInfoResult result = new PostAnswerInfoResult("成功しました");
            String json = ClassMapper.toJson(result);

            return new JsonResult(json);
        }, InvokerOption.TRANSACTIONAL);
    }
}
