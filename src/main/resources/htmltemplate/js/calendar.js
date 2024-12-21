"use strict";
var __classPrivateFieldGet = (this && this.__classPrivateFieldGet) || function (receiver, state, kind, f) {
    if (kind === "a" && !f) throw new TypeError("Private accessor was defined without a getter");
    if (typeof state === "function" ? receiver !== state || !f : !state.has(receiver)) throw new TypeError("Cannot read private member from an object whose class did not declare it");
    return kind === "m" ? f : kind === "a" ? f.call(receiver) : f ? f.value : state.get(receiver);
};
var _a, _Calendar_getLastNth, _Calendar_getFirstYoubi, _Calendar_fixLastDate, _Calendar_prevMonthExists, _Calendar_isFutureDate;
class Calendar {
    constructor(targetElement, date) {
        this.datePickedCallback = null;
        this.targetElement = targetElement;
        this.currentDate = date;
    }
    setDatePickedCallback(callback) {
        this.datePickedCallback = callback;
    }
    getCurrentDate() {
        return this.currentDate;
    }
    render() {
        while (this.targetElement.firstChild) {
            this.targetElement.removeChild(this.targetElement.firstChild);
        }
        const firstYoubi = __classPrivateFieldGet(_a, _a, "m", _Calendar_getFirstYoubi).call(_a, this.currentDate);
        let nth = 0; // 月の中の何日目かを示す
        let lastNth = __classPrivateFieldGet(_a, _a, "m", _Calendar_getLastNth).call(_a, this.currentDate);
        let endFlag = false;
        const tableElem = document.createElement("table");
        tableElem.setAttribute("id", "calendar-table");
        const headTr = document.createElement("tr");
        const leftArrowTd = document.createElement("td");
        if (__classPrivateFieldGet(_a, _a, "m", _Calendar_prevMonthExists).call(_a, this.currentDate)) {
            leftArrowTd.innerText = "≪";
            leftArrowTd.classList.add("calendar-left-arrow");
            leftArrowTd.onclick = this.leftArrowClicked.bind(this);
        }
        headTr.appendChild(leftArrowTd);
        const monthTd = document.createElement("td");
        monthTd.colSpan = 5;
        monthTd.innerText = this.currentDate.getFullYear() + "年"
            + (this.currentDate.getMonth() + 1) + "月";
        monthTd.classList.add("calendar-header-month");
        headTr.appendChild(monthTd);
        const rightArrowTd = document.createElement("td");
        rightArrowTd.innerText = "≫";
        rightArrowTd.classList.add("calendar-right-arrow");
        headTr.appendChild(rightArrowTd);
        rightArrowTd.onclick = this.rightArrowClicked.bind(this);
        tableElem.appendChild(headTr);
        const youbiTr = document.createElement("tr");
        const youbiStrArray = ["日", "月", "火", "水", "木", "金", "土"];
        for (const youbiStr of youbiStrArray) {
            const youbiTh = document.createElement("th");
            youbiTh.innerText = youbiStr;
            youbiTr.appendChild(youbiTh);
        }
        tableElem.appendChild(youbiTr);
        for (;;) {
            const trElem = document.createElement("tr");
            tableElem.appendChild(trElem);
            for (let youbi = 0; youbi < 7; youbi++) {
                const tdElem = document.createElement("td");
                trElem.appendChild(tdElem);
                if (nth == 0 && youbi < firstYoubi) {
                    ;
                }
                else if (nth <= lastNth) {
                    if (nth == 0 && youbi == firstYoubi) {
                        nth = 1;
                    }
                    tdElem.innerText = "" + nth;
                    tdElem.setAttribute("data-date", nth.toString());
                    if (__classPrivateFieldGet(_a, _a, "m", _Calendar_isFutureDate).call(_a, this.currentDate.getFullYear(), this.currentDate.getMonth(), nth)) {
                        tdElem.classList.add("calendar-date");
                        if (youbi == 0) {
                            tdElem.classList.add("calendar-sunday");
                        }
                        if (youbi == 6) {
                            tdElem.classList.add("calendar-saturday");
                        }
                        tdElem.onclick = this.dateClicked.bind(this);
                    }
                    else {
                        tdElem.classList.add("unclickable-date");
                    }
                    nth++;
                    if (nth > lastNth) {
                        endFlag = true;
                    }
                }
                else {
                    ;
                }
            }
            if (endFlag) {
                break;
            }
        }
        this.targetElement.appendChild(tableElem);
    }
    leftArrowClicked() {
        let newYear = this.currentDate.getFullYear();
        let newMonth;
        let newDate;
        if (this.currentDate.getMonth() == 0) {
            newMonth = 11;
            newYear--;
        }
        else {
            newMonth = this.currentDate.getMonth() - 1;
        }
        newDate = __classPrivateFieldGet(_a, _a, "m", _Calendar_fixLastDate).call(_a, newYear, newMonth, this.currentDate.getDate());
        this.currentDate = new Date(newYear, newMonth, newDate);
        this.render();
    }
    rightArrowClicked() {
        let newYear = this.currentDate.getFullYear();
        let newMonth;
        let newDate;
        if (this.currentDate.getMonth() == 11) {
            newMonth = 0;
            newYear++;
        }
        else {
            newMonth = this.currentDate.getMonth() + 1;
        }
        newDate = __classPrivateFieldGet(_a, _a, "m", _Calendar_fixLastDate).call(_a, newYear, newMonth, this.currentDate.getDate());
        this.currentDate = new Date(newYear, newMonth, newDate);
        this.render();
    }
    dateClicked(e) {
        const selectedDate = e.target.dataset.date;
        this.currentDate.setDate(parseInt(selectedDate));
        this.render();
        if (this.datePickedCallback !== null) {
            this.datePickedCallback(this.currentDate);
        }
    }
}
_a = Calendar, _Calendar_getLastNth = function _Calendar_getLastNth(date) {
    const date2 = new Date(date.getTime());
    date2.setMonth(date.getMonth() + 1, 0);
    return date2.getDate();
}, _Calendar_getFirstYoubi = function _Calendar_getFirstYoubi(date) {
    const date2 = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    date2.setDate(1);
    return date2.getDay();
}, _Calendar_fixLastDate = function _Calendar_fixLastDate(newYear, newMonth, oldDate) {
    const tempDate = new Date(newYear, newMonth, 1);
    const lastNth = __classPrivateFieldGet(_a, _a, "m", _Calendar_getLastNth).call(_a, tempDate);
    let newDate;
    if (oldDate > lastNth) {
        newDate = lastNth;
    }
    else {
        newDate = oldDate;
    }
    return newDate;
}, _Calendar_prevMonthExists = function _Calendar_prevMonthExists(date) {
    const nowDate = new Date();
    return date.getFullYear() > nowDate.getFullYear()
        || (date.getFullYear() == nowDate.getFullYear()
            && date.getMonth() > nowDate.getMonth());
}, _Calendar_isFutureDate = function _Calendar_isFutureDate(year, month, nth) {
    const nowDate = new Date();
    return year > nowDate.getFullYear()
        || (year == nowDate.getFullYear()
            && month > nowDate.getMonth())
        || (year == nowDate.getFullYear()
            && month == nowDate.getMonth()
            && nth >= nowDate.getDate());
};
