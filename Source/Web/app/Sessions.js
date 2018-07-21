const baseUrl = 'https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/flattendSessions?';
var pageIndex = null;
var pageSize = 21;
const user = function() { return firebase.auth().currentUser };
const userID = function() {
  if (user() !== null) {
    return user().uid 
  } else {
    return ""
  }
}

function getWorkers(callback) {
  const ref = firebase.database().ref('/' + userID() + '/workers');
  ref.once('value').then((snapshot) => {
    callback(snapshot);
  });
}

function yieldsRef() {
  return firebase.database().ref('/' + userID() + '/sessions');
}

$(window).bind("load", () => {
  let succ = () => {
    initPage();
    initMap();
    google.charts.load('current', {'packages':['corechart']});
  };
  let fail = () => {
    sessions = [];
  };
  retryUntilTimeout(succ, fail, 1000);
});

var map;
function initMap() {
  locationLookup((data, response) => {
    var latLng = new google.maps.LatLng(data.lat, data.lon);
    map.setCenter(latLng);
    map.setZoom(11);
  });
  map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: -25, lng: 28 },
    zoom: 14,
    mapTypeId: 'satellite'
  });
}

function foremanForKey(key) {
  for (var k in foremen) {
    if (foremen[k].key === key) {
      return foremen[k];
    }
  }
  return {value: {name: "Farm", surname: "Owner"}};
}

function workerForKey(key) {
  for (var k in workers) {
    if (workers[k].key === key) {
      return workers[k];
    }
  }
  return undefined;
}

function sessionForKey(key) {
  for (var k in sessions) {
    if (sessions[k].key === key) {
      return sessions[k];
    }
  }
  return undefined;
}

function sessionsListLoader(loading) {
  var sessionsListHolder = document.getElementById("sessionsListLoader");
  if (!loading) {
    sessionsListHolder.innerHTML = "<button type='button' class='btn btn-secoundary' style='margin: 4px' onclick='newPage()'>Load More Sessions</button>";
  } else {
    sessionsListHolder.innerHTML = "<h2>Loading Sessions...</h2>";
  }
}

var foremen = [];
var workers = []
function initPage() {
  var sessionsList = document.getElementById("sessionsList");
  sessionsListLoader(true);
  getWorkers((workersSnap) => {
    foremen = [];
    workers = [];
    workersSnap.forEach((worker) => {
      const w = worker.val();
      const k = worker.key;
      if (w.type === "Foreman") {
        foremen.push({key: k, value: w});
      } else {
        workers.push({key: k, value: w});
      }
    });
    newPage();
  });
}

var sessions = [];
function newPage() {
  var ref;
  sessionsListLoader(true);
  var sessionsList = document.getElementById("sessionsList");
  if (pageIndex === null) {
    ref = firebase.database().ref('/' + userID() + '/sessions')
      .orderByKey()
      .limitToLast(pageSize);
  } else {
    ref = firebase.database().ref('/' + userID() + '/sessions')
      .orderByKey()
      .endAt(pageIndex)
      .limitToLast(pageSize);
  }
  var tempSessions = [];
  ref.once('value').then((snapshot) => {
    var lastSession = "";
    var resultHtml = [];
    snapshot.forEach((child) => {
      const obj = child.val();
      const foreman = foremanForKey(obj.wid);
      if (foreman !== undefined) {
        if (lastSession === "") {
          lastSession = child.key;
        }
        const name = foreman.value.name + " " + foreman.value.surname;
        const text = name + " - " + (new Date(obj.start_date * 1000)).toLocaleString();
        resultHtml.unshift("<button type='button' class='btn btn-primary' style='margin: 4px' onclick=loadSession('" + child.key + "') >" + text + "</button>");
        tempSessions.unshift({val: obj, key: child.key});
      }
    });
    tempSessions.pop();
    for (var i = 0; i < tempSessions.length; i++) {
      sessions.push(tempSessions[i]);
    }
    
    resultHtml.pop();
    sessionsList.innerHTML += resultHtml.join("");
    
    pageIndex = lastSession;
    sessionsListLoader(false)
  });
}

var markers = [];
var polypath;
function loadSession(sessionID) {
  const ref = firebase.database().ref('/' + userID() + '/sessions/' + sessionID);
  
  var graphData = [
    ["Worker", "Total Bags Collected"],
  ];
  
  const val = sessionForKey(sessionID).val;
  
  const start = new Date(val.start_date * 1000);
  const end = new Date(val.end_date * 1000);
  const wid = val.wid;
  const foreman = foremanForKey(wid);
  const fname = foreman.value.name + " " + foreman.value.surname;
  
  var sessionDetails = document.getElementById("sessionDetails");
  
  sessionDetails.innerHTML = "<form class=form-horizontal'><div class='form-group'>"
  sessionDetails.innerHTML += "<div class='col-sm-12'><label>Foreman: </label> " + fname + "</div>"
  sessionDetails.innerHTML += "<div class='col-sm-6'><label>Time Started: </label><p> " + start.toLocaleString() + "</p></div>"
  sessionDetails.innerHTML += "<div class='col-sm-6'><label>Time Ended: </label><p> " + end.toLocaleString() + "</p></div>"
  sessionDetails.innerHTML += "</div></form>";
  
  var first = true;
  
  if (val.track !== undefined) {
    var track = [];
    for (const ckey in val.track) {
      const coord = val.track[ckey];
      const loc = new google.maps.LatLng(coord.lat, coord.lng)
      track.push(loc);
      if (first) {
        map.setCenter(loc);
        map.setZoom(15);
        first = false;
      }
    }
    if (polypath !== undefined) {
      polypath.setMap(null);
    }
    polypath = new google.maps.Polyline({
      path: track,
      geodesic: true,
      strokeColor: '#0000FF',
      strokeOpacity: 1.0,
      strokeWeight: 2,
      map: map
    });
  }
  
  for (const marker in markers) {
    markers[marker].setMap(null)
  }
  
  if (val.collections !== undefined) {
    for (const ckey in val.collections) {
      const collection = val.collections[ckey];
      const worker = workerForKey(ckey);
      
      var wname = "";
      if (worker !== undefined) {
        wname = worker.value.name + " " + worker.value.surname;
      }
      
      graphData.push([wname, collection.length]);
      
      for (const pkey in collection) {
        const pickup = collection[pkey];
        const coord = new google.maps.LatLng(pickup.coord.lat, pickup.coord.lng);
        if (first) {
          map.setCenter(coord);
          first = false;
        }
        var marker = new google.maps.Marker({
          position: coord,
          map: map,
          title: wname
        });
        markers.push(marker);
      }
    }
  }
  initGraph(graphData);
}

function initGraph(collections) {
  var options = {
    title: 'Worker Performance Summary',
    pieHole: 0.5
  };
  
  var data = google.visualization.arrayToDataTable(collections);
  
  var doc = document.getElementById('doughnut');
  var chart = new google.visualization.PieChart(doc);
  chart.draw(data, options);
}
