const user = function() { return firebase.auth().currentUser };
const userID = function() {
  if (user() !== null) {
    return user().uid 
  } else {
    return ""
  }
}

function getOrchards(callback) {
  const ref = firebase.database().ref('/' + userID() + '/orchards');
  ref.once('value').then((snapshot) => {
    callback(snapshot);
  });
}

function getFarms(callback) {
  const ref = firebase.database().ref('/' + userID() + '/farms');
  ref.once('value').then((snapshot) => {
    callback(snapshot);
  });
}

function yieldsRef() {
  return firebase.database().ref('/' + userID() + '/sessions');
}

firebase.auth().onAuthStateChanged(function (user) {
  if (user) {
    $(window).bind("load", function() {
      initMap();
      initOrchards();
    });
  } else {
    orchardPolys = [];
    orchards = [];
    farms = [];
  }
});

var orchards = [];
var farms = [];
function changeSelection(checkbox) {
  for (var i = 0; i < orchards.length; i++) {
    if (orchards[i].key === checkbox.value) {
      if (checkbox.checked !== orchards[i].showing) {
        orchards[i].showing = checkbox.checked;
        updateHeatmap();
      }
    }
  }
}

function createOrchardSelectionButton(name, key) {
  result = '';
  result += '<div class="checkbox">';
  result += '<label><input type="checkbox" onchange="changeSelection(this)" checked value="' + key + '">' + name + '</label>';
  result += '</div>';
  
  return result;
}

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
      farms.push({key: farm.key, value: farm.val()});
    });
    getOrchards((orchardsSnap) => {
      var orchardsDiv = document.getElementById("orchards");
      orchardsDiv.innerHTML = "";
      orchards = [];
      orchardsSnap.forEach((orchard) => {
        const o = orchard.val();
        const k = orchard.key;
        orchards.push({key: k, value: o, showing: true});
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
        orchardsDiv.innerHTML += createOrchardSelectionButton(name, k);
      });
      updateHeatmap();
    });
  });
}

var map;
function initMap() {
  navigator.geolocation.getCurrentPosition(function(loc) {
    var latLng = new google.maps.LatLng(loc.coords.latitude, loc.coords.longitude);
    map.setCenter(latLng);
  });
  map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: -25, lng: 28 },
    zoom: 14,
    mapTypeId: 'satellite'
  });
}

function requestedOrchardIds() {
  var result = {};
  for (var i = 0; i < orchards.length; i++) {
    if (orchards[i].showing) {
      result["orchardId" + String(i)] = orchards[i].key;
    }
  }
  return result;
}

var heatmap;
function updateHeatmap() {
  var keys = requestedOrchardIds();
  const startDate = new Date(document.getElementById("startDate").value);
  const endDate = new Date(document.getElementById("endDate").value);
  const startTime = startDate.getTime() / 1000;
  const endTime = endDate.getTime() / 1000;
  
  keys.startDate = startTime;
  keys.endDate = endTime;
  keys.uid = userID();
  
  const url = 'https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/orchardCollectionsWithinDate';
  
  updateSpiner(true);
  $.post(url, keys, (data, status) => {
    var formattedData = [];
    for (var d in data) {
      const latLng = new google.maps.LatLng(data[d].lat, data[d].lng);
      formattedData.push(latLng);
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
  if (shouldSpin) {
    spinner = new Spinner(opts).spin(target);
  } else {
    spinner.stop();
    spinner = null;
  }
}