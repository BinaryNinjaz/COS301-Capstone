function initializeFirebase() {
    var config = {
        apiKey: "AIzaSyBTTgAtocwDfror-XZLi4R5rUEphTUK4PU",
        authDomain: "harvest-ios-1522082524457.firebaseapp.com",
        databaseURL: "https://harvest-ios-1522082524457.firebaseio.com",
        projectId: "harvest-ios-1522082524457",
        storageBucket: "harvest-ios-1522082524457.appspot.com",
        messagingSenderId: "167365669216"
    };
    firebase.initializeApp(config);
}
initializeFirebase();// set up the database

function refreshPage() {
    location.reload();
}

function fbSignIn(email, password) {
    firebase.auth().signInWithEmailAndPassword(email, password).catch(function(error) {
        // Handle Errors here.
        alert("Sign In Fail");
        // ...
    });
}