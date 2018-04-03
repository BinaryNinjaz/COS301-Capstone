
const database = firebase.database();
let newId = -1;

function popOrch() {
    clear();
    const col2 = document.getElementById("col2");
    col2.innerHTML = "<h2>Loading Orchard Information</h2>"

    const orchardsRef = firebase.database().ref('/orchards');
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
            // "<div class='col-lg-9'><input type='text' class='form-control' id='oi'></div> </div>" +
            "<div class='col-lg-9'><textarea class='form-control' rows='4' id='oi'></textarea></div></div>" +
            "" +
            "</form>"
        ;
    }
    else {

        firebase.database().ref('/orchards/' + id).once('value').then(function (snapshot) {

            col3.innerHTML = "" +
                "<form class='form-horizontal'>" +
                "" +
                "<div class='form-group'><div class='col-lg-9 col-lg-offset-2'><button onclick='orchMod(" + id + ")' type='button' class='btn btn-default'>Modify</button></div></div> " +
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
            // "<div class='col-lg-9'><input type='text' class='form-control' id='oi' value='"+snapshot.val().further+"'></div> </div>" +
            "<div class='col-lg-9'><textarea class='form-control' rows='4' id='oi'>"+snapshot.val().further+"</textarea></div> </div>" +
            "" +
            "</form>"
        ;
    });
}

function delOrch(id) {
    firebase.database().ref('/orchards/' + id).remove();
    popOrch();
}

function popWork() {
    clear();
    const col2 = document.getElementById("col2");
    col2.innerHTML = "<h2>Loading Worker Information</h2>"

    const workersRef = firebase.database().ref('/workers');
    workersRef.off();

    workersRef.on('value', function (snapshot) {
        col2.innerHTML="" +
            "<button type='button' class='btn btn-success' onclick='dispWork(-1)'>Add Worker</button>"
        ;

        snapshot.forEach(function (child) {
            col2.innerHTML+="" +
                "<button type='button' class='btn btn-info' onclick='dispWork("+child.key+")'>"+child.val().name.charAt(0) +". "+ child.val().surname+"</button>"
            ;
            newId=child.key;
        });

    });
}

function dispWork(id) {
    const col3 = document.getElementById("col3");

    if(id === -1){
        /*Create New Worker*/


        firebase.database().ref('/orchards').once('value').then(function (snapshot) {
            col3.innerHTML="" +
                "<form class='form-horizontal'>" +
                "" +
                "<div class='form-group'><div class='col-lg-9 col-lg-offset-2'><button onclick='workSave(0,"+id+")' type='button' class='btn btn-warning'>Save</button></div></div> " +
                "" +
                "<div class='form-group'><label class='control-label col-lg-2' for='text'>Worker Name:</label>" +
                "<div class='col-lg-9'><input type='text' class='form-control' id='workName'></div> </div> " +
                "" +
                "<div class='form-group'><label class='control-label col-lg-2' for='text'>Worker Surname:</label>" +
                "<div class='col-lg-9'><input type='text' class='form-control' id='workSName'></div> </div>" +
                "" +
                "<div class='form-group'><label class='control-label col-lg-2' for='sel1'>Assigned Orchard:</label>" +
                "<div class='col-lg-9'><select class='form-control' id='workOrch'></select></div></div>" +
                "" +
                "<div class='form-group'><label class='control-label col-lg-2' id='workType'>Worker Type:</label>" +
                "<label class='radio-inline'><input type='radio' name='optradio' onclick='dispWorkEmail(false)' id='rWorker' checked>Worker</label>" +
                "<label class='radio-inline'><input type='radio' name='optradio' onclick='dispWorkEmail(true)' id='rForeman'>Foreman</label>" +
                "</div>" +
                "" +
                "<div class='form-group'><label class='control-label col-lg-2' for='text'>Information:</label>" +
                "<div class='col-lg-9'><textarea class='form-control' rows='4' id='workInfo'></textarea></div></div>" +
                "" +
                "<div id='emailSpace'></div>" +
                "" +

                "</form>"
            ;

            snapshot.forEach(function (child) {
                document.getElementById("workOrch").innerHTML+="<option><" +child.key+"> " + child.val().name+"  :  "+child.val().crop + "</option>";
            });
        });
    }
    else {
        firebase.database().ref('/workers' + id).once('value').then(function (snapshot) {
            firebase.database().ref('/orchards').once('value').then(function (orchardSnapshot) {
                col3.innerHTML = "" +
                    "<form class='form-horizontal'>" +
                    "" +
                    "<div class='form-group'><div class='col-lg-9 col-lg-offset-2'><button onclick='workMod(" + id + ")' type='button' class='btn btn-default'>Modify</button></div></div> " +
                    "" +
                    "<div class='form-group'><label class='control-label col-lg-2' for='text'>Worker Name:</label>" +
                    "<div class='col-lg-9'><p class='form-control-static'>" + snapshot.val().name + "</p> </div> </div> " +
                    "" +
                    "<div class='form-group'><label class='control-label col-lg-2' for='text'>Worker Surname:</label>" +
                    "<div class='col-lg-9'><p class='form-control-static'>" + snapshot.val().surname + "</p> </div> </div>" +
                    "" +
                    "<div class='form-group'><label class='control-label col-lg-2' for='sel1'>Assigned Orchard:</label>" +
                    "<div class='col-lg-9'><span id='workOrchDisp'></span></div></div>" +
                    "" +
                    "<div class='form-group'><label class='control-label col-lg-2' id='workType'>Worker Type:</label>" +
                    "<div class='col-lg-9'><p class='form-control-static'>"+ snapshot.val().type+"</p> </div>" +
                    "</div>" +
                    "" +
                    "<div class='form-group'><label class='control-label col-lg-2' for='text'>Information:</label>" +
                    "<div class='col-lg-9'><p class='form-control-static'>"+ snapshot.val().info+"</p> </div></div>" +
                    "" +
                    "<div id='emailDispSpace'></div>" +
                    "" +

                    "</form>"
                ;

                orchardSnapshot.forEach(function (orchard) {
                    if(orchard.key === snapshot.val().orchard){
                        document.getElementById("workOrchDisp").innerHTML="<a class='form-control-static' href='javascript:;' onclick='dispOrch(" + orchard.key + ")>"+orchard.name+"</a>"
                    }
                });

                if(snapshot.type === "Foreman"){
                    document.getElementById("emailDispSpace").innerHTML = ""+
                        "<div class='form-group'><label class='control-label col-lg-2' for='text'>Foreman Email:</label>" +
                        "<div class='col-lg-9'><p class='form-control-static'>"+ snapshot.val().email+"</p> </div></div>"
                    ;
                }
            });
        });


    }
}

