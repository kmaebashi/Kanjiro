package com.kmaebashi.kanjiro.service;

import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.ServiceInvoker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.nio.file.Path;

public class OrganizerPageService {
    private OrganizerPageService() {}

    public static DocumentResult showTopPage(ServiceInvoker invoker) {
        return invoker.invoke((context) -> {
            Path htmlPath = context.getHtmlTemplateDirectory().resolve("top.html");
            Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");
            renderBlankPage(doc);
            return new DocumentResult(doc);
        });
    }

    private static void renderBlankPage(Document doc) {
        Element calendarDiv = doc.getElementById("calendar-div");
        calendarDiv.empty();
    }
}
