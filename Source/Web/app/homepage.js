// A reference to the database location
var yieldRef = firebase.database().ref('yields');
// When a yield is added, changed or deleted
yieldRef.on('value', function(snapshot) {
    // snapshot is the part of the database that was changed

    var yields = document.getElementById('yields');
    yields.innerHTML = "";

    // yields has multiple yield inputs
    // so we iterate over each yield individually
    snapshot.forEach(function(childSnapshot) {
        // we use val() to get the actual data.
        // the we can obj like it was just a normal object with fields
        var obj = childSnapshot.val();

        var date = obj.date;
        var duration = obj.duration;
        var email = obj.email;
        var lat = obj.location.lat;
        var lng = obj.location.lng;
        var yieldAmount = obj.yield;

        yields.innerHTML += "<b>Date: </b>" + date + "</br>";
        yields.innerHTML += "<b>Duration: </b>" + duration + "</br>";
        yields.innerHTML += "<b>Email: </b>" + email + "</br>";
        yields.innerHTML += "<b>Location: </b>" + lat + "," + lng + "</br>";
        yields.innerHTML += "<b>Yield: </b>" + yieldAmount + "</br></br>";
    });
});