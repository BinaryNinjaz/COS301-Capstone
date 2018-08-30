/*
* 	File:	Sessions.js
*	Author:	Binary Ninjaz (Letanyan,Ojo)
*
*	Description:	This file contais functions for the data representation on
*					"Sessions.html". It requests and recieves data from firebase
*					databse, and uses google graph APIs
*/
var pageIndex = null; // track the last session loaded. Used for pagination
var pageSize = 21;
$(window).bind("load", () => {
	var divHide = document.getElementById('loader'); /* When the page loads, the error div should be hidden */
	divHide.style.visibility = "hidden"; /* When the page loads, the error div should be hidden, do not remove */
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

/* This function initiates the coordinates on the map*/
var map;
function initMap() {
  locationLookup((data, response) => {
    var latLng = new google.maps.LatLng(data.lat, data.lon);
		map = new google.maps.Map(document.getElementById('map'), {
	    center: latLng,
	    zoom: 11,
	    mapTypeId: 'satellite'
	  });
  });
}

/* Function returns a session, given a particular key */
function sessionForKey(key, sortedMap) {
  for (const groupIdx in sortedMap) {
    const group = sortedMap[groupIdx];
    for (const itemIdx in group.values) {
      const item = group.values[itemIdx];
      if (item.key === key) {
        return item;
      }
    }
  }
  return undefined;
}

/* Function loads a list of sessions on the side of the screen */
function sessionsListLoader(loading) {
  var sessionsListHolder = document.getElementById("sessionsListLoader");
  if (!loading) {
		var divHide = document.getElementById('loader');
		divHide.style.visibility = "hidden";
		updateSpiner(false);
    sessionsListHolder.innerHTML = "<button type='button' class='btn btn-sm btn-secoundary' style='margin: 4px' onclick='newPage()'>Load More Sessions</button>";
  } else {
		var divHide = document.getElementById('loader');
		divHide.style.visibility = "visible";
		updateSpiner(true);
  }
}

var farms = {};
var orchards = {};
var workers = {};
function initPage() {
  var sessionsList = document.getElementById("sessionsList");
  sessionsListLoader(true);
  farms = {};
  orchards = {};
  workers = {};
  setWorkers(workers, () => {
    newPage();
    setFarms(farms, () => {});
    setOrchards(orchards, () => {
			drawOrchards();
		});
  });
}

// sorted map
var sessions = [];
var filteredSessions = [];
function insertSessionIntoSortedMap(session, key, checkEqualKey, sortedMap) {
  var belongsInGroup = undefined;
  for (const groupIdx in sortedMap) {
    const group = sortedMap[groupIdx];
    if (checkEqualKey(group.key, key)) {
      belongsInGroup = groupIdx;
      break;
    }
  }

  if (belongsInGroup !== undefined) {
    sortedMap[belongsInGroup].values.push(session);
    sortedMap[belongsInGroup].values = sortedMap[belongsInGroup].values.sort((a, b) => {
			const ma = moment(a.value.start_date);
			const mb = moment(b.value.start_date);
      return ma.isSame(mb)
				? 0
				: ma.isAfter(mb) ? -1 : 1;
    });
  } else {
    sortedMap.push({key: key, values: [session]});
  }

  sortedMap = sortedMap.sort((a, b) => {
    return b.key - a.key;
  });
}

function displaySessions(sortedMap, displayHeader, isFiltered) {
  var sessionsList = document.getElementById("sessionsList");
  sessionsList.innerHTML = "";
  for (const groupIdx in sortedMap) {
    const group = sortedMap[groupIdx];
    const key = group.key;
    sessionsList.innerHTML += "<h5>" + displayHeader(key) + "</h5>";
    for (const itemIdx in group.values) {
      const item = group.values[itemIdx];
      const foreman = workers[item.value.wid];
      const time = moment(item.value.start_date).format(isFiltered ? "YYYY/MM/DD HH:mm" : "HH:mm");
      const text = foreman.name + " " + foreman.surname + " - " + time;
      sessionsList.innerHTML += "<button type='button' class='btn btn-sm btn-info' style='margin: 4px' onclick=loadSession('" + item.key + "') >" + text + "</button>";
      if (isFiltered && item.reason !== "") {
        sessionsList.innerHTML += "<p class='searchReason'>" + item.reason + "</p>";
      }
    }
  }
}

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
    var i = 0;
    snapshot.forEach((child) => {
      const obj = child.val();
      const foreman = workers[obj.wid];
      if (foreman !== undefined) {
        if (lastSession === "") {
          lastSession = child.key;
        }
        const session = {value: obj, key: child.key};

        const key = moment(session.value.start_date).startOf('day');
        const equalDates = (a, b) => {
          return a.isSame(b);
        };

        insertSessionIntoSortedMap(session, key, equalDates, sessions);
      }
    });

    pageIndex = lastSession;
    sessionsListLoader(false);
    const formatHeader = (date) => {
      return date.format("dddd, DD MMMM YYYY");
    };
    filterSessions();
  });
}

