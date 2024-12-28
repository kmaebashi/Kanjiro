"use strict";
window.onload = function (e) {
    const mediaQueryList = window.matchMedia("(min-width: 768px)");
    renderPossibleDatesTable();
    mediaQueryList.addEventListener("change", handleOrientationOnChange);
};
function handleOrientationOnChange(event) {
    renderPossibleDatesTable();
}
function renderPossibleDatesTable() {
    console.log("renderPossibleDatesTable() called");
    const mediaQueryList = window.matchMedia("(min-width: 768px)");
    const tableElem = document.getElementById("possible-dates-table");
    while (tableElem.firstChild) {
        tableElem.removeChild(tableElem.firstChild);
    }
    if (mediaQueryList.matches) {
        // PC表示
        console.log("PC mode");
        renderPossibleDatesTableDayRow(tableElem);
    }
    else {
        // スマホ表示
        console.log("SP mode");
        renderPossibleDatesTableUserRow(tableElem);
    }
}
const markTable = ["〇", "△", "×"];
function renderPossibleDatesTableDayRow(tableElem) {
    const headerTr = document.createElement("tr");
    tableElem.appendChild(headerTr);
    const headerTh1 = document.createElement("th");
    headerTh1.innerText = "日程";
    headerTr.appendChild(headerTh1);
    const headerTh2 = document.createElement("th");
    headerTh2.innerText = "〇";
    headerTr.appendChild(headerTh2);
    const headerTh3 = document.createElement("th");
    headerTh3.innerText = "△";
    headerTr.appendChild(headerTh3);
    const headerTh4 = document.createElement("th");
    headerTh4.innerText = "×";
    headerTr.appendChild(headerTh4);
    for (let userIdx = 0; userIdx < possibleDatesTable.userAnswers.length; userIdx++) {
        const userNameTh = document.createElement("th");
        userNameTh.innerText = possibleDatesTable.userAnswers[userIdx].userName;
        headerTr.appendChild(userNameTh);
    }
    for (let dateIdx = 0; dateIdx < possibleDatesTable.possibleDateNames.length; dateIdx++) {
        const row = document.createElement("tr");
        const dateTh = document.createElement("th");
        dateTh.innerText = possibleDatesTable.possibleDateNames[dateIdx];
        row.appendChild(dateTh);
        const td1 = document.createElement("td");
        td1.innerText = "" + countAnswer(dateIdx, 1);
        row.appendChild(td1);
        const td2 = document.createElement("td");
        td2.innerText = "" + countAnswer(dateIdx, 2);
        row.appendChild(td2);
        const td3 = document.createElement("td");
        td3.innerText = "" + countAnswer(dateIdx, 3);
        row.appendChild(td3);
        for (let userIdx = 0; userIdx < possibleDatesTable.userAnswers.length; userIdx++) {
            const answer = possibleDatesTable.userAnswers[userIdx].answers[dateIdx];
            const answerTd = document.createElement("td");
            answerTd.innerText = markTable[answer - 1];
            row.appendChild(answerTd);
        }
        tableElem.appendChild(row);
    }
}
function renderPossibleDatesTableUserRow(tableElem) {
    const headerTr = document.createElement("tr");
    tableElem.appendChild(headerTr);
    const headerTh1 = document.createElement("th");
    headerTh1.innerText = "参加者";
    headerTr.appendChild(headerTh1);
    for (let dateIdx = 0; dateIdx < possibleDatesTable.possibleDateNames.length; dateIdx++) {
        const dateTh = document.createElement("th");
        dateTh.innerText = possibleDatesTable.possibleDateNames[dateIdx];
        headerTr.appendChild(dateTh);
    }
    for (let userIdx = 0; userIdx < possibleDatesTable.userAnswers.length; userIdx++) {
        const row = document.createElement("tr");
        const userTh = document.createElement("th");
        userTh.innerText = possibleDatesTable.userAnswers[userIdx].userName;
        row.appendChild(userTh);
        for (let dateIdx = 0; dateIdx < possibleDatesTable.possibleDateNames.length; dateIdx++) {
            const answer = possibleDatesTable.userAnswers[userIdx].answers[dateIdx];
            const answerTd = document.createElement("td");
            answerTd.innerText = markTable[answer - 1];
            row.appendChild(answerTd);
        }
        tableElem.appendChild(row);
    }
    for (let markIdx = 0; markIdx < markTable.length; markIdx++) {
        const markRow = document.createElement("tr");
        const markTh = document.createElement("th");
        markTh.innerText = markTable[markIdx];
        markRow.appendChild(markTh);
        for (let dateIdx = 0; dateIdx < possibleDatesTable.possibleDateNames.length; dateIdx++) {
            const markTd = document.createElement("td");
            markTd.innerText = "" + countAnswer(dateIdx, markIdx + 1);
            markRow.appendChild(markTd);
        }
        tableElem.appendChild(markRow);
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
