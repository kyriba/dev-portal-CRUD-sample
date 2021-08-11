    var table = document.getElementById("update_table");

    // Create an empty <thead> element and add it to the table:
    var header = table.createTHead();

    // Create an empty <tr> element and add it to the first position of <thead>:
    var headerRow = header.insertRow(0);

    // Insert a new cell (<td>) at the first position of the "new" <tr> element:
    //var cell = row.insertCell(0);
    headerRow.innerHTML = '<th colspan="2" style="text-align:center; vertical-align:middle"><h2>Update Form</h2></th>';

    var tbody = table.createTBody();

    var isCheckbox = false;

    for (let position = 0; position < keySet.length; position++) {
        var key = keySet[position];
        var value = values[position];
        var initial_data_value = initial_data_values[position][0];
        var bodyRow = tbody.insertRow(position);
        if (availableValues != null) {
            for (let i = 0; i < availableValuesKeySet.length; i++) {
                if (key === availableValuesKeySet[i]) {
                    var htmlCode ='<td><label for="' + key + '_input">' + key + '</label><br></td>' +
                    '<td style="text-align:center; vertical-align:middle"><select name="' + key + '" id="' + key + '_input">';

                    var optionStart = '<option value="';
                    var valueEnd = '">';
                    var optionEnd = '</option>';
                    htmlCode = htmlCode + optionStart + initial_data_value + '" selected hidden>' +
                    initial_data_value + optionEnd;
                    for (let j = 0; j < availableValuesLists[i].length; j++) {
                        htmlCode = htmlCode + optionStart + availableValuesLists[i][j] + valueEnd +
                        availableValuesLists[i][j] + optionEnd;
                    }
                    bodyRow.innerHTML = htmlCode + '</select><br></td>';
                    isCheckbox = true;
                }
            }
        }
        if (key === 'uuid') {
            bodyRow.innerHTML = '<td hidden><label for="uuid_input">uuid</label></td>' +
            '<td hidden><input type="text" name="uuid" id="uuid_input" value="' + initial_data_value + '"><br></td>';
        } else if (key === 'code') {
            bodyRow.innerHTML = '<td hidden><label for="code_input">code</label></td>' +
            '<td hidden><input type="text" name="code" id="code_input" value="' + initial_data_value + '"><br></td>';
        } else if (!isCheckbox) {
            var htmlCode ='<td><label for="' + key + '">' + key + '</label><br></td>' +
            '<td style="text-align:center; vertical-align:middle"><input type="text" list="' + key +
            '" name="' + key + '" id="' + key + '_input" autocomplete="off" value="' + initial_data_value +
            '"/><datalist id="' + key + '">';

            var optionStart = '<option value="';
            var optionEnd = '">';
            for (let j = 0; j < value.length; j++) {
                htmlCode = htmlCode + optionStart + value[j] + optionEnd;
            }
            bodyRow.innerHTML = htmlCode + '</datalist><br></td>';
        }
        isCheckbox = false;
    }

    var submitBodyRow = tbody.insertRow(keySet.length);
    submitBodyRow.innerHTML = '<td colspan="3" style="text-align:center; vertical-align:middle">' +
    '<button type="submit" style="background-color: #fca130; width: 150px; color: #fff; font-weight: 700;' +
    'border: none; border-radius: 4px; height: 28px;"><b>PUT</b></button></td>';