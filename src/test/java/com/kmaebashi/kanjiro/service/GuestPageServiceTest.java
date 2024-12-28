package com.kmaebashi.kanjiro.service;

import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.kanjiro.KanjiroTestUtil;
import com.kmaebashi.kanjiro.controller.data.PossibleDatesTable;
import com.kmaebashi.kanjiro.dbaccess.AnswerDbAccess;
import com.kmaebashi.kanjiro.dbaccess.AuthenticationDbAccess;
import com.kmaebashi.kanjiro.dbaccess.EventDbAccess;
import com.kmaebashi.kanjiro.dbaccess.PossibleDateDbAccess;
import com.kmaebashi.kanjiro.dto.AnswerDto;
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
    }

    private static void deleteAll() throws Exception {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);

        KanjiroTestUtil.deleteAll(context, "EVENTS");
        KanjiroTestUtil.deleteAll(context, "USERS");
        KanjiroTestUtil.deleteAll(context, "POSSIBLE_DATES");
        KanjiroTestUtil.deleteAll(context, "ANSWERS");
        KanjiroTestUtil.deleteAll(context, "DATE_ANSWERS");
    }

    private static void insertEvent001() {
        DbAccessContext context = new DbAccessContextImpl(conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);

        EventDbAccess.insertEvent(invoker, "GuestPageSTestEvent001", "幹事太郎", "GuestPageSTestOUser001",
                                  "なんとかさん送別会", "なんとかさんの送別会です。\r\n盛大に送り出しましょう。",
                                  false, false);
        AuthenticationDbAccess.insertUser(invoker, "GuestPageSTestOUser001", "幹事太郎2");
        AuthenticationDbAccess.insertUser(invoker, "GuestPageSTestOUser002", "ゲスト1");
        AuthenticationDbAccess.insertUser(invoker, "GuestPageSTestOUser003", "ゲスト2");
        AuthenticationDbAccess.insertUser(invoker, "GuestPageSTestOUser004", "ゲスト3");
        PossibleDateDbAccess.insertPossibleDate(invoker, "GuestPageTestPD001", "GuestPageSTestEvent001", "10/01(月)", 1);
        PossibleDateDbAccess.insertPossibleDate(invoker, "GuestPageTestPD002", "GuestPageSTestEvent001", "10/02(火)", 2);
        PossibleDateDbAccess.insertPossibleDate(invoker, "GuestPageTestPD003", "GuestPageSTestEvent001", "10/03(水)", 3);
        PossibleDateDbAccess.insertPossibleDate(invoker, "GuestPageTestPD004", "GuestPageSTestEvent001", "10/04(木)", 4);
        PossibleDateDbAccess.insertPossibleDate(invoker, "GuestPageTestPD005", "GuestPageSTestEvent001", "10/05(金)", 5);
        AnswerDbAccess.insertAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser001", "幹事太郎3",
                                    "幹事太郎3です。よろしくお願いいたします。", true);
        AnswerDbAccess.insertAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser002", "ゲスト1_",
                "ゲスト1_です。よろしくお願いいたします。", true);
        AnswerDbAccess.insertAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser003", "ゲスト2_",
                "ゲスト2_です。よろしくお願いいたします。", false);
        AnswerDbAccess.insertAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser004", "ゲスト3_",
                "ゲスト3_です。よろしくお願いいたします。", false);
        AnswerDbAccess.insertDateAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser001", "GuestPageTestPD001", 1);
        AnswerDbAccess.insertDateAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser001", "GuestPageTestPD002", 2);
        AnswerDbAccess.insertDateAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser001", "GuestPageTestPD003", 3);
        AnswerDbAccess.insertDateAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser001", "GuestPageTestPD004", 1);
        AnswerDbAccess.insertDateAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser001", "GuestPageTestPD005", 2);
        AnswerDbAccess.insertDateAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser002", "GuestPageTestPD001", 1);
        AnswerDbAccess.insertDateAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser002", "GuestPageTestPD002", 2);
        AnswerDbAccess.insertDateAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser002", "GuestPageTestPD003", 3);
        AnswerDbAccess.insertDateAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser002", "GuestPageTestPD004", 1);
        AnswerDbAccess.insertDateAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser002", "GuestPageTestPD005", 2);
        AnswerDbAccess.insertDateAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser003", "GuestPageTestPD001", 1);
        AnswerDbAccess.insertDateAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser003", "GuestPageTestPD002", 2);
        AnswerDbAccess.insertDateAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser003", "GuestPageTestPD003", 3);
        AnswerDbAccess.insertDateAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser003", "GuestPageTestPD004", 1);
        AnswerDbAccess.insertDateAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser003", "GuestPageTestPD005", 2);
        AnswerDbAccess.insertDateAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser004", "GuestPageTestPD001", 1);
        AnswerDbAccess.insertDateAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser004", "GuestPageTestPD002", 2);
        AnswerDbAccess.insertDateAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser004", "GuestPageTestPD003", 3);
        AnswerDbAccess.insertDateAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser004", "GuestPageTestPD004", 1);
        AnswerDbAccess.insertDateAnswer(invoker, "GuestPageSTestEvent001", "GuestPageSTestOUser004", "GuestPageTestPD005", 2);
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

        DocumentResult dr = GuestPageService.showPage(si, "GuestPageSTestEvent001", "dummyDevice", "dummyCsrfToken");
        String html = dr.getDocument().html();
    }

    @Test
    void getPossibleDatesTableTest001() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);

        String eventId = "GuestPageSTestEvent001";
        List<AnswerDto> answerDtoList = AnswerDbAccess.getAnswers(sc.getDbAccessInvoker(), eventId);
        PossibleDatesTable pdt = GuestPageService.getPossibleDatesTable(sc, eventId, answerDtoList);

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
    }
}