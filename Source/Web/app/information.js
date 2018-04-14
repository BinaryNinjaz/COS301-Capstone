const database = firebase.database();
let newId = -1;
const orchardsRef = firebase.database().ref('/orchards');
const workersRef = firebase.database().ref('/workers');
const farmRef = firebase.database().ref('/farms');
popOrch();
popWork();
popFarm();
document.getElementById("col2").innerHTML = "";

var editingOrchard = false;
var orchardCoords = [];
var orchardPoly;
var loc;
var map;
function initEditOrchardMap(withCurrentLoc, editing) {
  editingOrchard = editing;
  if (withCurrentLoc) {
    updateLocationMap();
  }
  initMap();
  google.maps.event.clearListeners(map, "click");
  if (orchardPoly !== undefined) {
    google.maps.event.clearListeners(orchardPoly, "click");
  }
  
  if (editing) {
    map.addListener("click", function(e) {
      pushOrchardCoord(e);
      updatePolyListener();
    });
  }
}
function pushOrchardCoord(e) {
  if (orchardCoords === undefined) {
    orchardCoords = [];
  }
  const c = {
    lat: e.latLng.lat(),
    lng: e.latLng.lng()
  }
  orchardCoords.push(c);
  if (orchardPoly !== undefined && orchardPoly !== null) {
    orchardPoly.setMap(null);
  }
  orchardPoly = new google.maps.Polygon({
    paths: orchardCoords,
    strokeColor: '#FF0000',
    strokeOpacity: 0.8,
    strokeWeight: 2,
    fillColor: '#FF0000',
    fillOpacity: 0.35,
    map: map
  });
  updatePolyListener();
}
function updateLocationMap() {
  navigator.geolocation.getCurrentPosition(function(loca) {
    loc = {
      lat: loca.coords.latitude,
      lng: loca.coords.longitude
    }
    initMap(); 
  });
  initMap();
}
function initMap() {
  if (loc === undefined) {
    loc = {lat: -25, lng: 28};
  }
  map = new google.maps.Map(document.getElementById('map'), {
    center: loc,
    zoom: 14,
    disableDoubleClickZoom: true,
    mapTypeId: "satellite"
  });
}
function updatePolygon(snapshot) {
  if (orchardCoords === undefined) {
    orchardCoords = [];
  }
  while (orchardCoords.length > 0) {
    orchardCoords.pop();
  }
  snapshot.val().coords.forEach(function(coord) {
    orchardCoords.push(coord);
  });
  if (orchardPoly !== undefined && orchardPoly !== null) {
    orchardPoly.setMap(null);
  }
  orchardPoly = new google.maps.Polygon({
    paths: orchardCoords,
    strokeColor: '#FF0000',
    strokeOpacity: 0.8,
    strokeWeight: 2,
    fillColor: '#FF0000',
    fillOpacity: 0.35,
    map: map
  });
  updatePolyListener();
  map.setCenter({lat: cenLat(orchardCoords), lng: cenLng(orchardCoords)});
  map.fitBounds(bounds(orchardCoords));
}
function updatePolyListener() {
  if (orchardPoly !== undefined) {
    google.maps.event.clearListeners(orchardPoly, "click");
    orchardPoly.addListener("click", pushOrchardCoord);
  }
}
function popOrchardCoord() {
  if (orchardCoords === undefined) {
    orchardCoords = [];
  }
  orchardCoords.pop();
  orchardPoly.setPath(orchardCoords);
  updatePolyListener();
}
function clearOrchardCoord() {
  if (orchardCoords === undefined) {
    orchardCoords = [];
  }
  while(orchardCoords.length > 0) {
    orchardCoords.pop();
  }
  orchardPoly.setPath(orchardCoords);
  updatePolyListener();
}

