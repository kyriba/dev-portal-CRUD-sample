function populate() {
    for (let position = 0; position < keySet.length; position++) {
        if (keySet[position] == 'code' || keySet[position] == 'uuid'
            || keySet[position] == 'interfaceCode' || keySet[position] == 'shortCode'){
            continue;
        }
        if (keySet[position] == 'firstName') {
            document.getElementById(keySet[position] + '_input').value = 'John'
        }
        if (keySet[position] == 'lastName') {
            document.getElementById(keySet[position] + '_input').value = 'Doe'
        }
        if (keySet[position] == 'accessProfile.code') {
            document.getElementById(keySet[position] + '_input').value = 'DEV'
        }
        if (keySet[position] == 'companyOwnership.code') {
            document.getElementById(keySet[position] + '_input').value = 'COMPANY01'
        }
        if (keySet[position] == 'email') {
            document.getElementById(keySet[position] + '_input').value = 'test@mail.com'
        }
        if (keySet[position] == 'fax') {
            document.getElementById(keySet[position] + '_input').value = '1-212-555-1234'
        }
        if (keySet[position] == 'mobilePhone') {
            document.getElementById(keySet[position] + '_input').value = '1-212-999-8765'
        }
        if (keySet[position] == 'officePhone') {
            document.getElementById(keySet[position] + '_input').value = '000-000-000'
        }
        if (keySet[position] == 'thirdParty.code') {
            document.getElementById(keySet[position] + '_input').value = 'DEMO_SUPPLIER'
        }
        if (keySet[position] == 'timeZone') {
            document.getElementById(keySet[position] + '_input').value = 'CET'
        }
        if (document.getElementById(keySet[position] + '_input').value == '' && values[position] != ''){
            document.getElementById(keySet[position] + '_input').value = values[position][0]
        }
    }
}
