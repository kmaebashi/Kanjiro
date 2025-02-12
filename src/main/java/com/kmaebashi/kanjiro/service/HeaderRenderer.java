package com.kmaebashi.kanjiro.service;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HeaderRenderer {
    private HeaderRenderer() {}

    public static void renderLinkDevice(Document doc, String eventId) {
        Element aElem = doc.getElementById("link-device-a");
        aElem.attr("href", "./linkdevice?eventId=" + eventId);
    }
}