function popFarm() {
  const col2 = document.getElementById("col2");
  col2.innerHTML = "<h2>Loading Farm List...</h2>";
  farmRef.off();

  farmRef.on('value', function (snapshot) {
    col2.innerHTML = "" +
      "<button type='button' class='btn btn-success' onclick='dispFarm(-1)'>Add Farm</button>"
    ;

    snapshot.forEach(function (child) {
      col2.innerHTML += "" +
        "<button type='button' class='btn btn-info' onclick='dispFarm(" + child.key + ")'>" + child.val().name + "</button>"
      ;
      newId = child.key;
    });

  });
}

function dispFarm(id) {
  const col3 = document.getElementById("col3");

  if (id === -1) {
    /*Create New Orchard*/
    col3.innerHTML = "" +
      "<form class='form-horizontal'>" +
      "" +
      "<div class='form-group'><div class='col-sm-9 col-sm-offset-2'><button onclick='farmSave(0," + id + ")' type='button' class='btn btn-warning'>Save</button></div></div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Farm Name:</label>" +
      "<div class='col-sm-9'><input type='text' class='form-control' id='farmName'></div> </div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
      // "<div class='col-sm-9'><input type='text' class='form-control' id='oi'></div> </div>" +
      "<div class='col-sm-9'><textarea class='form-control' rows='4' id='farmFurther'></textarea></div></div>" +
      "" +
      "</form>"
    ;
  }
  else {

    firebase.database().ref('/farms/' + id).once('value').then(function (snapshot) {

      col3.innerHTML = "" +
        "<form class='form-horizontal'>" +
        "" +
        "<div class='form-group'><div class='col-sm-9 col-sm-offset-2'><button onclick='farmMod(" + id + ")' type='button' class='btn btn-default'>Modify</button></div></div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Farm Name:</label>" +
        "<div class='col-sm-9'><p class='form-control-static'>" + snapshot.val().name + "</p> </div> </div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
        "<div class='col-sm-9'><p class='form-control-static'>" + snapshot.val().further + "</p></div> </div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Assigned Orchards:</label>" +
        "<div class='col-sm-9' id='orchardButtons'></div></div>" +
        "" +
        "</form>"
      ;

      firebase.database().ref("/orchards").once('value').then(function (workers) {
        const buttons = document.getElementById("orchardButtons");
        workers.forEach(function (orchard) {
          if (orchard.val().farm == id) {
            buttons.innerHTML += "<div class='col-sm-4'><button type='button' class='btn btn-default' onclick='dispOrch(" + orchard.key + ")'>" + orchard.val().name + "</button></div>";
          }
        });
      });
    });


  }
}

function farmSave(type, id) {
  /*0 means create, 1 means modify*/

  if (type === 0) {
    newId++;
    firebase.database().ref("/farms/" + newId).set({
      name: document.getElementById("farmName").value,
      further: document.getElementById("farmFurther").value
    });
    id = newId;
    popFarm();
  }
  else if (type === 1) {
    firebase.database().ref("/farms/" + id).update({
      name: document.getElementById("farmName").value,
      further: document.getElementById("farmFurther").value
    });
  }
  dispFarm(id);
}

function farmMod(id) {
  firebase.database().ref('/farms/' + id).once('value').then(function (snapshot) {
    document.getElementById('modalDelBut').innerHTML = "<button type='button' class='btn btn-danger' data-dismiss='modal' onclick='delFarm(" + id + ")'>Delete</button>";
    document.getElementById('modalText').innerText = "Please confirm deletion of " + snapshot.val().name;
    document.getElementById('col3').innerHTML = "" +
      "<form class='form-horizontal'>" +
      "" +
      "<div class='form-group'>" +
      "<div class='col-sm-3 col-sm-offset-2'><button onclick='farmSave(" + 1 + "," + id + ")' type='button' class='btn btn-warning'>Save</button></div>" +
      "<div class='col-sm-3'><button type='button' class='btn btn-danger' data-toggle='modal' data-target='#delModal'>Delete</button></div> " +
      "<div class='col-sm-3'><button onclick='dispFarm(" + id + ")' type='button' class='btn btn-default'>Cancel</button></div>" +
      "</div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Farm Name:</label>" +
      "<div class='col-sm-9'><input type='text' class='form-control' id='farmName' value='" + snapshot.val().name + "'></div> </div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
      // "<div class='col-sm-9'><input type='text' class='form-control' id='oi' value='"+snapshot.val().further+"'></div> </div>" +
      "<div class='col-sm-9'><textarea class='form-control' rows='4' id='farmFurther'>" + snapshot.val().further + "</textarea></div> </div>" +
      "" +
      "</form>"
    ;
  });
}