function dispWorkEmail(disp) {
    if(disp) {
        document.getElementById("emailSpace").innerHTML = "" +
            "<div class='form-group'><label class='control-label col-lg-2' for='email'>Foreman Email:</label>" +
            "<div class='col-lg-9'><input type='email' class='form-control' id='workEmail' data-toggle='tooltip' title='We will send the foreman an email so they can get started on the app.'>" +
            "</div></div>"
        ;
    }
    else {
        document.getElementById("emailSpace").innerHTML = "";
    }
}

function workSave(type, id) {
    /*0 means create, 1 means modify*/
    const orchard = document.getElementById("workOrch").value;
    const orchID = orchard.substring(orchard.indexOf("<") + 1, orchard.indexOf(">"));
    let workType="Foreman";
    if (document.getElementById("rWorker").checked){
        workType = "Worker";
    }
    let email = "";
    if (workType === "Foreman"){
        email = document.getElementById("workEmail").value;
    }
    if(type === 0){
        newId++;
        firebase.database().ref("/workers/" + newId).set({
            name : document.getElementById("workName").value,
            surname : document.getElementById("workSName").value,
            orchard : orchID,
            type : workType,
            info : document.getElementById("workInfo").value,
            email : email
        });
        id = newId;
        popWork();
    }
    else if(type === 1){
        firebase.database().ref("/workers/" + id).update({
            name : document.getElementById("workName").value,
            surname : document.getElementById("workSName").value,
            orchard : orchID,
            type : workType,
            info : document.getElementById("workInfo").value,
            email : email
        });
    }
    dispWork(id);
}

function workMod(id) {
    firebase.database().ref('/workers/' + id).once('value').then(function (snapshot) {
        document.getElementById('modalDelBut').innerHTML="<button type='button' class='btn btn-danger' data-dismiss='modal' onclick='delWork("+id+")'>Delete</button>";
        document.getElementById('modalText').innerText="Please confirm deletion of " + snapshot.val().name + " " + snapshot.val().surname;
        document.getElementById('col3').innerHTML="" +
            "<form class='form-horizontal'>" +
            "" +
            "<div class='form-group'>" +
            "<div class='col-lg-3 col-lg-offset-2'><button onclick='workSave("+1+","+id+")' type='button' class='btn btn-warning'>Save</button></div>" +
            "<div class='col-lg-3'><button type='button' class='btn btn-danger' data-toggle='modal' data-target='#delModal'>Delete</button></div> " +
            "<div class='col-lg-3'><button onclick='dispWork("+id+")' type='button' class='btn btn-default'>Cancel</button></div>" +
            "</div> " +
            "" +
            "<div class='form-group'><label class='control-label col-lg-2' for='text'>Worker Name:</label>" +
            "<div class='col-lg-9'><input type='text' class='form-control' id='workName' value='"+snapshot.val().name+"'></div> </div> " +
            "" +
            "<div class='form-group'><label class='control-label col-lg-2' for='text'>Worker Surname:</label>" +
            "<div class='col-lg-9'><input type='text' class='form-control' id='workSName' value='"+snapshot.val().surname+"'></div> </div>" +
            "" +
            "<div class='form-group'><label class='control-label col-lg-2' for='text'>Worker Email:</label>" +
            "<div class='col-lg-9'><input type='text' class='form-control' id='workEmail' value='"+snapshot.val().email+"'></div> </div>" +
            "" +
            "</form>"
        ;
    });
}

function delWork(id) {
    firebase.database().ref('/workers/' + id).remove();
    popWork();
}

function clear(){
    document.getElementById('col3').innerHTML='';
}