window.onload = function(e: Event): void {
  const calendarElem: HTMLElement = document.getElementById("calendar-div")!;
  const calendar: Calendar = new Calendar(calendarElem, new Date());
  calendar.render();
  calendar.setDatePickedCallback(datePickedCallback);

  document.getElementById("create-event-button")!.onclick = createEventButtonClicked;
};

function datePickedCallback(date: Date): void {
  const appendTimeElem :HTMLInputElement
           = document.getElementById("schedule-append-time")! as HTMLInputElement;
  const textAreaElem: HTMLTextAreaElement
           = document.getElementById("schedule-textarea")! as HTMLTextAreaElement;
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

type EventInfo = {
  eventId: string | null;
  organizerName: string;
  eventName: string;
  eventDescription: string;
  scheduleArray: string[];
  isSecretMode: boolean;
  isAutoSchedule: boolean;
  registerForce: boolean;
  updatedAt: string | null;
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
  } else {
    console.log("retJson.." + retJson);
    alert("投稿に成功しました");
  }
}

function scheduleToArray(textAreaStr: string): [string[], string | null] {
  const ret: string[] = [];

  const dupeCheckSet = new Set<string>();
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
