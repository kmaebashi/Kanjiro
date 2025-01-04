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
  appendTime: string;
  isSecretMode: boolean;
  isAutoSchedule: boolean;
  registerForce: boolean;
  updatedAt: string | null;
};

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
