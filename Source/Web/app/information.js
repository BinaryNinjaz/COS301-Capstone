const database = firebase.database();
let findables = [1];

$(window).bind("load", () => {
  let succ = () => {
    initEntities();
  };
  let fail = () => {
    orchardPolys = [];
    orchards = {};
    farms = {};
    workers = {};
  };
  retryUntilTimeout(succ, fail, 1000);
});

var editingOrchard = false;
var orchardCoords = [];
var orchardPoly;
var orchardMarkers = [];
var orchardColor;
var orchardCoordsChanged;
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
  const latlng = e.latLng;
  var c;
  if (latlng !== undefined) {
    c = {
      lat: e.latLng.lat(),
      lng: e.latLng.lng()
    }
  } else {
    c = {
      lat: e.lat || 0.0,
      lng: e.lng || 0.0
    }
  }

  orchardCoords.push(c);
  orchardCoordsChanged = true;
  if (orchardPoly !== undefined && orchardPoly !== null) {
    orchardPoly.setMap(null);
  }
  const clr = orchardColor === undefined ? 'rgb(255, 0, 0)' : orchardColor;
  orchardPoly = new google.maps.Polygon({
    paths: orchardCoords,
    strokeColor: clr,
    strokeOpacity: 0.75,
    strokeWeight: 3,
    fillColor: clr,
    fillOpacity: 0.25,
    map: map
  });
  clearMarkers();
  updateMarkers();
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
  var mapDiv = document.getElementById('map');
  if (mapDiv === undefined) {
    return;
  }
  map = new google.maps.Map(mapDiv, {
    disableDoubleClickZoom: true,
    mapTypeId: "satellite"
  });
}

function updateMarkers() {
  for (const idx in orchardCoords) {
    orchardMarkers.push(new google.maps.Marker({
      position: orchardCoords[idx],
      map: map
    }));
  }
}

function updatePolygon(orchard) {
  if (orchardCoords === undefined) {
    orchardCoords = [];
  }
  while (orchardCoords.length > 0) {
    orchardCoords.pop();
  }
  if (orchard !== null && orchard !== undefined && orchard.coords !== undefined) {
    orchard.coords.forEach(function(coord) {
      orchardCoords.push(coord);
    });
  }
  if (orchardPoly !== undefined && orchardPoly !== null) {
    orchardPoly.setMap(null);
  }
  const clr = orchardColor === undefined ? 'rgb(255, 0, 0)' : orchardColor;
  orchardPoly = new google.maps.Polygon({
    paths: orchardCoords,
    strokeColor: clr,
    strokeOpacity: 0.75,
    strokeWeight: 3,
    fillColor: clr,
    fillOpacity: 0.25,
    map: map
  });
  clearMarkers();
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
  orchardCoordsChanged = true;
  orchardCoords.pop();
  orchardPoly.setPath(orchardCoords);
  const m = orchardMarkers.pop();
  m.setMap(null);
  updatePolyListener();
}

function clearOrchardCoord() {
  if (orchardCoords === undefined) {
    orchardCoords = [];
  }
  while(orchardCoords.length > 0) {
    orchardCoords.pop();
  }
  orchardCoordsChanged = true;
  orchardPoly.setPath(orchardCoords);
  clearMarkers();
  updatePolyListener();
}

function clearMarkers() {
  while (orchardMarkers.length > 0) {
    let m = orchardMarkers.pop()
    m.setMap(null);
  }
}

function titleFormatter(entity) {
  if (entity === "Farm") {
    return (key, farm) => {
      return farm.name;
    };
  } else if (entity === "Orchard") {
    return (key, orchard) => {
      const farm = farms[orchard.farm];
      const farmName = farm === undefined ? key : farm.name;
      return farmName + " - " + orchard.name;
    };
  } else if (entity === "Worker") {
    return (key, worker) => {
      return worker.name + " " + worker.surname;
    };
  }
}

var selectedEntity = "";
var farms = {};
function showFarmsList() {
  var entityList = document.getElementById("AddButt");
  if (Object.keys(farms).length === 0) {
    farms = {};
    setFarms(farms, () => {
      displayEntityList(farms, "Farm");
    });
  } else {
    displayEntityList(farms, "Farm");
  }
}

var orchards = {};
function showOrchardsList() {
  var entityList = document.getElementById("AddButt");
  if (Object.keys(orchards).length === 0) {
    orchards = {};
    setOrchards(orchards, () => {
      displayEntityList(orchards, "Orchard");
    });
  } else {
    displayEntityList(orchards, "Orchard");
  }
}
function orchardsSortedList() {
  var possibleOrchards = []
  for (const oKey in orchards) {
    const orchard = orchards[oKey];
    possibleOrchards.push({value: orchard, key: oKey});
  }
  possibleOrchards = possibleOrchards.sort((a, b) => {
    const farmA = farms[a.value.farm];
    const farmNameA = farmA !== undefined ? farmA.name : a.key;
    const titleA = farmNameA + " - " + a.value.name;
    const farmB = farms[b.value.farm];
    const farmNameB = farmB !== undefined ? farmB.name : b.key;
    const titleB = farmNameB + " - " + b.value.name;
    return titleA.localeCompare(titleB);
  });

  return possibleOrchards;
}

var workers = {};
function showWorkersList() {
  var entityList = document.getElementById("AddButt");
  if (Object.keys(workers).length === 0) {
    workers = {};
    setWorkers(workers, () => {
      displayEntityList(workers, "Worker");
    });
  } else {
    displayEntityList(workers, "Worker");
  }
}

function showEntityList() {
  if (selectedEntity === "Farm") {
    showFarmsList();
  } else if (selectedEntity === "Orchard") {
    showOrchardsList();
  } else if (selectedEntity === "Worker") {
    showWorkersList();
  }
}

