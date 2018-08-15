const database = firebase.database();	/* Pointing to database on firebase cloud */
const user = function() { return firebase.auth().currentUser }; /* Function which authenticates user */

/* Function returns the user ID of the selected user */
const userID = function() {
  if (user() !== null) {
    return user().uid ;
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
  return database.ref('/' +  userID() + '/admin');
}

function initPage(){
    if (user() !== null) {
        email = user().email;
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
        var auth = passwordPrompt();
        if(auth){
            database.ref('/' + userID() +'/admin').update({   
              firstname: document.getElementById("first").value,
              lastname: document.getElementById("sur").value,
              organization: document.getElementById("org").value
            });  
        }
    }
}

function saveEmail(){
    if(confirm("Are you sure you want to change your email?")){
        var auth = passwordPrompt();
        if(auth){
            email = document.getElementById("email").value;
            user().updateEmail(email);
            database.ref('/' + userID() +'/admin').update({
                email: email
            });
        }
    }
}

function savePassword(){
    var newPass = document.getElementById("psw").value;
    var newPass2 = document.getElementById("psw2").value;
    if(checkPass(newPass, newPass2)){
        if(confirm("Would you like to proceed with changing your password?")){
            var auth = passwordPrompt();
            if(auth){
                user().updatePassword(newPass);
                alert("Your password has been changed successfully.");
            }
        }
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
        var auth = passwordPrompt();
        if(auth){
            database.ref('/' + userID()).remove();
        }
    }
}   

/*makes use of a prompt to get user to enter current password before
making account changes*/
function passwordPrompt(){
    var thePrompt = window.open("", "", "height=200,width=300");
    var theHTML = "";

    theHTML += "<p>To continue this action, please enter your current password.</p>";
    theHTML += "<br/>";
    theHTML += "Password: <input type='password' id='thePass'/>";
    theHTML += "<br />";
    theHTML += "<input type='button' value='OK' id='authOK'/>";
    thePrompt.document.body.innerHTML = theHTML;

    thePrompt.document.getElementById("authOK").onclick = function () {
        var thePass = thePrompt.document.getElementById("thePass").value;
        if(user().reauthenticateWithCredential(firebase.auth.EmailAuthProvider.credential(email, thePass))){
            return true;
        }else{
            alert("Incorrect password entered.");
            return false;
        }
    };
}