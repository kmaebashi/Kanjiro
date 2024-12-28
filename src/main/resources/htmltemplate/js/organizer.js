"use strict";
function datePickedCallback(date) {
    const appendTimeElem = document.getElementById("schedule-append-time");
    const textAreaElem = document.getElementById("schedule-textarea");
    const youbiStrArray = ["日", "月", "火", "水", "木", "金", "土"];
    const dateStr = "" + (date.getMonth() + 1) + "/" + (date.getDate())
        + "(" + youbiStrArray[date.getDay()] + ")"
        + ((appendTimeElem.value.length > 0) ? " " : "")
        + appendTimeElem.value + "\n";
    if (textAreaElem.value.length > 0
        && !textAreaElem.value.endsWith("\n")) {
        textAreaElem.value += "\n";
    }
    textAreaElem.value += dateStr;
}
function scheduleToArray(textAreaStr) {
    const ret = [];
    const dupeCheckSet = new Set();
    const possibleDatesArray = textAreaStr.split("\n");
    for (let line of possibleDatesArray) {
        const trimed = line.trim();
        if (trimed.length == 0) {
            continue;
        }
        if (dupeCheckSet.has(trimed)) {
            return [ret, "候補が重複しています(" + trimed + ")"];
        }
        dupeCheckSet.add(trimed);
        ret.push(trimed);
    }
    return [ret, null];
}