function delFarm(id) {
  firebase.database().ref('/farms/' + id).remove();
  popFarm();
  clear3();
}


function popOrch() {
  const col2 = document.getElementById("col2");
  col2.innerHTML = "<h2>Loading Orchard List...</h2>";
  orchardsRef.off();

  orchardsRef.on('value', function (snapshot) {
    col2.innerHTML = "" +
      "<button type='button' class='btn btn-success' onclick='dispOrch(-1)'>Add Orchard</button>"
    ;

    snapshot.forEach(function (child) {
      col2.innerHTML += "" +
        "<button type='button' class='btn btn-info' onclick='dispOrch(" + child.key + ")'>" + child.val().name + "</button>"
      ;
      newId = child.key;
    });

  });
}

function dispOrch(id) {
  const col3 = document.getElementById("col3");

  if (id === -1) {
    /*Create New Orchard*/

    firebase.database().ref('/farms').once('value').then(function (snapshot) {
      col3.innerHTML = "" +
        "<form class='form-horizontal'>" +
        "" +
        "<div class='form-group'><div class='col-sm-9 col-sm-offset-2'><button onclick='orchSave(0," + id + ")' type='button' class='btn btn-warning'>Save</button></div></div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Orchard Name:</label>" +
        "<div class='col-sm-9'><input type='text' class='form-control' id='orchName'></div> </div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Orchard Crop:</label>" +
        "<div class='col-sm-9'><input type='text' class='form-control' id='orchCrop'></div> </div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Orchard Location:</label>" +
        "<div class='col-sm-9'>" +
        "<div class='col-sm-12'><h4>Click the corners of a field to demarcate area</h4></div>" +
        "<div class='col-sm-12'><div id='map'></div></div>" +
        "<div class='col-sm-4'><button onclick='popOrchardCoord()' type='button' class='btn btn-default'>Remove Last Point</button></div><div class='col-sm-4'><button onclick='clearOrchardCoord()' type='button' class='btn btn-default'>Clear Area</button></div></div></div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Mean Bag Mass:</label>" +
        "<div class='col-sm-8'><input type='number' class='form-control' id='orchBagMass'></div>" +
        "<div class='col-sm-1'><p class='form-control-static'>Kg</p></div>" +
        "</div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='date'>Date Planted:</label>" +
        "<div class='col-sm-9'><input type='date' class='form-control' id='orchDate'></div></div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='date'>Dimensions:</label>" +
        "<div class='col-sm-2'><input type='number' class='form-control' id='orchDimX'></div>" +
        "<div class='col-sm-1'><p class='form-control-static' style='text-align: center'>x</p></div>" +
        "<div class='col-sm-2'><input type='number' class='form-control' id='orchDimY'> </div>" +
        "<div class='col-sm-1 col-sm-offset-1'><p class='form-control-static' style='text-align: right'>Unit:</p></div>" +
        "<div class='col-sm-2'><input type='text' class='form-control' id='orchDimUnit'></div> " +
        "</div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
        // "<div class='col-sm-9'><input type='text' class='form-control' id='oi'></div> </div>" +
        "<div class='col-sm-9'><textarea class='form-control' rows='4' id='oi'></textarea></div></div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='sel1'>Assigned Farm:</label>" +
        "<div class='col-sm-9'><select class='form-control' id='orchFarm'></select></div></div>" +
        "" +
        "</form>"
      ;
      initEditOrchardMap(true, true);
      
      snapshot.forEach(function (child) {
        document.getElementById("orchFarm").innerHTML += "<option><" + child.key + "> " + child.val().name + "</option>";
      });
    });
  }
  else {

    firebase.database().ref('/orchards/' + id).once('value').then(function (snapshot) {
      farmRef.once('value').then(function (farmSnapshot) {
        col3.innerHTML = "" +
          "<form class='form-horizontal'>" +
          "" +
          "<div class='form-group'><div class='col-sm-9 col-sm-offset-2'><button onclick='orchMod(" + id + ")' type='button' class='btn btn-default'>Modify</button></div></div> " +
          "" +
          "<div class='form-group'><label class='control-label col-sm-2' for='text'>Orchard Name:</label>" +
          "<div class='col-sm-9'><p class='form-control-static'>" + snapshot.val().name + "</p> </div> </div> " +
          "" +
          "<div class='form-group'><label class='control-label col-sm-2' for='text'>Orchard Crop:</label>" +
          "<div class='col-sm-9'><p class='form-control-static'>" + snapshot.val().crop + "</p></div> </div>" +
          "" +
          "<div class='form-group'><label class='control-label col-sm-2' for='text'>Orchard Location:</label>" +
          "<div class='col-sm-9'><div id='map'></div></div></div> " +
          "" +
          "<div class='form-group'><label class='control-label col-sm-2' for='text'>Mean Bag Mass:</label>" +
          "<div class='col-sm-9'><p class='form-control-static'>" + snapshot.val().bagMass + " Kg</p></div> </div>" +
          "" +
          "<div class='form-group'><label class='control-label col-sm-2' for='date'>Date Planted:</label>" +
          "<div class='col-sm-9'><p class='form-control-static'>" + snapshot.val().date + "</p></div></div> " +
          "" +
          "<div class='form-group'><label class='control-label col-sm-2' for='date'>Dimensions:</label>" +
          "<div class='col-sm-9'><p class ='form-control-static'>" + snapshot.val().xDim + " x " + snapshot.val().yDim + " " + snapshot.val().unit + "</p></div>" +
          "</div> " +
          "" +
          "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
          "<div class='col-sm-9'><p class='form-control-static'>" + snapshot.val().further + "</p></div> </div>" +
          "" +
          "<div class='form-group'><label class='control-label col-sm-2' for='text'>Assigned Farm:</label>" +
          "<div class='col-sm-9'><span id='orchFarmDisp'></span></div> </div>" +
          "" +
          "<div class='form-group'><label class='control-label col-sm-2' for='text'>Assigned Workers:</label>" +
          "<div class='col-sm-9' id='workerButtons'></div></div>" +
          "" +
          "</form>"
        ;
        
        initEditOrchardMap(false, false);
        updatePolygon(snapshot);
        
        farmSnapshot.forEach(function (farm) {
          if (farm.key === snapshot.val().farm) {
            // document.getElementById("workOrchDisp").innerHTML="<p class='form-control-static' onclick='dispOrch("+id+")'>"+orchard.val().name+"</p>"
            document.getElementById("orchFarmDisp").innerHTML = "<div class='col-sm-4'><button type='button' class='btn btn-default' onclick='dispFarm(" + farm.key + ")'>" + farm.val().name + "</button></div>";
          }
        });

        firebase.database().ref("/workers").once('value').then(function (workers) {
          const buttons = document.getElementById("workerButtons");
          workers.forEach(function (worker) {
            if (worker.val().orchard == id) {
              buttons.innerHTML += "<div class='col-sm-4'><button type='button' class='btn btn-default' onclick='dispWork(" + worker.key + ")'>" + worker.val().name + " " + worker.val().surname + "</button></div>";
            }
          });
        });
      });
    });


  }
}

