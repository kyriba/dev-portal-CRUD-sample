function do_hide() {
    var x = document.getElementsByClassName("to_hide");
    var i;
    for (i = 0; i < x.length; i++) {
      x[i].style.display = "none";
    }
}