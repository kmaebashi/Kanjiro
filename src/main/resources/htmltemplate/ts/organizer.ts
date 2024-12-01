window.onload = function(e: Event): void {
  console.log("onload pass1");
  const calendarElem: HTMLElement = document.getElementById("calendar-div")!;
  console.log("onload pass2");
  const calendar: Calendar = new Calendar(calendarElem, new Date());
  console.log("onload pass3");
  calendar.render();
};
