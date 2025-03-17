"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
window.onload = function (e) {
    const calendarElem = document.getElementById("calendar-div");
    const calendar = new Calendar(calendarElem, new Date());
    calendar.render();
    document.getElementById("set-deadline").onchange = changeDeadlineCheck;
    calendar.setDatePickedCallback(datePickedCallback);
    document.getElementById("apply-button").onclick = createEventButtonClicked;
    changeDeadlineCheck(e);
};
function createEventButtonClicked(e) {
    return __awaiter(this, void 0, void 0, function* () {
        const organizerName = document.getElementById("organizer-name-input").value.trim();
        if (organizerName.length == 0) {
            alert("幹事様のお名前は必須です。");
            return;
        }
        const eventName = document.getElementById("event-name-input").value.trim();
        if (eventName.length == 0) {
            alert("イベント名は必須です。");
            return;
        }
        const eventDescription = document.getElementById("event-description").value;
        const deadlineCheck = document.getElementById("set-deadline");
        const eventDeadline = getEventDeadline();
        if (deadlineCheck.checked && eventDeadline == null) {
            alert("締め切りを入力してください。");
            return;
        }
        const appendTime = document.getElementById("schedule-append-time").value;
        const scheduleText = document.getElementById("schedule-textarea").value;
        const isSecretMode = document.getElementById("is-secret-mode").checked;
        const isAutoSchedule = document.getElementById("auto-schedule").checked;
        const scheduleArrayRet = scheduleToArray(scheduleText);
        if (scheduleArrayRet[1] != null) {
            alert(scheduleArrayRet[1]);
            return;
        }
        var eventInfo = {
            eventId: null,
            organizerName: organizerName,
            eventName: eventName,
            eventDescription: eventDescription,
            eventDeadline: eventDeadline,
            scheduleArray: scheduleArrayRet[0],
            appendTime: appendTime,
            fixedDate: null,
            isSecretMode: isSecretMode,
            isAutoSchedule: isAutoSchedule,
            registerForce: false,
            updatedAt: null
        };
        console.log("json.." + JSON.stringify(eventInfo));
        const csrfToken = getCsrfToken();
        const response = yield fetch("./api/createeventinfo", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-Csrf-Token": csrfToken
            },
            body: JSON.stringify(eventInfo)
        });
        console.log("response.." + response.status);
        const retJson = yield response.json();
        if (!response.ok) {
            console.log("エラー! retJson.." + retJson);
            alert("登録に失敗しました。" + retJson.message);
        }
        else {
            console.log("retJson.." + JSON.stringify(retJson));
            window.location.href = "./event?eventId=" + retJson.eventId;
        }
    });
}