function initEntities() {
  farms = {};
  orchards = {};
  workers = {};
  updateSpiner(true);
  setFarms(farms, () => {
    setOrchards(orchards, () => {
      setWorkers(workers, () => {
        updateSpiner(false);
        watchFarms(farms, () => {
          if (selectedEntity === "Farm") {
            filterInformation(document.getElementById("informationSearchField"));
          }
        });
        watchOrchards(orchards, () => {
          if (selectedEntity === "Orchard") {
            filterInformation(document.getElementById("informationSearchField"));
          }
        });
        watchWorkers(workers, () => {
          if (selectedEntity === "Worker") {
            filterInformation(document.getElementById("informationSearchField"));
          }
        });
        showFarmsList();
      });
    });
  });
}

function displayEntityList(entities, entityName) {
  selectedEntity = entityName;
  formatter = titleFormatter(entityName);
  var addButtonDiv = document.getElementById("AddButt");
  var entityList = document.getElementById("DispButt");
  var entityDetails = document.getElementById("entityDetails");

  const addFunction = " onclick='disp" + entityName + "(\"-1\")'";
  const addName = "Add " + entityName;
  addButtonDiv.innerHTML = "<button type='button' class='btn btn-success'" + addFunction + ">" + addName + "</button>";

  entityList.innerHTML = "";

  var sortedEntityList = [];

  for (const key in entities) {
    const entity = entities[key];
    sortedEntityList.push({value: entity, key: key});
  }
  sortedEntityList = sortedEntityList.sort((a, b) => {
    if (selectedEntity === "Worker") {
      return (a.value.surname + " " + a.value.name).localeCompare(b.value.surname + " " + b.value.name);
    }
    return formatter(a.key, a.value).localeCompare(formatter(b.key, b.value));
  });

  for (const i in sortedEntityList) {
    const entity = sortedEntityList[i].value;
    const key = sortedEntityList[i].key;

    if (entity.isFarmOwner !== undefined) {
      continue
    }

    const displayFunction = " onclick='disp" + entityName + "(\"" + key+ "\")'";
    const displayName = formatter(key, entity);
    entityList.innerHTML += "<button type='button' class='btn btn-info' " + displayFunction + ">" + displayName + "</button>";
  }

  document.getElementById("selectedEntity").style.visibility = "visible";
}

/*Displays a farm in col 3, the farm displayed is set by the id. If -1 is received then display to create a new farm.*/
function dispFarm(id) {
  const entityDetails = document.getElementById("entityDetails");
  const farm = farms[id];

  if (id === "-1") {
    /*Create New Orchard*/
    entityDetails.innerHTML = "" +
      "<form class='form-horizontal'>" +
      "" +
      "<div class='form-group'><div class='col-sm-9 col-sm-offset-2'><button onclick='farmSave(0,\"" + id + "\")' type='button' class='btn btn-warning'>Save</button></div></div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Farm Name:</label>" +
      "<div class='col-sm-9'><input type='text' class='form-control' id='farmName' required></div> </div> " +
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
  } else {
    entityDetails.innerHTML = "" +
      "<form class='form-horizontal'>" +
      "" +
      "<div class='form-group'><div class='col-sm-9 col-sm-offset-2'><button onclick='farmMod(\"" + id + "\")' type='button' class='btn btn-default'>Modify</button></div></div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Farm Name:</label>" +
      "<div class='col-sm-9'><p class='form-control-static' id='farmFurther'>" + farm.name + "</p> </div> </div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='companyName'>Company Name:</label>" +
      "<div class='col-sm-9'><p class='form-control-static' id='companyName'>" + farm.companyName + "</p> </div> </div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Contact Number:</label>" +
      "<div class='col-sm-9'><p class='form-control-static' id='farmFurther'>" + farm.contactNumber + "</p> </div> </div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Email:</label>" +
      "<div class='col-sm-9'><p class='form-control-static' id='farmContact'>" + farm.email + "</p> </div> </div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
      "<div class='col-sm-9'><p class='form-control-static' id='farmEmail'>" + farm.further + "</p></div> </div>" +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='AssignedOrchards'>Assigned Orchards:</label>" +
      "<div class='col-sm-9' id='assignedOrchards'></div></div>" +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='farmProvince'>Province:</label>" +
      "<div class='col-sm-9'><p class='form-control-static' id='farmProvince'>" + farm.province + "</p> </div> </div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='farmTown'>Nearest Town:</label>" +
      "<div class='col-sm-9'><p class='form-control-static' id='farmTown'>" + farm.town + " </p> </div> </div> " +
      "" +
      "</form>"
    ;

    const assignedOrchardsDiv = document.getElementById("assignedOrchards");
    for (const key in orchards) {
      const orchard = orchards[key];
      if (orchard.farm === id) {
        assignedOrchardsDiv.innerHTML += "<div class='col-sm-4'><button type='button' class='btn btn-default' onclick='dispOrchard(\"" + key + "\")'>" + orchard.name + "</button></div>";
      }
    }
  }
}

function farmIsValidToSave(candidateKey, candidateFarm) {
  var valid = "";

  if (candidateFarm.name == "" || candidateFarm.name == undefined) {
    valid += "Farms must have a name.\n";
  }


  for (const fKey in farms) {
    const farm = farms[fKey];
    if (farm === undefined || candidateFarm === undefined) {
      continue;
    }
    if (fKey !== candidateKey && farm.name === candidateFarm.name) {
      valid = "Farms must have different names.";
    }
  }

  return valid;
}

