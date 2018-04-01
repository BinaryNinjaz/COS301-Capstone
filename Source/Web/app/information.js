const database = firebase.database();

function popOrch() {
    clear();
    const col2 = document.getElementById("col2");
    let count = 0;
    const orchards = [];

    firebase.database().ref('/orchards').once('value').then(function(snapshot) {
        count = (snapshot.val() && snapshot.val().count);
    });

    for (let i = 0; i < count; i++){
        let orchard;
        firebase.database().ref('/orchards/' + i).once('value').then(function(snapshot) {
            orchard = {
                name : (snapshot.val() && snapshot.val().name),
                id : i,
                crop : (snapshot.val() && snapshot.val().crop),
                further : (snapshot.val() && snapshot.val().further)
            };
        });
        orchards.push(orchard);
    }

    col2.innerHTML="" +
        "<button type='button' class='btn btn-success' onclick='dispOrch(-1)'>Add orchard</button>"
    ;

    for (i = 0; i < orchards.length; i++){
        col2.innerHTML+="" +
            "<button type='button' class='btn btn-info' onclick='dispOrch(" + i + ")'>"+ orchards[i].name +"</button>"
        ;
    }
}

function dispOrch(id) {
    const col3 = document.getElementById("col3");

    if(id === -1){
        col3.innerHTML="" +
            "<form class='form-horizontal'>" +
            "" +
            "<div class='form-group'><div class='col-lg-9 col-lg-offset-2'><button onclick='orchSave(0,"+id+")' type='button' class='btn btn-warning'>Save</button></div></div> " +
            "" +
            "<div class='form-group'><label class='control-label col-lg-2' for='text'>Orchard Name:</label>" +
            "<div class='col-lg-9'><input type='text' class='form-control' id='orchName'></div> </div> " +
            "" +
            "<div class='form-group'><label class='control-label col-lg-2' for='text'>Orchard Crop:</label>" +
            "<div class='col-lg-9'><input type='text' class='form-control' id='orchCrop'></div> </div>" +
            "" +
            "<div class='form-group'><label class='control-label col-lg-2' for='text'>Information:</label>" +
            "<div class='col-lg-9'><input type='text' class='form-control' id='oi'></div> </div>" +
            "" +
            "</form>"
        ;
    }
    else {

        let orchard;
        firebase.database().ref('/orchards/' + id).once('value').then(function (snapshot) {
            orchard = {
                name: snapshot.name,
                id: id,
                crop: snapshot.crop,
                further: snapshot.further
            };
        });

        col3.innerHTML = "" +
            "<form class='form-horizontal'>" +
            "" +
            "<div class='form-group'><div class='col-lg-9 col-lg-offset-2'><button onclick='orchMod(" + orchard + ")' type='button' class='btn btn-danger'>Modify</button></div></div> " +
            "" +
            "<div class='form-group'><label class='control-label col-lg-2' for='text'>Orchard Name:</label>" +
            "<div class='col-lg-9'><p class='form-control-static'>" + orchard.name + "</p> </div> </div> " +
            "" +
            "<div class='form-group'><label class='control-label col-lg-2' for='text'>Orchard Crop:</label>" +
            "<div class='col-lg-9'><p class='form-control-static'>" + orchard.crop + "</p></div> </div>" +
            "" +
            "<div class='form-group'><label class='control-label col-lg-2' for='text'>Information:</label>" +
            "<div class='col-lg-9'><p class='form-control-static'>" + orchard.further + "</p></div> </div>" +
            "" +
            "</form>"
        ;
    }
}

function orchSave(type, id) {
    /*0 means create, 1 means modify*/
    const col3 = document.getElementById("col3");

    firebase.database().ref('/orchards/').once('value').then(function (snapshot) {
        id = snapshot.val().count || 0;
    });
    if (id === -1){
        id = 0;
    }
    if(type === 0){
        firebase.database().ref("/orchards/" + id).set({
            name : document.getElementById("orchName").value,
            crop : document.getElementById("orchCrop").value,
            further : document.getElementById("oi").value
        });
        firebase.database().ref("/orchards").update({
            count : id + 1
        });
    }
    else if(type === 1){

    }

    dispOrch(id);
    popOrch();
}

function orchMod(orchard) {
    const col3 = document.getElementById("col3");
    col3.innerHTML="" +
        "<form class='form-horizontal'>" +
        "" +
        "<div class='form-group'><div class='col-lg-9 col-lg-offset-2'><button onclick='orchSave("+1+","+orchard.id+")' type='button' class='btn btn-warning'>Save</button></div></div> " +
        "" +
        "<div class='form-group'><label class='control-label col-lg-2' for='text'>orchard Name:</label>" +
        "<div class='col-lg-9'><input type='text' class='form-control' id='orchName' placeholder='"+orchard.name+"'></div> </div> " +
        "" +
        "<div class='form-group'><label class='control-label col-lg-2' for='text'>orchard Crop:</label>" +
        "<div class='col-lg-9'><input type='text' class='form-control' id='orchCrop' placeholder='"+orchard.crop+"'></div> </div>" +
        "" +
        "<div class='form-group'><label class='control-label col-lg-2' for='text'>Information:</label>" +
        "<div class='col-lg-9'><input type='text' class='form-control' id='orchInfo' placeholder='"+orchard.further+"'></div> </div>" +
        "" +
        "</form>"
    ;
}

function popWorkers() {

}

function popYield() {

}

function clear(){
    document.getElementById('col3').innerHTML='';
}