    var table = document.getElementById("create_table");

    var header = table.createTHead();

    var headerRow = header.insertRow(0);

    var isCheckbox = false;
    var isLast = false;
    var fieldPartsMaxAmount = 0;
    var fieldParts = new Array(keySet.length);
    var tdTag = '<td></td>';

    for (let k = 0; k < keySet.length; k++) {
        fieldParts[k] = new Array(keySet[k].split(".").length);
        fieldParts[k] = keySet[k].split(".");
        if (fieldParts[k].length > fieldPartsMaxAmount) {
            fieldPartsMaxAmount = fieldParts[k].length;
        }
    }

    headerRow.innerHTML = '<th colspan="' + (fieldPartsMaxAmount + 1) +
    '" style="text-align:center; vertical-align:middle"><h2>New Item</h2></th>';

    var tbody = table.createTBody();

    var rowCounter = 0;

    var bodyRow1 = tbody.insertRow(rowCounter++);
    bodyRow1.innerHTML = '<td colspan="' + (fieldPartsMaxAmount + 1) + '" style="text-align:center; vertical-align:' +
    'middle"><button type="button" style="width: 150px" onclick="populate()"><b>Populate</b></button></td>';

    for (let position = 0; position < keySet.length; position++) {
        var key = keySet[position];
        if (key === 'code' || key === 'interfaceCode' || key === 'shortCode') {
            var bodyRow = tbody.insertRow(rowCounter++);
            var htmlCode = '<td><label for="' + key + '_input">' + key + '</label></td>' +
            '<td style="text-align:center; vertical-align:middle"><input type="text" name="' + key +
            '" id="' + key + '_input"><br></td>';
            for (let i = 2; i <= fieldPartsMaxAmount; i++) {
                htmlCode += tdTag;
            }
            bodyRow.innerHTML = htmlCode;
        }
    }

    for (let position = 0; position < keySet.length; position++) {
        var key = keySet[position];
        var value = values[position];
        if (key === 'uuid' || key === 'code' || key === 'interfaceCode' || key === 'shortCode') {
            continue;
        }
        if (key.includes('[]')) {
            for (let i = 0; i < fieldParts[position].length; i++) {
                if (position != 0 && i < fieldParts[position - 1].length) {
                    var isDifferent = false;
                    for (let j = 0; j <= i; j++) {
                        if (fieldParts[position - 1][j] != fieldParts[position][j]) {
                            isDifferent = true;
                            break;
                        }
                    }
                    if (!isDifferent) {
                        continue;
                    }
                }
                var bodyRow = tbody.insertRow(rowCounter++);
                var htmlCode = '';
                for (let j = 0; j <= fieldPartsMaxAmount; j++) {
                    if (isLast) {
                        htmlCode += '<td style="text-align:center; vertical-align:middle">[ <input type="text" name="' +
                        key + '" id="' + key + '_input" style="width: 250px" placeholder="VAL1, VAL2, VAL3, VAL4"> ]' +
                        '<br></td>';
                        isLast = false;
                    } else if (i == j) {
                        if (i == fieldParts[position].length - 1) {
                            htmlCode += '<td><label for="' + key + '_input">' + fieldParts[position][i] + '</label></td>';
                            isLast = true;
                        } else {
                            htmlCode += '<td><label>' + fieldParts[position][i] + ':</label></td>';
                        }
                    } else {
                        htmlCode += tdTag;
                    }
                }
                bodyRow.innerHTML = htmlCode;
            }
            continue;
        }
        if (availableValues != null) {
            for (let k = 0; k < availableValuesKeySet.length; k++) {
                if (key === availableValuesKeySet[k]) {
                    for (let i = 0; i < fieldParts[position].length; i++) {
                        if (position != 0 && i < fieldParts[position - 1].length) {
                            var isDifferent = false;
                            for (let j = 0; j <= i; j++) {
                                if (fieldParts[position - 1][j] != fieldParts[position][j]) {
                                    isDifferent = true;
                                    break;
                                }
                            }
                            if (!isDifferent) {
                                continue;
                            }
                        }
                        var bodyRow = tbody.insertRow(rowCounter++);
                        var htmlCode = '';
                        for (let j = 0; j <= fieldPartsMaxAmount; j++) {
                            if (isLast) {
                                htmlCode += '<td style="text-align:center; vertical-align:middle"><select name="' + key +
                                '" id="' + key + '_input">';
                                var optionStart = '<option value="';
                                var valueEnd = '">';
                                var optionEnd = '</option>';
                                for (let l = 0; l < availableValuesLists[k].length; l++) {
                                    htmlCode += optionStart + availableValuesLists[k][l] + valueEnd +
                                    availableValuesLists[k][l] + optionEnd;
                                }
                                htmlCode += '</select><br></td>';
                                isCheckbox = true;
                                isLast = false;
                            } else if (i == j) {
                                if (i == fieldParts[position].length - 1) {
                                    htmlCode += '<td><label for="' + key + '_input">' + fieldParts[position][i] +
                                    '</label></td>';
                                     isLast = true;
                                } else {
                                    htmlCode += '<td><label>' + fieldParts[position][i] + ':</label></td>';
                                }
                            } else {
                                htmlCode += tdTag;
                            }
                        }
                        bodyRow.innerHTML = htmlCode;
                    }
                    isCheckbox = true;
                }
            }
        }
        if (!isCheckbox) {
            for (let i = 0; i < fieldParts[position].length; i++) {
                if (position != 0 && i < fieldParts[position - 1].length) {
                    var isDifferent = false;
                    for (let j = 0; j <= i; j++) {
                        if (fieldParts[position - 1][j] != fieldParts[position][j]) {
                            isDifferent = true;
                            break;
                        }
                    }
                    if (!isDifferent) {
                        continue;
                    }
                }
                var bodyRow = tbody.insertRow(rowCounter++);
                var htmlCode = '';
                for (let j = 0; j <= fieldPartsMaxAmount; j++) {
                    if (isLast) {
                        htmlCode += '<td style="text-align:center; vertical-align:middle"><input type="text"' +
                        ' list="' + key + '" name="' + key + '" id="' + key +
                        '_input" autocomplete="off"/><datalist id="' + key + '">';
                        var optionStart = '<option value="';
                        var optionEnd = '">';
                        for (let k = 0; k < value.length; k++) {
                            htmlCode += optionStart + value[k] + optionEnd;
                        }
                        htmlCode += '</datalist><br></td>';
                        isLast = false;
                    } else if (i == j) {
                        if (i == fieldParts[position].length - 1) {
                            htmlCode += '<td><label for="' + key + '">' + fieldParts[position][i] + '</label><br></td>';
                            isLast = true;
                        } else {
                            htmlCode += '<td><label>' + fieldParts[position][i] + ':</label></td>';
                        }
                    } else {
                        htmlCode += tdTag;
                    }
                }
                bodyRow.innerHTML = htmlCode;
            }
        }
        isCheckbox = false;
    }

    var submitBodyRow = tbody.insertRow(rowCounter++);
    submitBodyRow.innerHTML = '<td colspan="' + (fieldPartsMaxAmount + 1) + '" style="text-align:center; ' +
    'vertical-align:middle"><button type="submit" style="background-color: #49cc90; width: 150px; color: #fff;' +
    'font-weight: 700; border: none; border-radius: 4px; height: 28px;"><b>POST</b></button></td>';