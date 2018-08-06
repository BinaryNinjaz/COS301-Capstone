const database = firebase.database();
let findables = [1];
const user = function() { return firebase.auth().currentUser };
const userID = function() { return user().uid }

function orchardsRef() {
  return firebase.database().ref('/' + userID()  + '/orchards/');
}

function workersRef() {
  return firebase.database().ref('/' + userID()  + '/workers');
}

function farmsRef() {
  return firebase.database().ref('/' + userID()  + '/farms');
}

$(window).bind("load", () => {
  let succ = () => {
    popFarm();
    initMap();
  };
  let fail = () => {
    orchardPolys = [];
    orchards = [];
    farms = [];
    workers = [];
  };
  retryUntilTimeout(succ, fail, 1000);
});


var editingOrchard = false;
var orchardCoords = [];
var orchardPoly;
var loc;
var map;
function initEditOrchardMap(withCurrentLoc, editing) {
  editingOrchard = editing;
  if (withCurrentLoc) {
    updateLocationMap(editing);
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
function updateLocationMap(editing) {
  locationLookup((data) => {
    loc = {
      lat: data.lat,
      lng: data.lon
    }
    initMap();
    if (editing) {
      google.maps.event.clearListeners(map, "click");
      if (orchardPoly !== undefined) {
        google.maps.event.clearListeners(orchardPoly, "click");
      }
      map.addListener("click", function(e) {
        pushOrchardCoord(e);
        updatePolyListener();
      });
    }
  });
  initMap();
  if (editing) {
    google.maps.event.clearListeners(map, "click");
    if (orchardPoly !== undefined) {
      google.maps.event.clearListeners(orchardPoly, "click");
    }
    map.addListener("click", function(e) {
      pushOrchardCoord(e);
      updatePolyListener();
    });
  }
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
  if (snapshot !== null && snapshot !== undefined && snapshot.val() !== undefined && snapshot.val().coords !== undefined) {
    snapshot.val().coords.forEach(function(coord) {
      orchardCoords.push(coord);
    });
  } else {
    orchardCoords.push(loc);
  }
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
  if (orchardCoords.length > 1) {
    map.fitBounds(bounds(orchardCoords));
  }
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

/*Populates the list of farms in col2*/
function popFarm() {
    document.getElementById("col3").innerHTML = "";
  findables = [];
  const add = document.getElementById("AddButt");
  add.innerHTML = "<h2>Loading Farm List...</h2>";
  document.getElementById("SearchSpace").innerHTML = "";
  farmsRef().off();

  farmsRef().once('value').then(function (snapshot) {
    add.innerHTML = "" +
      "<button type='button' class='btn btn-success' onclick='dispFarm(\"-1\")'>Add Farm</button>"
    ;

    snapshot.forEach(function (child) {
      let temp = {
        Name : child.val().name,
        Button : "<button type='button' class='btn btn-info' onclick='dispFarm(\"" + child.key + "\")'>" + child.val().name + "</button>"
      };
      findables.push(temp);
    });
    searchDisp();
  });
}

/*Displays a farm in col 3, the farm displayed is set by the id. If -1 is received then display to create a new farm.*/
function dispFarm(id) {
  const col3 = document.getElementById("col3");

  if (id === "-1") {
    /*Create New Orchard*/
    col3.innerHTML = "" +
      "<form class='form-horizontal'>" +
      "" +
      "<div class='form-group'><div class='col-sm-9 col-sm-offset-1'><button onclick='farmSave(0,\"" + id + "\")' type='button' class='btn btn-warning'>Save</button></div></div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Farm Name:</label>" +
      "<div class='col-sm-9'><input type='text' class='form-control' id='farmName'></div> </div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Company Name:</label>" +
      "<div class='col-sm-9'><input type='text' class='form-control' id='companyName'></div> </div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Contact Number:</label>" +
      "<div class='col-sm-9'><input type='text' class='form-control' id='farmContact'></div> </div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Email Address:</label>" +
      "<div class='col-sm-9'><input type='text' class='form-control' id='farmEmail'></div> </div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
      "<div class='col-sm-9'><textarea class='form-control' rows='4' id='farmFurther'></textarea></div></div>" +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Province:</label>" +
      "<div class='col-sm-9'><input type='text' class='form-control' id='farmProvince'></div> </div> " +     
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Nearest Town:</label>" +
      "<div class='col-sm-9'><input type='text' class='form-control' id='farmTown'> </div> </div> " +     
      "" +
      "</form>"
    ;
  }
  else {

    firebase.database().ref('/' + userID() + '/farms/' + id).once('value').then(function (snapshot) {

      col3.innerHTML = "" +
        "<form class='form-horizontal'>" +
        "" +
        "<div class='form-group'><div class='col-sm-9 col-sm-offset-1'><button onclick='farmMod(\"" + id + "\")' type='button' class='btn btn-default'>Modify</button></div></div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Farm Name:</label>" +
        "<div class='col-sm-9'><p class='form-control-static' id='farmFurther'>" + snapshot.val().name + "</p> </div> </div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='companyName2'>Company Name:</label>" +
        "<div class='col-sm-9'><p class='form-control-static' id='companyName2'>" + snapshot.val().companyName + "</p> </div> </div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Contact Number:</label>" +
        "<div class='col-sm-9'><p class='form-control-static' id='farmFurther'>" + snapshot.val().contactNumber + "</p> </div> </div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Email:</label>" +
        "<div class='col-sm-9'><p class='form-control-static' id='farmContact'>" + snapshot.val().email + "</p> </div> </div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
        "<div class='col-sm-9'><p class='form-control-static' id='farmEmail'>" + snapshot.val().further + "</p></div> </div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='AssignedOrchards'>Assigned Orchards:</label>" +
        "<div class='col-sm-9' id='orchardButtons' id='AssignedOrchards'></div></div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='farmProvince'>Province:</label>" +
        "<div class='col-sm-9'><p class='form-control-static' id='farmProvince'>" + snapshot.val().province + "</p> </div> </div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='farmTown'>Nearest Town:</label>" +
        "<div class='col-sm-9'><p class='form-control-static' id='farmTown'>" + snapshot.val().town + " </p> </div> </div> " +
        "" +
        "</form>"
      ;

      firebase.database().ref('/' + userID() + "/orchards").once('value').then(function (workers) {
        const buttons = document.getElementById("orchardButtons");
        workers.forEach(function (orchard) {
          if (orchard.val().farm == id) {
            buttons.innerHTML += "<div class='col-sm-4'><button type='button' class='btn btn-default' onclick='dispOrch(\"" + orchard.key + "\")'>" + orchard.val().name + "</button></div>";
          }
        });
      });
    });


  }
}

/*Handles the saving of a farm with the set id, if it receives 0, the farm is created, else it is updated.*/
function farmSave(type, id) {
  /*0 means create, 1 means modify*/
  if (type === 0) {
    let newRef = firebase.database().ref('/' + userID() + "/farms/").push({
      name: document.getElementById("farmName").value,
      companyName: document.getElementById("companyName").value,
      further: document.getElementById("farmFurther").value,
      contactNumber: document.getElementById("farmContact").value,
      email: document.getElementById("farmEmail").value,
      province: document.getElementById("farmProvince").value,
      town: (document.getElementById("farmTown").value+"")
    });
    id = newRef.getKey();
    popFarm();
  }
  else if (type === 1) {
      //alert("we are modifying");
    firebase.database().ref('/' + userID() + "/farms/" + id).update({
       name: document.getElementById("farmName").value,
       companyName: document.getElementById("companyName3").value,
       further: document.getElementById("farmFurther").value,
       contactNumber: document.getElementById("farmContact").value,
       email: document.getElementById("farmEmail").value,
       province: document.getElementById("farmProvince").value,
      //companyName: (document.getElementById("companyName2").value+""),
       town: (document.getElementById("farmTown").value+"")
    });
     //alert("modified");
  }
  popFarm();
  dispFarm(id);
}

/*Displays in col 3, the interface to modify a farm*/
function farmMod(id) {
  firebase.database().ref('/' + userID() + '/farms/' + id).once('value').then(function (snapshot) {
    document.getElementById('modalDelBut').innerHTML = "<button type='button' class='btn btn-danger' data-dismiss='modal' onclick='delFarm(\"" + id + "\")'>Delete</button>";
    document.getElementById('modalText').innerText = "Please confirm deletion of " + snapshot.val().name;
    document.getElementById('col3').innerHTML = "" +
      "<form class='form-horizontal'>" +
      "" +
      "<div class='form-group'>" +
      "<div class='col-sm-3 col-sm-offset-1'><button onclick='farmSave(" + 1 + ",\"" + id + "\")' type='button' class='btn btn-warning'>Save</button></div>" +
      "<div class='col-sm-3'><button type='button' class='btn btn-danger' data-toggle='modal' data-target='#delModal'>Delete</button></div> " +
      "<div class='col-sm-3'><button onclick='dispFarm(\"" + id + "\")' type='button' class='btn btn-default'>Cancel</button></div>" +
      "</div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Farm Name:</label>" +
      "<div class='col-sm-9'><input type='text' class='form-control' id='farmName' value='" + snapshot.val().name + "'></div> </div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Company Name:</label>" +
      "<div class='col-sm-9'><input type='text' class='form-control' id='companyName3' value='" + snapshot.val().companyName + "'></div> </div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
      "<div class='col-sm-9'><textarea class='form-control' rows='4' id='farmFurther'>" + snapshot.val().further + "</textarea></div> </div>" +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Contact Number:</label>" +
      "<div class='col-sm-9'><input type='text' class='form-control' id='farmContact' value='" + snapshot.val().contactNumber + "'></div> </div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Email:</label>" +
      "<div class='col-sm-9'><input type='text' class='form-control' id='farmEmail' value='" + snapshot.val().email + "'></div> </div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Province:</label>" +
      "<div class='col-sm-9'><input type='text' class='form-control' id='farmProvince' value='" + snapshot.val().province + "'></div> </div> " +
       "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Nearest Town:</label>" +
      "<div class='col-sm-9'><input type='text' class='form-control' id='farmTown' value='" + snapshot.val().town + " '></div> </div> " +
      "" +
      "</form>";
  });
}

/*Delets a given farm*/
function delFarm(id) {
  firebase.database().ref('/' + userID() + '/farms/' + id).remove();
  popFarm();
  //clear3();
  document.getElementById("col3").innerHTML = "";
}

/*From here, all the functions are similar to that of above.*/


function popOrch() {
  document.getElementById("col3").innerHTML = "";
  findables = [];
  const add = document.getElementById("AddButt");
  add.innerHTML = "<h2>Loading Orchard List...</h2>";
  document.getElementById("SearchSpace").innerHTML = "";

  orchardsRef().once('value').then(function (snapshot) {
    add.innerHTML = "" +
      "<button type='button' class='btn btn-success' onclick='dispOrch(\"-1\")'>Add Orchard</button>"
    ;

    snapshot.forEach(function (child) {
      let temp = {
        Name : child.val().name,
        Button : "<button type='button' class='btn btn-info' onclick='dispOrch(\"" + child.key + "\")'>" + child.val().name + "</button>"
      };
      findables.push(temp);
    });
    searchDisp();
  });
}

function dispOrch(id) {
  const col3 = document.getElementById("col3");

  if (id === "-1") {
    /*Create New Orchard*/
    cCount = 0;
    firebase.database().ref('/' + userID() + '/farms').once('value').then(function (snapshot) {
      col3.innerHTML = "" +
        "<form class='form-horizontal'>" +
        "" +
        "<div class='form-group'><div class='col-sm-9 col-sm-offset-1'><button onclick='orchSave(0,\"" + id + "\")' type='button' class='btn btn-warning'>Save</button></div></div> " +
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
        "<div class='form-group'><label class='control-label col-sm-2' for='irrigationType'>Irrigation:</label>" +
        "<div class='col-sm-9'>"+
            "<select class='form-control' id='irrigationType'>"+
                "<option value='Micro'>Micro</option>"+
                "<option value='Drip'>Drip</option>"+
                "<option value='Floppy'>Floppy</option>"+
                "<option value='Drag Lines'>Drag Lines</option>"+
                "<option value='Other'>Other</option>"+
                "<option value='None (dry land)'>None (dry land)</option>"+
            "</select>"+
        "</div></div>"+
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='date'>Date Planted:</label>" +
        "<div class='col-sm-9'><input type='date' class='form-control' id='orchDate'></div></div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Cultivars: </label>" +
            "<div class='col-sm-9'>"+
                "<div id='cultivarBoxes'>"+
                    "<input type='text' class='form-control' id='cultivars0' />"+
                "</div>"+
                "<button type='button' class='btn btn-primary' onclick='moreCult()'>Add More Cultivar</button>"+
            "</div>"+            
        "</div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Row Spacing:</label>" +
        "<div class='col-sm-9'><input type='text' class='form-control' id='rowSpacing'></div></div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Tree Spacing:</label>" +
        "<div class='col-sm-9'><input type='text' class='form-control' id='treeSpacing'></div></div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
        "<div class='col-sm-9'><textarea class='form-control' rows='4' id='oi'></textarea></div></div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='sel1'>Assigned Farm:</label>" +
        "<div class='col-sm-9'><select class='form-control' id='orchFarm'></select></div></div>" +
         "" +        
        "</form>"
      ;
      initEditOrchardMap(true, true);
      updatePolygon(null);

      snapshot.forEach(function (child) {
        document.getElementById("orchFarm").innerHTML += "<option><" + child.key + "> " + child.val().name + "</option>";
      });
    });
  }
  else {

    firebase.database().ref('/' + userID() + '/orchards/' + id).once('value').then(function (snapshot) {
      farmsRef().once('value').then(function (farmSnapshot) {
          const date = new Date(snapshot.val().date * 1000);
        col3.innerHTML = "" +
          "<form class='form-horizontal'>" +
          "" +
          "<div class='form-group'><div class='col-sm-9 col-sm-offset-1'><button onclick='orchMod(\"" + id + "\")' type='button' class='btn btn-default'>Modify</button></div></div> " +
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
          "<div class='form-group'><label class='control-label col-sm-2' for='text'>Irrigation:</label>" +
          "<div class='col-sm-9'><p class='form-control-static'>" + snapshot.val().irrigation + "</p></div> </div>" +
          "" +
          "<div class='form-group'><label class='control-label col-sm-2' for='date'>Date Planted:</label>" +
          "<div class='col-sm-9'><p class='form-control-static'>" + date.toLocaleDateString() + "</p></div></div> " +
          "" +
          "<div class='form-group'><label class='control-label col-sm-2' for='date'>Cultivars:</label>" +
          "<div class='col-sm-9'><p class='form-control-static'>" + snapshot.val().cultivars + "</p></div></div> " +
          "" +
          "<div class='form-group'><label class='control-label col-sm-2' for='text'>Row Spacing:</label>" +
          "<div class='col-sm-9'><p class='form-control-static'>" + snapshot.val().rowSpacing + "</p></div></div> " +
          "" +
          "<div class='form-group'><label class='control-label col-sm-2' for='text'>Tree spacing:</label>" +
          "<div class='col-sm-9'><p class='form-control-static'>" + snapshot.val().treeSpacing + "</p></div></div> " +
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
            document.getElementById("orchFarmDisp").innerHTML = "<div class='col-sm-4'><button type='button' class='btn btn-default' onclick='dispFarm(\"" + farm.key + "\")'>" + farm.val().name + "</button></div>";
          }
        });

        firebase.database().ref('/' + userID() + "/workers").once('value').then(function (workers) {
          const buttons = document.getElementById("workerButtons");
          workers.forEach(function (worker) {
            if (worker.val().orchard == id) {
              buttons.innerHTML += "<div class='col-sm-4'><button type='button' class='btn btn-default' onclick='dispWork(\"" + worker.key + "\")'>" + worker.val().name + " " + worker.val().surname + "</button></div>";
            }
          });
        });
      });
    });


  }
}
var cCount = 0;
//var cDataField =
function moreCult(){
   
    cCount++;
    var input = document.createElement('input');
    input.setAttribute('type','text');
    input.setAttribute('id',('cultivars'+cCount));
    input.setAttribute('class','form-control');
    document.getElementById('cultivarBoxes').appendChild(input);
    //alert(input);
}

