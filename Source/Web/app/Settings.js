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

$(window).bind("load", () => {
  let succ = () => {
    initPage();
  };
  let fail = () => {
    
  };
  retryUntilTimeout(succ, fail, 1000);
});

function initPage(){
    
}

function saveChanges(){
    
}

function deleteAccount(){
    
}