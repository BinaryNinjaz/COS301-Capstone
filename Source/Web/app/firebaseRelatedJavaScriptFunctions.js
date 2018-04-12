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

//Old from testing, to be removed.
/*function fbSignIn(email, password) {
    firebase.auth().signInWithEmailAndPassword(email, password).catch(function (error) {
        // Handle Errors here.
        alert("Sign In Fail");
        // ...
    });
}*/

// This is automatic whenever there's a change to a users authorization
/*firebase.auth().onAuthStateChanged(function (user) {
    if (user) {
        // user logged in
        // window.location.href = "HomePage.html";
    } else {
        // user logged out
        window.location.href = "DynamicLoginRegister.html";
    }
});*/

function firebaseRegister(email, password) {
    password += email + "123456";
    firebase.auth()
        .createUserWithEmailAndPassword(email, password)
        .then(function (user) { // user details correct
            document.location.href = "HomePage.html";
        })
        .catch(function (error) { // some error occured
            var errorCode = error.code;
            var errorMessage = error.message;

            if (errorCode === 'auth/wrong-password') {
                alert('Wrong password.');
            } else {
                alert(errorMessage);
            }
            console.log(error);
        });
}

function firebaseLogin() {
    var email = document.getElementById("username").value;
    var password = document.getElementById("password").value;
    password += email + "123456";

    firebase.auth().signInWithEmailAndPassword(email, password).then(function (user) { // user details correct
        document.location.href = "HomePage.html";
    }).catch(function (error) { // some error occured
        var errorCode = error.code;
        var errorMessage = error.message;

        if (errorCode === 'auth/wrong-password') {
            alert('Wrong password.');
        } else {
            alert(errorMessage);
        }
        console.log(error);
    });
}

function signOut() {
    firebase.auth().signOut().then(function () {
        document.location.href = "DynamicLoginRegister.html";
    });
}

function sendPasswordResetEmail(emailAddress) {
    firebase.auth().sendPasswordResetEmail(emailAddress).then(function () {
        // Email sent.
    }).catch(function (error) {
        // An error happened.
    });
}

// let user /*= firebase.auth().currentUser*/;
//
// window.onload = function() {
//     let otherUser = firebase.auth().currentUser;
//     if (otherUser != null){
//         user = otherUser;
//     }
//     document.getElementById("emailInNav").innerHTML = user.email + " <span class=\"glyphicon glyphicon-cog\"></span>";
// };

//IT'S ASYNC TIME BABY"!!! TODO:Get this working with async.

window.onload = function () {
    document.getElementById("emailInNav").innerHTML = "user email" + " <span class=\"glyphicon glyphicon-cog\"></span>"
};

// document.addEventListener("DOMContentLoaded", function() {
//     document.getElementById("emailInNav").innerHTML = firebase.auth().currentUser.email + " <span class=\"glyphicon glyphicon-cog\"></span>";
// });