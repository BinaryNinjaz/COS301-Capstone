
const database = firebase.database();
let newId = -1;
let newIdSet = false;

function popOrch() {
    clear();
    const col2 = document.getElementById("col2");
    col2.innerHTML = "<h2>Loading Orchard Information</h2>"

    orchardsRef = firebase.database().ref('/orchards');
    orchardsRef.off();

    orchardsRef.on('value', function (snapshot) {
        col2.innerHTML="" +
            "<button type='button' class='btn btn-success' onclick='dispOrch(-1)'>Add Orchard</button>"
        ;

        snapshot.forEach(function (child) {
            col2.innerHTML+="" +
                "<button type='button' class='btn btn-info' onclick='dispOrch("+child.key+")'>"+child.val().name+"</button>"
            ;
            newId=child.key;
        });

    });
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

    if(type === 0){
        newId++;
        firebase.database().ref("/orchards/" + newId).set({
            name : document.getElementById("orchName").value,
            crop : document.getElementById("orchCrop").value,
            further : document.getElementById("oi").value
        });
        id = newId;
        popOrch();
    }
    else if(type === 1){
        firebase.database().ref("/orchards/" + id).update({
            name : document.getElementById("orchName").value,
            crop : document.getElementById("orchCrop").value,
            further : document.getElementById("oi").value
        });
    }
    dispOrch(id);
}

function orchMod(id) {
    firebase.database().ref('/orchards/' + id).once('value').then(function (snapshot) {
        document.getElementById('modalDelBut').innerHTML="<button type='button' class='btn btn-danger' data-dismiss='modal' onclick='delOrch("+id+")'>Delete</button>";
        document.getElementById('modalText').innerText="Please confirm deletion of " + snapshot.val().name;
        document.getElementById('col3').innerHTML="" +
            "<form class='form-horizontal'>" +
            "" +
            "<div class='form-group'>" +
            "<div class='col-lg-3 col-lg-offset-2'><button onclick='orchSave("+1+","+id+")' type='button' class='btn btn-warning'>Save</button></div>" +
            "<div class='col-lg-3'><button type='button' class='btn btn-danger' data-toggle='modal' data-target='#delModal'>Delete</button></div> " +
            "<div class='col-lg-3'><button onclick='dispOrch("+id+")' type='button' class='btn btn-default'>Cancel</button></div>" +
            "</div> " +
            "" +
            "<div class='form-group'><label class='control-label col-lg-2' for='text'>Orchard Name:</label>" +
            "<div class='col-lg-9'><input type='text' class='form-control' id='orchName' value='"+snapshot.val().name+"'></div> </div> " +
            "" +
            "<div class='form-group'><label class='control-label col-lg-2' for='text'>Orchard Crop:</label>" +
            "<div class='col-lg-9'><input type='text' class='form-control' id='orchCrop' value='"+snapshot.val().crop+"'></div> </div>" +
            "" +
            "<div class='form-group'><label class='control-label col-lg-2' for='text'>Information:</label>" +
            "<div class='col-lg-9'><input type='text' class='form-control' id='oi' value='"+snapshot.val().further+"'></div> </div>" +
            "" +
            "</form>"
        ;
    });
}

function delOrch(id) {
    firebase.database().ref('/orchards/' + id).remove();
    popOrch();
}

function clear(){
    document.getElementById('col3').innerHTML='';
}