class Calendar {
  targetElement: HTMLElement;
  currentDate: Date;
  datePickedCallback: ((date: Date) => void) | null = null;

  constructor(targetElement: HTMLElement, date: Date) {
    this.targetElement = targetElement;
    this.currentDate = date;
  }

  setDatePickedCallback(callback: (date: Date) => void): void {
    this.datePickedCallback = callback;
  }

  getCurrentDate(): Date {
    return this.currentDate;
  }

  render(): void {
    while (this.targetElement.firstChild) {
      this.targetElement.removeChild(this.targetElement.firstChild);
    }
    const firstYoubi: number = Calendar.#getFirstYoubi(this.currentDate);
    let nth: number = 0; // 月の中の何日目かを示す
    let lastNth: number = Calendar.#getLastNth(this.currentDate);
    let endFlag: boolean = false;

    const tableElem: HTMLTableElement = document.createElement("table");
    tableElem.setAttribute("id", "calendar-table");
    const headTr: HTMLTableRowElement = document.createElement("tr");

    const leftArrowTd: HTMLTableCellElement = document.createElement("td");
    leftArrowTd.innerText = "≪";
    leftArrowTd.classList.add("calendar-left-arrow");
    headTr.appendChild(leftArrowTd);

    const monthTd: HTMLTableCellElement = document.createElement("td");
    monthTd.colSpan = 5;
    monthTd.innerText = this.currentDate.getFullYear() + "年"
                        + (this.currentDate.getMonth() + 1) + "月";
    monthTd.classList.add("calendar-header-month");
    headTr.appendChild(monthTd);

    const rightArrowTd: HTMLTableCellElement = document.createElement("td");
    rightArrowTd.innerText = "≫";
    rightArrowTd.classList.add("calendar-right-arrow");
    headTr.appendChild(rightArrowTd);
    tableElem.appendChild(headTr);

    leftArrowTd.onclick = this.leftArrowClicked.bind(this);
    rightArrowTd.onclick = this.rightArrowClicked.bind(this);

    const youbiTr: HTMLTableRowElement = document.createElement("tr");
    const youbiStrArray = ["日", "月", "火", "水", "木", "金", "土"];
    for (const youbiStr of youbiStrArray) {
      const youbiTh: HTMLTableCellElement = document.createElement("th")
      youbiTh.innerText = youbiStr;
      youbiTr.appendChild(youbiTh);
    }
    tableElem.appendChild(youbiTr);

    for (;;) {
      const trElem: HTMLTableRowElement = document.createElement("tr");
      tableElem.appendChild(trElem);
      for (let youbi: number = 0; youbi < 7; youbi++) {
        const tdElem: HTMLTableCellElement = document.createElement("td");
        trElem.appendChild(tdElem);

        if (nth == 0 && youbi < firstYoubi) {
          ;
        } else if (nth <= lastNth) {
          if (nth == 0 && youbi == firstYoubi) {
            nth = 1;
          }
          tdElem.innerText = "" + nth;
          tdElem.setAttribute("data-date", nth.toString());
          tdElem.classList.add("calendar-date");
          if (youbi == 0) {
            tdElem.classList.add("calendar-sunday");
          }
          if (youbi == 6) {
            tdElem.classList.add("calendar-saturday");
          }
          if (nth == this.currentDate.getDate()) {
            tdElem.classList.add("calendar-target-date");
          }
          tdElem.onclick = this.dateClicked.bind(this);
          nth++;
          if (nth > lastNth) {
            endFlag = true;
          }
	} else {
          ;
        }
      }
      if (endFlag) {
        break;
      }
    }
    this.targetElement.appendChild(tableElem);
  }

  leftArrowClicked(): void {
    let newYear: number = this.currentDate.getFullYear();
    let newMonth: number;
    let newDate: number;

    if (this.currentDate.getMonth() == 0) {
      newMonth = 11;
      newYear--;
    } else {
      newMonth = this.currentDate.getMonth() - 1;
    }
    newDate = Calendar.#fixLastDate(newYear, newMonth, this.currentDate.getDate());
    this.currentDate = new Date(newYear, newMonth, newDate);
    this.render();
  }

  rightArrowClicked(): void {
    let newYear: number = this.currentDate.getFullYear();
    let newMonth: number;
    let newDate: number;

    if (this.currentDate.getMonth() == 11) {
      newMonth = 0;
      newYear++;
    } else {
      newMonth = this.currentDate.getMonth() + 1;
    }
    newDate = Calendar.#fixLastDate(newYear, newMonth, this.currentDate.getDate());
    this.currentDate = new Date(newYear, newMonth, newDate);
    this.render();
  }

  dateClicked(e: Event): void {
    const selectedDate: string = (e!.target as HTMLElement).dataset.date!;
    this.currentDate.setDate(parseInt(selectedDate));
    this.render();
    if (this.datePickedCallback !== null) {
      this.datePickedCallback(this.currentDate);
    }
  }

  static #getLastNth(date: Date): number {
    const date2: Date = new Date(date.getTime());
    date2.setMonth(date.getMonth() + 1, 0);
    return date2.getDate();
  }

  static #getFirstYoubi(date: Date): number {
    const date2: Date = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    date2.setDate(1);
    return date2.getDay();
  }

  static #fixLastDate(newYear: number, newMonth: number, oldDate: number): number {
    const tempDate: Date = new Date(newYear, newMonth, 1);
    const lastNth: number = Calendar.#getLastNth(tempDate);
    let newDate: number;

    if (oldDate > lastNth) {
      newDate = lastNth;
    } else {
      newDate = oldDate;
    }

    return newDate;
  }
}