/*Handles the saving of a farm with the set id, if it receives 0, the farm is created, else it is updated.*/
function farmSave(type, id) {
  /*0 means create, 1 means modify*/
  const tempFarm = {
    name: document.getElementById("farmName").value,
    companyName: document.getElementById("companyName").value,
    further: document.getElementById("farmFurther").value,
    contactNumber: document.getElementById("farmContact").value,
    email: document.getElementById("farmEmail").value,
    province: document.getElementById("farmProvince").value,
    town: (document.getElementById("farmTown").value+"")
  };

  const valid = farmIsValidToSave(id, tempFarm);
  if (valid !== "") {
    alert(valid);
    return;
  }

  if (type === 0) {
    let newRef = firebase.database().ref('/' + userID() + "/farms/").push(tempFarm);
    id = newRef.getKey();
    farms[id] = tempFarm;
    showFarmsList();
    dispFarm(id)
  }
  else if (type === 1) {
    farms[id] = tempFarm;
    firebase.database().ref('/' + userID() + "/farms/" + id).update(farms[id]);
     //alert("modified");
  }
  showFarmsList();
  dispFarm(id);
}

/*Displays in col 3, the interface to modify a farm*/
function farmMod(id) {
  const farm = farms[id];
  document.getElementById('modalDelBut').innerHTML = "<button type='button' class='btn btn-danger' data-dismiss='modal' onclick='delFarm(\"" + id + "\")'>Delete</button>";
  document.getElementById('modalText').innerText = "Please confirm deletion of " + farm.name;
  document.getElementById('entityDetails').innerHTML = "" +
    "<form class='form-horizontal'>" +
    "" +
    "<div class='form-group'>" +
    "<div class='col-sm-3 col-sm-offset-2'><button onclick='farmSave(" + 1 + ",\"" + id + "\")' type='button' class='btn btn-warning'>Save</button></div>" +
    "<div class='col-sm-3'><button type='button' class='btn btn-danger' data-toggle='modal' data-target='#delModal'>Delete</button></div> " +
    "<div class='col-sm-3'><button onclick='dispFarm(\"" + id + "\")' type='button' class='btn btn-default'>Cancel</button></div>" +
    "</div> " +
    "" +
    "<div class='form-group'><label class='control-label col-sm-2' for='text'>Farm Name:</label>" +
    "<div class='col-sm-9'><input type='text' class='form-control' required id='farmName' value='" + farm.name + "'></div> </div> " +
    "" +
    "<div class='form-group'><label class='control-label col-sm-2' for='text'>Company Name:</label>" +
    "<div class='col-sm-9'><input type='text' class='form-control' id='companyName' value='" + farm.companyName + "'></div> </div> " +
    "" +
    "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
    "<div class='col-sm-9'><textarea class='form-control' rows='4' id='farmFurther'>" + farm.further + "</textarea></div> </div>" +
    "" +
    "<div class='form-group'><label class='control-label col-sm-2' for='text'>Contact Number:</label>" +
    "<div class='col-sm-9'><input type='text' class='form-control' id='farmContact' value='" + farm.contactNumber + "'></div> </div> " +
    "" +
    "<div class='form-group'><label class='control-label col-sm-2' for='text'>Email:</label>" +
    "<div class='col-sm-9'><input type='text' class='form-control' id='farmEmail' value='" + farm.email + "'></div> </div> " +
    "" +
    "<div class='form-group'><label class='control-label col-sm-2' for='text'>Province:</label>" +
    "<div class='col-sm-9'><input type='text' class='form-control' id='farmProvince' value='" + farm.province + "'></div> </div> " +
     "" +
    "<div class='form-group'><label class='control-label col-sm-2' for='text'>Nearest Town:</label>" +
    "<div class='col-sm-9'><input type='text' class='form-control' id='farmTown' value='" + farm.town + " '></div> </div> " +
    "" +
    "</form>";
}

/*Delets a given farm*/
function delFarm(id) {
  firebase.database().ref('/' + userID() + '/farms/' + id).remove();
  delete farms[id];
  showFarmsList();
  document.getElementById("entityDetails").innerHTML = "<h1 class='infoDetailHelp'>Select An Item From the Sidebar</h1>";
}

