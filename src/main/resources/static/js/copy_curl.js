function copyCode(endPoint, copyId) {
    var inputText = '';
    if (copyId != '') {
        inputText = document.getElementById(copyId).value;
    }
    const copyText = document.createElement('textarea');
    copyText.value = baseUrl + endPoint + inputText;
    copyText.setAttribute('readonly', '');
    copyText.style.position = 'absolute';
    copyText.style.left = '-9999px';
    document.body.appendChild(copyText);
    copyText.select();
    document.execCommand('copy');
    alert("cURL " + baseUrl + endPoint + inputText + " successfully copied!")
    document.body.removeChild(copyText);
}

function clear_all() {
    document.getElementById('get_uuid').value = ''
    document.getElementById('get_code').value = ''
    document.getElementById('update_code').value = ''
    document.getElementById('delete_code').value = ''
}