function orchSave(type, id) {
  /*0 means create, 1 means modify*/
  const farm = document.getElementById("orchFarm").value;
  const farmID = farm.substring(farm.indexOf("<") + 1, farm.indexOf(">"));
  if (type === 0) {
    newId++;
    firebase.database().ref("/orchards/" + newId).set({
      name: document.getElementById("orchName").value,
      crop: document.getElementById("orchCrop").value,
      further: document.getElementById("oi").value,
      date: document.getElementById("orchDate").value,
      xDim: document.getElementById("orchDimX").value,
      yDim: document.getElementById("orchDimY").value,
      unit: document.getElementById("orchDimUnit").value,
      bagMass: document.getElementById("orchBagMass").value,
      coords: orchardCoords,
      farm: farmID
    });
    id = newId;
    popOrch();
  }
  else if (type === 1) {
    firebase.database().ref("/orchards/" + id).update({
      name: document.getElementById("orchName").value,
      crop: document.getElementById("orchCrop").value,
      further: document.getElementById("oi").value,
      date: document.getElementById("orchDate").value,
      xDim: document.getElementById("orchDimX").value,
      yDim: document.getElementById("orchDimY").value,
      unit: document.getElementById("orchDimUnit").value,
      bagMass: document.getElementById("orchBagMass").value,
      coords: orchardCoords,
      farm: farmID
    });
  }
  dispOrch(id);
}

