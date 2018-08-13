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

}

function savePassword(){

}

function deleteAccount(){
    if(confirm("Are you sure you want to permanently delete this account?")){
       firebase.database().ref('/' + userID()).remove();
    }
}   
