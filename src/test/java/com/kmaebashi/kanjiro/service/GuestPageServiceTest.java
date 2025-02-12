package com.kmaebashi.kanjiro.service;

import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.kanjiro.KanjiroTestUtil;
import com.kmaebashi.kanjiro.controller.data.PossibleDatesTable;
import com.kmaebashi.kanjiro.dbaccess.AnswerDbAccess;
import com.kmaebashi.kanjiro.dbaccess.AuthenticationDbAccess;
import com.kmaebashi.kanjiro.dbaccess.EventDbAccess;
import com.kmaebashi.kanjiro.dbaccess.PossibleDateDbAccess;
import com.kmaebashi.kanjiro.dto.AnswerDto;
import com.kmaebashi.kanjiro.dto.EventDto;
import com.kmaebashi.kanjiro.dto.UserDto;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GuestPageServiceTest {
    private static Connection conn;
    private static Logger logger;

    @BeforeAll
    static void connectDb() throws Exception {
        GuestPageServiceTest.conn = KanjiroTestUtil.getConnection();
        GuestPageServiceTest.logger = new FileLogger("./log", "GuestPageServiceTest");
        Log.setLogger(logger);
        deleteAll();
        insertEvent001();
        insertEvent002();
    }

    private static void deleteAll() throws Exception {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);

        KanjiroTestUtil.deleteAll(context, "EVENTS");
        KanjiroTestUtil.deleteAll(context, "USERS");
        KanjiroTestUtil.deleteAll(context, "DEVICES");
        KanjiroTestUtil.deleteAll(context, "POSSIBLE_DATES");
        KanjiroTestUtil.deleteAll(context, "ANSWERS");
        KanjiroTestUtil.deleteAll(context, "DATE_ANSWERS");
    }

    private static void insertEvent001() {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);

        String eventId = "GuestPageSTestEvent001";
        EventDbAccess.insertEvent(invoker, eventId, "幹事太郎", "GuestPageSTestOUser001",
                "なんとかさん送別会", "なんとかさんの送別会です。\r\n盛大に送り出しましょう。", LocalDateTime.of(2025, 1, 31, 23, 59),
                "19:00～", false, false);
        AuthenticationDbAccess.insertUser(invoker, "GuestPageSTestOUser001", "幹事太郎2");
        AuthenticationDbAccess.insertUser(invoker, "GuestPageSTestOUser002", "ゲスト1");
        AuthenticationDbAccess.insertUser(invoker, "GuestPageSTestOUser003", "ゲスト2");
        AuthenticationDbAccess.insertUser(invoker, "GuestPageSTestOUser004", "ゲスト3");
        PossibleDateDbAccess.insertPossibleDate(invoker, "GuestPageTestPD001__01", eventId, "10/01(月)", 1);
        PossibleDateDbAccess.insertPossibleDate(invoker, "GuestPageTestPD002__01", eventId, "10/02(火)", 2);
        PossibleDateDbAccess.insertPossibleDate(invoker, "GuestPageTestPD003__01", eventId, "10/03(水)", 3);
        PossibleDateDbAccess.insertPossibleDate(invoker, "GuestPageTestPD004__01", eventId, "10/04(木)", 4);
        PossibleDateDbAccess.insertPossibleDate(invoker, "GuestPageTestPD005__01", eventId, "10/05(金)", 5);
        AnswerDbAccess.insertAnswer(invoker, eventId, "GuestPageSTestOUser001", "幹事太郎3",
                "幹事太郎3です。よろしくお願いいたします。", true);
        AnswerDbAccess.insertAnswer(invoker, eventId, "GuestPageSTestOUser002", "ゲスト1_",
                "ゲスト1_です。よろしくお願いいたします。", true);
        AnswerDbAccess.insertAnswer(invoker, eventId, "GuestPageSTestOUser003", "ゲスト2_",
                "ゲスト2_です。よろしくお願いいたします。", false);
        AnswerDbAccess.insertAnswer(invoker, eventId, "GuestPageSTestOUser004", "ゲスト3_",
                "ゲスト3_です。よろしくお願いいたします。", false);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser001", "GuestPageTestPD001__01", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser001", "GuestPageTestPD002__01", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser001", "GuestPageTestPD003__01", 3);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser001", "GuestPageTestPD004__01", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser001", "GuestPageTestPD005__01", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser002", "GuestPageTestPD001__01", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser002", "GuestPageTestPD002__01", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser002", "GuestPageTestPD003__01", 3);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser002", "GuestPageTestPD004__01", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser002", "GuestPageTestPD005__01", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser003", "GuestPageTestPD001__01", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser003", "GuestPageTestPD002__01", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser003", "GuestPageTestPD003__01", 3);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser003", "GuestPageTestPD004__01", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser003", "GuestPageTestPD005__01", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser004", "GuestPageTestPD001__01", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser004", "GuestPageTestPD002__01", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser004", "GuestPageTestPD003__01", 3);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser004", "GuestPageTestPD004__01", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser004", "GuestPageTestPD005__01", 2);
    }

    private static void insertEvent002() {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);

        String eventId = "GuestPageSTestEvent002";
        EventDbAccess.insertEvent(invoker, eventId, "幹事太郎", "GuestPageSTestOUser001",
                "なんとかさん送別会", "なんとかさんの送別会です。\r\n盛大に送り出しましょう。", LocalDateTime.of(2025, 1, 31, 23, 59),
                "19:00～", true, false);
        PossibleDateDbAccess.insertPossibleDate(invoker, "GuestPageTestPD001__02", eventId, "10/01(月)", 1);
        PossibleDateDbAccess.insertPossibleDate(invoker, "GuestPageTestPD002__02", eventId, "10/02(火)", 2);
        PossibleDateDbAccess.insertPossibleDate(invoker, "GuestPageTestPD003__02", eventId, "10/03(水)", 3);
        PossibleDateDbAccess.insertPossibleDate(invoker, "GuestPageTestPD004__02", eventId, "10/04(木)", 4);
        PossibleDateDbAccess.insertPossibleDate(invoker, "GuestPageTestPD005__02", eventId, "10/05(金)", 5);
        AnswerDbAccess.insertAnswer(invoker, eventId, "GuestPageSTestOUser001", "幹事太郎3",
                "幹事太郎3です。よろしくお願いいたします。", true);
        AnswerDbAccess.insertAnswer(invoker, eventId, "GuestPageSTestOUser002", "ゲスト1_",
                "ゲスト1_です。よろしくお願いいたします。", true);
        AnswerDbAccess.insertAnswer(invoker, eventId, "GuestPageSTestOUser003", "ゲスト2_",
                "ゲスト2_です。よろしくお願いいたします。", false);
        AnswerDbAccess.insertAnswer(invoker, eventId, "GuestPageSTestOUser004", "ゲスト3_",
                "ゲスト3_です。よろしくお願いいたします。", false);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser001", "GuestPageTestPD001__02", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser001", "GuestPageTestPD002__02", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser001", "GuestPageTestPD003__02", 3);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser001", "GuestPageTestPD004__02", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser001", "GuestPageTestPD005__02", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser002", "GuestPageTestPD001__02", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser002", "GuestPageTestPD002__02", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser002", "GuestPageTestPD003__02", 3);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser002", "GuestPageTestPD004__02", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser002", "GuestPageTestPD005__02", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser003", "GuestPageTestPD001__02", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser003", "GuestPageTestPD002__02", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser003", "GuestPageTestPD003__02", 3);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser003", "GuestPageTestPD004__02", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser003", "GuestPageTestPD005__02", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser004", "GuestPageTestPD001__02", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser004", "GuestPageTestPD002__02", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser004", "GuestPageTestPD003__02", 3);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser004", "GuestPageTestPD004__02", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser004", "GuestPageTestPD005__02", 2);
    }

    @AfterAll
    static void closeDb() throws Exception {
        conn.close();
    }

    @Test
    void showPageTest001() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);

        DocumentResult dr = GuestPageService.showPage(si, "GuestPageSTestEvent001", null, "dummyDevice", "dummyCsrfToken");
        String html = dr.getDocument().html();
    }

    // 幹事が非シークレットモードで回答を閲覧
    @Test
    void getPossibleDatesTableTest001() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);

        String eventId = "GuestPageSTestEvent001";
        EventDto eventDto = EventDbAccess.getEvent(sc.getDbAccessInvoker(), eventId);
        UserDto deviceUser = new UserDto();
        deviceUser.userId = "GuestPageSTestOUser001";

        List<AnswerDto> answerDtoList = AnswerDbAccess.getAnswers(sc.getDbAccessInvoker(), eventId);
        GuestPageService.PossibleDatesInfo pdi
                = GuestPageService.getPossibleDatesTable(sc, eventDto, deviceUser, answerDtoList);
        PossibleDatesTable pdt = pdi.pdt();

        assertEquals(5, pdt.possibleDateNames.length);
        assertEquals("10/01(月)", pdt.possibleDateNames[0]);
        assertEquals("10/02(火)", pdt.possibleDateNames[1]);
        assertEquals("10/03(水)", pdt.possibleDateNames[2]);
        assertEquals("10/04(木)", pdt.possibleDateNames[3]);
        assertEquals("10/05(金)", pdt.possibleDateNames[4]);
        assertEquals(4, pdt.userAnswers.length);
        assertEquals("幹事太郎3", pdt.userAnswers[0].userName);
        assertEquals("ゲスト1_", pdt.userAnswers[1].userName);
        assertEquals("ゲスト2_", pdt.userAnswers[2].userName);
        assertEquals("ゲスト3_", pdt.userAnswers[3].userName);
        assertEquals(1, pdt.userAnswers[0].answers[0]);
        assertEquals(2, pdt.userAnswers[0].answers[1]);
        assertEquals(3, pdt.userAnswers[0].answers[2]);
        assertEquals(1, pdt.userAnswers[0].answers[3]);
        assertEquals(2, pdt.userAnswers[0].answers[4]);
        assertEquals(1, pdt.userAnswers[1].answers[0]);
        assertEquals(2, pdt.userAnswers[1].answers[1]);
        assertEquals(3, pdt.userAnswers[1].answers[2]);
        assertEquals(1, pdt.userAnswers[1].answers[3]);
        assertEquals(2, pdt.userAnswers[1].answers[4]);
        assertEquals(1, pdt.userAnswers[2].answers[0]);
        assertEquals(2, pdt.userAnswers[2].answers[1]);
        assertEquals(3, pdt.userAnswers[2].answers[2]);
        assertEquals(1, pdt.userAnswers[2].answers[3]);
        assertEquals(2, pdt.userAnswers[2].answers[4]);
        assertEquals(1, pdt.userAnswers[3].answers[0]);
        assertEquals(2, pdt.userAnswers[3].answers[1]);
        assertEquals(3, pdt.userAnswers[3].answers[2]);
        assertEquals(1, pdt.userAnswers[3].answers[3]);
        assertEquals(2, pdt.userAnswers[3].answers[4]);
    }

    // 幹事以外が非シークレットモードで回答を閲覧
    @Test
    void getPossibleDatesTableTest002() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);

        String eventId = "GuestPageSTestEvent001";
        EventDto eventDto = EventDbAccess.getEvent(sc.getDbAccessInvoker(), eventId);
        UserDto deviceUser = new UserDto();
        deviceUser.userId = "GuestPageSTestOUser002";

        List<AnswerDto> answerDtoList = AnswerDbAccess.getAnswers(sc.getDbAccessInvoker(), eventId);
        GuestPageService.PossibleDatesInfo pdi
                = GuestPageService.getPossibleDatesTable(sc, eventDto, deviceUser, answerDtoList);
        PossibleDatesTable pdt = pdi.pdt();

        assertEquals(5, pdt.possibleDateNames.length);
        assertEquals("10/01(月)", pdt.possibleDateNames[0]);
        assertEquals("10/02(火)", pdt.possibleDateNames[1]);
        assertEquals("10/03(水)", pdt.possibleDateNames[2]);
        assertEquals("10/04(木)", pdt.possibleDateNames[3]);
        assertEquals("10/05(金)", pdt.possibleDateNames[4]);
        assertEquals(4, pdt.userAnswers.length);
        assertEquals("幹事太郎3", pdt.userAnswers[0].userName);
        assertEquals("ゲスト1_", pdt.userAnswers[1].userName);
        assertEquals("ゲスト2_", pdt.userAnswers[2].userName);
        assertEquals("ゲスト3_", pdt.userAnswers[3].userName);
        assertEquals(1, pdt.userAnswers[0].answers[0]);
        assertEquals(2, pdt.userAnswers[0].answers[1]);
        assertEquals(3, pdt.userAnswers[0].answers[2]);
        assertEquals(1, pdt.userAnswers[0].answers[3]);
        assertEquals(2, pdt.userAnswers[0].answers[4]);
        assertEquals(1, pdt.userAnswers[1].answers[0]);
        assertEquals(2, pdt.userAnswers[1].answers[1]);
        assertEquals(3, pdt.userAnswers[1].answers[2]);
        assertEquals(1, pdt.userAnswers[1].answers[3]);
        assertEquals(2, pdt.userAnswers[1].answers[4]);
        assertEquals(1, pdt.userAnswers[2].answers[0]);
        assertEquals(2, pdt.userAnswers[2].answers[1]);
        assertEquals(3, pdt.userAnswers[2].answers[2]);
        assertEquals(1, pdt.userAnswers[2].answers[3]);
        assertEquals(2, pdt.userAnswers[2].answers[4]);
        assertEquals(1, pdt.userAnswers[3].answers[0]);
        assertEquals(2, pdt.userAnswers[3].answers[1]);
        assertEquals(3, pdt.userAnswers[3].answers[2]);
        assertEquals(1, pdt.userAnswers[3].answers[3]);
        assertEquals(2, pdt.userAnswers[3].answers[4]);
    }

    // 幹事がシークレットモードで回答を閲覧
    @Test
    void getPossibleDatesTableTest003() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);

        String eventId = "GuestPageSTestEvent002";
        EventDto eventDto = EventDbAccess.getEvent(sc.getDbAccessInvoker(), eventId);
        UserDto deviceUser = new UserDto();
        deviceUser.userId = "GuestPageSTestOUser001";

        List<AnswerDto> answerDtoList = AnswerDbAccess.getAnswers(sc.getDbAccessInvoker(), eventId);
        GuestPageService.PossibleDatesInfo pdi
                = GuestPageService.getPossibleDatesTable(sc, eventDto, deviceUser, answerDtoList);
        PossibleDatesTable pdt = pdi.pdt();

        assertEquals(5, pdt.possibleDateNames.length);
        assertEquals("10/01(月)", pdt.possibleDateNames[0]);
        assertEquals("10/02(火)", pdt.possibleDateNames[1]);
        assertEquals("10/03(水)", pdt.possibleDateNames[2]);
        assertEquals("10/04(木)", pdt.possibleDateNames[3]);
        assertEquals("10/05(金)", pdt.possibleDateNames[4]);
        assertEquals(4, pdt.userAnswers.length);
        assertEquals("幹事太郎3", pdt.userAnswers[0].userName);
        assertEquals("ゲスト1_", pdt.userAnswers[1].userName);
        assertEquals("ゲスト2_", pdt.userAnswers[2].userName);
        assertEquals("ゲスト3_", pdt.userAnswers[3].userName);
        assertEquals(1, pdt.userAnswers[0].answers[0]);
        assertEquals(2, pdt.userAnswers[0].answers[1]);
        assertEquals(3, pdt.userAnswers[0].answers[2]);
        assertEquals(1, pdt.userAnswers[0].answers[3]);
        assertEquals(2, pdt.userAnswers[0].answers[4]);
        assertEquals(1, pdt.userAnswers[1].answers[0]);
        assertEquals(2, pdt.userAnswers[1].answers[1]);
        assertEquals(3, pdt.userAnswers[1].answers[2]);
        assertEquals(1, pdt.userAnswers[1].answers[3]);
        assertEquals(2, pdt.userAnswers[1].answers[4]);
        assertEquals(1, pdt.userAnswers[2].answers[0]);
        assertEquals(2, pdt.userAnswers[2].answers[1]);
        assertEquals(3, pdt.userAnswers[2].answers[2]);
        assertEquals(1, pdt.userAnswers[2].answers[3]);
        assertEquals(2, pdt.userAnswers[2].answers[4]);
        assertEquals(1, pdt.userAnswers[3].answers[0]);
        assertEquals(2, pdt.userAnswers[3].answers[1]);
        assertEquals(3, pdt.userAnswers[3].answers[2]);
        assertEquals(1, pdt.userAnswers[3].answers[3]);
        assertEquals(2, pdt.userAnswers[3].answers[4]);
    }

    // 幹事以外がシークレットモードで回答を閲覧
    @Test
    void getPossibleDatesTableTest004() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);

        String eventId = "GuestPageSTestEvent002";
        EventDto eventDto = EventDbAccess.getEvent(sc.getDbAccessInvoker(), eventId);
        UserDto deviceUser = new UserDto();
        deviceUser.userId = "GuestPageSTestOUser002";

        List<AnswerDto> answerDtoList = AnswerDbAccess.getAnswers(sc.getDbAccessInvoker(), eventId);
        GuestPageService.PossibleDatesInfo pdi
                = GuestPageService.getPossibleDatesTable(sc, eventDto, deviceUser, answerDtoList);
        PossibleDatesTable pdt = pdi.pdt();

        assertEquals(5, pdt.possibleDateNames.length);
        assertEquals("10/01(月)", pdt.possibleDateNames[0]);
        assertEquals("10/02(火)", pdt.possibleDateNames[1]);
        assertEquals("10/03(水)", pdt.possibleDateNames[2]);
        assertEquals("10/04(木)", pdt.possibleDateNames[3]);
        assertEquals("10/05(金)", pdt.possibleDateNames[4]);
        assertEquals(4, pdt.userAnswers.length);
        assertEquals("幹事太郎3", pdt.userAnswers[0].userName);
        assertEquals("ゲスト1_", pdt.userAnswers[1].userName);
        assertEquals("ゲスト2_", pdt.userAnswers[2].userName);
        assertEquals("ゲスト3_", pdt.userAnswers[3].userName);
        assertEquals(-1, pdt.userAnswers[0].answers[0]);
        assertEquals(-1, pdt.userAnswers[0].answers[1]);
        assertEquals(-1, pdt.userAnswers[0].answers[2]);
        assertEquals(-1, pdt.userAnswers[0].answers[3]);
        assertEquals(-1, pdt.userAnswers[0].answers[4]);
        assertEquals(1, pdt.userAnswers[1].answers[0]);
        assertEquals(2, pdt.userAnswers[1].answers[1]);
        assertEquals(3, pdt.userAnswers[1].answers[2]);
        assertEquals(1, pdt.userAnswers[1].answers[3]);
        assertEquals(2, pdt.userAnswers[1].answers[4]);
        assertEquals(-1, pdt.userAnswers[2].answers[0]);
        assertEquals(-1, pdt.userAnswers[2].answers[1]);
        assertEquals(-1, pdt.userAnswers[2].answers[2]);
        assertEquals(-1, pdt.userAnswers[2].answers[3]);
        assertEquals(-1, pdt.userAnswers[2].answers[4]);
        assertEquals(-1, pdt.userAnswers[3].answers[0]);
        assertEquals(-1, pdt.userAnswers[3].answers[1]);
        assertEquals(-1, pdt.userAnswers[3].answers[2]);
        assertEquals(-1, pdt.userAnswers[3].answers[3]);
        assertEquals(-1, pdt.userAnswers[3].answers[4]);
    }
}