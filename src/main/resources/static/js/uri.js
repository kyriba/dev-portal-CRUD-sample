let uri2 = new URL(document.querySelector('#basic-url2').value);
let uri3 = new URL(document.querySelector('#basic-url3').value);
let uri5 = new URL(document.querySelector('#basic-url5').value);
let uri6 = new URL(document.querySelector('#basic-url6').value);

let cURL2 = document.querySelector('#basic-url2');
let cURL3 = document.querySelector('#basic-url3');
let cURL5 = document.querySelector('#basic-url5');
let cURL6 = document.querySelector('#basic-url6');

const get_uuid = document.querySelector('#get_uuid');
const get_code = document.querySelector('#get_code');
const update_code = document.querySelector('#update_code');
const delete_code = document.querySelector('#delete_code');

get_uuid.addEventListener('input', event => {
    cURL2.value = decodeURIComponent(uri2 + event.target.value);
});

get_code.addEventListener('input', event => {
    cURL3.value = decodeURIComponent(uri3 + event.target.value);
});

update_code.addEventListener('input', event => {
    cURL5.value = decodeURIComponent(uri5 + event.target.value);
});

delete_code.addEventListener('input', event => {
    cURL6.value = decodeURIComponent(uri6 + event.target.value);
});