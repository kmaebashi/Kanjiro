window.onload = function(e: Event): void {
  const mediaQueryList = window.matchMedia("(min-width: 768px)");
  renderPossibleDatesTable();

  mediaQueryList.addEventListener("change", handleOrientationOnChange);

  const replyButton: HTMLElement = document.getElementById("reply-button")!;
  replyButton.onclick = sendReply;
};

function handleOrientationOnChange(event: MediaQueryListEvent): void {
  renderPossibleDatesTable();
}

type UserAnswers = {
  userId: string;
  userName: string;
  isProtected: boolean;
  answers: number[];
};

type PossibleDatesTable = {
  possibleDateNames: string[];
  userAnswers: UserAnswers[];
};

declare const possibleDatesTable: PossibleDatesTable;

function renderPossibleDatesTable(): void {
  const mediaQueryList = window.matchMedia("(min-width: 768px)");

  const tableElem: HTMLElement = document.getElementById("possible-dates-table")!;

  while (tableElem.firstChild) {
    tableElem.removeChild(tableElem.firstChild);
  }

  if (mediaQueryList.matches) {
    // PC表示
    renderPossibleDatesTableDayRow(tableElem);
  } else {
    // スマホ表示
    renderPossibleDatesTableUserRow(tableElem);
  }
}

const markTable: string[] = ["〇", "△", "×"];

function renderPossibleDatesTableDayRow(tableElem: HTMLElement): void {
  const headerTr: HTMLTableRowElement = document.createElement("tr");
  tableElem.appendChild(headerTr);
  const headerTh1: HTMLTableCellElement = document.createElement("th");
  headerTh1.innerText = "日程";
  headerTh1.classList.add("date-th");
  headerTr.appendChild(headerTh1);
  const headerTh2: HTMLTableCellElement = document.createElement("th");
  headerTh2.innerText = "〇";
  headerTh2.classList.add("count-th");
  headerTr.appendChild(headerTh2);
  const headerTh3: HTMLTableCellElement = document.createElement("th");
  headerTh3.innerText = "△";
  headerTh3.classList.add("count-th");
  headerTr.appendChild(headerTh3);
  const headerTh4: HTMLTableCellElement = document.createElement("th");
  headerTh4.innerText = "×";
  headerTh4.classList.add("count-th");
  headerTr.appendChild(headerTh4);

  for (let userIdx:number = 0; userIdx < possibleDatesTable.userAnswers.length; userIdx++) {
    const userNameTh: HTMLTableCellElement
      = createUserNameTh(possibleDatesTable.userAnswers[userIdx]);
    headerTr.appendChild(userNameTh);
  }

  for (let dateIdx: number = 0; dateIdx < possibleDatesTable.possibleDateNames.length; dateIdx++) {
    const row: HTMLTableRowElement = document.createElement("tr");
    const dateTh: HTMLTableCellElement = document.createElement("th");
    dateTh.innerText = possibleDatesTable.possibleDateNames[dateIdx];
    row.appendChild(dateTh);
    const td1: HTMLTableCellElement = document.createElement("td");
    td1.innerText = "" + countAnswer(dateIdx, 1);
    td1.classList.add("count");
    row.appendChild(td1);
    const td2: HTMLTableCellElement = document.createElement("td");
    td2.innerText = "" + countAnswer(dateIdx, 2);
    td2.classList.add("count");
    row.appendChild(td2);
    const td3: HTMLTableCellElement = document.createElement("td");
    td3.innerText = "" + countAnswer(dateIdx, 3);
    td3.classList.add("count");
    row.appendChild(td3);

    for (let userIdx: number = 0; userIdx < possibleDatesTable.userAnswers.length; userIdx++) {
      const answer: number = possibleDatesTable.userAnswers[userIdx].answers[dateIdx];
      const answerTd: HTMLTableCellElement = document.createElement("td");
      answerTd.innerText = getAnswerMark(answer);
      row.appendChild(answerTd);
    }
    tableElem.appendChild(row);
  }
}

