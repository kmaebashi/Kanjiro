package com.kmaebashi.kanjiro.service;

import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.jsonparser.ClassMapper;
import com.kmaebashi.kanjiro.KanjiroTestUtil;
import com.kmaebashi.kanjiro.common.Constants;
import com.kmaebashi.kanjiro.controller.data.DeleteAnswerInfo;
import com.kmaebashi.kanjiro.controller.data.DeleteAnswerResult;
import com.kmaebashi.kanjiro.controller.data.PossibleDatesTable;
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
import com.kmaebashi.kanjiro.util.Log;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.DbAccessContext;
import com.kmaebashi.nctfw.DbAccessInvoker;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.JsonResult;
import com.kmaebashi.nctfw.ServiceContext;
import com.kmaebashi.nctfw.ServiceInvoker;
import com.kmaebashi.nctfwimpl.DbAccessContextImpl;
import com.kmaebashi.nctfwimpl.DbAccessInvokerImpl;
import com.kmaebashi.nctfwimpl.ServiceContextImpl;
import com.kmaebashi.nctfwimpl.ServiceInvokerImpl;
import com.kmaebashi.simplelogger.Logger;
import com.kmaebashi.simpleloggerimpl.FileLogger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        insertEvent003();
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
        AuthenticationDbAccess.insertUser(invoker, "GuestPageSTestOUser005", "ゲスト4");
        AuthenticationDbAccess.upsertDevice(invoker, "GuestPageSTestODevi001", LocalDateTime.now(), "dummy");
        AuthenticationDbAccess.upsertDevice(invoker, "GuestPageSTestODevi002", LocalDateTime.now(), "dummy");
        AuthenticationDbAccess.upsertDevice(invoker, "GuestPageSTestODevi003", LocalDateTime.now(), "dummy");
        AuthenticationDbAccess.upsertDevice(invoker, "GuestPageSTestODevi004", LocalDateTime.now(), "dummy");
        AuthenticationDbAccess.upsertDevice(invoker, "GuestPageSTestODevi005", LocalDateTime.now(), "dummy");
        AuthenticationDbAccess.setUserToDevice(invoker, "GuestPageSTestODevi001", "GuestPageSTestOUser001");
        AuthenticationDbAccess.setUserToDevice(invoker, "GuestPageSTestODevi002", "GuestPageSTestOUser002");
        AuthenticationDbAccess.setUserToDevice(invoker, "GuestPageSTestODevi003", "GuestPageSTestOUser003");
        AuthenticationDbAccess.setUserToDevice(invoker, "GuestPageSTestODevi004", "GuestPageSTestOUser004");
        AuthenticationDbAccess.setUserToDevice(invoker, "GuestPageSTestODevi005", "GuestPageSTestOUser005");
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
        EventDbAccess.updateEvent(invoker, eventId, "幹事太郎", "なんとかさん送別会", "なんとかさんの送別会です。\r\n盛大に送り出しましょう。",
                                  LocalDateTime.of(2025, 1, 31, 23, 59),  "19:00～", "GuestPageTestPD003__02", true, false);
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

    // deleteAnswer()テスト用
    private static void insertEvent003() {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);

        String eventId = "GuestPageSTestEvent003";
        EventDbAccess.insertEvent(invoker, eventId, "幹事太郎", "GuestPageSTestOUser001",
                                  "なんとかさん送別会", "なんとかさんの送別会です。\r\n盛大に送り出しましょう。", LocalDateTime.of(2025, 1, 31, 23, 59),
                                  "19:00～", false, false);
        PossibleDateDbAccess.insertPossibleDate(invoker, "GuestPageTestPD001__03", eventId, "10/01(月)", 1);
        PossibleDateDbAccess.insertPossibleDate(invoker, "GuestPageTestPD002__03", eventId, "10/02(火)", 2);
        PossibleDateDbAccess.insertPossibleDate(invoker, "GuestPageTestPD003__03", eventId, "10/03(水)", 3);
        PossibleDateDbAccess.insertPossibleDate(invoker, "GuestPageTestPD004__03", eventId, "10/04(木)", 4);
        PossibleDateDbAccess.insertPossibleDate(invoker, "GuestPageTestPD005__03", eventId, "10/05(金)", 5);
        AnswerDbAccess.insertAnswer(invoker, eventId, "GuestPageSTestOUser001", "幹事太郎3",
                                    "幹事太郎3です。よろしくお願いいたします。", false);
        AnswerDbAccess.insertAnswer(invoker, eventId, "GuestPageSTestOUser002", "ゲスト1_",
                                    "ゲスト1_です。よろしくお願いいたします。", false);
        AnswerDbAccess.insertAnswer(invoker, eventId, "GuestPageSTestOUser003", "ゲスト2_",
                                    "ゲスト2_です。よろしくお願いいたします。", false);
        AnswerDbAccess.insertAnswer(invoker, eventId, "GuestPageSTestOUser004", "ゲスト3_",
                                    "ゲスト3_です。よろしくお願いいたします。", false);
        AnswerDbAccess.insertAnswer(invoker, eventId, "GuestPageSTestOUser005", "ゲスト4_",
                                    "ゲスト4_です。よろしくお願いいたします。", true);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser001", "GuestPageTestPD001__03", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser001", "GuestPageTestPD002__03", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser001", "GuestPageTestPD003__03", 3);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser001", "GuestPageTestPD004__03", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser001", "GuestPageTestPD005__03", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser002", "GuestPageTestPD001__03", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser002", "GuestPageTestPD002__03", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser002", "GuestPageTestPD003__03", 3);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser002", "GuestPageTestPD004__03", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser002", "GuestPageTestPD005__03", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser003", "GuestPageTestPD001__03", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser003", "GuestPageTestPD002__03", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser003", "GuestPageTestPD003__03", 3);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser003", "GuestPageTestPD004__03", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser003", "GuestPageTestPD005__03", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser004", "GuestPageTestPD001__03", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser004", "GuestPageTestPD002__03", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser004", "GuestPageTestPD003__03", 3);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser004", "GuestPageTestPD004__03", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser004", "GuestPageTestPD005__03", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser005", "GuestPageTestPD001__03", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser005", "GuestPageTestPD002__03", 2);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser005", "GuestPageTestPD003__03", 3);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser005", "GuestPageTestPD004__03", 1);
        AnswerDbAccess.insertDateAnswer(invoker, eventId, "GuestPageSTestOUser005", "GuestPageTestPD005__03", 2);
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

        assertEquals("×　幹事太郎3", pdi.guestList()[0]);
        assertEquals("×　ゲスト1_", pdi.guestList()[1]);
        assertEquals("×　ゲスト2_", pdi.guestList()[2]);
        assertEquals("×　ゲスト3_", pdi.guestList()[3]);
    }

    @Test
    void renderDateFixedAreaTest001() throws Exception {
        Path htmlPath = Path.of("./src/main/resources/htmltemplate/guest.html");
        Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");

        UserAnswers[] ua = new UserAnswers[4];
        ua[0] = new UserAnswers();
        ua[0].userName = "参加者1";
        ua[0].answers = new int[] {1, 2, 3};
        ua[1] = new UserAnswers();
        ua[1].userName = "参加者2";
        ua[1].answers = new int[] {2, 3, 1};
        ua[2] = new UserAnswers();
        ua[2].userName = "参加者3";
        ua[2].answers = new int[] {3, 1, 2};
        ua[3] = new UserAnswers();
        ua[3].userName = "参加者4";
        ua[3].answers = new int[] {1, 1, 1};

        PossibleDatesTable pdt = new PossibleDatesTable();
        pdt.possibleDateNames = new String[] {"3/15(土) 19:00～", "3/22(土) 19:00～", "3/29(土) 19:00～"};
        pdt.userAnswers = ua;

        List<PossibleDateDto> possibleDateDtoList = new ArrayList<PossibleDateDto>();
        PossibleDateDto pdDto = new PossibleDateDto();
        pdDto.possibleDateId = "id0";
        pdDto.name = pdt.possibleDateNames[0];
        possibleDateDtoList.add(pdDto);
        pdDto = new PossibleDateDto();
        pdDto.possibleDateId = "id1";
        pdDto.name = pdt.possibleDateNames[1];
        possibleDateDtoList.add(pdDto);
        pdDto = new PossibleDateDto();
        pdDto.possibleDateId = "id2";
        pdDto.name = pdt.possibleDateNames[2];
        possibleDateDtoList.add(pdDto);

        GuestPageService.PossibleDatesInfo pdi = new GuestPageService.PossibleDatesInfo(pdt, possibleDateDtoList, new String[0]);
        EventDto eventDto = new EventDto();
        eventDto.fixedDateId = "id0";

        GuestPageService.renderDateFixedArea(doc, eventDto, pdi);
    }

    @Test
    void makeScheduleTest001() {
        //     User1 User2 User3 User4
        // 4/1 ×　　　×　　　×　　　×　
        // 4/2 ○　　　○　　　×     ○　　　　
        // 4/3 ○　　　○　　　○     △　
        // 4/4 ○　　　○　　　△     ○　

        PossibleDatesTable pdt = new PossibleDatesTable();
        pdt.possibleDateNames = new String[] {
            "4/1(火) 19:00～", "4/2(水) 19:00～", "4/3(木) 19:00～", "4/4(金) 19:00～"
        };
        pdt.userAnswers = new UserAnswers[4];
        pdt.userAnswers[0] = new UserAnswers();
        pdt.userAnswers[0].userName = "User1";
        pdt.userAnswers[0].answers = new int[] {3, 1, 1, 1};
        pdt.userAnswers[1] = new UserAnswers();
        pdt.userAnswers[1].userName = "User2";
        pdt.userAnswers[1].answers = new int[] {3, 1, 1, 1};
        pdt.userAnswers[2] = new UserAnswers();
        pdt.userAnswers[2].userName = "User3";
        pdt.userAnswers[2].answers = new int[] {3, 3, 1, 2};
        pdt.userAnswers[3] = new UserAnswers();
        pdt.userAnswers[3].userName = "User4";
        pdt.userAnswers[3].answers = new int[] {3, 1, 2, 1};

        List<PossibleDateDto> pdDtoList = new ArrayList<PossibleDateDto>();
        PossibleDateDto pdDto = new PossibleDateDto();
        pdDto.possibleDateId = "id1";
        pdDtoList.add(pdDto);
        pdDto = new PossibleDateDto();
        pdDto.possibleDateId = "id2";
        pdDtoList.add(pdDto);
        pdDto = new PossibleDateDto();
        pdDto.possibleDateId = "id3";
        pdDtoList.add(pdDto);
        pdDto = new PossibleDateDto();
        pdDto.possibleDateId = "id4";
        pdDtoList.add(pdDto);
        GuestPageService.PossibleDatesInfo pdi = new GuestPageService.PossibleDatesInfo(pdt, pdDtoList, null);
        String pdId = GuestPageService.makeSchedule(pdi);

        assertEquals("id3", pdId);
    }

    @Test
    void deleteAnswerTest001() throws Exception {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);
        ServiceContext sc = new ServiceContextImpl(invoker,
                                                   Paths.get("./src/main/resources/htmltemplate"),
                                                   logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);

        // ログインユーザと対象ユーザは異なるが、deleteForce=trueなので成功する
        AnswerDto ad = AnswerDbAccess.getAnswer(invoker, "GuestPageSTestEvent003", "GuestPageSTestOUser002");
        DeleteAnswerInfo dai = new DeleteAnswerInfo();
        dai.eventId = "GuestPageSTestEvent003";
        dai.userId = "GuestPageSTestOUser002";
        dai.deleteForce = true;
        dai.updatedAt = Constants.lastUpdateFormatter.format(ad.updatedAt);
        JsonResult result = GuestPageService.deleteAnswer(si, "dummyDeviceId", dai);
        assertEquals("{\r\n" +
                             "    \"deleted\":true,\r\n" +
                             "    \"warningMessage\":\"成功しました。\"\r\n" +
                             "}", result.getJson());
        List<AnswerDto> answerDtoList = AnswerDbAccess.getAnswers(invoker, "GuestPageSTestEvent003");
        assertEquals(4, answerDtoList.size());
        List<DateAnswerDto> dateAnswerDtoList = AnswerDbAccess.getDateAnswers(invoker, "GuestPageSTestEvent003");
        assertEquals(20, dateAnswerDtoList.size());

        // GuestPageのクエリストリングにuserIdがなかったケース(普通のケース)
        ad = AnswerDbAccess.getAnswer(invoker, "GuestPageSTestEvent003", "GuestPageSTestOUser003");
        dai.userId = null;
        dai.deleteForce = false;
        dai.updatedAt = Constants.lastUpdateFormatter.format(ad.updatedAt);
        result = GuestPageService.deleteAnswer(si, "GuestPageSTestODevi003", dai);
        assertEquals("{\r\n" +
                             "    \"deleted\":true,\r\n" +
                             "    \"warningMessage\":\"成功しました。\"\r\n" +
                             "}", result.getJson());

        answerDtoList = AnswerDbAccess.getAnswers(invoker, "GuestPageSTestEvent003");
        assertEquals(3, answerDtoList.size());
        dateAnswerDtoList = AnswerDbAccess.getDateAnswers(invoker, "GuestPageSTestEvent003");
        assertEquals(15, dateAnswerDtoList.size());

        // GuestPageのクエリストリングにuserIdがあり、デバイスと一致している
        ad = AnswerDbAccess.getAnswer(invoker, "GuestPageSTestEvent003", "GuestPageSTestOUser004");
        dai.userId = "GuestPageSTestOUser004";
        dai.deleteForce = false;
        dai.updatedAt = Constants.lastUpdateFormatter.format(ad.updatedAt);
        result = GuestPageService.deleteAnswer(si, "GuestPageSTestODevi004", dai);
        assertEquals("{\r\n" +
                             "    \"deleted\":true,\r\n" +
                             "    \"warningMessage\":\"成功しました。\"\r\n" +
                             "}", result.getJson());

        answerDtoList = AnswerDbAccess.getAnswers(invoker, "GuestPageSTestEvent003");
        assertEquals(2, answerDtoList.size());
        dateAnswerDtoList = AnswerDbAccess.getDateAnswers(invoker, "GuestPageSTestEvent003");
        assertEquals(10, dateAnswerDtoList.size());

        // 削除に成功した回答の削除
        dai.userId = "GuestPageSTestOUser004";
        dai.deleteForce = false;
        try {
            result = GuestPageService.deleteAnswer(si, "GuestPageSTestODevi004", dai);
        } catch (BadRequestException ex) {
            assertEquals("削除対象が存在しません。", ex.getMessage());
            return;
        }
        fail();
    }
    
    @Test
    void deleteAnswerTest002() throws Exception {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);
        ServiceContext sc = new ServiceContextImpl(invoker,
                                                   Paths.get("./src/main/resources/htmltemplate"),
                                                   logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);

        DeleteAnswerInfo dai = new DeleteAnswerInfo();
        dai.eventId = "dummyEventId";
        dai.userId = "GuestPageSTestOUser002";
        dai.deleteForce = true;
        dai.updatedAt = "dummy";
        try {
            JsonResult result = GuestPageService.deleteAnswer(si, "dummyDeviceId", dai);
        } catch (BadRequestException ex) {
            assertEquals("そのイベントはありません。", ex.getMessage());
            return;
        }
        fail();
    }

    @Test
    void deleteAnswerTest003() throws Exception {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);
        ServiceContext sc = new ServiceContextImpl(invoker,
                                                   Paths.get("./src/main/resources/htmltemplate"),
                                                   logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);

        DeleteAnswerInfo dai = new DeleteAnswerInfo();
        dai.eventId = "GuestPageSTestEvent003";
        dai.userId = null;
        dai.deleteForce = true;
        dai.updatedAt = "dummy";
        try {
            JsonResult result = GuestPageService.deleteAnswer(si, null, dai);
        } catch (BadRequestException ex) {
            assertEquals("削除対象が指定されていません。", ex.getMessage());
            return;
        }
        fail();
    }

    @Test
    void deleteAnswerTest004() throws Exception {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);
        ServiceContext sc = new ServiceContextImpl(invoker,
                                                   Paths.get("./src/main/resources/htmltemplate"),
                                                   logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);

        AnswerDto ad = AnswerDbAccess.getAnswer(invoker, "GuestPageSTestEvent003", "GuestPageSTestOUser001");
        DeleteAnswerInfo dai = new DeleteAnswerInfo();
        dai.eventId = "GuestPageSTestEvent003";
        dai.userId = "GuestPageSTestOUser001";
        dai.deleteForce = false;
        dai.updatedAt = Constants.lastUpdateFormatter.format(ad.updatedAt);
        JsonResult result = GuestPageService.deleteAnswer(si, null, dai);
        assertEquals("{\r\n" +
                             "    \"deleted\":false,\r\n" +
                             "    \"warningMessage\":\"あなたはユーザとして登録されていません。\"\r\n" +
                             "}", result.getJson());

    }

    @Test
    void deleteAnswerTest005() throws Exception {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);
        ServiceContext sc = new ServiceContextImpl(invoker,
                                                   Paths.get("./src/main/resources/htmltemplate"),
                                                   logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);

        AnswerDto ad = AnswerDbAccess.getAnswer(invoker, "GuestPageSTestEvent003", "GuestPageSTestOUser001");
        DeleteAnswerInfo dai = new DeleteAnswerInfo();
        dai.eventId = "GuestPageSTestEvent003";
        dai.userId = "GuestPageSTestOUser001";
        dai.deleteForce = false;
        dai.updatedAt = Constants.lastUpdateFormatter.format(ad.updatedAt);
        JsonResult result = GuestPageService.deleteAnswer(si, "GuestPageSTestODevi004", dai);
        assertEquals("{\r\n" +
                             "    \"deleted\":false,\r\n" +
                             "    \"warningMessage\":\"他人の回答を削除しようとしています。\"\r\n" +
                             "}", result.getJson());

    }

    @Test
    void deleteAnswerTest006() throws Exception {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);
        ServiceContext sc = new ServiceContextImpl(invoker,
                                                   Paths.get("./src/main/resources/htmltemplate"),
                                                   logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);

        AnswerDto ad = AnswerDbAccess.getAnswer(invoker, "GuestPageSTestEvent003", "GuestPageSTestOUser005");
        DeleteAnswerInfo dai = new DeleteAnswerInfo();
        dai.eventId = "GuestPageSTestEvent003";
        dai.userId = "GuestPageSTestOUser005";
        dai.deleteForce = false;
        dai.updatedAt = Constants.lastUpdateFormatter.format(ad.updatedAt);
        try {
            JsonResult result = GuestPageService.deleteAnswer(si, "GuestPageSTestODevi001", dai);
        } catch (BadRequestException ex) {
            assertEquals("この回答はロックされています。", ex.getMessage());
            return;
        }
        fail();
    }

    @Test
    void deleteAnswerTest007() throws Exception {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);
        ServiceContext sc = new ServiceContextImpl(invoker,
                                                   Paths.get("./src/main/resources/htmltemplate"),
                                                   logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);

        DeleteAnswerInfo dai = new DeleteAnswerInfo();
        dai.eventId = "GuestPageSTestEvent003";
        dai.userId = "GuestPageSTestOUser005";
        dai.deleteForce = false;
        dai.updatedAt = "dummy";
        try {
            JsonResult result = GuestPageService.deleteAnswer(si, "GuestPageSTestODevi005", dai);
        } catch (BadRequestException ex) {
            assertEquals("この画面を開いている間に回答が修正されています。", ex.getMessage());
            return;
        }
        fail();
    }
}