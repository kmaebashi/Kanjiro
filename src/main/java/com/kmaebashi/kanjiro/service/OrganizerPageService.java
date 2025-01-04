package com.kmaebashi.kanjiro.service;

import com.kmaebashi.jsonparser.ClassMapper;
import com.kmaebashi.kanjiro.controller.data.EventInfo;
import com.kmaebashi.kanjiro.controller.data.PostEventInfoResult;
import com.kmaebashi.kanjiro.dbaccess.AnswerDbAccess;
import com.kmaebashi.kanjiro.dbaccess.AuthenticationDbAccess;
import com.kmaebashi.kanjiro.dbaccess.EventDbAccess;
import com.kmaebashi.kanjiro.dbaccess.PossibleDateDbAccess;
import com.kmaebashi.kanjiro.dto.AnswerDto;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrganizerPageService {
    private OrganizerPageService() {}

    public static DocumentResult showTopPage(ServiceInvoker invoker, String deviceId, String nextCsrfToken) {
        return invoker.invoke((context) -> {
            Path htmlPath = context.getHtmlTemplateDirectory().resolve("top.html");
            Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");
            renderBlankPage(doc);
            renderOrganizerName(context, doc, deviceId);

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
            renderBlankPage(doc);
            renderEventUrl(context, doc, requestUrl, eventId);
            renderOrganizerName(context, doc, deviceId);
            renderEventInfo(context, doc, eventId);

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

    private static void renderEventInfo(ServiceContext context, Document doc, String eventId) {
        EventDto eventDto = EventDbAccess.getEvent(context.getDbAccessInvoker(), eventId);
        if (eventDto == null) {
            throw new BadRequestException("そのイベントはありません。");
        }
        Element eventNameInputElem = doc.getElementById("event-name-input");
        eventNameInputElem.attr("value", eventDto.eventName);
        Element eventDescriptionElem = doc.getElementById("event-description");
        eventDescriptionElem.text(eventDto.description);
        Element appendTimeInputElem = doc.getElementById("schedule-append-time");
        appendTimeInputElem.attr("value", eventDto.appendTime);
        List<PossibleDateDto> dtoList
                = PossibleDateDbAccess.getPossbleDates(context.getDbAccessInvoker(), eventId);
        StringBuilder scheduleTextSb = new StringBuilder();
        for (PossibleDateDto dto: dtoList) {
            scheduleTextSb.append(dto.name + "\r\n");
        }
        Element scheduleTextElem = doc.getElementById("schedule-textarea");
        scheduleTextElem.text(scheduleTextSb.toString());

        Element isSecretModeCheckElem = doc.getElementById("is-secret-mode");
        isSecretModeCheckElem.attr("checked", eventDto.isSecretMode);

        Element autoScheduleCheckElem = doc.getElementById("auto-schedule");
        autoScheduleCheckElem.attr("checked", eventDto.isAutoSchedule);
    }

    public static JsonResult mergeEventInfo(ServiceInvoker invoker, String deviceId, EventInfo eventInfo) {
        return invoker.invoke((context) -> {
            Logger logger = context.getLogger();
            logger.info("eventInfo.eventName.." + eventInfo.eventName);



            boolean createNew = false;
            if (eventInfo.eventId == null) {
                createNew = true;
            } else {
                EventDto eventDto = EventDbAccess.getEvent(context.getDbAccessInvoker(), eventInfo.eventId);
                if (eventDto == null) {
                    createNew = true;
                }
            }
            PostEventInfoResult result;
            if (createNew) {
                result = createNewEvent(context, deviceId, eventInfo);
            } else {
                if (!eventInfo.registerForce) {
                    String[] updatedAtBuf = new String[1];
                    List<AnswerDto> answerDtoList
                            = AnswerDbAccess.getAnswers(context.getDbAccessInvoker(), eventInfo.eventId);
                    String checkResult = checkExistingEvent(context, eventInfo, updatedAtBuf);
                    if (checkResult == null) {
                        result = mergeEvent(context, eventInfo);
                    } else {
                        result = new PostEventInfoResult(eventInfo.eventId, false, checkResult, updatedAtBuf[0]);
                    }
                } else {
                    result = mergeEvent(context, eventInfo);
                }
            }
            String json = ClassMapper.toJson(result);

            return new JsonResult(json);
        });
    }

    private static DateTimeFormatter lastUpdateFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    static PostEventInfoResult createNewEvent(ServiceContext context, String deviceId, EventInfo eventInfo) {
        String userId = DbUtil.getOrCreateUser(context, deviceId, eventInfo.organizerName);
        String eventId = UuidUtil.getUniqueId();
        EventDbAccess.insertEvent(context.getDbAccessInvoker(),
                                  eventId, eventInfo.organizerName, userId,
                                  eventInfo.eventName, eventInfo.eventDescription,
                                  eventInfo.appendTime, eventInfo.isSecretMode, eventInfo.isAutoSchedule);

        int displayOrder = 1;
        for (String possibleDateStr : eventInfo.scheduleArray) {
            String possibleDateId = UuidUtil.getUniqueId();
            PossibleDateDbAccess.insertPossibleDate(context.getDbAccessInvoker(),
                                                possibleDateId, eventId, possibleDateStr, displayOrder);
            displayOrder++;
        }
        EventDto eventDto = EventDbAccess.getEvent(context.getDbAccessInvoker(), eventId);
        String lastUpdateAt = lastUpdateFormatter.format(eventDto.updatedAt);

        return new PostEventInfoResult(eventId, true, null, lastUpdateAt);
    }

    static String checkExistingEvent(ServiceContext context, EventInfo eventInfo, String[] updatedAtBuf) {


        return null;
    }

    static PostEventInfoResult mergeEvent(ServiceContext context, EventInfo eventInfo) {
        return null;
    }
}