function getCultivars(){
    var data = [];
    var actualCount =0;
    for(var i = 0; i <= cCount; i++){
        var c = document.getElementById('cultivars'+i).value;
        if(c==null||c==""){
        }
        else{
            data[actualCount] = c;
            actualCount++;
        }        
        //alert(c);
    }   
    cCount = actualCount;
    return data;
}
function orchSave(type, id, cultivars) {
  /*0 means create, 1 means modify*/
    const farm = document.getElementById("orchFarm").value;
    const farmID = farm.substring(farm.indexOf("<") + 1, farm.indexOf(">"));
    let d = new Date(document.getElementById("orchDate").valueAsDate);
    let seconds = d.getTime() / 1000;
  var data = getCultivars();
  if (type === 0) {
      firebase.database().ref('/' + userID() +"/orchards/").push({
      name: document.getElementById("orchName").value,
      crop: document.getElementById("orchCrop").value,
      further: document.getElementById("oi").value,
      irrigation: document.getElementById("irrigationType").value,
      date: seconds,
      cultivars: data,
      bagMass: document.getElementById("orchBagMass").value,
      coords: orchardCoords,
      farm: farmID,
      rowSpacing: document.getElementById("rowSpacing").value,
      treeSpacing: document.getElementById("treeSpacing").value
    });

    
    document.getElementById("col3").innerHTML = "";
     popOrch(); 
    clear3();
  }
  else if (type === 1) {    
      firebase.database().ref('/' + userID() +"/orchards/" + id).update({
      name: document.getElementById("orchName").value,
      crop: document.getElementById("orchCrop").value,
      further: document.getElementById("oi").value,
      irrigation: document.getElementById("irrigationType").value,
      date: seconds,
      cultivars: data,
      bagMass: document.getElementById("orchBagMass").value,
      coords: orchardCoords,
      farm: farmID,
      rowSpacing: document.getElementById("rowSpacing").value,
      treeSpacing: document.getElementById("treeSpacing").value
    });
    popOrch();
    dispOrch(id);      
  clear3();
  }
 
}
var myDataCount=0;
function orchMod(id) {
  firebase.database().ref('/' + userID() +'/orchards/' + id).once('value').then(function (snapshot) {
    document.getElementById('modalDelBut').innerHTML = "<button type='button' class='btn btn-danger' data-dismiss='modal' onclick='delOrch(\"" + id + "\")'>Delete</button>";
    document.getElementById('modalText').innerText = "Please confirm deletion of " + snapshot.val().name;
    firebase.database().ref('/' + userID() +'/farms').once('value').then(function (farm) {
        const date = new Date(snapshot.val().date * 1000);
        var myData="";         
        myDataCount = 0;
        try{        
            (snapshot.val().cultivars).forEach(function(entry) {
                myData += "<input id='cultivars"+myDataCount+"' type='text' class='form-control'  value='"+entry+"' />";
                myDataCount++;
            }); 
        }
        catch(err){
            myData += "<input id='cultivars0' type='text' class='form-control'  value='' />";
        }
        cCount = 0;
        if(myData==""){
             myData += "<input id='cultivars0' type='text' class='form-control'  value='' />";
             
        }       
      document.getElementById('col3').innerHTML = "" +
        "<form class='form-horizontal'>" +
        "" +
        "<div class='form-group'>" +
        "<div class='col-sm-3 col-sm-offset-1'><button onclick='orchSave(" + 1 + ",\"" + id + "\")' type='button' class='btn btn-warning'>Save</button></div>" +
        "<div class='col-sm-3'><button type='button' class='btn btn-danger' data-toggle='modal' data-target='#delModal'>Delete</button></div> " +
        "<div class='col-sm-3'><button onclick='dispOrch(\"" + id + "\")' type='button' class='btn btn-default'>Cancel</button></div>" +
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
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Irrigation Type:</label>" +
        "<div class='col-sm-9'>"+
        "<select class='form-control' id='irrigationType'>"+
            "<option value='"+snapshot.val().irrigation+"'>"+snapshot.val().irrigation+"</option>"+
            "<option value='Micro'>Micro</option>"+
            "<option value='Drip'>Drip</option>"+
            "<option value='Floppy'>Floppy</option>"+
            "<option value='Drag Lines'>Drag Lines</option>"+
            "<option value='Other'>Other</option>"+
            "<option value='None (dry land)'>None (dry land)</option>"+
        "</select>"+
        "</div> </div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='date'>Date Planted:</label>" +
        "<div class='col-sm-9'><input type='date' class='form-control' id='orchDate' value='" + date.toISOString().substr(0, 10) + "'></div></div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Cultivars: (comma seperate)</label>" +
            "<div class='col-sm-9'>"+
                "<div id='cultivarBoxes'>"+
                    myData+        
                "</div>"+
                "<button type='button' class='btn btn-primary' onclick='moreCult()'>Add More Cultivar</button>"+
            "</div>"+
        "</div>" +
        
       /* "<div class='form-group'><label class='control-label col-sm-2' for='text'>Cultivars: </label>" +
            "<div class='col-sm-9'>"+
                "<div id='cultivarBoxes'>"+
                    "<input type='text' class='form-control' id='cultivars0' />"+
                "</div>"+
                "<button type='button' class='btn btn-primary' onclick='moreCult()'>Add More Cultivar</button>"+
            "</div>"+            
        "</div>" +*/
        
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Row Spacing:</label>" +
        "<div class='col-sm-9'><input type='text' class='form-control' id='rowSpacing' value='" +  snapshot.val().rowSpacing + "'></div></div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Tree Spacing:</label>" +
        "<div class='col-sm-9'><input type='text' class='form-control' id='treeSpacing' value='" + snapshot.val().treeSpacing + "'></div></div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
        "<div class='col-sm-9'><textarea class='form-control' rows='4' id='oi'>" + snapshot.val().further + "</textarea></div> </div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='sel1'>Assigned Farm:</label>" +
        "<div class='col-sm-9'><select class='form-control' id='orchFarm'></select></div></div>" +
        "" +
        "</form>"
      ;//need to fix referencing
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
  firebase.database().ref('/' + userID() +'/orchards/' + id).remove();
  popOrch();
  //dispOrch();
 // clear3();
  document.getElementById("col3").innerHTML = "";
}


function popWork() {
    document.getElementById("col3").innerHTML = "";
  findables = [];
  const add = document.getElementById("AddButt");
  add.innerHTML = "<h2>Loading Worker List...</h2>";
  document.getElementById("SearchSpace").innerHTML = "";

  workersRef().off();

  workersRef().once('value').then(function (snapshot) {
    add.innerHTML = "" +
      "<button type='button' class='btn btn-success' onclick='dispWork(\"-1\")'>Add Worker</button>"
    ;

    snapshot.forEach(function (child) {
      // col2.innerHTML += "" +
      //   "<button type='button' class='btn btn-info' onclick='dispWork(" + child.key + ")'>" + child.val().name + " " + child.val().surname + "</button>"
      // ;
      let temp = {
        Name : child.val().name + " " + child.val().surname,
        Button : "<button type='button' class='btn btn-info' onclick='dispWork(\"" + child.key + "\")'>" + child.val().name + " " + child.val().surname + "</button>"
      };
      findables.push(temp);
    });
    searchDisp();
  });
}

//worker functions
function dispWork(id) {
  const col3 = document.getElementById("col3");

  if (id === "-1") {
    /*Create New Worker*/


    orchardsRef().once('value').then(function (snapshot) {
      col3.innerHTML = "" +
        "<form class='form-horizontal'>" +
        "" +
        "<div class='form-group'><div class='col-sm-9 col-sm-offset-1'><button onclick='workSave(0,\"" + id + "\")' type='button' class='btn btn-warning'>Save</button></div></div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Worker Name:</label>" +
        "<div class='col-sm-9'><input type='text' class='form-control' id='workName'></div> </div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Worker Surname:</label>" +
        "<div class='col-sm-9'><input type='text' class='form-control' id='workSName'></div> </div>" +
        "" +
	"<div class='form-group'><label class='control-label col-sm-2' for='text'>Identity Number:</label>" +
	"<div class='col-sm-9'><input type='text' class='form-control' id='workID'></div> </div>" +
        "" +
	"<div class='form-group'><label class='control-label col-sm-2' for='text'>Phone Number:</label>" +
	"<div class='col-sm-9'><input type='text' class='form-control' id='workContactNo'></div> </div>" +
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
        "</form>";

      snapshot.forEach(function (child) {
        document.getElementById("workOrch").innerHTML += "<option><" + child.key + "> " + child.val().name + "  :  " + child.val().crop + "</option>";
      });
    });
  }
  else {
    firebase.database().ref('/' + userID() +'/workers/' + id).once('value').then(function (snapshot) {

        // firebase.database().ref('/' + userID() + '/orchards').once('value').then(function (orchardSnapshot) {
            orchardsRef().once('value').then(function (orchardSnapshot) {
                col3.innerHTML = "" +
                    "<form class='form-horizontal'>" +
                    "" +
                    "<div class='form-group'><div class='col-sm-9 col-sm-offset-1'><button onclick='workMod(\"" + id + "\")' type='button' class='btn btn-default'>Modify</button></div></div> " +
                    "" +
                    "<div class='form-group'><label class='control-label col-sm-2' for='text'>Worker Name:</label>" +
                    "<div class='col-sm-9'><p class='form-control-static'>" + snapshot.val().name + "</p> </div> </div> " +
                    "" +
                    "<div class='form-group'><label class='control-label col-sm-2' for='text'>Worker Surname:</label>" +
                    "<div class='col-sm-9'><p class='form-control-static'>" + snapshot.val().surname + "</p> </div> </div>" +
		    "" +
                    "<div class='form-group'><label class='control-label col-sm-2' for='text'>Identity Number:</label>" +
                    "<div class='col-sm-9'><p class='form-control-static'>" + snapshot.val().idNumber + "</p> </div> </div>" +
		    "" +
		    "<div class='form-group'><label class='control-label col-sm-2' for='text'>Phone Number:</label>" +
                    "<div class='col-sm-9'><p class='form-control-static'>" + snapshot.val().phoneNumber + "</p> </div> </div>" +
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
                        document.getElementById("workOrchDisp").innerHTML = "<div class='col-sm-4'><button type='button' class='btn btn-default' onclick='dispOrch(\"" + orchard.key + "\")'>" + orchard.val().name + "</button></div>";
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
    // });
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
  var pn = document.getElementById("workContactNo").value;
  if(pn.charAt(0)=="0")  
    pn = pn.replace(pn.charAt(0), "+27");
  //otherwise we assume it's proper
  
  let workType = "Foreman";
  if (document.getElementById("rWorker").checked) {
    workType = "Worker";
  }
  let email = "";
  if (workType === "Foreman") {
    email = document.getElementById("workEmail").value;
  }
  let newRef;
  if (type === 0) {
    //CreateWorker
    newRef = firebase.database().ref('/' + userID() +"/workers/").push({
      name: document.getElementById("workName").value,
      surname: document.getElementById("workSName").value,
      idNumber: document.getElementById("workID").value,
      phoneNumber: pn,
      orchards: orchID,
      type: workType,
      info: document.getElementById("workInfo").value,
      email: email
    });
      if (workType === "Foreman"){
          /**
           * Create the correct entry in workingFor
           */
          email = email.replace(/\./g, ",");
          firebase.database().ref('/WorkingFor/' + email).set({
              [userID()]: newRef.getKey()
          });
      }
    id = newRef.getKey();
    popWork();
  }
  else if (type === 1) {
    //Update Worker
      firebase.database().ref('/' + userID() +"/workers/" + id).update({
      name: document.getElementById("workName").value,
      surname: document.getElementById("workSName").value,
      idNumber: document.getElementById("workID").value,
      phoneNumber: pn,
      orchards: orchID,
      type: workType,
      info: document.getElementById("workInfo").value,
      email: email
    });
      if (workType === "Foreman"){
          /**
           * Update the correct entry in workingFor
           */
          email = email.replace(/\./g, ",");
          firebase.database().ref('/WorkingFor/' + email).update({
              [userID()]: id
          });
      }
  }
  popWork();
  dispWork(id);
}

function workMod(id) {
  firebase.database().ref('/' + userID() +'/workers/' + id).once('value').then(function (snapshot) {
    document.getElementById('modalDelBut').innerHTML = "<button type='button' class='btn btn-danger' data-dismiss='modal' onclick='delWork(\""+id+"\")'>Delete</button>";
    document.getElementById('modalText').innerText = "Please confirm deletion of " + snapshot.val().name + " " + snapshot.val().surname;
    firebase.database().ref('/' + userID() +'/orchards').once('value').then(function (orchard) {
      document.getElementById('col3').innerHTML = "" +
        "<form class='form-horizontal'>" +
        "" +
        "<div class='form-group'>" +
        "<div class='col-sm-3 col-sm-offset-1'><button onclick='workSave(" + 1 + ",\"" + id + "\")' type='button' class='btn btn-warning'>Save</button></div>" +
        "<div class='col-sm-3'><button type='button' class='btn btn-danger' data-toggle='modal' data-target='#delModal'>Delete</button></div> " +
        "<div class='col-sm-3'><button onclick='dispWork(\"" + id + "\")' type='button' class='btn btn-default'>Cancel</button></div>" +
        "</div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Worker Name:</label>" +
        "<div class='col-sm-9'><input type='text' class='form-control' id='workName' value='" + snapshot.val().name + "'></div> </div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Worker Surname:</label>" +
        "<div class='col-sm-9'><input type='text' class='form-control' id='workSName' value='" + snapshot.val().surname + "'></div> </div>" +
        "" +
	"<div class='form-group'><label class='control-label col-sm-2' for='text'>Identity Number:</label>" +
        "<div class='col-sm-9'><input type='text' class='form-control' id='workID' value='" + snapshot.val().idNumber + "'></div> </div>" +
        "" +	    
	"<div class='form-group'><label class='control-label col-sm-2' for='text'>Phone Number:</label>" +
        "<div class='col-sm-9'><input type='text' class='form-control' id='workContactNo' value='" + snapshot.val().phoneNumber + "'></div> </div>" +
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
 
    
  const ref = firebase.database().ref('/' + userID() + '/workers/' + id);
  let email;
  ref.once('value').then(function (snapshot) {
      email = snapshot.val().email;
  });
  ref.remove();
 
  try{
    email = email.replace(/\./g, ",");
    firebase.database().ref('/WorkingFor/' + email).remove();
  }catch(err){
      
  }
  document.getElementById("col3").innerHTML = "";
  popWork();
  clear3();
}

/*This filters displayed items in col2, based on what is present in the search box*/
function searchDisp(){
  document.getElementById("SearchSpace").innerHTML = "" +
    "" +
    "<input class='form-control' type='text' placeholder='Search' id='SearchBar' oninput='popResults()'>" +
    ""
  ;

  findables.sort(function (a, b) {
    return a.Name.localeCompare(b.Name);
  });

  const buttons = document.getElementById("DispButt");
  buttons.innerHTML = "";

  findables.forEach(function (item, index) {
    buttons.innerHTML += item.Button;
  });

  document.getElementById("selectedEntity").style.visibility = "visible";
}

/*This populates the found results.*/
function popResults() {
  const searchText = document.getElementById("SearchBar").value;
  const buttons = document.getElementById("DispButt");
  buttons.innerHTML = "";
  findables.forEach(function (item, index) {
    if (isValid(searchText, item.Name)){
      buttons.innerHTML += item.Button;
    }
  });
}

/*This checks if the given name is a valid find*/
function isValid(search, name) {
  /*This should get fancy, in time, for now just check if the characters match a pattern, and are in the correct order*/
  for (let i = 0; i < search.length; i++){
    if (i === name.length){
      return true;
    }
    if (search.charAt(i).toLowerCase() !== name.charAt(i).toLowerCase()) {
      return false;
    }
  }
  return true;
}


function clear3() {
  document.getElementById("AddButt").innerHTML = "";
  document.getElementById("SearchSpace").innerHTML = "";
  document.getElementById("DispButt").innerHTML = "";
  col3 = document.getElementById("col3").innerHTML = "";
}