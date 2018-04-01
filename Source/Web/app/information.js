var database = firebase.database();

function popOrch() {
    var col2=document.getElementById("col2");
    var count = 0;
    var orchids;

    firebase.database().ref('/orchids').once('value').then(function(snapshot) {
        count = snapshot.val().count || 0;
    });

    for (var i = 0; i < count; i++){
        var orchid;
        firebase.database().ref('/orchids/' + i).once('value').then(function(snapshot) {
            orchid = {
                name : snapshot.name,
                id : snapshot.id,
                crop : snapshot.crop,
                further : snapshot.further
            };
        });
        orchids.push(orchid);
    }

    col2.innerHTML="" +
        "<button type='button' class='btn btn-success' onclick='dispOrch(-1)'>Add Orchid</button>"
    ;

    for (i = 0; i < orchids.length; i++){
        col2.innerHTML+="" +
            "<button type='button' class='btn btn-info' onclick='dispOrch(" + i + ")'>"+ orchids[i] +"</button>"
        ;
    }
}

function dispOrch(id) {

}

function popWorkers() {

}

function popYield() {

}