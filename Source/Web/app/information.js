const database = firebase.database();
orchardsRef = firebase.database().ref('/orchards');
let newId = -1;

function popOrch() {
    clear();
    const col2 = document.getElementById("col2");
    let count = 0;
    const orchards = [];

    orchardsRef.off();

    orchardsRef.on('value', function (snapshot) {
        col2.innerHTML="" +
            "<button type='button' class='btn btn-success' onclick='dispOrch(-1)'>Add Orchard</button>"
        ;

        snapshot.forEach(function (child) {
            let orchard = child.val();
            orchards.push(orchard);
            // newId = orchard.key;

            col2.innerHTML+="" +
                // "<button type='button' class='btn btn-info' onclick='dispOrch(" + i + ")'>"+ orchards[i].name +"</button>"
                "<button type='button' class='btn btn-info' onclick='dispOrch("+child.key+")'>"+orchard.name+"</button>"
            ;
            newId=child.key;
        });

    });



    // firebase.database().ref('/orchards').once('value').then(function(snapshot) {
    //     count = (snapshot.val() && snapshot.val().count);
    // });

    // for (let i = 0; i < count; i++){
    //     let orchard;
    //     firebase.database().ref('/orchards/' + i).once('value').then(function(snapshot) {
    //         orchard = {
    //             name : (snapshot.val() && snapshot.val().name),
    //             id : i,
    //             crop : (snapshot.val() && snapshot.val().crop),
    //             further : (snapshot.val() && snapshot.val().further)
    //         };
    //     });
    //     orchards.push(orchard);
    // }



    // for (i = 0; i < orchards.length; i++){
    //
    // }
}

function dispOrch(id) {
    const col3 = document.getElementById("col3");

    if(id === -1){
        /*Create New Orchard*/
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

        firebase.database().ref('/orchards/' + id).once('value').then(function (snapshot) {

            col3.innerHTML = "" +
                "<form class='form-horizontal'>" +
                "" +
                "<div class='form-group'><div class='col-lg-9 col-lg-offset-2'><button onclick='orchMod(" + id + ")' type='button' class='btn btn-danger'>Modify</button></div></div> " +
                "" +
                "<div class='form-group'><label class='control-label col-lg-2' for='text'>Orchard Name:</label>" +
                "<div class='col-lg-9'><p class='form-control-static'>" + snapshot.val().name + "</p> </div> </div> " +
                "" +
                "<div class='form-group'><label class='control-label col-lg-2' for='text'>Orchard Crop:</label>" +
                "<div class='col-lg-9'><p class='form-control-static'>" + snapshot.val().crop + "</p></div> </div>" +
                "" +
                "<div class='form-group'><label class='control-label col-lg-2' for='text'>Information:</label>" +
                "<div class='col-lg-9'><p class='form-control-static'>" + snapshot.val().further + "</p></div> </div>" +
                "" +
                "</form>"
            ;
        });


    }
}

function orchSave(type, id) {
    /*0 means create, 1 means modify*/
    const col3 = document.getElementById("col3");

    if(type === 0){
        newId++;
        firebase.database().ref("/orchards/" + newId).set({
            name : document.getElementById("orchName").value,
            crop : document.getElementById("orchCrop").value,
            further : document.getElementById("oi").value
        });
        id = newId;
    }
    else if(type === 1){
        firebase.database().ref("/orchards/" + id).update({
            name : document.getElementById("orchName").value,
            crop : document.getElementById("orchCrop").value,
            further : document.getElementById("oi").value
        })
    }


    popOrch();
    dispOrch(id);
}

function orchMod(id) {
    firebase.database().ref('/orchards/' + id).once('value').then(function (snapshot) {
        document.getElementById('col3').innerHTML="" +
            "<form class='form-horizontal'>" +
            "" +
            "<div class='form-group'>" +
            "<div class='col-lg-4 col-lg-offset-2'><button onclick='orchSave("+1+","+id+")' type='button' class='btn btn-warning'>Save</button></div>" +
            "<div class='col-lg-4 col-lg-offset-1'><button onclick='dispOrch("+id+")' type='button' class='btn btn-default'>Cancel</button> </div> " +
            "</div> " +
            "" +
            "<div class='form-group'><label class='control-label col-lg-2' for='text'>orchard Name:</label>" +
            "<div class='col-lg-9'><input type='text' class='form-control' id='orchName' placeholder='"+snapshot.val().name+"'></div> </div> " +
            "" +
            "<div class='form-group'><label class='control-label col-lg-2' for='text'>orchard Crop:</label>" +
            "<div class='col-lg-9'><input type='text' class='form-control' id='orchCrop' placeholder='"+snapshot.val().crop+"'></div> </div>" +
            "" +
            "<div class='form-group'><label class='control-label col-lg-2' for='text'>Information:</label>" +
            "<div class='col-lg-9'><input type='text' class='form-control' id='orchInfo' placeholder='"+snapshot.val().further+"'></div> </div>" +
            "" +
            "</form>"
        ;
    });
}

function popWorkers() {

}

function popYield() {

}

function clear(){
    document.getElementById('col3').innerHTML='';
}