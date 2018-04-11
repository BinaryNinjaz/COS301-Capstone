function register() {
  const pass = document.getElementById("password");
  const pass2 = document.getElementById("passwordConf");
  if (pass.value !== pass2.value) {
    pass.value = "";
    pass2.value = "";
    document.getElementById("errorSpace").innerHTML = "<p class='errmsg'>Passwords do not match</p>";
  }
  else {
    document.getElementById("errorSpace").innerHTML = "<br>";
    firebaseRegister();
  }
}