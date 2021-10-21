function populate() {
    for (let position = 0; position < keySet.length; position++) {
        if (keySet[position] == 'code' || keySet[position] == 'uuid'
            || keySet[position] == 'interfaceCode' || keySet[position] == 'shortCode'){
            continue;
        } else if (keySet[position] == 'firstName') {
            document.getElementById(keySet[position] + '_input').value = 'John'
        } else if (keySet[position] == 'lastName') {
            document.getElementById(keySet[position] + '_input').value = 'Doe'
        } else if (keySet[position] == 'accessProfile.code') {
            document.getElementById(keySet[position] + '_input').value = 'DEV'
        } else if (keySet[position] == 'companyOwnership.code') {
            document.getElementById(keySet[position] + '_input').value = 'COMPANY01'
        } else if (keySet[position] == 'email') {
            document.getElementById(keySet[position] + '_input').value = 'test@mail.com'
        } else if (keySet[position] == 'fax') {
            document.getElementById(keySet[position] + '_input').value = '1-212-555-1234'
        } else if (keySet[position] == 'mobilePhone') {
            document.getElementById(keySet[position] + '_input').value = '1-212-999-8765'
        } else if (keySet[position] == 'officePhone') {
            document.getElementById(keySet[position] + '_input').value = '000-000-000'
        } else if (keySet[position] == 'thirdParty.code') {
            document.getElementById(keySet[position] + '_input').value = 'DEMO_SUPPLIER'
        } else if (keySet[position] == 'timeZone') {
            document.getElementById(keySet[position] + '_input').value = 'CET'
        } else if (document.getElementById(keySet[position] + '_input').value == '' && values[position] != ''){
            document.getElementById(keySet[position] + '_input').value = values[position][0]
        }
    }
}
