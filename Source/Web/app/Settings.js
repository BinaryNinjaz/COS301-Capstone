const database = firebase.database();	/* Pointing to database on firebase cloud */
const user = function() { return firebase.auth().currentUser }; /* Function which authenticates user */

/* Function returns the user ID of the selected user */
const userID = function() {
  if (user() !== null) {
    return user().uid ;
  } else {
    return "";
  }
}

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
    if(confirm("Are you sure you want to save your account changes?")){
        firebase.database().ref('/' + userID() +'/admin').update({   
          firstname: document.getElementById("first").value,
          lastname: document.getElementById("sur").value,
          organization: document.getElementById("org").value
        });  
    }
}

function saveEmail(){
    var password = passwordPrompt();
}

function savePassword(){
    var password = passwordPrompt();
}

function deleteAccount(){
    if(confirm("Are you sure you want to permanently delete this account?")){
       var password = passwordPrompt();
       firebase.database().ref('/' + userID()).remove();
    }
}   

function passwordPrompt(){
    var thePrompt = window.open("", "", "widht=500");
    var theHTML = "";

    theHTML += "<p>To continue this action, please enter your current password.</p>";
    theHTML += "<br/>";
    theHTML += "Password: <input type='password' id='thePass'/>";
    theHTML += "<br />";
    theHTML += "<input type='button' value='OK' id='authOK'/>";
    thePrompt.document.body.innerHTML = theHTML;

    var thePass = thePrompt.document.getElementById("thePass").value;
    thePrompt.document.getElementById("authOK").onclick = function () {
        // do authentication
    }
}