    var table = document.getElementById("myTable");

    // Create an empty <thead> element and add it to the table:
    var header = table.createTHead();

    // Create an empty <tr> element and add it to the first position of <thead>:
    var headerRow = header.insertRow(0);

    // Insert a new cell (<td>) at the first position of the "new" <tr> element:
    //var cell = row.insertCell(0);
    headerRow.innerHTML = '<th colspan="2" style="text-align:center; vertical-align:middle"><h2>Create Form</h2></th>';

    var tbody = table.createTBody();

    var bodyRow1 = tbody.insertRow(0);
    bodyRow1.innerHTML = '<td colspan="2" style="text-align:center; vertical-align:middle"><button type="button" style="width: 150px" onclick="populate()"><b>Populate</b></button></td>';

    var num = 0;

    for (let position = 1; position <= keySet.length; position++) {
        var key = keySet[position - 1];
        var value = values[position - 1];
        if (key === 'uuid') {
            num = 1;
            continue;
        }
        var bodyRow = tbody.insertRow(position - num);
        if (key === 'code') {
            bodyRow.innerHTML = '<td><label for="code_input">code</label></td>' +
            '<td style="text-align:center; vertical-align:middle"><input type="text" name="code" id="code_input"><br></td>';
        } else {
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
    }

    var bodyRow2 = tbody.insertRow(keySet.length - num + 1);
    bodyRow2.innerHTML = '<td colspan="3" style="text-align:center; vertical-align:middle">' +
    '<button type="submit" style="background-color: #49cc90; width: 150px; color: #fff;' +
    'font-weight: 700; border: none; border-radius: 4px; height: 28px;"><b>POST</b></button></td>';