function orchMod(id) {
  firebase.database().ref('/orchards/' + id).once('value').then(function (snapshot) {
    document.getElementById('modalDelBut').innerHTML = "<button type='button' class='btn btn-danger' data-dismiss='modal' onclick='delOrch(" + id + ")'>Delete</button>";
    document.getElementById('modalText').innerText = "Please confirm deletion of " + snapshot.val().name;
    firebase.database().ref('/farms').once('value').then(function (farm) {
      document.getElementById('col3').innerHTML = "" +
        "<form class='form-horizontal'>" +
        "" +
        "<div class='form-group'>" +
        "<div class='col-sm-3 col-sm-offset-2'><button onclick='orchSave(" + 1 + "," + id + ")' type='button' class='btn btn-warning'>Save</button></div>" +
        "<div class='col-sm-3'><button type='button' class='btn btn-danger' data-toggle='modal' data-target='#delModal'>Delete</button></div> " +
        "<div class='col-sm-3'><button onclick='dispOrch(" + id + ")' type='button' class='btn btn-default'>Cancel</button></div>" +
        "</div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Orchard Name:</label>" +
        "<div class='col-sm-9'><input type='text' class='form-control' id='orchName' value='" + snapshot.val().name + "'></div> </div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Orchard Crop:</label>" +
        "<div class='col-sm-9'><input type='text' class='form-control' id='orchCrop' value='" + snapshot.val().crop + "'></div> </div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Orchard Location:</label>" +
        "<div class='col-sm-9'>" +
        "<div class='col-sm-12'><h4>Click the corners of a field to demarcate area</h4></div>" +
        "<div class='col-sm-12'><div id='map'></div></div>" +
        "<div class='col-sm-4'><button onclick='popOrchardCoord()' type='button' class='btn btn-default'>Remove Last Point</button></div><div class='col-sm-4'><button onclick='clearOrchardCoord()' type='button' class='btn btn-default'>Clear Area</button></div></div></div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Mean Bag Mass:</label>" +
        "<div class='col-sm-8'><input type='number' class='form-control' id='orchBagMass' value='" + snapshot.val().bagMass + "'></div>" +
        "<div class='col-sm-1'><p class='form-control-static'>Kg</p></div>" +
        "</div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='date'>Date Planted:</label>" +
        "<div class='col-sm-9'><input type='date' class='form-control' id='orchDate' value='" + snapshot.val().date + "'></div></div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='date'>Dimensions:</label>" +
        "<div class='col-sm-2'><input type='number' class='form-control' id='orchDimX' value ='" + snapshot.val().xDim + "'></div>" +
        "<div class='col-sm-1'><p class='form-control-static' style='text-align: center'>x</p></div>" +
        "<div class='col-sm-2'><input type='number' class='form-control' id='orchDimY' value ='" + snapshot.val().yDim + "'> </div>" +
        "<div class='col-sm-1 col-sm-offset-1'><p class='form-control-static' style='text-align: right'>Unit:</p></div>" +
        "<div class='col-sm-2'><input type='text' class='form-control' id='orchDimUnit' value ='" + snapshot.val().unit + "'></div> " +
        "</div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
        // "<div class='col-sm-9'><input type='text' class='form-control' id='oi' value='"+snapshot.val().further+"'></div> </div>" +
        "<div class='col-sm-9'><textarea class='form-control' rows='4' id='oi'>" + snapshot.val().further + "</textarea></div> </div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='sel1'>Assigned Farm:</label>" +
        "<div class='col-sm-9'><select class='form-control' id='orchFarm'></select></div></div>" +
        "" +
        "</form>"
      ;
      initEditOrchardMap(false, true);
      updatePolygon(snapshot);
      
      farm.forEach(function (child) {
        const orchFarm = document.getElementById("orchFarm");
        let selec = "";
        // workOrch.innerHTML = workOrch.innerHTML + "<option";
        if (child.key === snapshot.val().farm) {
          selec = ' selected';
        }
        // workOrch.innerHTML = workOrch.innerHTML + "><" +child.key+"> " + child.val().name+"  :  "+child.val().crop + "</option>";
        orchFarm.innerHTML += "<option" + selec + "><" + child.key + "> " + child.val().name + "</option>";
      });
    });
  });
}

