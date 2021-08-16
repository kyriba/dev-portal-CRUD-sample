    var table = document.getElementById("create_table");

    var header = table.createTHead();

    var headerRow = header.insertRow(0);

    headerRow.innerHTML = '<th colspan="2" style="text-align:center; vertical-align:middle"><h2>New Item</h2></th>';

    var tbody = table.createTBody();

    var bodyRow1 = tbody.insertRow(0);
    bodyRow1.innerHTML = '<td colspan="2" style="text-align:center; vertical-align:middle"><button type="button" style="width: 150px" onclick="populate()"><b>Populate</b></button></td>';

    var num = 0;
    var isCheckbox = false;

    for (let position = 1; position <= keySet.length; position++) {
        var key = keySet[position - 1];
        var value = values[position - 1];
        if (key === 'uuid') {
            num = 1;
            continue;
        }
        var bodyRow = tbody.insertRow(position - num);
        if (availableValues != null) {
            for (let i = 0; i < availableValuesKeySet.length; i++) {
                if (key === availableValuesKeySet[i]) {
                    var htmlCode ='<td><label for="' + key + '_input">' + key + '</label><br></td>' +
                    '<td style="text-align:center; vertical-align:middle"><select name="' + key + '" id="' + key + '_input">';

                    var optionStart = '<option value="';
                    var valueEnd = '">';
                    var optionEnd = '</option>';
                    for (let j = 0; j < availableValuesLists[i].length; j++) {
                        htmlCode = htmlCode + optionStart + availableValuesLists[i][j] + valueEnd +
                        availableValuesLists[i][j] + optionEnd;
                    }
                    bodyRow.innerHTML = htmlCode + '</select><br></td>';
                    isCheckbox = true;
                }
            }
        }
        if (key === 'code' || key === 'interfaceCode') {
            bodyRow.innerHTML = '<td><label for="' + key + '_input">' + key + '</label></td>' +
            '<td style="text-align:center; vertical-align:middle"><input type="text" name="' + key
            + '" id="' + key + '_input"><br></td>';
        } else if (!isCheckbox) {
            var htmlCode ='<td><label for="' + key + '">' + key + '</label><br></td>' +
            '<td style="text-align:center; vertical-align:middle"><input type="text" list="' + key +
            '" name="' + key + '" id="' + key + '_input" autocomplete="off"/><datalist id="' + key + '">';

            var optionStart = '<option value="';
            var optionEnd = '">';
            for (let j = 0; j < value.length; j++) {
                htmlCode = htmlCode + optionStart + value[j] + optionEnd;
            }
            bodyRow.innerHTML = htmlCode + '</datalist><br></td>';
        }
        isCheckbox = false;
    }

    var submitBodyRow = tbody.insertRow(keySet.length - num + 1);
    submitBodyRow.innerHTML = '<td colspan="3" style="text-align:center; vertical-align:middle">' +
    '<button type="submit" style="background-color: #49cc90; width: 150px; color: #fff;' +
    'font-weight: 700; border: none; border-radius: 4px; height: 28px;"><b>POST</b></button></td>';