var markers = []; /* An array of markers for the map */
var polypath; /* Variable for storing the path of the polygon */

/* This functions plots the graph of a choosen session by a particular foreman */
function loadSession(sessionID) {
  var gdatai = 0;
  var graphData = {datasets: [{data: [], backgroundColor: []}], labels: []};

  const session = sessionForKey(sessionID, sessions);
  const val = session.value;

  const start = moment(val.start_date);
  const end = moment(val.end_date);
  const wid = val.wid;
  const foreman = workers[wid];
  const fname = foreman.name + " " + foreman.surname;

  var sessionDetails = document.getElementById("sessionDetails");

  sessionDetails.innerHTML = "<form class=form-horizontal'><div class='form-group'>"
  sessionDetails.innerHTML += "<div class='col-sm-12'><label>Foreman: </label><span> " + fname + "</span></div>"
  sessionDetails.innerHTML += "<div class='col-sm-6'><label>Time Started: </label><p> " + start.format("dddd, DD MMMM YYYY HH:mm") + "</p></div>"
  sessionDetails.innerHTML += "<div class='col-sm-6'><label>Time Ended: </label><p> " + end.format("dddd, DD MMMM YYYY HH:mm") + "</p></div>"
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
      strokeColor: '#FF0000',
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
      const worker = workers[ckey];

      var wname = "";
      if (worker !== undefined) {
        wname = worker.name + " " + worker.surname;
      }

      graphData.datasets[0].data.push(collection.length);
      graphData.datasets[0].backgroundColor.push(colorForIndex(gdatai));
      graphData["labels"].push(wname);
      gdatai++;

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

/* This function (is a subfunction) simply displays the doughnut graph */
var chart;
function initGraph(collections) {
  if (chart !== undefined) {
    chart.destroy();
  }

  var options = {
    title: {
      display: true,
      text: "Worker Performance Summary",
			fontColor: 'white'
    },
    legend: {
			labels: {
				fontColor: "white"
			},
			position: 'right'
    }
  };
  var ctx = document.getElementById("doughnut").getContext('2d');
  chart = null;

  chart = new Chart(ctx,{
    type: 'doughnut',
    data: collections,
    options: options
  });
}

var orchardPolygons = [];
function drawOrchards() {
	for (const idx in orchardPolygons) {
		orchardPolygons[idx].setMap(null);
	}

	for (const oKey in orchards) {
		var coords = [];
		const orchard = orchards[oKey];
		const oCoords = orchard.coords;
		for (const cidx in oCoords) {
			coords.push({lat: oCoords[cidx].lat, lng: oCoords[cidx].lng});
		}
		orchardPolygons.push(new google.maps.Polygon({
	    paths: coords,
	    strokeColor: hashColor(orchard.farm, oKey),
	    strokeOpacity: 0.75,
	    strokeWeight: 3,
	    fillColor: hashColor(orchard.farm, oKey),
	    fillOpacity: 0.25,
	    map: map
	  }));
	}
}

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
	  spinner.stop();
	  spinner = null;
  }
}

function filterSessions() {
  const searchField = document.getElementById("sessionSearchField");
  const searchText = searchField.value;

  if (searchText === "") {
    const formatHeader = (date) => {
      return date.format("dddd, DD MMMM YYYY");
    };
    displaySessions(sessions, formatHeader, false);
  } else {
    filteredSessions = []
    for (const groupKey in sessions) {
      const group = sessions[groupKey].values;

      for (const sessionId in group) {
        const session = group[sessionId];
        const sessionResults = querySession(session.value, searchText, farms, orchards, workers);
        for (const key in sessionResults) {
					var newSession = Object.assign({}, session);
          newSession["reason"] = sessionResults[key];
          insertSessionIntoSortedMap(newSession, key, (a, b) => { return a === b; }, filteredSessions);
        }
      }
    }

    const formatHeader = (title) => { return title };

    displaySessions(filteredSessions, formatHeader, true);
  }
}
