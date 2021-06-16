function populate() {
    if (document.getElementById('country_input').value == ''){
        document.getElementById('country_input').value = 'FR'
    }
    if (document.getElementById('city').value == ''){
        document.getElementById('city').value = 'PUTEAUX'
    }
    if (document.getElementById('value').value == ''){
        document.getElementById('value').value = '0000'
    }
    if (document.getElementById('branch_code_input').value == ''){
        document.getElementById('branch_code_input').value = 'BRANCHDEMO'
    }
    if (document.getElementById('calendar_code_input').value == ''){
        document.getElementById('calendar_code_input').value = 'FR'
    }
    if (document.getElementById('company_code_input').value == ''){
        document.getElementById('company_code_input').value = 'COMPANY-DEMO'
    }
    if (document.getElementById('currency_code_input').value == ''){
        document.getElementById('currency_code_input').value = 'EUR'
    }
    if (document.getElementById('time_zone_input').value == ''){
        document.getElementById('time_zone_input').value = 'Asia/Tokyo'
    }
}

function populateWithErrorData(data, methodType) {
    if (methodType == "POST") {
        document.getElementById('code').value = data.code
    }
    document.getElementById('country_input').value = data.country;
    document.getElementById('city').value = data.city;
    document.getElementById('value').value = data.value;
    document.getElementById('ban_structure').value = data.ban_structure;
    document.getElementById('branch_code_input').value = data.branch_code;
    document.getElementById('calendar_code_input').value = data.calendar_code;
    document.getElementById('company_code_input').value = data.company_code;
    document.getElementById('currency_code_input').value = data.currency_code;
    document.getElementById('time_zone_input').value = data.time_zone;
}