package com.kmaebashi.kanjiro.service;

import com.kmaebashi.jsonparser.ClassMapper;
import com.kmaebashi.kanjiro.common.Answer;
import com.kmaebashi.kanjiro.controller.data.EventInfo;
import com.kmaebashi.kanjiro.controller.data.PostEventInfoResult;
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
import com.kmaebashi.kanjiro.util.RandomIdGenerator;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.JsonResult;
import com.kmaebashi.nctfw.ServiceContext;
import com.kmaebashi.nctfw.ServiceInvoker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import com.kmaebashi.simplelogger.Logger;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class OrganizerPageService {
    private OrganizerPageService() {}

    public static DocumentResult showTopPage(ServiceInvoker invoker, String deviceId, String nextCsrfToken) {
        return invoker.invoke((context) -> {
            Path htmlPath = context.getHtmlTemplateDirectory().resolve("top.html");
            Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");
            renderBlankPage(doc);
            renderOrganizerName(context, doc, deviceId);
            renderDeadline(context, doc, null);

            DocumentResult ret = new DocumentResult(doc);
            CsrfUtil.addCsrfToken(ret, nextCsrfToken);

            return ret;
        });
    }

    public static DocumentResult showEditEventPage(ServiceInvoker invoker, String eventId, String deviceId,
                                                   String nextCsrfToken, String requestUrl) {
        return invoker.invoke((context) -> {
            Path htmlPath = context.getHtmlTemplateDirectory().resolve("edit_event.html");
            Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");

            EventDto eventDto = EventDbAccess.getEvent(context.getDbAccessInvoker(), eventId);
            if (eventDto == null) {
                throw new BadRequestException("そのイベントはありません。");
            }
            renderBlankPage(doc);
            renderEventUrl(context, doc, requestUrl, eventId);
            renderOrganizerName(context, doc, deviceId);
            List<PossibleDateDto> possibleDateDtoList
                    = PossibleDateDbAccess.getPossbleDates(context.getDbAccessInvoker(), eventDto.eventId);
            renderEventInfo(context, doc, eventDto, possibleDateDtoList);
            renderDeadline(context, doc, eventDto);
            renderFixDateArea(context, doc, eventDto, possibleDateDtoList);

            DocumentResult ret = new DocumentResult(doc);
            CsrfUtil.addCsrfToken(ret, nextCsrfToken);

            return ret;
        });
    }

    private static void renderBlankPage(Document doc) {
        Element calendarDiv = doc.getElementById("calendar-div");
        calendarDiv.empty();
    }

    private static void renderEventUrl(ServiceContext context, Document doc, String requestUrl, String eventId) {
        String editUrl = requestUrl + "?eventId=" + eventId;
        Element editAElem = doc.getElementById("event-edit-url");
        editAElem.attr("href", editUrl);
        editAElem.text(editUrl);

        String guestUrl = requestUrl.replaceFirst("/event$", "/guest") + "?eventId=" + eventId;
        Element guestAElem = doc.getElementById("event-guest-url");
        guestAElem.attr("href", guestUrl);
        guestAElem.text(guestUrl);
    }

    private static void renderOrganizerName(ServiceContext context, Document doc, String deviceId) {
        UserDto userDto = AuthenticationDbAccess.getUserByDeviceId(context.getDbAccessInvoker(), deviceId);
        if (userDto != null) {
            Element organizerInputElem = doc.getElementById("organizer-name-input");
            organizerInputElem.attr("value", userDto.name);
        }
    }

    private static void renderEventInfo(ServiceContext context, Document doc, EventDto eventDto,
                                        List<PossibleDateDto> possibleDateDtoList) {
        Element eventNameInputElem = doc.getElementById("event-name-input");
        eventNameInputElem.attr("value", eventDto.eventName);
        Element eventDescriptionElem = doc.getElementById("event-description");
        eventDescriptionElem.text(eventDto.description);
        Element appendTimeInputElem = doc.getElementById("schedule-append-time");
        appendTimeInputElem.attr("value", eventDto.appendTime);

        StringBuilder scheduleTextSb = new StringBuilder();
        for (PossibleDateDto dto: possibleDateDtoList) {
            scheduleTextSb.append(dto.name + "\r\n");
        }
        Element scheduleTextElem = doc.getElementById("schedule-textarea");
        scheduleTextElem.text(scheduleTextSb.toString());

        Element isSecretModeCheckElem = doc.getElementById("is-secret-mode");
        isSecretModeCheckElem.attr("checked", eventDto.isSecretMode);

        Element autoScheduleCheckElem = doc.getElementById("auto-schedule");
        autoScheduleCheckElem.attr("checked", eventDto.isAutoSchedule);
    }

    private static void renderDeadline(ServiceContext context, Document doc, EventDto eventDto) {
        Element deadlineCheck = doc.getElementById("set-deadline");
        Element deadlineDate = doc.getElementById("deadline-date");
        Element deadlineTime = doc.getElementById("deadline-time");

        LocalDateTime deadlineLDT = null;
        if (eventDto != null && eventDto.deadline != null) {
            deadlineCheck.attr("checked", true);
            deadlineLDT = eventDto.deadline;
        } else {
            deadlineCheck.attr("checked", false);
            LocalDateTime nowLDT = LocalDateTime.now();
            deadlineLDT = LocalDateTime.of(nowLDT.getYear(), nowLDT.getMonth(), nowLDT.getDayOfMonth(),
                                           23, 59);
        }
        String deadlineStr = deadlineFormatter.format(deadlineLDT);
        String[] deadlineArray = deadlineStr.split(" ");
        deadlineDate.attr("value", deadlineArray[0]);
        deadlineTime.attr("value", deadlineArray[1]);
    }

    private static void renderFixDateArea(ServiceContext context, Document doc, EventDto eventDto,
                                          List<PossibleDateDto> possibleDateDtoList) {
        Element ulElem = doc.getElementById("fix-date-list");

        Element firstLiElem = ulElem.getElementsByTag("li").first();
        ulElem.empty();

        Element undecidedLiElem = firstLiElem.clone();
        setFixDateLi(undecidedLiElem, "undecided", "未定", eventDto.fixedDateId == null);
        ulElem.appendChild(undecidedLiElem);

        for (PossibleDateDto dto : possibleDateDtoList) {
            Element liElem = firstLiElem.clone();
            setFixDateLi(liElem, dto.possibleDateId, dto.name, dto.possibleDateId.equals(eventDto.fixedDateId));
            ulElem.appendChild(liElem);
        }
    }

    private static void setFixDateLi(Element liElem, String possibleDateKey, String possibleDateName, boolean selected) {
        Element inputElem = liElem.getElementsByTag("input").first();
        inputElem.attr("value", possibleDateKey);
        inputElem.attr("checked", selected);
        liElem.getElementsByClass("date-name").first().text(possibleDateName);
    }

    private static DateTimeFormatter deadlineFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static PostEventInfoResult createNewEvent(ServiceInvoker invoker, String deviceId, EventInfo eventInfo) {
        return invoker.invoke((context) -> {
            String userId = DbUtil.getOrCreateUser(context, deviceId, eventInfo.organizerName);
            String eventId = UuidUtil.getUniqueId();

            LocalDateTime deadline = eventInfo.eventDeadline == null
                                        ? null : LocalDateTime.parse(eventInfo.eventDeadline, deadlineFormatter);
            EventDbAccess.insertEvent(context.getDbAccessInvoker(),
                    eventId, eventInfo.organizerName, userId,
                    eventInfo.eventName, eventInfo.eventDescription, deadline,
                    eventInfo.appendTime, eventInfo.isSecretMode, eventInfo.isAutoSchedule);

            int displayOrder = 1;
            for (String possibleDateStr : eventInfo.scheduleArray) {
                String possibleDateId = UuidUtil.getUniqueId();
                PossibleDateDbAccess.insertPossibleDate(context.getDbAccessInvoker(),
                        possibleDateId, eventId, possibleDateStr, displayOrder);
                displayOrder++;
            }
            return new PostEventInfoResult(eventId, true, null, null);
        });
    }

    public static PostEventInfoResult modifyEventInfo(ServiceInvoker invoker, String deviceId, EventInfo eventInfo) {
        return invoker.invoke((context) -> {
            Logger logger = context.getLogger();
            logger.info("eventInfo.eventName.." + eventInfo.eventName);

            String loginUser = AuthenticationDbAccess.getUserIdByDeviceId(context.getDbAccessInvoker(), deviceId);
            EventDto eventDto = EventDbAccess.getEvent(context.getDbAccessInvoker(), eventInfo.eventId);
            if (loginUser == null || !loginUser.equals(eventDto.organizierId)) {
                throw new BadRequestException("認証エラー。非幹事からのイベント変更。");
            }
            PostEventInfoResult result;

            List<PossibleDateDto> possibleDateDtoList
                    = PossibleDateDbAccess.getPossbleDates(context.getDbAccessInvoker(), eventInfo.eventId);
            List<AnswerDto> answerDtoList
                    = AnswerDbAccess.getAnswers(context.getDbAccessInvoker(), eventInfo.eventId);
            String lastUpdate = getAnswerLastUpdate(answerDtoList);
            List<DateAnswerDto> dateAnswerDtoList
                    = AnswerDbAccess.getDateAnswers(context.getDbAccessInvoker(), eventInfo.eventId);

            if (!eventInfo.registerForce
                || (eventInfo.updatedAt != null && !eventInfo.updatedAt.equals(lastUpdate))) {
                String checkResult = checkExistingAnswer(eventInfo,
                                          possibleDateDtoList, answerDtoList, dateAnswerDtoList);
                if (checkResult == null) {
                    result = mergeEvent(context, eventInfo, possibleDateDtoList, answerDtoList,
                                        dateAnswerDtoList);
                } else {
                    result = new PostEventInfoResult(eventInfo.eventId, false, checkResult, lastUpdate);
                }
            } else {
                result = mergeEvent(context, eventInfo, possibleDateDtoList, answerDtoList,
                                    dateAnswerDtoList);
            }
            return result;
        });
    }

    private static DateTimeFormatter lastUpdateFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    static String getAnswerLastUpdate(List<AnswerDto> answerDtoList) {
        LocalDateTime lastUpdatedAt = null;
        for (AnswerDto ad : answerDtoList) {
            if (lastUpdatedAt == null || ad.updatedAt.isAfter(lastUpdatedAt)) {
                lastUpdatedAt = ad.updatedAt;
            }
        }
        if (lastUpdatedAt == null) {
            return null;
        } else {
            return lastUpdateFormatter.format(lastUpdatedAt);
        }
    }

    record DeleteAnswer(PossibleDateDto possibleDateDto, LinkedHashMap<String, Integer> userIdAnswer) {}


    static String checkExistingAnswer(EventInfo eventInfo,
                                     List<PossibleDateDto> possibleDateDtoList, List<AnswerDto> answerDtoList,
                                     List<DateAnswerDto> dateAnswerDtoList) {
        HashMap<String, String> userIdName = new HashMap<>();
        for (AnswerDto ad : answerDtoList) {
            userIdName.put(ad.userId, ad.userName);
        }
        List<String> newScheduleArray = Arrays.asList(eventInfo.scheduleArray);
        List<DeleteAnswer> deleteAnswerList = new ArrayList<>();
        HashMap<String, String> toBeDeleted = new HashMap<>();
        for (PossibleDateDto pd : possibleDateDtoList) {
            if (!newScheduleArray.contains(pd.name)) {
                deleteAnswerList.add(new DeleteAnswer(pd, new LinkedHashMap<String, Integer>()));
                toBeDeleted.put(pd.name, pd.possibleDateId);
            }
        }
        for (DeleteAnswer delAns : deleteAnswerList) {
            for (DateAnswerDto dateAns : dateAnswerDtoList) {
                if (dateAns.possibleDateId.equals(delAns.possibleDateDto.possibleDateId)) {
                    if (dateAns.answer > Answer.UNKNOWN.getValue()) {
                        continue;
                    }
                    if (delAns.userIdAnswer.containsKey(dateAns.userId)) {
                        if (dateAns.answer < delAns.userIdAnswer().get(dateAns.userId)) {
                            delAns.userIdAnswer().put(dateAns.userId, dateAns.answer);
                        }
                    } else {
                        delAns.userIdAnswer().put(dateAns.userId, dateAns.answer);
                    }
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (DeleteAnswer delAns : deleteAnswerList) {
            if (!delAns.userIdAnswer().isEmpty()) {
                sb.append("日程「" + delAns.possibleDateDto.name + "」を削除しようとしています。この日程には、");
                boolean isFirst = true;
                for (String userId : delAns.userIdAnswer.keySet()) {
                    if (!isFirst) {
                        sb.append(", ");
                    }
                    isFirst = false;
                    sb.append("" + userIdName.get(userId) + "さん");
                }
                sb.append("が、〇または△の回答をしています。\n");
            }
        }

        if (sb.length() == 0) {
            return null;
        } else {
            return sb.toString();
        }
    }

    static PostEventInfoResult mergeEvent(ServiceContext context, EventInfo eventInfo,
                                          List<PossibleDateDto> possibleDateDtoList, List<AnswerDto> answerDtoList,
                                          List<DateAnswerDto> dateAnswerDtoList) {

        LocalDateTime deadline = eventInfo.eventDeadline == null
                ? null : LocalDateTime.parse(eventInfo.eventDeadline, deadlineFormatter);
        EventDbAccess.updateEvent(context.getDbAccessInvoker(), eventInfo.eventId, eventInfo.organizerName,
                                  eventInfo.eventName, eventInfo.eventDescription, deadline, eventInfo.appendTime,
                                  eventInfo.fixedDate, eventInfo.isSecretMode, eventInfo.isAutoSchedule);

        List<String> newScheduleArray = Arrays.asList(eventInfo.scheduleArray);
        for (PossibleDateDto pd : possibleDateDtoList) {
            if (!newScheduleArray.contains(pd.name)) {
                PossibleDateDbAccess.logicalDeletePossibleDate(context.getDbAccessInvoker(),
                                                               pd.eventId, pd.possibleDateId);
            }
        }
        int displayOrder = 1;
        for (String name : eventInfo.scheduleArray) {
            String newPossibleDateId = UuidUtil.getUniqueId();
            PossibleDateDbAccess.upsertPossibleDate(context.getDbAccessInvoker(),
                                                    eventInfo.eventId, newPossibleDateId, name, displayOrder);
            displayOrder++;
        }

        return null;
    }
}
