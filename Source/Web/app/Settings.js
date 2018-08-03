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
//var password;
var organization;
var firstname;
var surname;

$(window).bind("load", () => {
  let succ = () => {
    initPage();
  };
  let fail = () => {
    email = '';
    password = '';
    organization = '';
    firstname = '';
    surname = '';
  };
  retryUntilTimeout(succ, fail, 1000);
});

function initPage(){
    if (user() !== null) {
        email = user().email;
        document.getElementById("email").value = email;
        const ref = database.ref('/' + userID() + '/admin');
        ref.once('value').then((snapshot) => {
            const val = snapshot.val();
            organization = val.organization;
            document.getElementById("org").value = organization;
            firstname = val.firstname;
            document.getElementById("first").value = firstname;
            surname = val.lastname;
            document.getElementById("sur").value = surname;
        });
    }
    
}

function saveChanges(){
    
}

function deleteAccount(){
    
}