function delOrch(id) {
  firebase.database().ref('/orchards/' + id).remove();
  popOrch();
  clear3();
}


function popWork() {
  const col2 = document.getElementById("col2");
  col2.innerHTML = "<h2>Loading Worker List...</h2>";

  workersRef.off();

  workersRef.on('value', function (snapshot) {
    col2.innerHTML = "" +
      "<button type='button' class='btn btn-success' onclick='dispWork(-1)'>Add Worker</button>"
    ;

    snapshot.forEach(function (child) {
      col2.innerHTML += "" +
        "<button type='button' class='btn btn-info' onclick='dispWork(" + child.key + ")'>" + child.val().name + " " + child.val().surname + "</button>"
      ;
      newId = child.key;
    });

  });
}

function dispWork(id) {
  const col3 = document.getElementById("col3");

  if (id === -1) {
    /*Create New Worker*/


    firebase.database().ref('/orchards').once('value').then(function (snapshot) {
      col3.innerHTML = "" +
        "<form class='form-horizontal'>" +
        "" +
        "<div class='form-group'><div class='col-sm-9 col-sm-offset-2'><button onclick='workSave(0," + id + ")' type='button' class='btn btn-warning'>Save</button></div></div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Worker Name:</label>" +
        "<div class='col-sm-9'><input type='text' class='form-control' id='workName'></div> </div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Worker Surname:</label>" +
        "<div class='col-sm-9'><input type='text' class='form-control' id='workSName'></div> </div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='sel1'>Assigned Orchard:</label>" +
        "<div class='col-sm-9'><select class='form-control' id='workOrch'></select></div></div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' id='workType'>Worker Type:</label>" +
        "<label class='radio-inline'><input type='radio' name='optradio' onclick='dispWorkEmail(false)' id='rWorker' checked>Worker</label>" +
        "<label class='radio-inline'><input type='radio' name='optradio' onclick='dispWorkEmail(true)' id='rForeman'>Foreman</label>" +
        "</div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
        "<div class='col-sm-9'><textarea class='form-control' rows='4' id='workInfo'></textarea></div></div>" +
        "" +
        "<div id='emailSpace'></div>" +
        "" +

        "</form>"
      ;

      snapshot.forEach(function (child) {
        document.getElementById("workOrch").innerHTML += "<option><" + child.key + "> " + child.val().name + "  :  " + child.val().crop + "</option>";
      });
    });
  }
  else {
    firebase.database().ref('/workers/' + id).once('value').then(function (snapshot) {
      // firebase.database().ref('/orchards').once('value').then(function (orchardSnapshot) {
      orchardsRef.once('value').then(function (orchardSnapshot) {
        col3.innerHTML = "" +
          "<form class='form-horizontal'>" +
          "" +
          "<div class='form-group'><div class='col-sm-9 col-sm-offset-2'><button onclick='workMod(" + id + ")' type='button' class='btn btn-default'>Modify</button></div></div> " +
          "" +
          "<div class='form-group'><label class='control-label col-sm-2' for='text'>Worker Name:</label>" +
          "<div class='col-sm-9'><p class='form-control-static'>" + snapshot.val().name + "</p> </div> </div> " +
          "" +
          "<div class='form-group'><label class='control-label col-sm-2' for='text'>Worker Surname:</label>" +
          "<div class='col-sm-9'><p class='form-control-static'>" + snapshot.val().surname + "</p> </div> </div>" +
          "" +
          "<div class='form-group'><label class='control-label col-sm-2' for='sel1'>Assigned Orchard:</label>" +
          "<div class='col-sm-9'><span id='workOrchDisp'></span></div></div>" +
          "" +
          "<div class='form-group'><label class='control-label col-sm-2' id='workType'>Worker Type:</label>" +
          "<div class='col-sm-9'><p class='form-control-static'>" + snapshot.val().type + "</p> </div>" +
          "</div>" +
          "" +
          "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
          "<div class='col-sm-9'><p class='form-control-static'>" + snapshot.val().info + "</p> </div></div>" +
          "" +
          "<div id='emailDispSpace'></div>" +
          "" +

          "</form>"
        ;

        orchardSnapshot.forEach(function (orchard) {
          if (orchard.key === snapshot.val().orchard) {
            // document.getElementById("workOrchDisp").innerHTML="<p class='form-control-static' onclick='dispOrch("+id+")'>"+orchard.val().name+"</p>"
            document.getElementById("workOrchDisp").innerHTML = "<div class='col-sm-4'><button type='button' class='btn btn-default' onclick='dispOrch(" + orchard.key + ")'>" + orchard.val().name + "</button></div>";
          }
        });

        if (snapshot.val().type === "Foreman") {
          document.getElementById("emailDispSpace").innerHTML = "" +
            "<div class='form-group'><label class='control-label col-sm-2' for='text'>Foreman Email:</label>" +
            "<div class='col-sm-9'><p class='form-control-static'>" + snapshot.val().email + "</p> </div></div>"
          ;
        }
      });
    });


  }
}

