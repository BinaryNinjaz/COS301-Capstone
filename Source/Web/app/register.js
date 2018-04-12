function register() {
    checkPass(document.getElementById("password").value, document.getElementById("passwordConf").value);
}

function checkPass(pass1, pass2) {
    if (pass1 === "" || pass2 === "") {
        document.getElementById("errorSpace").innerHTML = "<p class='errmsg'>Passwords cannot be empty</p>";
        return false;
    }
    else if (pass1 !== pass2) {
        document.getElementById("errorSpace").innerHTML = "<p class='errmsg'>Passwords do not match</p>";
        document.getElementById("password").value = 0;
        document.getElementById("passwordConf").value = 0;
        return false;
    }
    if (pass1 === pass2) {
        document.getElementById("errorSpace").innerHTML = "<br>";
        return true;
    }
}