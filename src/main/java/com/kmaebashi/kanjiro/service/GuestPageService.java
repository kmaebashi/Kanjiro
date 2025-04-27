package com.kmaebashi.kanjiro.service;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.kmaebashi.jsonparser.ClassMapper;
import com.kmaebashi.kanjiro.common.Constants;
import com.kmaebashi.kanjiro.controller.data.AnswerInfo;
import com.kmaebashi.kanjiro.controller.data.DateAnswerInfo;
import com.kmaebashi.kanjiro.controller.data.DeleteAnswerInfo;
import com.kmaebashi.kanjiro.controller.data.DeleteAnswerResult;
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
            List<AnswerDto> answerDtoList = AnswerDbAccess.getAnswers(context.getDbAccessInvoker(), eventId);
            UserDto deviceUser = AuthenticationDbAccess.getUserByDeviceId(context.getDbAccessInvoker(), deviceId);
            PossibleDatesInfo pdi = getPossibleDatesTable(context, eventDto, deviceUser, answerDtoList);
            if (eventDto.isAutoSchedule && eventDto.fixedDateId == null
                    && eventDto.deadline.isAfter(LocalDateTime.now())) {
                eventDto.fixedDateId = makeSchedule(pdi);
            }

            HeaderRenderer.renderLinkDevice(doc, eventId);
            renderEventInfo(context, doc, eventDto);
            renderMessageArea(doc, answerDtoList);
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

    private static void renderMessageArea(Document doc, List<AnswerDto> answerDtoList) {

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
    }

    record PossibleDatesInfo(PossibleDatesTable pdt, List<PossibleDateDto> possibleDateDtoList, String[] guestList) {}
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
            ua.updatedAt = Constants.lastUpdateFormatter.format(ans.updatedAt);
            pdt.userAnswers[ansIdx] = ua;
            ansIdx++;
        }
        pdt.deviceUser = deviceUser != null ? deviceUser.userId : null;
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
        String[] guestList = null;
        if (eventDto.fixedDateId != null) {
            guestList = createGuestList(answerDtoList, dateAnswerDtoList, eventDto.fixedDateId);
        }
        return new PossibleDatesInfo(pdt, possibleDateDtoList, guestList);
    }

    private static String[] createGuestList(List<AnswerDto> answerDtoList, List<DateAnswerDto> dateAnswerDtoList,
                                            String fixedDateId) {
        HashMap<String, String> userNameHash = new HashMap<>();
        for (AnswerDto ansDto : answerDtoList) {
            userNameHash.put(ansDto.userId, ansDto.userName);
        }
        List<String> guestList = new ArrayList<String>();
        String[] answerStr = {"〇", "△", "×"};
        for (int ansIdx = 1; ansIdx <= 3; ansIdx++) {
            for (DateAnswerDto dateAnsDto : dateAnswerDtoList) {
                if (dateAnsDto.answer == ansIdx && dateAnsDto.possibleDateId.equals(fixedDateId)) {
                    guestList.add(answerStr[ansIdx - 1] + "　" + userNameHash.get(dateAnsDto.userId));
                }
            }
        }
        return guestList.toArray(new String[0]);
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

        for (String guestStr : pdi.guestList) {
            Element liElem = firstLiElem.clone();
            liElem.text(guestStr);
            guestListElem.appendChild(liElem);
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
        } else {
            Element deleteButtonDivElem = doc.getElementById("delete-button-div-id");
            deleteButtonDivElem.remove();
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

    public static String makeSchedule(PossibleDatesInfo pdi) {
        List<Integer> maxOkDateIdxes = getMaxOkPossibleDates(pdi.pdt());
        if (maxOkDateIdxes == null) {
            return null;
        }
        List<Integer> maxUnknownDateIdxes = getMaxUnknownPossibleDates(pdi.pdt(), maxOkDateIdxes);
        List<Integer> maxScoreIdxes = getMaxScorePossibleDates(pdi.pdt(), maxUnknownDateIdxes);

        int dateIdx = maxScoreIdxes.get(0);

        return pdi.possibleDateDtoList.get(dateIdx).possibleDateId;
    }

    private static List<Integer> getMaxOkPossibleDates(PossibleDatesTable pdt) {
        List<Integer> maxPds = null;
        int maxOk = 0;

        for (int i = 0; i < pdt.possibleDateNames.length; i++) {
            int okCount = countDateAnswer(pdt, i, 1); // 1は○
            if (okCount > maxOk) {
                maxOk = okCount;
                maxPds = new ArrayList<>();
                maxPds.add(i);
            } else if (maxPds != null && okCount == maxOk) {
                maxPds.add(i);
            }
        }
        return maxPds;
    }

    private static List<Integer> getMaxUnknownPossibleDates(PossibleDatesTable pdt, List<Integer> maxOkDateIdxes) {
        List<Integer> maxPds = null;
        int maxUnknown = -1;

        for (int dateIdx : maxOkDateIdxes) {
            int unknownCount = countDateAnswer(pdt, dateIdx, 2); // 2は△
            if (unknownCount > maxUnknown) {
                maxUnknown = unknownCount;
                maxPds = new ArrayList<>();
                maxPds.add(dateIdx);
            } else if (maxPds != null && unknownCount == maxUnknown) {
                maxPds.add(dateIdx);
            }
        }
        return maxPds;
    }

    private static int countDateAnswer(PossibleDatesTable pdt, int dateIdx, int answer) {
        int count = 0;

        for (int userIdx = 0; userIdx < pdt.userAnswers.length; userIdx++) {
            if (pdt.userAnswers[userIdx].answers[dateIdx] == answer) {
                count++;
            }
        }

        return count;
    }

    private static List<Integer> getMaxScorePossibleDates(PossibleDatesTable pdt, List<Integer> dateIdxes) {
        List<Integer> maxPds = null;
        int maxScore = -1;

        for (int dateIdx : dateIdxes) {
            int dateScore = calcDateScore(pdt, dateIdx);
            if (dateScore > maxScore) {
                maxScore = dateScore;
                maxPds = new ArrayList<>();
                maxPds.add(dateIdx);
            } else if (maxPds != null && dateScore == maxScore) {
                maxPds.add(dateIdx);
            }
        }
        return maxPds;
    }

    private static int calcDateScore(PossibleDatesTable pdt, int dateIdx) {
        int score = 0;

        for (int userIdx = 0; userIdx < pdt.userAnswers.length; userIdx++) {
            if (pdt.userAnswers[userIdx].answers[dateIdx] == 1) {
                score += pdt.userAnswers.length - userIdx;
            }
        }

        return score;
    }

    public static JsonResult postAnswerInfo(ServiceInvoker invoker, String deviceId, AnswerInfo answerInfo) {
        return invoker.invoke((context) -> {
            EventDto eventDto = EventDbAccess.getEvent(context.getDbAccessInvoker(), answerInfo.eventId);
            if (eventDto == null) {
                throw new BadRequestException("そのイベントはありません。");
            }
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

    public static JsonResult deleteAnswer(ServiceInvoker invoker, String deviceId, DeleteAnswerInfo deleteAnswerInfo) {
        return invoker.invoke((context) -> {
            EventDto eventDto = EventDbAccess.getEvent(context.getDbAccessInvoker(), deleteAnswerInfo.eventId);
            if (eventDto == null) {
                throw new BadRequestException("そのイベントはありません。");
            }
            String targetUserId = null;
            String deviceUserId = AuthenticationDbAccess.getUserIdByDeviceId(context.getDbAccessInvoker(), deviceId);
            if (deleteAnswerInfo.userId == null) {
                targetUserId = deviceUserId;
            } else {
                targetUserId = deleteAnswerInfo.userId;
            }
            if (targetUserId == null) {
                throw new BadRequestException("削除対象が指定されていません。");
            }
            AnswerDto targetAnswer = AnswerDbAccess.getAnswer(context.getDbAccessInvoker(), deleteAnswerInfo.eventId, targetUserId);
            if (targetAnswer == null) {
                throw new BadRequestException("削除対象が存在しません。");
            }
            if (targetAnswer.isProtected && !targetAnswer.userId.equals(deviceUserId)) {
                throw new BadRequestException("この回答はロックされています。");
            }
            if (!deleteAnswerInfo.updatedAt.equals(Constants.lastUpdateFormatter.format(targetAnswer.updatedAt))) {
                throw new BadRequestException("この画面を開いている間に回答が修正されています。");
            }
            if (!deleteAnswerInfo.deleteForce) {
                if (deviceUserId == null) {
                    DeleteAnswerResult dar = new DeleteAnswerResult(false, "あなたはユーザとして登録されていません。");
                    return ResultFactory.createJsonResult(dar);
                }
                if (!deviceUserId.equals(targetUserId)) {
                    DeleteAnswerResult dar = new DeleteAnswerResult(false, "他人の回答を削除しようとしています。");
                    return ResultFactory.createJsonResult(dar);
                }
            }
            AnswerDbAccess.deleteDateAnswer(context.getDbAccessInvoker(), deleteAnswerInfo.eventId, targetUserId);
            AnswerDbAccess.deleteAnswer(context.getDbAccessInvoker(), deleteAnswerInfo.eventId, targetUserId);
            DeleteAnswerResult dar = new DeleteAnswerResult(true, "成功しました。");
            return ResultFactory.createJsonResult(dar);
        });
    }
}
