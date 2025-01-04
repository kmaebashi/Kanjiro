window.onload = function(e: Event): void {
  const calendarElem: HTMLElement = document.getElementById("calendar-div")!;
  const calendar: Calendar = new Calendar(calendarElem, new Date());
  calendar.render();
  calendar.setDatePickedCallback(datePickedCallback);

  document.getElementById("apply-button")!.onclick = createEventButtonClicked;
};

async function createEventButtonClicked(e: Event) {
  const organizerName: string
    = (document.getElementById("organizer-name-input")! as HTMLInputElement).value.trim();
  if (organizerName.length == 0) {
    alert("幹事様のお名前は必須です。");
    return;
  }
  const eventName: string
    = (document.getElementById("event-name-input")! as HTMLInputElement).value.trim();
  if (eventName.length == 0) {
    alert("イベント名は必須です。");
    return;
  }
  const eventDescription: string
    = (document.getElementById("event-description")! as HTMLInputElement).value;
  const appendTime: string
    = (document.getElementById("schedule-append-time")! as HTMLInputElement).value;
  const scheduleText: string
    = (document.getElementById("schedule-textarea")! as HTMLInputElement).value;
  const isSecretMode: boolean
    = (document.getElementById("is-secret-mode")! as HTMLInputElement).checked;
  const isAutoSchedule: boolean
    = (document.getElementById("auto-schedule")! as HTMLInputElement).checked;

  const scheduleArrayRet = scheduleToArray(scheduleText);
  if (scheduleArrayRet[1] != null) {
    alert(scheduleArrayRet[1]);
    return;
  }

  var eventInfo: EventInfo = {
    eventId: null,
    organizerName: organizerName,
    eventName: eventName,
    eventDescription: eventDescription,
    scheduleArray: scheduleArrayRet[0],
    appendTime: appendTime,
    isSecretMode: isSecretMode,
    isAutoSchedule: isAutoSchedule,
    registerForce: false,
    updatedAt: null
  };
  console.log("json.." + JSON.stringify(eventInfo));

  const csrfToken = getCsrfToken();
  const response = await fetch("./api/posteventinfo", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Csrf-Token": csrfToken
      },
      body: JSON.stringify(eventInfo)
    });
  console.log("response.." + response.status);
  const retJson = await response.json();

  if (!response.ok) {
    console.log("エラー! retJson.." + retJson);
    alert("登録に失敗しました。" + retJson.message);
  } else {
    console.log("retJson.." + JSON.stringify(retJson));
    window.location.href = "./event?eventId=" + retJson.eventId;
  }
}
