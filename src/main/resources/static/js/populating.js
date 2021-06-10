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
    if (document.getElementById('branch_input').value == ''){
        document.getElementById('branch_input').value = 'BRANCHDEMO'
    }
    if (document.getElementById('calendar_input').value == ''){
        document.getElementById('calendar_input').value = 'FR'
    }
    if (document.getElementById('company_input').value == ''){
        document.getElementById('company_input').value = 'COMPANY-DEMO'
    }
    if (document.getElementById('currency_input').value == ''){
        document.getElementById('currency_input').value = 'EUR'
    }
    if (document.getElementById('time_zone_input').value == ''){
        document.getElementById('time_zone_input').value = 'Asia/Tokyo'
    }
}