function dispOrchard(id) {
  const entityDetails = document.getElementById("entityDetails");
  const orchard = orchards[id];
  orchardCoordsChanged = false;

  if (id === "-1") {
    /*Create New Orchard*/
    cCount = 0;
    orchardColor = undefined;
    entityDetails.innerHTML = "" +
      "<form class='form-horizontal'>" +
      "" +
      "<div class='form-group'><div class='col-sm-9 col-sm-offset-2'><button onclick='orchSave(0,\"" + id + "\")' type='button' class='btn btn-warning'>Save</button></div></div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Orchard Name:</label>" +
      "<div class='col-sm-9'><input type='text' class='form-control' id='orchName' required></div> </div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Orchard Crop:</label>" +
      "<div class='col-sm-9'><input type='text' class='form-control' id='orchCrop'></div> </div>" +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Orchard Location:</label>" +
      "<div class='col-sm-9'>" +
      "<div class='col-sm-12'><h4>Click the corners of a field to demarcate area</h4></div>" +
      "<div class='col-sm-12'><div id='map'></div></div>" +
      "<div class='col-sm-4'><button onclick='popOrchardCoord()' type='button' class='btn btn-warning'>Remove Last Point</button></div><div class='col-sm-4'><button onclick='clearOrchardCoord()' type='button' class='btn btn-danger'>Remove All</button></div></div></div>" +
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
              "<button type='button' class='btn btn-info' onclick='moreCult()'>Add More Cultivar</button>"+
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
      "<div class='col-sm-9'><select class='form-control' id='orchFarm' onchange='updateOrchardColor(this, \"0\")'></select></div></div>" +
       "" +
      "</form>"
    ;
    initEditOrchardMap(true, true);
    updatePolygon(null);

    for (const fKey in farms) {
      const farm = farms[fKey];
      document.getElementById("orchFarm").innerHTML += "<option value='" + fKey + "'>" + farm.name + "</option>";
    }
  }
  else {
    const date = moment(orchard.date).format("DD MMMM YYYY");
    cCount = (orchard.cultivars || []).length;
    entityDetails.innerHTML = "" +
      "<form class='form-horizontal'>" +
      "" +
      "<div class='form-group'><div class='col-sm-9 col-sm-offset-2'><button onclick='orchMod(\"" + id + "\")' type='button' class='btn btn-default'>Modify</button></div></div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Orchard Name:</label>" +
      "<div class='col-sm-9'><p class='form-control-static'>" + orchard.name + "</p> </div> </div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Orchard Crop:</label>" +
      "<div class='col-sm-9'><p class='form-control-static'>" + orchard.crop + "</p></div> </div>" +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Orchard Location:</label>" +
      "<div class='col-sm-9'><div id='map'></div></div></div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Mean Bag Mass:</label>" +
      "<div class='col-sm-9'><p class='form-control-static'>" + orchard.bagMass + " Kg</p></div> </div>" +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Irrigation:</label>" +
      "<div class='col-sm-9'><p class='form-control-static'>" + orchard.irrigation + "</p></div> </div>" +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='date'>Date Planted:</label>" +
      "<div class='col-sm-9'><p class='form-control-static'>" + date + "</p></div></div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='date'>Cultivars:</label>" +
      "<div class='col-sm-9'><p class='form-control-static'>" + (orchard.cultivars || "") + "</p></div></div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Row Spacing:</label>" +
      "<div class='col-sm-9'><p class='form-control-static'>" + orchard.rowSpacing + "</p></div></div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Tree spacing:</label>" +
      "<div class='col-sm-9'><p class='form-control-static'>" + orchard.treeSpacing + "</p></div></div> " +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
      "<div class='col-sm-9'><p class='form-control-static'>" + orchard.further + "</p></div> </div>" +
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
    orchardColor = hashColor(orchard.farm, id);
    updatePolygon(orchard);

    const assignedFarm = farms[orchard.farm];
    if (assignedFarm !== undefined) {
      document.getElementById("orchFarmDisp").innerHTML = "<div class='col-sm-4'><button type='button' class='btn btn-default' onclick='dispFarm(\"" + orchard.farm + "\")'>" + assignedFarm.name + "</button></div>";
    }

    const buttons = document.getElementById("workerButtons");
    for (wKey in workers) {
      const worker = workers[wKey];
      if (worker !== undefined && worker.orchards !== undefined && worker.orchards.includes(id)) {
        buttons.innerHTML += "<div class='col-sm-4'><button type='button' class='btn btn-default' onclick='dispWorker(\"" + wKey + "\")'>" + worker.name + " " + worker.surname + "</button></div>";
      }
    }
  }
}

function updateOrchardColor(selector, id) {
  orchardColor = hashColor(selector.value, id);
  if (orchards[id] === undefined) {
    const e = orchardCoords[0];
    if (e !== undefined) {
      pushOrchardCoord(e);
      popOrchardCoord();
    }
  } else {
    updatePolygon(orchards[id]);
  }
}

function moreCult(){
    var input = document.createElement('input');
    input.setAttribute('type','text');
    input.setAttribute('id',('cultivars'+cCount));
    input.setAttribute('class','form-control');
    document.getElementById('cultivarBoxes').appendChild(input);
    cCount++;
    //alert(input);
}

function orchardIsValidToSave(candidateKey, candidateOrchard) {
  var valid = "";

  if (candidateOrchard.name == "" || candidateOrchard.name == undefined) {
    valid += "Orchards must have a name.\n";
  }

  for (const oKey in orchards) {
    const orchard = orchards[oKey];
    const farm = farms[orchard.farm];
    const candidateFarm = farms[candidateOrchard.farm];
    if (farm === undefined || candidateFarm === undefined) {
      continue;
    }
    if (oKey !== candidateKey && farm.name === candidateFarm.name && orchard.name === candidateOrchard.name) {
      valid = "Orchards must have different names if they are both in the same farm.\n";
    }
  }

  if (candidateOrchard.farm == undefined || candidateOrchard.farm === "") {
    valid += "Orchards must be assigned to a farm.\n"
  }

  return valid;
}

