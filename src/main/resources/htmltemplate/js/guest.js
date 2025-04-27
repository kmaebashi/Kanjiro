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
    const mediaQueryList = window.matchMedia("(min-width: 768px)");
    renderPossibleDatesTable();
    mediaQueryList.addEventListener("change", handleOrientationOnChange);
    const replyButton = document.getElementById("reply-button");
    replyButton.onclick = sendReply;
    const deleteButton = document.getElementById("delete-button");
    deleteButton.onclick = deleteAnswer;
};
function handleOrientationOnChange(event) {
    renderPossibleDatesTable();
}
function renderPossibleDatesTable() {
    const mediaQueryList = window.matchMedia("(min-width: 768px)");
    const tableElem = document.getElementById("possible-dates-table");
    while (tableElem.firstChild) {
        tableElem.removeChild(tableElem.firstChild);
    }
    if (mediaQueryList.matches) {
        // PC表示
        if (possibleDatesTable.userAnswers.length > 8) {
            renderPossibleDatesTableUserRow(tableElem);
        }
        else {
            renderPossibleDatesTableDayRow(tableElem);
        }
    }
    else {
        // スマホ表示
        renderPossibleDatesTableUserRow(tableElem);
    }
}
const markTable = ["〇", "△", "×"];
const FIX_WIDTH_COLS = 8;
function renderPossibleDatesTableDayRow(tableElem) {
    const fixWidthFlag = (possibleDatesTable.userAnswers.length + 3) <= FIX_WIDTH_COLS;
    if (!fixWidthFlag) {
        tableElem.style.width = "100%";
    }
    const headerTr = document.createElement("tr");
    tableElem.appendChild(headerTr);
    const headerTh1 = document.createElement("th");
    headerTh1.innerText = "日程";
    headerTh1.classList.add("date-th");
    headerTr.appendChild(headerTh1);
    const headerTh2 = document.createElement("th");
    headerTh2.innerText = "〇";
    headerTh2.classList.add("count-th");
    headerTr.appendChild(headerTh2);
    const headerTh3 = document.createElement("th");
    headerTh3.innerText = "△";
    headerTh3.classList.add("count-th");
    headerTr.appendChild(headerTh3);
    const headerTh4 = document.createElement("th");
    headerTh4.innerText = "×";
    headerTh4.classList.add("count-th");
    headerTr.appendChild(headerTh4);
    for (let userIdx = 0; userIdx < possibleDatesTable.userAnswers.length; userIdx++) {
        const userNameTh = createUserNameTh(possibleDatesTable.userAnswers[userIdx]);
        if (fixWidthFlag) {
            userNameTh.style.width = "5em";
        }
        headerTr.appendChild(userNameTh);
    }
    for (let dateIdx = 0; dateIdx < possibleDatesTable.possibleDateNames.length; dateIdx++) {
        const row = document.createElement("tr");
        const dateTh = document.createElement("th");
        dateTh.innerText = possibleDatesTable.possibleDateNames[dateIdx];
        row.appendChild(dateTh);
        const td1 = document.createElement("td");
        td1.innerText = "" + countAnswer(dateIdx, 1);
        td1.classList.add("count");
        row.appendChild(td1);
        const td2 = document.createElement("td");
        td2.innerText = "" + countAnswer(dateIdx, 2);
        td2.classList.add("count");
        row.appendChild(td2);
        const td3 = document.createElement("td");
        td3.innerText = "" + countAnswer(dateIdx, 3);
        td3.classList.add("count");
        row.appendChild(td3);
        for (let userIdx = 0; userIdx < possibleDatesTable.userAnswers.length; userIdx++) {
            const answer = possibleDatesTable.userAnswers[userIdx].answers[dateIdx];
            const answerTd = document.createElement("td");
            answerTd.innerText = getAnswerMark(answer);
            row.appendChild(answerTd);
        }
        tableElem.appendChild(row);
    }
}
function renderPossibleDatesTableUserRow(tableElem) {
    const fixWidthFlag = possibleDatesTable.possibleDateNames.length <= FIX_WIDTH_COLS;
    if (!fixWidthFlag) {
        tableElem.style.width = "100%";
    }
    const headerTr = document.createElement("tr");
    tableElem.appendChild(headerTr);
    const headerTh1 = document.createElement("th");
    headerTh1.innerText = "参加者";
    headerTh1.style.width = "6em";
    headerTr.appendChild(headerTh1);
    for (let dateIdx = 0; dateIdx < possibleDatesTable.possibleDateNames.length; dateIdx++) {
        const dateTh = document.createElement("th");
        if (fixWidthFlag) {
            dateTh.style.width = "5em";
        }
        dateTh.innerText = possibleDatesTable.possibleDateNames[dateIdx];
        headerTr.appendChild(dateTh);
    }
    for (let userIdx = 0; userIdx < possibleDatesTable.userAnswers.length; userIdx++) {
        const row = document.createElement("tr");
        const userTh = createUserNameTh(possibleDatesTable.userAnswers[userIdx]);
        row.appendChild(userTh);
        for (let dateIdx = 0; dateIdx < possibleDatesTable.possibleDateNames.length; dateIdx++) {
            const answer = possibleDatesTable.userAnswers[userIdx].answers[dateIdx];
            const answerTd = document.createElement("td");
            answerTd.innerText = getAnswerMark(answer);
            row.appendChild(answerTd);
        }
        tableElem.appendChild(row);
    }
    for (let markIdx = 0; markIdx < markTable.length; markIdx++) {
        const markRow = document.createElement("tr");
        const markTh = document.createElement("th");
        markTh.innerText = markTable[markIdx];
        markTh.classList.add("count-th");
        markRow.appendChild(markTh);
        for (let dateIdx = 0; dateIdx < possibleDatesTable.possibleDateNames.length; dateIdx++) {
            const markTd = document.createElement("td");
            markTd.innerText = "" + countAnswer(dateIdx, markIdx + 1);
            markTd.classList.add("count");
            markRow.appendChild(markTd);
        }
        tableElem.appendChild(markRow);
    }
}
function getAnswerMark(answer) {
    if (answer == -1) {
        return "*";
    }
    else if (answer == 0) {
        return "-";
    }
    else {
        return markTable[answer - 1];
    }
}
function countAnswer(dateIdx, answer) {
    let count = 0;
    for (let userIdx = 0; userIdx < possibleDatesTable.userAnswers.length; userIdx++) {
        if (possibleDatesTable.userAnswers[userIdx].answers[dateIdx] == answer) {
            count++;
        }
    }
    return count;
}
function createUserNameTh(ua) {
    const th = document.createElement("th");
    if (ua.isProtected) {
        th.innerText = ua.userName;
    }
    else {
        const queryParams = new URLSearchParams(window.location.search);
        const eventId = queryParams.get("eventId");
        const aElem = document.createElement("a");
        aElem.href = "./guest?eventId=" + eventId + "&userId=" + ua.userId;
        aElem.innerText = ua.userName;
        th.appendChild(aElem);
    }
    return th;
}
function sendReply(e) {
    return __awaiter(this, void 0, void 0, function* () {
        const queryParams = new URLSearchParams(window.location.search);
        const eventId = queryParams.get("eventId");
        const userId = queryParams.get("userId");
        const userName = document.getElementById("guest-name").value;
        const message = document.getElementById("comment-textarea").value;
        const isProtected = document.getElementById("protected-check").checked;
        const answers = getAnswers();
        const answerInfo = {
            eventId: eventId,
            userId: userId,
            userName: userName,
            message: message,
            isProtected: isProtected,
            answers: answers
        };
        const csrfToken = getCsrfToken();
        const response = yield fetch("./api/postanswerinfo", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-Csrf-Token": csrfToken
            },
            body: JSON.stringify(answerInfo)
        });
        console.log("response.." + response.status);
        const retJson = yield response.json();
        if (!response.ok) {
            console.log("エラー! retJson.." + retJson);
            alert("登録に失敗しました。" + retJson.message);
        }
        else {
            console.log("retJson.." + JSON.stringify(retJson));
            window.location.reload();
        }
    });
}
function getAnswers() {
    const tableElem = document.getElementById("reply-table");
    const trList = tableElem.getElementsByTagName("tr");
    const ret = [];
    for (let dateIdx = 0; dateIdx < trList.length; dateIdx++) {
        const trElem = trList[dateIdx];
        const radioElems = trElem.getElementsByTagName("input");
        let selected = 0;
        for (let radioIdx = 0; radioIdx < radioElems.length; radioIdx++) {
            const radioElem = radioElems[radioIdx];
            if (radioElem.checked) {
                selected = radioIdx;
            }
        }
        const dateAnswerInfo = {
            possibleDateId: trElem.dataset.possibleDateId,
            answer: selected + 1
        };
        ret.push(dateAnswerInfo);
    }
    return ret;
}
function deleteAnswer(e) {
    return __awaiter(this, void 0, void 0, function* () {
        const queryParams = new URLSearchParams(window.location.search);
        const eventId = queryParams.get("eventId");
        const userId = queryParams.get("userId");
        const updatedAt = getUpdatedAt(userId);
        if (updatedAt === null) {
            alert("削除対象がありません。");
            return;
        }
        const deleteAnswerInfo = {
            eventId: eventId,
            userId: userId,
            deleteForce: false,
            updatedAt: updatedAt
        };
        const csrfToken = getCsrfToken();
        let registered = false;
        for (;;) {
            console.log("delete loop");
            const response = yield fetch("./api/deleteanswer", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "X-Csrf-Token": csrfToken
                },
                body: JSON.stringify(deleteAnswerInfo)
            });
            console.log("response.." + response.status);
            const retJson = yield response.json();
            if (!response.ok) {
                console.log("エラー! retJson.." + retJson);
                alert("削除に失敗しました。" + retJson.message);
                break;
            }
            else {
                console.log("retJson.." + JSON.stringify(retJson));
                const result = retJson;
                if (result.deleted) {
                    window.location.href = "./guest?eventId=" + eventId;
                    break;
                }
                else {
                    if (confirm(result.warningMessage + "\n削除しますか?")) {
                        deleteAnswerInfo.deleteForce = true;
                    }
                    else {
                        break;
                    }
                }
            }
        }
    });
}
function getUpdatedAt(queryUserId) {
    let targetUserId;
    if (possibleDatesTable.deviceUser === null) {
        if (queryUserId === null) {
            return null;
        }
        else {
            targetUserId = queryUserId;
        }
    }
    else {
        if (queryUserId === null) {
            targetUserId = possibleDatesTable.deviceUser;
        }
        else {
            targetUserId = queryUserId;
        }
    }
    return possibleDatesTable.userAnswers
        .find(user => user.userId == targetUserId).updatedAt;
}
