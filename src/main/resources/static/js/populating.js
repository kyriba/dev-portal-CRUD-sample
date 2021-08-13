function populate() {
    for (let position = 0; position < keySet.length; position++) {
        if (keySet[position] == 'code' || keySet[position] == 'uuid'){
            continue;
        }
        if (document.getElementById(keySet[position] + '_input').value == '' && values[position] != ''){
            document.getElementById(keySet[position] + '_input').value = values[position][0]
        }
    }
}
