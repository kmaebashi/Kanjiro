package com.kmaebashi.kanjiro.service;

import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.kanjiro.KanjiroTestUtil;
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
import com.kmaebashi.kanjiro.util.Log;
import com.kmaebashi.nctfw.DbAccessContext;
import com.kmaebashi.nctfw.DbAccessInvoker;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.ServiceContext;
import com.kmaebashi.nctfw.ServiceInvoker;
import com.kmaebashi.nctfwimpl.DbAccessContextImpl;
import com.kmaebashi.nctfwimpl.DbAccessInvokerImpl;
import com.kmaebashi.nctfwimpl.ServiceContextImpl;
import com.kmaebashi.nctfwimpl.ServiceInvokerImpl;
import com.kmaebashi.simplelogger.Logger;
import com.kmaebashi.simpleloggerimpl.FileLogger;
import org.jsoup.nodes.Element;
import org.jsoup.select.CombiningEvaluator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrganizerPageServiceTest {
    private static Connection conn;
    private static Logger logger;

    @BeforeAll
    static void connectDb() throws Exception {
        OrganizerPageServiceTest.conn = KanjiroTestUtil.getConnection();
        OrganizerPageServiceTest.logger = new FileLogger("./log", "OrganizerPageServiceTest");
        Log.setLogger(logger);
        deleteAll();
    }

    private static void deleteAll() throws Exception {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);

        KanjiroTestUtil.deleteAll(context, "USERS");
        KanjiroTestUtil.deleteAll(context, "DEVICES");
        KanjiroTestUtil.deleteAll(context, "EVENTS");
        KanjiroTestUtil.deleteAll(context, "POSSIBLE_DATES");
        KanjiroTestUtil.deleteAll(context, "ANSWERS");
        KanjiroTestUtil.deleteAll(context, "DATE_ANSWERS");
    }

    @AfterAll
    static void closeDb() throws Exception {
        conn.close();
    }
    @Test
    void showTopPageTest001() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);

        // Cookieなしでの初回閲覧
        DocumentResult dr = OrganizerPageService.showTopPage(si, "deviceId", "CSRFTOKEN");
        String html = dr.getDocument().html();
        Element mockCalendar = dr.getDocument().getElementById("calendar-table");
        assertNull(mockCalendar);
    }

    @Test
    void createNewEventTest001() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker di = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(di,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker  si = new ServiceInvokerImpl(sc);
        EventInfo eventInfo = new EventInfo();
        eventInfo.organizerName = "Test001幹事";
        eventInfo.eventName = "Test001送別会";
        eventInfo.eventDescription = "Test001さんの送別会です。";
        eventInfo.scheduleArray = new String[] {
                "12/24(火) 17:00～",
                "12/25(水) 17:00～",
                "12/26(木) 17:00～"
        };
        eventInfo.appendTime = "17:00～";
        eventInfo.isSecretMode = true;
        eventInfo.isAutoSchedule = true;
        eventInfo.registerForce = false;
        eventInfo.updatedAt = "20241221123315111";

        OrganizerPageService.createNewEvent(si, "createNewEventTest001_", eventInfo);
    }

    @Test
    void checkExistingEventTest001() {
        EventInfo eventInfo = new EventInfo();
        eventInfo.eventId = "EventId001";
        eventInfo.scheduleArray = new String[] {"1月1日(水) 19:00～", "1月4日(土) 19:00～"};
        List<PossibleDateDto> possibleDateDtoList = new ArrayList<>();
        possibleDateDtoList.add(new PossibleDateDto("PossibleDateId001", "EventId001", "1月1日(水) 19:00～", 1));
        possibleDateDtoList.add(new PossibleDateDto("PossibleDateId002", "EventId001", "1月2日(木) 19:00～", 2));
        possibleDateDtoList.add(new PossibleDateDto("PossibleDateId003", "EventId001", "1月3日(金) 19:00～", 3));
        possibleDateDtoList.add(new PossibleDateDto("PossibleDateId004", "EventId001", "1月4日(土) 19:00～", 4));
        List<AnswerDto> answerDtoList = new ArrayList<>();
        answerDtoList.add(new AnswerDto("UserId001", "ユーザ1", "みなさんよろしく1", true));
        answerDtoList.add(new AnswerDto("UserId002", "ユーザ2", "みなさんよろしく2", true));
        answerDtoList.add(new AnswerDto("UserId003", "ユーザ3", "みなさんよろしく3", true));
        answerDtoList.add(new AnswerDto("UserId004", "ユーザ4", "みなさんよろしく4", true));
        List<DateAnswerDto> dateAnswerDtoList = new ArrayList<>();
        dateAnswerDtoList.add(new DateAnswerDto("UserId001", "PossibleDateId001", 1));
        dateAnswerDtoList.add(new DateAnswerDto("UserId001", "PossibleDateId002", 1));
        dateAnswerDtoList.add(new DateAnswerDto("UserId001", "PossibleDateId003", 1));
        dateAnswerDtoList.add(new DateAnswerDto("UserId001", "PossibleDateId004", 1));
        dateAnswerDtoList.add(new DateAnswerDto("UserId002", "PossibleDateId001", 3));
        dateAnswerDtoList.add(new DateAnswerDto("UserId002", "PossibleDateId002", 3));
        dateAnswerDtoList.add(new DateAnswerDto("UserId002", "PossibleDateId003", 3));
        dateAnswerDtoList.add(new DateAnswerDto("UserId002", "PossibleDateId004", 3));
        dateAnswerDtoList.add(new DateAnswerDto("UserId003", "PossibleDateId001", 1));
        dateAnswerDtoList.add(new DateAnswerDto("UserId003", "PossibleDateId002", 2));
        dateAnswerDtoList.add(new DateAnswerDto("UserId003", "PossibleDateId003", 3));
        dateAnswerDtoList.add(new DateAnswerDto("UserId003", "PossibleDateId004", 1));
        dateAnswerDtoList.add(new DateAnswerDto("UserId004", "PossibleDateId001", 3));
        dateAnswerDtoList.add(new DateAnswerDto("UserId004", "PossibleDateId002", 3));
        dateAnswerDtoList.add(new DateAnswerDto("UserId004", "PossibleDateId003", 3));
        dateAnswerDtoList.add(new DateAnswerDto("UserId004", "PossibleDateId004", 3));

        String ret = OrganizerPageService.checkExistingAnswer(eventInfo, possibleDateDtoList,
                                                              answerDtoList, dateAnswerDtoList);
        assertEquals("日程「1月2日(木) 19:00～」を削除しようとしています。この日程には、ユーザ1さん, ユーザ3さんが、〇または△の回答をしています。\n" +
                "日程「1月3日(金) 19:00～」を削除しようとしています。この日程には、ユーザ1さんが、〇または△の回答をしています。\n", ret);
    }

    @Test
    void modifyEventInfoTest001() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker di = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(di,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);

        AuthenticationDbAccess.upsertDevice(di, "OrgPSrvMergeEvtDevi001", LocalDateTime.now(), "dummysecret");
        AuthenticationDbAccess.insertUser(di, "OrgPSrvMergeEvtUser001", "幹事太郎");
        AuthenticationDbAccess.setUserToDevice(di, "OrgPSrvMergeEvtDevi001", "OrgPSrvMergeEvtUser001");

        EventDbAccess.insertEvent(di, "OrgPSrvMergeEvtTest001", "幹事太郎", "OrgPSrvMergeEvtUser001",
                "なんとかさん送別会", "なんとかさんの送別会です。\r\n盛大に送り出しましょう。", LocalDateTime.of(2025, 1, 31, 23, 59),
                "19:00～", false, false);
        PossibleDateDbAccess.insertPossibleDate(di, "OrgPSrvMergeEvtPD001__", "OrgPSrvMergeEvtTest001", "10/01(月)", 1);
        PossibleDateDbAccess.insertPossibleDate(di, "OrgPSrvMergeEvtPD002__", "OrgPSrvMergeEvtTest001", "10/02(火)", 2);
        PossibleDateDbAccess.insertPossibleDate(di, "OrgPSrvMergeEvtPD003__", "OrgPSrvMergeEvtTest001", "10/03(水)", 3);
        PossibleDateDbAccess.insertPossibleDate(di, "OrgPSrvMergeEvtPD004__", "OrgPSrvMergeEvtTest001", "10/04(木)", 4);
        PossibleDateDbAccess.insertPossibleDate(di, "OrgPSrvMergeEvtPD005__", "OrgPSrvMergeEvtTest001", "10/05(金)", 5);

        EventInfo eventInfo = new EventInfo();
        eventInfo.eventId = "OrgPSrvMergeEvtTest001";
        eventInfo.organizerName = "幹事次郎";
        eventInfo.eventName = "なんとかさん送別会(改)";
        eventInfo.eventDescription = "ひかえめに送り出しましょう。";
        eventInfo.scheduleArray = new String[] {
                "10/01(月)",
                "10/02(火)",
                "10/05(金)",
                "10/06(土)",
                "10/07(日)",
        };
        eventInfo.appendTime = "20:00～";
        eventInfo.isSecretMode = true;
        eventInfo.isAutoSchedule = true;
        eventInfo.registerForce = true;
        eventInfo.updatedAt = null;

        PostEventInfoResult result = OrganizerPageService.modifyEventInfo(si, "OrgPSrvMergeEvtDevi001", eventInfo);
        EventDto eventDto = EventDbAccess.getEvent(di, "OrgPSrvMergeEvtTest001");
        assertEquals("幹事次郎", eventDto.organizerName);
        assertEquals("なんとかさん送別会(改)", eventDto.eventName);
        assertEquals("ひかえめに送り出しましょう。", eventDto.description);
        assertEquals("20:00～", eventDto.appendTime);
        assertEquals(true, eventDto.isSecretMode);
        assertEquals(true, eventDto.isAutoSchedule);
        assertEquals("ひかえめに送り出しましょう。", eventDto.description);

        List<PossibleDateDto> possibleDateDtoList = PossibleDateDbAccess.getPossbleDates(di, "OrgPSrvMergeEvtTest001");
        assertEquals(5, possibleDateDtoList.size());
        assertEquals("10/01(月)", possibleDateDtoList.get(0).name);
        assertEquals("10/02(火)", possibleDateDtoList.get(1).name);
        assertEquals("10/05(金)", possibleDateDtoList.get(2).name);
        assertEquals("10/06(土)", possibleDateDtoList.get(3).name);
        assertEquals("10/07(日)", possibleDateDtoList.get(4).name);
    }

    @Test
    void modifyEventInfoTest002() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker di = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(di,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);

        AuthenticationDbAccess.upsertDevice(di, "OrgPSrvMergeEvtDevi002", LocalDateTime.now(), "dummysecret");
        AuthenticationDbAccess.insertUser(di, "OrgPSrvMergeEvtUser002", "幹事太郎");
        AuthenticationDbAccess.setUserToDevice(di, "OrgPSrvMergeEvtDevi002", "OrgPSrvMergeEvtUser002");

        EventDbAccess.insertEvent(di, "OrgPSrvMergeEvtTest002", "幹事太郎", "OrgPSrvMergeEvtUser002",
                "なんとかさん送別会", "なんとかさんの送別会です。\r\n盛大に送り出しましょう。", LocalDateTime.of(2025, 1, 31, 23, 59),
                "19:00～", false, false);
        PossibleDateDbAccess.insertPossibleDate(di, "OrgPSrvMergeEvt02PD001", "OrgPSrvMergeEvtTest002", "10/01(月)", 1);
        PossibleDateDbAccess.insertPossibleDate(di, "OrgPSrvMergeEvt02PD002", "OrgPSrvMergeEvtTest002", "10/02(火)", 2);
        PossibleDateDbAccess.insertPossibleDate(di, "OrgPSrvMergeEvt02PD003", "OrgPSrvMergeEvtTest002", "10/03(水)", 3);
        PossibleDateDbAccess.insertPossibleDate(di, "OrgPSrvMergeEvt02PD004", "OrgPSrvMergeEvtTest002", "10/04(木)", 4);
        PossibleDateDbAccess.insertPossibleDate(di, "OrgPSrvMergeEvt02PD005", "OrgPSrvMergeEvtTest002", "10/05(金)", 5);

        AnswerDbAccess.insertAnswer(di, "OrgPSrvMergeEvtTest002", "OrgPSrvMergeEvt02User1", "ゲスト1", "よろしく1", false);
        //AnswerDbAccess.insertDateAnswer(di, "OrgPSrvMergeEvtTest002", "OrgPSrvMergeEvt02User1", "ゲスト1", "よろしく1", false);
        EventInfo eventInfo = new EventInfo();
        eventInfo.eventId = "OrgPSrvMergeEvtTest002";
        eventInfo.organizerName = "幹事次郎";
        eventInfo.eventName = "なんとかさん送別会(改)";
        eventInfo.eventDescription = "ひかえめに送り出しましょう。";
        eventInfo.scheduleArray = new String[] {
                "10/01(月)",
                "10/02(火)",
                "10/05(金)",
                "10/06(土)",
                "10/07(日)",
        };
        eventInfo.appendTime = "20:00～";
        eventInfo.isSecretMode = true;
        eventInfo.isAutoSchedule = true;
        eventInfo.registerForce = true;
        eventInfo.updatedAt = null;

        PostEventInfoResult result = OrganizerPageService.modifyEventInfo(si, "OrgPSrvMergeEvtDevi002", eventInfo);
        EventDto eventDto = EventDbAccess.getEvent(di, "OrgPSrvMergeEvtTest002");
        assertEquals("幹事次郎", eventDto.organizerName);
        assertEquals("なんとかさん送別会(改)", eventDto.eventName);
        assertEquals("ひかえめに送り出しましょう。", eventDto.description);
        assertEquals("20:00～", eventDto.appendTime);
        assertEquals(true, eventDto.isSecretMode);
        assertEquals(true, eventDto.isAutoSchedule);
        assertEquals("ひかえめに送り出しましょう。", eventDto.description);

        List<PossibleDateDto> possibleDateDtoList = PossibleDateDbAccess.getPossbleDates(di, "OrgPSrvMergeEvtTest002");
        assertEquals(5, possibleDateDtoList.size());
        assertEquals("10/01(月)", possibleDateDtoList.get(0).name);
        assertEquals("10/02(火)", possibleDateDtoList.get(1).name);
        assertEquals("10/05(金)", possibleDateDtoList.get(2).name);
        assertEquals("10/06(土)", possibleDateDtoList.get(3).name);
        assertEquals("10/07(日)", possibleDateDtoList.get(4).name);
    }
}