function dispWorkEmail(disp) {
  if (disp) {
    document.getElementById("emailSpace").innerHTML = "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='email'>Foreman Email:</label>" +
      "<div class='col-sm-9'><input type='email' class='form-control' id='workEmail' data-toggle='tooltip' title='We will send the foreman an email so they can create, or link their account on the app to yours.'>" +
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
  let workType = "Foreman";
  if (document.getElementById("rWorker").checked) {
    workType = "Worker";
  }
  let email = "";
  if (workType === "Foreman") {
    email = document.getElementById("workEmail").value;
  }
  if (type === 0) {
    newId++;
    firebase.database().ref("/workers/" + newId).set({
      name: document.getElementById("workName").value,
      surname: document.getElementById("workSName").value,
      orchard: orchID,
      type: workType,
      info: document.getElementById("workInfo").value,
      email: email
    });
    id = newId;
    popWork();
  }
  else if (type === 1) {
    firebase.database().ref("/workers/" + id).update({
      name: document.getElementById("workName").value,
      surname: document.getElementById("workSName").value,
      orchard: orchID,
      type: workType,
      info: document.getElementById("workInfo").value,
      email: email
    });
  }
  dispWork(id);
}

function workMod(id) {
  firebase.database().ref('/workers/' + id).once('value').then(function (snapshot) {
    document.getElementById('modalDelBut').innerHTML = "<button type='button' class='btn btn-danger' data-dismiss='modal' onclick='delWork(" + id + ")'>Delete</button>";
    document.getElementById('modalText').innerText = "Please confirm deletion of " + snapshot.val().name + " " + snapshot.val().surname;
    firebase.database().ref('/orchards').once('value').then(function (orchard) {
      document.getElementById('col3').innerHTML = "" +
        "<form class='form-horizontal'>" +
        "" +
        "<div class='form-group'>" +
        "<div class='col-sm-3 col-sm-offset-2'><button onclick='workSave(" + 1 + "," + id + ")' type='button' class='btn btn-warning'>Save</button></div>" +
        "<div class='col-sm-3'><button type='button' class='btn btn-danger' data-toggle='modal' data-target='#delModal'>Delete</button></div> " +
        "<div class='col-sm-3'><button onclick='dispWork(" + id + ")' type='button' class='btn btn-default'>Cancel</button></div>" +
        "</div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Worker Name:</label>" +
        "<div class='col-sm-9'><input type='text' class='form-control' id='workName' value='" + snapshot.val().name + "'></div> </div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Worker Surname:</label>" +
        "<div class='col-sm-9'><input type='text' class='form-control' id='workSName' value='" + snapshot.val().surname + "'></div> </div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='sel1'>Assigned Orchard:</label>" +
        "<div class='col-sm-9'><select class='form-control' id='workOrch'></select></div></div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' id='workType'>Worker Type:</label>" +
        "<label class='radio-inline'><input type='radio' name='optradio' onclick='dispWorkEmail(false)' id='rWorker'>Worker</label>" +
        "<label class='radio-inline'><input type='radio' name='optradio' onclick='dispWorkEmail(true)' id='rForeman'>Foreman</label>" +
        "</div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
        "<div class='col-sm-9'><textarea class='form-control' rows='4' id='workInfo'>" + snapshot.val().info + "</textarea></div></div>" +
        "" +
        "<div id='emailSpace'></div>" +
        "" +

        "</form>"
      ;

      if (snapshot.val().type === "Foreman") {
        document.getElementById("rForeman").setAttribute("checked", "");
        document.getElementById("emailSpace").innerHTML = "" +
          "<div class='form-group'><label class='control-label col-sm-2' for='text'>Foreman Email:</label>" +
          "<div class='col-sm-9'><input type='text' class='form-control' id='workEmail' value='" + snapshot.val().email + "'></div> </div> "
        ;
      }
      else {
        document.getElementById("rWorker").setAttribute("checked", "");
      }

      orchard.forEach(function (child) {
        const workOrch = document.getElementById("workOrch");
        let selec = "";
        // workOrch.innerHTML = workOrch.innerHTML + "<option";
        if (child.key === snapshot.val().orchard) {
          selec = ' selected';
        }
        // workOrch.innerHTML = workOrch.innerHTML + "><" +child.key+"> " + child.val().name+"  :  "+child.val().crop + "</option>";
        workOrch.innerHTML += "<option" + selec + "><" + child.key + "> " + child.val().name + "  :  " + child.val().crop + "</option>";
      });
    });
  });
}

function delWork(id) {
  firebase.database().ref('/workers/' + id).remove();
  popWork();
  clear3();
}


function clear3() {
  document.getElementById("col3").innerHTML = "";
}