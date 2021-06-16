function do_hide() {
    var hide = document.getElementsByClassName("to_hide");
    var i;
    for (i = 0; i < hide.length; i++) {
      hide[i].style.display = "none";
    }
    var unhide = document.getElementsByClassName("to_unhide");
    var j;
    for (j = 0; j < unhide.length; j++) {
      unhide[j].style.display = "block";
    }
}

function do_unhide() {
    var hide = document.getElementsByClassName("to_unhide");
    var i;
    for (i = 0; i < hide.length; i++) {
      hide[i].style.display = "none";
    }
    var unhide = document.getElementsByClassName("to_hide");
    var j;
    for (j = 0; j < unhide.length; j++) {
      unhide[j].style.display = "block";
    }
}