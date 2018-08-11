/*
*	File:	HeatMap.js
*	Authour:	Binary Ninjaz (Letanyan, Vincent)
*
*	Description: 	This file requests and receives data from firebase database
*					To display a heat map on >HeatMap.html". It shows heat where 
*					bags were collected.
*/
$(window).bind("load", () => {
  let succ = () => {
    initOrchards();
    initWorkers();
    $("#selectedFilter :input").change(function() {
      loadEntity(this.id === "workerFilter" 
        ? "worker" 
        : this.id === "orchardFilter"
          ? "orchard"
          : this.id === "foremanFilter"
            ? "foreman"
            : "farm");
    });
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

var orchards = []; //An array of available orchards 
var farms = []; //An array of available farms 

/* This functions allows the user to filter out orchards, workers,farms, or forman*/
function changeSelection(checkbox) {
  if (selectedEntity === "orchard") { /* Checks if selected entity is an orchard*/
    for(var i = 0; i < orchards.length; i++) {
      if (orchards[i].key === checkbox.value) {
        if (checkbox.checked !== orchards[i].showing) {
          orchards[i].showing = checkbox.checked;
        }
      }
    }
  } else if (selectedEntity === "worker") { /* Checks if selected entity is a worker*/
    for(var i = 0; i < workers.length; i++) {
      if (workers[i].value.type === "Worker" && workers[i].key === checkbox.value) {
        if (checkbox.checked !== workers[i].showing) {
          workers[i].showing = checkbox.checked;
        }
      }
    }
  } else if (selectedEntity === "foreman") { /* Checks if selected entity is a forman*/
    for(var i = 0; i < workers.length; i++) {
      if (workers[i].value.type === "Foreman" && workers[i].key === checkbox.value) {
        if (checkbox.checked !== workers[i].showing) {
          workers[i].showing = checkbox.checked;
        }
      }
    }
  } else if (selectedEntity === "farm") { /* Checks if selected entity is a farm */
    for(var i = 0; i < farms.length; i++) {
      if (farms[i].key === checkbox.value) {
        if (checkbox.checked !== farms[i].showing) {
          farms[i].showing = checkbox.checked;
        }
      }
    }
  }
}

var selectedEntity = "orchard"; //initializes the selected entity

/* This function loads entities of interest (Farm, Foreman, Orchard, Worker)*/
function loadEntity(entity) {
  var entityDiv = document.getElementById("entities");
  entityDiv.innerHTML = "";
  selectedEntity = entity;
  if (entity === "orchard") {
    for (const orchardId in orchards) {
      const orchard = orchards[orchardId];
      entityDiv.innerHTML += createSelectionButton(orchard.title, orchard.key, orchard.showing);
    }
  } else if (entity === "worker") {
    for (const workerId in workers) {
      const worker = workers[workerId];
      if (worker.value.type === "Foreman") {
        continue;
      }
      entityDiv.innerHTML += createSelectionButton(worker.value.name + " " + worker.value.surname, worker.key, worker.showing);
    }
  } else if (entity === "foreman") {
    for (const workerId in workers) {
      const worker = workers[workerId];
      if (worker.value.type === "Worker") {
        continue;
      }
      entityDiv.innerHTML += createSelectionButton(worker.value.name + " " + worker.value.surname, worker.key, worker.showing);
    }
  } else if (entity === "farm") {
    for (const farmId in farms) {
      const farm = farms[farmId];
      entityDiv.innerHTML += createSelectionButton(farm.value.name, farm.key, farm.showing);
    }
  }
}

/* This function creates a selction button, returns the output as a string*/
function createSelectionButton(name, key, checked) {
  const isChecked = checked ? 'checked' : '';
  result = '';
  result += '<div class="checkbox">';
  result += '<label><input type="checkbox" onchange="changeSelection(this)" ' + isChecked + ' value="' + key + '">' + name + '</label>';
  result += '</div>';
  
  return result;
}

/* This function checks (loops through) the farm ID compared to the ID received as a parameter*/
function farmForId(id) {
  for (var farm in farms) {
    if (farms[farm].key === id) {
      return farms[farm].value;
    }
  }
  return null;
}

var orchardPolys = [];
function orchardCoords(orchardVal) {
  var result = [];
  for (var ckey in orchardVal.coords) {
    var coord = orchardVal.coords[ckey];
    const latlng = new google.maps.LatLng(coord.lat, coord.lng);
    result.push(latlng);
  }
  return result;
}

/* This function initialises the orchards */
function initOrchards() {
  var today = new Date();
  var dd = ("0" + today.getDate()).slice(-2);
  var mm = ("0" + (today.getMonth() + 1)).slice(-2);
  var yyyy = today.getFullYear();
  
  $("#endDate").val(yyyy + "-" + mm + "-" + dd);
  
  today.setDate(today.getDate() - 7);
  dd = ("0" + today.getDate()).slice(-2);
  mm = ("0" + (today.getMonth() + 1)).slice(-2);
  yyyy = today.getFullYear();
  
  $("#startDate").val(yyyy + "-" + mm + "-" + dd);
  
  getFarms((farmsSnap) => {
    farmsSnap.forEach((farm) => {
      farms.push({key: farm.key, value: farm.val(), showing: false});
    });
    farms.sort((a, b) => { return a.value.name < b.value.name ? -1 : 1 })
    getOrchards((orchardsSnap) => {
      var entityDiv = document.getElementById("entities");
      entityDiv.innerHTML = "";
      orchards = [];
      orchardsSnap.forEach((orchard) => {
        const o = orchard.val();
        const k = orchard.key;
        var orchardPoly = new google.maps.Polygon({
          paths: orchardCoords(o),
          strokeColor: '#0000BB',
          strokeOpacity: 0.5,
          strokeWeight: 3,
          fillColor: '#0000BB',
          fillOpacity: 0.1,
          map: map
        });
        orchardPolys.push(orchardPoly);
        var fval = farmForId(o.farm);
        var fname = "?";
        if (fval !== null) {
          fname = fval.name;
        }
        const name = fname + " - " + o.name;
        orchards.push({key: k, value: o, showing: true, title: name});
      });
      orchards = orchards.sort((a, b) => { return a.title < b.title ? -1 : 1 });
      for (const oid in orchards) {
        const o = orchards[oid];
        entityDiv.innerHTML += createSelectionButton(o.title, o.key, true);
      }
      updateHeatmap();
    });
  });
}

/* The workers array annd initWorkers initialises the workers on Heatmap.html*/
var workers = [];
function initWorkers() {
  getWorkers((workersSnap) => {
    workersSnap.forEach((worker) => {
      const w = worker.val();
      const k = worker.key;
      workers.push({key: k, value: w, showing: false});
    });
    workers.sort((a, b) => { 
      return (a.value.surname + a.value.name) < (b.value.surname + b.value.name) ? -1 : 1 
    });
  });
}

/* The map variable and the initMap initialize the coordinates of the area of farming*/
var map;
function initMap() {
  map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: -25, lng: 28 },
    zoom: 14,
    mapTypeId: 'satellite'
  });
  locationLookup((data) => {
    var latLng = new google.maps.LatLng(data.lat, data.lon);
    map.setCenter(latLng);
    map.setZoom(11);
  });
}

