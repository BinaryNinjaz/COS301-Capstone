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