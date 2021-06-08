function copyCode(copyId) {
    var copyText = document.getElementById(copyId);
    copyText.select();
    copyText.setSelectionRange(0,9999);
    document.execCommand("copy");
}