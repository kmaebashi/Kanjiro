"use strict";
window.onload = function (e) {
    console.log("onload pass1");
    const calendarElem = document.getElementById("calendar-div");
    console.log("onload pass2");
    const calendar = new Calendar(calendarElem, new Date());
    console.log("onload pass3");
    calendar.render();
};
