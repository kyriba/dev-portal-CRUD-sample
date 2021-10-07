function copy_curl(parameters, copyId) {
    var inputText = '';
    if (copyId != '') {
        inputText = document.getElementById(copyId).value;
    }
    const copyText = document.createElement('textarea');
    switch (apiUrl){
        case '/v1/bank-balances/accounts':
        case '/v1/cash-balances/accounts':
            inputText += '/balances';
            break;
        case '/v1/data-permissions':
            if (copyId != '') {
                inputText += '/entities';
            }
            break;
        case '/v1/data-permission-profiles':
            if (copyId != '') {
                inputText += '/permissions';
            }
            break;
    }
    copyText.value = baseUrl + apiUrl + parameters + inputText;
    copyText.setAttribute('readonly', '');
    copyText.style.position = 'absolute';
    copyText.style.left = '-9999px';
    document.body.appendChild(copyText);
    copyText.select();
    document.execCommand('copy');
    alert("cURL " + baseUrl + apiUrl + parameters + inputText + " successfully copied!")
    document.body.removeChild(copyText);
}

function clear_all() {
    document.getElementById('get_uuid').value = ''
    document.getElementById('get_code').value = ''
    document.getElementById('get_ref').value = ''
    document.getElementById('update_code').value = ''
    document.getElementById('update_uuid').value = ''
    document.getElementById('update_ref').value = ''
    document.getElementById('delete_code').value = ''
    document.getElementById('delete_uuid').value = ''
}