function getCultivars(){
    var data = [];
    var actualCount = 0;
    for(var i = 0; i < cCount; i++){
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
function farmWithName(name) {
  for (const fKey in farms) {
    if (farms[fKey].name === name) {
      return fKey;
    }
  }
  return undefined;
}

function orchSave(type, id, cultivars) {
  /*0 means create, 1 means modify*/
  const farmID = document.getElementById("orchFarm").value;
  // const farmID = farmWithName(farmName);
  let d = moment(new Date(document.getElementById("orchDate").valueAsDate)).format("D MMM YYYY HH:mm ZZ");
  var data = getCultivars();
  const tempOrchard = {
    name: document.getElementById("orchName").value,
    crop: document.getElementById("orchCrop").value,
    further: document.getElementById("oi").value,
    irrigation: document.getElementById("irrigationType").value,
    date: d,
    cultivars: data,
    bagMass: document.getElementById("orchBagMass").value,
    coords: orchardCoords.slice(0),
    farm: farmID,
    rowSpacing: document.getElementById("rowSpacing").value,
    treeSpacing: document.getElementById("treeSpacing").value,
    inferArea: orchardCoords.length == 0
  };

  const valid = orchardIsValidToSave(id, tempOrchard);
  if (valid !== "") {
    alert(valid);
    return;
  }

  if (type === 0) {
    const ref = firebase.database().ref('/' + userID() +"/orchards/").push(tempOrchard);
    const newId = ref.getKey();

    orchards[newId] = tempOrchard;
    showOrchardsList();
    dispOrchard(newId);
  } else if (type === 1) {
    tempOrchard.inferArea = (orchards[id].inferArea || false && !orchardCoordsChanged) || orchardCoords.length == 0;
    orchards[id] = tempOrchard;
    firebase.database().ref('/' + userID() +"/orchards/" + id).update(orchards[id]);
    showOrchardsList();
    dispOrchard(id);
  }
}

function orchMod(id) {
  const orchard = orchards[id];

  document.getElementById('modalDelBut').innerHTML = "<button type='button' class='btn btn-danger' data-dismiss='modal' onclick='delOrch(\"" + id + "\")'>Delete</button>";
  document.getElementById('modalText').innerText = "Please confirm deletion of " + orchard.name;
  const date = moment(orchard.date).format("YYYY-MM-DD");
  var myData="";
  cCount = 0;
  try{
      orchard.cultivars.forEach(function(entry) {
          myData += "<input id='cultivars"+cCount+"' type='text' class='form-control'  value='"+entry+"' />";
          cCount++;
      });
  }
  catch(err){
      myData += "<input id='cultivars0' type='text' class='form-control'  value='' />";
      cCount++;
  }
  if(myData==""){
       myData += "<input id='cultivars0' type='text' class='form-control'  value='' />";
       cCount++;
  }

  document.getElementById('entityDetails').innerHTML = "" +
  "<form class='form-horizontal'>" +
  "" +
  "<div class='form-group'>" +
  "<div class='col-sm-3 col-sm-offset-2'><button onclick='orchSave(" + 1 + ",\"" + id + "\")' type='button' class='btn btn-warning'>Save</button></div>" +
  "<div class='col-sm-3'><button type='button' class='btn btn-danger' data-toggle='modal' data-target='#delModal'>Delete</button></div> " +
  "<div class='col-sm-3'><button onclick='dispOrchard(\"" + id + "\")' type='button' class='btn btn-default'>Cancel</button></div>" +
  "</div> " +
  "" +
  "<div class='form-group'><label class='control-label col-sm-2' for='text'>Orchard Name:</label>" +
  "<div class='col-sm-9'><input type='text' class='form-control' id='orchName' required value='" + orchard.name + "'></div> </div> " +
  "" +
  "<div class='form-group'><label class='control-label col-sm-2' for='text'>Orchard Crop:</label>" +
  "<div class='col-sm-9'><input type='text' class='form-control' id='orchCrop' value='" + orchard.crop + "'></div> </div>" +
  "" +
  "<div class='form-group'><label class='control-label col-sm-2' for='text'>Orchard Location:</label>" +
  "<div class='col-sm-9'>" +
  "<div class='col-sm-12'><h4>Click the corners of a field to demarcate area</h4></div>" +
  "<div class='col-sm-12'><div id='map'></div></div>" +
  "<div class='col-sm-4'><button onclick='popOrchardCoord()' type='button' class='btn btn-warning'>Remove Last Point</button></div><div class='col-sm-4'><button onclick='clearOrchardCoord()' type='button' class='btn btn-danger'>Remove All</button></div></div></div>" +
  "" +
  "<div class='form-group'><label class='control-label col-sm-2' for='text'>Mean Bag Mass:</label>" +
  "<div class='col-sm-8'><input type='number' class='form-control' id='orchBagMass' value='" + orchard.bagMass + "'></div>" +
  "<div class='col-sm-1'><p class='form-control-static'>Kg</p></div>" +
  "</div>" +
  "" +
  "<div class='form-group'><label class='control-label col-sm-2' for='text'>Irrigation Type:</label>" +
  "<div class='col-sm-9'>"+
  "<select class='form-control' id='irrigationType'>"+
      "<option value='"+orchard.irrigation+"'>"+orchard.irrigation+"</option>"+
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
  "<div class='col-sm-9'><input type='date' class='form-control' id='orchDate' value='" + date + "'></div></div> " +
  "" +
  "<div class='form-group'><label class='control-label col-sm-2' for='text'>Cultivars: (comma seperate)</label>" +
      "<div class='col-sm-9'>"+
          "<div id='cultivarBoxes'>"+
              myData+
          "</div>"+
          "<button type='button' class='btn btn-info' onclick='moreCult()'>Add More Cultivar</button>"+
      "</div>"+
  "</div>" +
  "" +
  "<div class='form-group'><label class='control-label col-sm-2' for='text'>Row Spacing:</label>" +
  "<div class='col-sm-9'><input type='text' class='form-control' id='rowSpacing' value='" +  orchard.rowSpacing + "'></div></div> " +
  "" +
  "<div class='form-group'><label class='control-label col-sm-2' for='text'>Tree Spacing:</label>" +
  "<div class='col-sm-9'><input type='text' class='form-control' id='treeSpacing' value='" + orchard.treeSpacing + "'></div></div> " +
  "" +
  "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
  "<div class='col-sm-9'><textarea class='form-control' rows='4' id='oi'>" + orchard.further + "</textarea></div> </div>" +
  "" +
  "<div class='form-group'><label class='control-label col-sm-2' for='sel1'>Assigned Farm:</label>" +
  "<div class='col-sm-9'><select class='form-control' id='orchFarm' onchange='updateOrchardColor(this, \"" + id + "\")'></select></div></div>" +
  "" +
  "</form>"
  ;//need to fix referencing
  initEditOrchardMap(false, true);
  updatePolygon(orchard);

  for (const fKey in farms) {
    const farm = farms[fKey];
    const orchFarm = document.getElementById("orchFarm");
    let selec = "";
    // workOrch.innerHTML = workOrch.innerHTML + "<option";
    if (fKey === orchard.farm) {
      selec = ' selected';
    }
    // workOrch.innerHTML = workOrch.innerHTML + "><" +child.key+"> " + child.val().name+"  :  "+child.val().crop + "</option>";
    orchFarm.innerHTML += "<option" + selec + " value='" + fKey + "'>" + farm.name + "</option>";
  }
}

function delOrch(id) {
  firebase.database().ref('/' + userID() +'/orchards/' + id).remove();
  delete orchards[id];
  showOrchardsList();

  document.getElementById("entityDetails").innerHTML = "<h1 class='infoDetailHelp'>Select An Item From the Sidebar</h1>";
}

//worker functions
function dispWorker(id) {
  const entityDetails = document.getElementById("entityDetails");
  const worker = workers[id];

  if (id === "-1") {
    /*Create New Worker*/
    entityDetails.innerHTML = "" +
      "<form class='form-horizontal'>" +
      "" +
      "<div class='form-group'><div class='col-sm-9 col-sm-offset-2'><button onclick='workSave(0,\"" + id + "\")' type='button' class='btn btn-warning'>Save</button></div></div> " +
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
      "<div class='form-group'><label class='control-label col-sm-2' for='sel1'>Assigned Orchards:</label>" +
      "<div class='col-sm-9'><div id='workOrch'></div></div></div>" +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' id='workType'>Worker Type:</label>" +
      "<label class='radio-inline'><input type='radio' name='optradio' id='rWorker' checked>Worker</label>" +
      "<label class='radio-inline'><input type='radio' name='optradio' id='rForeman'>Foreman</label>" +
      "</div>" +
      "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
      "<div class='col-sm-9'><textarea class='form-control' rows='4' id='workInfo'></textarea></div></div>" +
      "" +
      "<div id='emailSpace'></div>" +
      "" +
      "</form>";

      const possibleOrchards = orchardsSortedList();
      for (const idx in possibleOrchards) {
        const orchard = possibleOrchards[idx];
        const workOrch = document.getElementById("workOrch");
        const farm = farms[orchard.value.farm];
        const farmName = farm !== undefined ? farm.name : orchard.key;
        const title = farmName + " - " + orchard.value.name;

        workOrch.innerHTML +=
        "<div class='form-check'>" +
          "<input class='form-check-input' type='checkbox' id='ass"+ orchard.key +"'" + + " value='" + orchard.key + "'>" +
          "<label class='form-check-label' for='ass" + orchard.key + "'> " + title + "</label>" +
        "</div>";
      }
  }
  else {
    entityDetails.innerHTML = "" +
        "<form class='form-horizontal'>" +
        "" +
        "<div class='form-group'><div class='col-sm-9 col-sm-offset-2'><button onclick='workMod(\"" + id + "\")' type='button' class='btn btn-default'>Modify</button></div></div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Worker Name:</label>" +
        "<div class='col-sm-9'><p class='form-control-static'>" + worker.name + "</p> </div> </div> " +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Worker Surname:</label>" +
        "<div class='col-sm-9'><p class='form-control-static'>" + worker.surname + "</p> </div> </div>" +
"" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Identity Number:</label>" +
        "<div class='col-sm-9'><p class='form-control-static'>" + worker.idNumber + "</p> </div> </div>" +
"" +
"<div class='form-group'><label class='control-label col-sm-2' for='text'>Phone Number:</label>" +
        "<div class='col-sm-9'><p class='form-control-static'>" + worker.phoneNumber + "</p> </div> </div>" +
"" +
        "<div class='form-group'><label class='control-label col-sm-2' for='sel1'>Assigned Orchards:</label>" +
        "<div class='col-sm-9'><span id='workOrchDisp'></span></div></div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' id='workType'>Worker Type:</label>" +
        "<div class='col-sm-9'><p class='form-control-static'>" + worker.type + "</p> </div>" +
        "</div>" +
        "" +
        "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
        "<div class='col-sm-9'><p class='form-control-static'>" + worker.info + "</p> </div></div>" +
        "" +
        "<div id='emailDispSpace'></div>" +
        "" +

        "</form>"
    ;

    for (const oKey in orchards) {
      const orchard = orchards[oKey];
      if (worker.orchards !== undefined && worker.orchards.includes(oKey)) {
        const farm = farms[orchard.farm];
        const farmName = farm !== undefined ? farm.name : oKey;
        document.getElementById("workOrchDisp").innerHTML += "<div class='col-sm-4'><button type='button' class='btn btn-default' onclick='dispOrchard(\"" + oKey + "\")'>" + farmName + " - " + orchard.name + "</button></div>";
      }
    }
  }
}

function assignedOrchardsForWorker() {
  var result = [];
  for (const oid in orchards) {
    const inp = document.getElementById("ass" + oid);
    if (inp.checked) {
      result.push(oid);
    }
  }
  return result;
}

function workSave(type, id) {
  /*0 means create, 1 means modify*/
  const orchard = document.getElementById("workOrch").value;
  const orchID = assignedOrchardsForWorker();
  var pn = document.getElementById("workContactNo").value;
  if(pn.charAt(0)=="0") {
    pn = pn.replace(pn.charAt(0), "+27");
  } //otherwise we assume it's proper

  let workType = "Foreman";
  if (document.getElementById("rWorker").checked) {
    workType = "Worker";
  }
  let email = "";
  if (workType === "Foreman") {
    email = "";//document.getElementById("workEmail").value;
  }
  const tempWorker = {
    name: document.getElementById("workName").value,
    surname: document.getElementById("workSName").value,
    idNumber: document.getElementById("workID").value,
    phoneNumber: pn,
    orchards: orchID,
    type: workType,
    info: document.getElementById("workInfo").value
  };
  let newRef;
  if (type === 0) {
    // Create Worker
    const pn = tempWorker.phoneNumber;
    if (tempWorker.type === "Foreman" && pn !== undefined && pn !== "") {
      const obj = {};
      obj[pn] = true;
      firebase.database().ref('/' + userID() + '/foremen/').update(obj);
    }
    newRef = firebase.database().ref('/' + userID() + '/workers/').push(tempWorker);
    id = newRef.getKey();
    workers[id] = tempWorker;
    showWorkersList();
    dispWorker(id);
  } else if (type === 1) {
    //Update Worker
    const oldPhone = workers[id].phoneNumber;
    const oldType = workers[id].type;
    workers[id] = tempWorker;
    firebase.database().ref('/' + userID() +"/workers/" + id).update(workers[id]);
    if (pn !== undefined && pn !== "") {
      if (oldType !== undefined && oldType === "Foreman" && workType === "Worker") {
        firebase.database().ref('/' + userID() + '/foremen/' + pn).remove();
      } else if (workType === "Foreman" && oldType === "Worker") {
        const obj = {};
        obj[pn] = true;
        firebase.database().ref('/' + userID() + '/foremen/').update(obj);
      }
    }
    if (oldType !== undefined && oldType === "Foreman" && workType === "Worker") {
      firebase.database().ref('/' + userID() + '/locations/' + id).remove();
      firebase.database().ref('/' + userID() + '/requestedLocations/' + id).remove();
    }
  }
  showWorkersList();
  dispWorker(id);
}

function workMod(id) {
  const worker = workers[id];

  document.getElementById('modalDelBut').innerHTML = "<button type='button' class='btn btn-danger' data-dismiss='modal' onclick='delWork(\""+id+"\")'>Delete</button>";
  document.getElementById('modalText').innerText = "Please confirm deletion of " + worker.name + " " + worker.surname;
  document.getElementById('entityDetails').innerHTML = "" +
    "<form class='form-horizontal'>" +
    "" +
    "<div class='form-group'>" +
    "<div class='col-sm-3 col-sm-offset-2'><button onclick='workSave(" + 1 + ",\"" + id + "\")' type='button' class='btn btn-warning'>Save</button></div>" +
    "<div class='col-sm-3'><button type='button' class='btn btn-danger' data-toggle='modal' data-target='#delModal'>Delete</button></div> " +
    "<div class='col-sm-3'><button onclick='dispWorker(\"" + id + "\")' type='button' class='btn btn-default'>Cancel</button></div>" +
    "</div>" +
    "" +
    "<div class='form-group'><label class='control-label col-sm-2' for='text'>Worker Name:</label>" +
    "<div class='col-sm-9'><input type='text' class='form-control' id='workName' value='" + worker.name + "'></div> </div> " +
    "" +
    "<div class='form-group'><label class='control-label col-sm-2' for='text'>Worker Surname:</label>" +
    "<div class='col-sm-9'><input type='text' class='form-control' id='workSName' value='" + worker.surname + "'></div> </div>" +
    "" +
  "<div class='form-group'><label class='control-label col-sm-2' for='text'>Identity Number:</label>" +
    "<div class='col-sm-9'><input type='text' class='form-control' id='workID' value='" + worker.idNumber + "'></div> </div>" +
    "" +
  "<div id='wf' class='form-group'><label class='control-label col-sm-2' for='workContactNo'>Phone Number:</label>" +
    "<div class='col-sm-9'><input type='text' class='form-control' id='workContactNo' value='" + worker.phoneNumber + "' ></div> </div>" +
    "" +
    "<div class='form-group'><label class='control-label col-sm-2' for='sel1'>Assigned Orchards:</label>" +
    "<div class='col-sm-9'><div id='workOrch'></div></div></div>" +
    "" +
    "<div class='form-group'><label class='control-label col-sm-2' id='workType'>Worker Type:</label>" +
    "<label class='radio-inline'><input type='radio' name='optradio' id='rWorker'>Worker</label>" +
    "<label class='radio-inline'><input type='radio' name='optradio' id='rForeman'>Foreman</label>" +
    "</div>" +
    "" +
    "<div class='form-group'><label class='control-label col-sm-2' for='text'>Information:</label>" +
    "<div class='col-sm-9'><textarea class='form-control' rows='4' id='workInfo'>" + worker.info + "</textarea></div></div>" +
    "" +
    "<div id='emailSpace'></div>" +
    "" +

    "</form>"
  ;

  if (worker.type === "Foreman") {
    document.getElementById("rForeman").setAttribute("checked", "");
    try{
    document.getElementById("workContactNo").setAttribute("required");
    }catch(err){}
    /*document.getElementById("emailSpace").innerHTML = "" +
      "<div class='form-group'><label class='control-label col-sm-2' for='text'>Foreman Email:</label>" +
      "<div class='col-sm-9'><input type='text' class='form-control' id='workEmail' value='" + snapshot.val().email + "'></div> </div> "
    ;*/
  }
  else {
    document.getElementById("rWorker").setAttribute("checked", "");
    try{
    document.getElementById("workContactNo").removeAttribute("required");
    }catch(err){

    }
  }

  const possibleOrchards = orchardsSortedList();
  for (const idx in possibleOrchards) {
    const orchard = possibleOrchards[idx];
    const workOrch = document.getElementById("workOrch");
    let isChecked = "";
    // workOrch.innerHTML = workOrch.innerHTML + "<option";
    if (worker.orchards !== undefined && worker.orchards.includes(orchard.key)) {
      isChecked = ' checked';
    }
    const farm = farms[orchard.value.farm];
    const farmName = farm !== undefined ? farm.name : orchard.key;
    const title = farmName + " - " + orchard.value.name;

    workOrch.innerHTML +=
    "<div class='form-check'>" +
      "<input class='form-check-input' type='checkbox' id='ass"+ orchard.key +"'" + + " value='" + orchard.key + "'" + isChecked + ">" +
      "<label class='form-check-label' for='ass" + orchard.key + "'> " + title + "</label>" +
    "</div>";
  }
}

function delWork(id) {
  const ref = firebase.database().ref('/' + userID() + '/workers/' + id);
  const pn = workers[id].phoneNumber;
  ref.remove();
  delete workers[id];

  try{
    if (pn !== undefined && pn !== "") {
      firebase.database().ref('/' + userID() + '/foremen/' + pn).remove();
    }
    firebase.database().ref('/' + userID() + '/locations/' + id).remove();
    firebase.database().ref('/' + userID() + '/requestedLocations/' + id).remove();
  } catch(err){

  }
  document.getElementById("entityDetails").innerHTML = "<h1 class='infoDetailHelp'>Select An Item From the Sidebar</h1>";
  showWorkersList();
}

function filterInformation(searchField) {
  const searchText = searchField.value;

  if (searchText === "") {
    showEntityList();
  } else {
    entitiesList = [];
    filteredEntities = [];
    const formatter = titleFormatter(selectedEntity);
    var entities;
    if (selectedEntity === "Farm") {
      entities = farms;
    } else if (selectedEntity === "Orchard") {
      entities = orchards;
    } else if (selectedEntity === "Worker") {
      entities = workers;
    }

    for (const key in entities) {
      const entity = entities[key];
      var searchResults;
      if (selectedEntity === "Farm") {
        searchResults = queryFarm(entity, searchText, true);
      } else if (selectedEntity === "Orchard") {
        searchResults = queryOrchard(entity, key, farms, orchards, workers, searchText, true);
      } else if (selectedEntity === "Worker") {
        searchResults = queryWorker(entity, orchards, searchText, true);
      }

      for (const property in searchResults) {
        var newEntity = {};
        newEntity["value"] = entity;
        newEntity["reason"] = searchResults[property];
        newEntity["key"] = key;
        insertEntityIntoSortedMap(newEntity, property, formatter, filteredEntities);
      }
    }

    displayFilteredEntities(formatter);
  }
}

// sorted map
var filteredEntities = [];
function insertEntityIntoSortedMap(entity, key, formatter, sortedMap) {
  var belongsInGroup = undefined;
  for (const groupIdx in sortedMap) {
    const group = sortedMap[groupIdx];
    if (group.key === key) {
      belongsInGroup = groupIdx;
      break;
    }
  }

  if (belongsInGroup !== undefined) {
    sortedMap[belongsInGroup].values.push(entity);
    sortedMap[belongsInGroup].values = sortedMap[belongsInGroup].values.sort((a, b) => {
      return formatter(a.key, a.value).localeCompare(formatter(b.key, b.value));
    });
  } else {
    sortedMap.push({key: key, values: [entity]});
  }

  sortedMap = sortedMap.sort((a, b) => {
    return a.key.localeCompare(b.key);
  });
}

function displayFilteredEntities(formatter) {
  var addButtonDiv = document.getElementById("AddButt");
  const addFunction = " onclick='disp" + selectedEntity + "(\"-1\")'";
  const addName = "Add " + selectedEntity;
  addButtonDiv.innerHTML = "<button type='button' class='btn btn-success'" + addFunction + ">" + addName + "</button>";

  var entityList = document.getElementById("DispButt");
  entityList.innerHTML = "";
  for (const groupIdx in filteredEntities) {
    const group = filteredEntities[groupIdx];
    const key = group.key;
    entityList.innerHTML += "<h4>" + key + "</h4>";
    for (const itemIdx in group.values) {
      const item = group.values[itemIdx];
      const displayFunction = " onclick='disp" + selectedEntity + "(\"" + item.key + "\")'";
      const displayName = formatter(item.key, item.value);
      entityList.innerHTML += "<button type='button' class='btn btn-info' " + displayFunction + ">" + displayName + "</button>";
      entityList.innerHTML += "<p class='searchReason'>" + item.reason + "</p>";
    }
  }
}

/* This function shows the spinner while still waiting for resources*/
var spinner;
function updateSpiner(shouldSpin) {
  var opts = {
		lines: 8, // The number of lines to draw
		length: 37, // The length of each line
		width: 10, // The line thickness
		radius: 20, // The radius of the inner circle
		scale: 1, // Scales overall size of the spinner
		corners: 1, // Corner roundness (0..1)
		color: 'white', // CSS color or array of colors
		fadeColor: 'transparent', // CSS color or array of colors
		speed: 1, // Rounds per second
		rotate: 0, // The rotation offset
		animation: 'spinner-line-fade-quick', // The CSS animation name for the lines
		direction: 1, // 1: clockwise, -1: counterclockwise
		zIndex: 2e9, // The z-index (defaults to 2000000000)
		className: 'spinner', // The CSS class to assign to the spinner
		shadow: '0 0 1px transparent', // Box-shadow for the lines
  };

  var target = document.getElementById("loader"); //This is where the spinner is gonna show
  if (shouldSpin) {
		if (spinner == null) {
		  spinner = new Spinner(opts).spin(target);
		}
  } else {
	  //target.style.top = "0px";
	  spinner.stop(); //This line stops the spinner.
	  spinner = null;
  }
}
