package com.kmaebashi.kanjiro.service;

import com.kmaebashi.jsonparser.ClassMapper;
import com.kmaebashi.kanjiro.controller.data.EventInfo;
import com.kmaebashi.kanjiro.controller.data.PostEventInfoResult;
import com.kmaebashi.kanjiro.dbaccess.AuthenticationDbAccess;
import com.kmaebashi.kanjiro.dbaccess.EventDbAccess;
import com.kmaebashi.kanjiro.dbaccess.PossibleDateDbAccess;
import com.kmaebashi.kanjiro.dto.EventDto;
import com.kmaebashi.kanjiro.dto.PossibleDateDto;
import com.kmaebashi.kanjiro.dto.UserDto;
import com.kmaebashi.kanjiro.util.CsrfUtil;
import com.kmaebashi.kanjiro.util.RandomIdGenerator;
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
                                                   String nextCsrfToken) {
        return invoker.invoke((context) -> {
            Path htmlPath = context.getHtmlTemplateDirectory().resolve("edit_event.html");
            Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");
            renderBlankPage(doc);
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

    private static void renderOrganizerName(ServiceContext context, Document doc, String deviceId) {
        UserDto userDto = AuthenticationDbAccess.getUserByDeviceId(context.getDbAccessInvoker(), deviceId);
        if (userDto != null) {
            Element organizerInputElem = doc.getElementById("organizer-name-input");
            organizerInputElem.attr("value", userDto.name);
        }
    }

    private static void renderEventInfo(ServiceContext context, Document doc, String eventId) {
        EventDto eventDto = EventDbAccess.getEvent(context.getDbAccessInvoker(), eventId);

        Element eventNameInputElem = doc.getElementById("event-name-input");
        eventNameInputElem.attr("value", eventDto.eventName);
        Element eventDescriptionElem = doc.getElementById("event-description");
        eventDescriptionElem.text(eventDto.description);

        List<PossibleDateDto> dtoList
                = PossibleDateDbAccess.getPossbleDates(context.getDbAccessInvoker(), eventId);
        StringBuilder scheduleTextSb = new StringBuilder();
        for (PossibleDateDto dto: dtoList) {
            scheduleTextSb.append(dto.name + "\r\n");
        }
        Element scheduleTextElem = doc.getElementById("schedule-textarea");
        scheduleTextElem.text(scheduleTextSb.toString());
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
                                  eventInfo.isSecretMode, eventInfo.isAutoSchedule);

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
