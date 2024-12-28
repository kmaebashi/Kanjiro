"use strict";
function getCsrfToken() {
    const metaElem = document.querySelector('meta[name="csrf_token"]');
    return metaElem.content;
}
