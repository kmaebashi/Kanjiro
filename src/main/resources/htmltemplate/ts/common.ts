function getCsrfToken(): string {
  const metaElem: HTMLMetaElement = document.querySelector('meta[name="csrf_token"]')!;

  return metaElem.content;
}
