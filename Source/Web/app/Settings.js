const database = firebase.database();	/* Pointing to database on firebase cloud */
const getUser = function() { return firebase.auth().currentUser; }; /* Function which authenticates user */

/* Function returns the user ID of the selected user */
const getUserID = function() {
  if (getUser() !== null) {
    return getUser().uid ;
  } else {
    return "";
  }
};

var email;

$(window).bind("load", () => {
  let succ = () => {
    initPage();
  };
  let fail = () => {
    email = '';
  };
  retryUntilTimeout(succ, fail, 1000);
});

/* Function returns admin */
function adminRef() {
  return database.ref('/' +  getUserID() + '/admin');
}

function initPage(){
    if (getUser() !== null) {
        email = getUser().email;
        const ref = adminRef();
        ref.once('value').then((snapshot) => {
            const val = snapshot.val();
            document.getElementById("email").value = email;
            document.getElementById("org").value = val.organization;
            document.getElementById("first").value = val.firstname;
            document.getElementById("sur").value = val.lastname;
        });
    }

}

function saveChanges(){
    if(confirm("Are you sure you want to save these account changes?")){
        database.ref('/' + getUserID() +'/admin').update({
          firstname: document.getElementById("first").value,
          lastname: document.getElementById("sur").value,
          organization: document.getElementById("org").value
        });
    }
}

/* TODO: verify new email as a real email address */
function saveEmail(){
    if(confirm("Are you sure you want to change your email?")){
        email = document.getElementById("email").value;
        getUser().updateEmail(email);
        database.ref('/' + getUserID() +'/admin').update({
            email: email
        });
    }
}

function savePassword(){
    var newPass = document.getElementById("psw").value;
    var newPass2 = document.getElementById("psw2").value;
    if(checkPass(newPass, newPass2)){
        var thePrompt = constructPrompt();
        thePrompt.document.getElementById("authOK").onclick = function () {
            var thePass = thePrompt.document.getElementById("thePass").value;
            firebase.auth().signInWithEmailAndPassword(email, thePass)
            .then(function() {
                getUser().updatePassword(newPass).then(function(){
                    thePrompt.close();
                    alert("Password successfully changed.");
                    document.getElementById("psw").value = '';
                    document.getElementById("psw2").value = '';
                }).catch(function(err){
                    thePrompt.close();
                    alert("An error has occurred with changing the password.");
                });
            }).catch(function(err){
                thePrompt.close();
                alert("Incorrect password entered.");
            });
        };
    }else{
        alert("Please make sure you have entered the new password correctly and that it is at least 6 characters long.");
    }
}

function checkPass(psw, psw2){
    if(psw === psw2){
        if(psw.length >= 6){
            return true;
        }else{
            return false;
        }
    }else{
        return false;
    }
}

function deleteAccount(){
    if(confirm("Are you sure you want to permanently delete this account and all the information it contains?")){
        var thePrompt = constructPrompt();
        thePrompt.document.getElementById("authOK").onclick = function () {
            var thePass = thePrompt.document.getElementById("thePass").value;
            if(getUser().reauthenticateWithCredential(firebase.auth.EmailAuthProvider.credential(email, thePass))){
                thePrompt.close();
                firebase.auth().signOut().then(function () {
                    database.ref('/' + getUserID()).remove();
                    document.location.href = "index.html";
                });
            }else{
                thePrompt.close();
                alert("Incorrect password entered.");
            }
        };
    }
}

/* creates a prompt to get user to enter current password before
making account changes */
function constructPrompt(){
    var thePrompt = window.open("", "", "height=200,width=300");
    var theHTML = "";

    theHTML += "<title>Enter password</title>";
    theHTML += "<p style='font-family:sans-serif'>To continue this action, please enter your current password.</p>";
    theHTML += "<br/><label for='thePass' style='font-family:sans-serif'>";
    theHTML +="<b>Password:</b></label><input type='password' id='thePass'/>";
    theHTML += "<br /><br />";
    theHTML += "<input type='button' value='Cancel' id='canc' ";
    theHTML += "style='background-color:red;border:none;color:white;cursor:pointer;";
    theHTML +="padding:10px 10px'/>";
    theHTML +="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp";
    theHTML += "<input type='button' value='Continue' id='authOK' ";
    theHTML += "style='background-color:#4CAF50;border:none;color:white;cursor:pointer;";
    theHTML +="padding:10px 10px'/>";
    thePrompt.document.body.innerHTML = theHTML;
    thePrompt.document.getElementById("canc").onclick = function () {
        thePrompt.close();
    };
    return thePrompt;
}

function loadUserManual() {
  window.location.href = "HarvestUserManual.pdf"
}