/* This function requests the IDs of entities of interest (Farm, Foreman, Orchard, Worker)*/
function requestedIds() {
  var result = {};
  var k = 0;
  if (selectedEntity === "orchard") { /* Requesting IDs for orchards */
    for (var i = 0; i < orchards.length; i++) {
      if (orchards[i].showing) {
        result["id" + k] = orchards[i].key;
        k++;
      }
    }
  } else if (selectedEntity === "worker") { /* Requesting IDs for workers */
    for (var i = 0; i < workers.length; i++) {
      if (workers[i].value.type === "Worker" && workers[i].showing) {
        result["id" + k] = workers[i].key;
        k++;
      }
    }
  } else if (selectedEntity === "foreman") { /* Requesting IDs for foreman */
    for (var i = 0; i < workers.length; i++) {
      if (workers[i].value.type === "Foreman" && workers[i].showing) {
        result["id" + k] = workers[i].key;
        k++;
      }
    }
  } else if (selectedEntity === "farm") { /* Requesting IDs for farms */
    for (var i = 0; i < farms.length; i++) {
      if (farms[i].showing) {
        result["id" + k] = farms[i].key;
        k++;
      }
    }
  }
  
  return result; /* Returns the IDs of interest */
}

var first = true; /* Setting the first variable true for the if statement inside*/
var heatmap; /* Varaible for the heatmap*/
/* This function updates the to show heat where the most number of bags were collected*/
function updateHeatmap() {
  var keys = requestedIds();
  const startDate = new Date(document.getElementById("startDate").value);
  const endDate = new Date(document.getElementById("endDate").value);
  const startTime = startDate.getTime() / 1000;
  const endTime = endDate.getTime() / 1000 + 60 * 60 * 24;
  
  keys.startDate = startTime;
  keys.endDate = endTime;
  keys.uid = userID();
  keys.groupBy = selectedEntity;
  
  const url = 'https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/collectionsWithinDate';
  
  updateSpiner(true);
  $.post(url, keys, (data, status) => {
    var formattedData = [];
    for (var d in data) {
      const latLng = new google.maps.LatLng(data[d].lat, data[d].lng);
      formattedData.push(latLng);
      if (first) {
        map.setCenter(latLng);
        map.setZoom(14);
      }
    }
    if (heatmap !== undefined) {
      heatmap.setMap(null);
    }
    heatmap = new google.maps.visualization.HeatmapLayer({
      data: formattedData,
      dissipating: true,
      radius: 50,
      map: map
    });
    updateSpiner(false);
  });
}

/* This function shows the spinner while still waiting for resources*/
var spinner;
function updateSpiner(shouldSpin) {
  var opts = {
    lines: 8, // The number of lines to draw
    length: 27, // The length of each line
    width: 10, // The line thickness
    radius: 20, // The radius of the inner circle
    scale: 0.5, // Scales overall size of the spinner
    corners: 1, // Corner roundness (0..1)
    color: '#2A2', // CSS color or array of colors
    fadeColor: 'transparent', // CSS color or array of colors
    speed: 1, // Rounds per second
    rotate: 0, // The rotation offset
    animation: 'spinner-line-fade-quick', // The CSS animation name for the lines
    direction: 1, // 1: clockwise, -1: counterclockwise
    zIndex: 2e9, // The z-index (defaults to 2000000000)
    className: 'spinner', // The CSS class to assign to the spinner
    top: '110%', // Top position relative to parent
    left: '50%', // Left position relative to parent
    shadow: '0 0 1px transparent', // Box-shadow for the lines
    position: 'absolute' // Element positioning
  };

  var target = document.getElementById('spinner');
  var button = document.getElementById('updateButton');
  if (shouldSpin) {
    spinner = new Spinner(opts).spin(target);
    button.style.visibility = "hidden";
  } else {
    button.style.visibility = "visible";
    spinner.stop();
    spinner = null;
  }
}