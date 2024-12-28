package com.kmaebashi.kanjiro.service;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import com.kmaebashi.jsonparser.ClassMapper;
import com.kmaebashi.kanjiro.controller.data.PossibleDatesTable;
import com.kmaebashi.kanjiro.controller.data.UserAnswers;
import com.kmaebashi.kanjiro.dbaccess.AnswerDbAccess;
import com.kmaebashi.kanjiro.dbaccess.EventDbAccess;
import com.kmaebashi.kanjiro.dbaccess.PossibleDateDbAccess;
import com.kmaebashi.kanjiro.dto.AnswerDto;
import com.kmaebashi.kanjiro.dto.DateAnswerDto;
import com.kmaebashi.kanjiro.dto.EventDto;
import com.kmaebashi.kanjiro.dto.PossibleDateDto;
import com.kmaebashi.kanjiro.util.CsrfUtil;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.JsonResult;
import com.kmaebashi.nctfw.ServiceContext;
import com.kmaebashi.nctfw.ServiceInvoker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class GuestPageService {
    private GuestPageService() {}

    public static DocumentResult showPage(ServiceInvoker invoker, String eventId, String deviceId,
                                          String nextCsrfToken) {
        return invoker.invoke((context) -> {
            Path htmlPath = context.getHtmlTemplateDirectory().resolve("guest.html");
            Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");

            DocumentResult ret = new DocumentResult(doc);

            renderEventInfo(context, doc, eventId);
            List<AnswerDto> answerDtoList = renderMessageArea(context, doc, eventId);
            PossibleDatesTable pdt = getPossibleDatesTable(context, eventId, answerDtoList);
            String pdtJson = ClassMapper.toJson(pdt);
            Element scriptElem = doc.getElementById("server-side-include-script");
            scriptElem.text("const possibleDatesTable = " + pdtJson);
            Element pdtTableElem = doc.getElementById("possible-dates-table");
            pdtTableElem.empty();

            CsrfUtil.addCsrfToken(ret, nextCsrfToken);

            return ret;
        });
    }

    private static void renderEventInfo(ServiceContext context, Document doc, String eventId) {
        EventDto eventDto = EventDbAccess.getEvent(context.getDbAccessInvoker(), eventId);

        Element eventNameElem = doc.getElementById("event-name");
        eventNameElem.text(eventDto.eventName);
        Element organizerNameElem = doc.getElementById("organizer-name");
        organizerNameElem.text(eventDto.organizerName);
        Element eventDescriptionElem = doc.getElementById("event-description");
        eventDescriptionElem.html(HtmlUtil.escapeHtml2(eventDto.description));
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

    static PossibleDatesTable getPossibleDatesTable(ServiceContext context, String eventId, List<AnswerDto> answerDtoList) {
        List<PossibleDateDto> possibleDateDtoList
                = PossibleDateDbAccess.getPossbleDates(context.getDbAccessInvoker(), eventId);
        HashMap<String, Integer> possibleDateIdToIndex = new HashMap<>();
        HashMap<String, Integer> userIdToIndex = new HashMap<>();

        PossibleDatesTable ret = new PossibleDatesTable();
        ret.possibleDateNames = new String[possibleDateDtoList.size()];
        int pdIdx = 0;
        for (PossibleDateDto pd : possibleDateDtoList) {
            possibleDateIdToIndex.put(pd.possibleDateId, pdIdx);
            ret.possibleDateNames[pdIdx] = pd.name;
            pdIdx++;
        }
        ret.userAnswers = new UserAnswers[answerDtoList.size()];
        int ansIdx = 0;
        for (AnswerDto ans : answerDtoList) {
            userIdToIndex.put(ans.userId, ansIdx);
            UserAnswers ua = new UserAnswers();
            ua.userId = ans.userId;
            ua.userName = ans.userName;
            ua.isProtected = ans.isProtected;
            ua.answers = new int[possibleDateDtoList.size()];
            ret.userAnswers[ansIdx] = ua;
            ansIdx++;
        }
        List<DateAnswerDto> dateAnswerDtoList = AnswerDbAccess.getDateAnswers(context.getDbAccessInvoker(), eventId);

        for (DateAnswerDto daDto : dateAnswerDtoList) {
            ret.userAnswers[userIdToIndex.get(daDto.userId)].answers[possibleDateIdToIndex.get(daDto.possibleDateId)]
                    = daDto.answer;
        }

        return ret;
    }

}