function renderPossibleDatesTableUserRow(tableElem: HTMLElement): void {
  const headerTr: HTMLTableRowElement = document.createElement("tr");
  tableElem.appendChild(headerTr);
  const headerTh1: HTMLTableCellElement = document.createElement("th");
  headerTh1.innerText = "参加者";
  headerTr.appendChild(headerTh1);

  for (let dateIdx:number = 0; dateIdx < possibleDatesTable.possibleDateNames.length; dateIdx++) {
    const dateTh: HTMLTableCellElement = document.createElement("th");
    dateTh.innerText = possibleDatesTable.possibleDateNames[dateIdx];
    headerTr.appendChild(dateTh);
  }

  for (let userIdx: number = 0; userIdx < possibleDatesTable.userAnswers.length; userIdx++) {
    const row: HTMLTableRowElement = document.createElement("tr");
    const userTh: HTMLTableCellElement
      = createUserNameTh(possibleDatesTable.userAnswers[userIdx]);
    row.appendChild(userTh);

    for (let dateIdx: number = 0; dateIdx < possibleDatesTable.possibleDateNames.length; dateIdx++) {
      const answer: number = possibleDatesTable.userAnswers[userIdx].answers[dateIdx];
      const answerTd: HTMLTableCellElement = document.createElement("td");
      answerTd.innerText = getAnswerMark(answer);
      row.appendChild(answerTd);
    }
    tableElem.appendChild(row);
  }

  for (let markIdx: number = 0; markIdx < markTable.length; markIdx++) {
    const markRow: HTMLTableRowElement = document.createElement("tr");
    const markTh: HTMLTableCellElement = document.createElement("th");
    markTh.innerText = markTable[markIdx];
    markTh.classList.add("count-th");
    markRow.appendChild(markTh);

    for (let dateIdx: number = 0; dateIdx < possibleDatesTable.possibleDateNames.length; dateIdx++) {
      const markTd: HTMLTableCellElement = document.createElement("td");
      markTd.innerText = "" + countAnswer(dateIdx, markIdx + 1);
      markTd.classList.add("count");
      markRow.appendChild(markTd);
    }
    tableElem.appendChild(markRow);
  }
}

function getAnswerMark(answer: number): string {
  if (answer == -1) {
    return "*";
  }else if (answer == 0) {
    return "-";
  } else {
    return markTable[answer - 1];
  }
}

function countAnswer(dateIdx: number, answer: number): number {
  let count = 0;

  for (let userIdx: number = 0; userIdx < possibleDatesTable.userAnswers.length; userIdx++) {
    if (possibleDatesTable.userAnswers[userIdx].answers[dateIdx] == answer) {
      count++;
    }
  }

  return count;
}

function createUserNameTh(ua: UserAnswers): HTMLTableCellElement {
  const th = document.createElement("th");

  if (ua.isProtected) {
    th.innerText = ua.userName;
  } else {
    const queryParams = new URLSearchParams(window.location.search);
    const eventId: string = queryParams.get("eventId")!;
    const aElem: HTMLAnchorElement = document.createElement("a") as HTMLAnchorElement;
    aElem.href = "./guest?eventId=" + eventId + "&userId=" + ua.userId;
    aElem.innerText = ua.userName;
    th.appendChild(aElem);
  }

  return th;
}

type DateAnswerInfo = {
  possibleDateId: string;
  answer: number;
};

type AnswerInfo = {
  eventId: string;
  userId: string | null;
  userName: string;
  message: string;
  isProtected: boolean;
  answers: DateAnswerInfo[];
};

async function sendReply(e: Event) {
  const queryParams = new URLSearchParams(window.location.search);

  const eventId: string = queryParams.get("eventId")!;
  const userId: string | null = queryParams.get("userId");
  const userName: string
    = (document.getElementById("guest-name")! as HTMLInputElement).value;
  const message: string
    = (document.getElementById("comment-textarea")! as HTMLInputElement).value;
  const isProtected: boolean
    = (document.getElementById("protected-check")! as HTMLInputElement).checked;
  const answers: DateAnswerInfo[] = getAnswers();

  const answerInfo: AnswerInfo = {
    eventId: eventId,
    userId: userId,
    userName: userName,
    message: message,
    isProtected: isProtected,
    answers: answers
  };

  const csrfToken = getCsrfToken();
  const response = await fetch("./api/postanswerinfo", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Csrf-Token": csrfToken
      },
      body: JSON.stringify(answerInfo)
    });

  console.log("response.." + response.status);
  const retJson = await response.json();

  if (!response.ok) {
    console.log("エラー! retJson.." + retJson);
    alert("登録に失敗しました。" + retJson.message);
  } else {
    console.log("retJson.." + JSON.stringify(retJson));
    window.location.reload();
  }
}

function getAnswers(): DateAnswerInfo[] {
  const tableElem: HTMLElement = document.getElementById("reply-table")!;
  const trList: HTMLCollection = tableElem.getElementsByTagName("tr");
  const ret: DateAnswerInfo[] = [];

  for (let dateIdx:number = 0; dateIdx < trList.length; dateIdx++) {
    const trElem: HTMLElement = trList[dateIdx] as HTMLElement;
    const radioElems: HTMLCollection = trElem.getElementsByTagName("input");
    let selected: number = 0;
    for (let radioIdx: number = 0; radioIdx < radioElems.length; radioIdx++) {
      const radioElem: HTMLInputElement = radioElems[radioIdx] as HTMLInputElement;
      if (radioElem.checked) {
        selected = radioIdx;
      }
    }
    const dateAnswerInfo: DateAnswerInfo = {
      possibleDateId: trElem.dataset.possibleDateId!,
      answer: selected + 1
    };
    ret.push(dateAnswerInfo);
  }
  return ret;
}

