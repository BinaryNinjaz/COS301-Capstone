/*!
*  	File:   homepage.js
*	Author: Binary Ninjaz (Teboho, Sizo, Kevin)
*
*	Description: Contains all functions required for the home page interface.
*/
var markers = [];
///This function is a get function, it returns the location of the user ID, from the database
function locationsRef() {
  return firebase.database().ref('/' + userID() + '/locations');
}

$(window).bind("load", function() {
  let succ = () => {
    updateSpiner(true);
    initForemen();
    initMap();
  };
  let fail = () => {
  };
  retryUntilTimeout(succ, fail, 1000);
});

var foremen = []; /* This is an array of tracked formen */

///This function functions allows the use to narrow down the number of foreman being tracked
function changeSelection(checkbox) {
  for (var i = 0; i < foremen.length; i++) {
    if (foremen[i].key === checkbox.value) {
      if (checkbox.checked !== foremen[i].beingTracked) {
        foremen[i].beingTracked = checkbox.checked;
        updateMarkers();
      }
    }
  }
}
///This function displays the changes in the tick boxes, of the names of the foreman being tracked
function createForemenSelectionButton(name, key) {
  result = '';
  result += '<div class="checkbox">';
  result += '<label><input type="checkbox" onchange="changeSelection(this)" checked value="' + key + '">' + name + '</label>';
  result += '</div>';

  return result;
}

///This function initiates the available foreman for tracking. Each checkbox for each farmer will be ticked for tracking
function initForemen() {
  getWorkers((workersSnap) => {
    var foremenDiv = document.getElementById("foremen");
    workers = {};
    workersSnap.forEach((worker) => {
      const w = worker.val();
      const k = worker.key;
      if (w.type === "Foreman") {
        foremen.push({key: k, value: w, beingTracked: true});
        const name = w.name + " " + w.surname;
        foremenDiv.innerHTML += createForemenSelectionButton(name, k);
      }
    });
    updateSpiner(false);
    displayForemanLocation();
    requestLocations();
  });
  setInterval(requestLocations, 1000 * 60 * 5);
}

///This function requests location of the foreman from the foreman's phone. The phone will delete this request after it was granted
function requestLocations() {
  for (var i = 0; i < foremen.length; i++) {
    if (foremen[i].beingTracked) {
      const reqRef = firebase.database().ref('/' + userID() + '/requestedLocations/' + foremen[i].key);
      reqRef.set({0: true});
    }
  }
}

var locations = []; /* This is an array of locations for the map */
var map; /* This variable will be used as a container for drawing on google maps */

///This function initialises the points which initially appear on the map
function initMap() {
  locationLookup((data) => {
    var latLng = new google.maps.LatLng(data.lat, data.lon);
    map = new google.maps.Map(document.getElementById('map'), {
      center: latLng,
      zoom: 11,
      mapTypeId: 'satellite'
    });
  });
}

///This function returns the name of each foreman
function initials(name) {
  var f = name[0];
  var e = " ";
  for (var i = 0; i < name.length; i++) {
    if (name[i] === " " && i < name.length - 1) {
      e = name[i + 1];
    }
  }
  return f + e
}

function clearMarkers() {
  while (markers.length > 0) {
    let m = markers.pop()
    m.setMap(null);
  }
}

function updateMarkers() {
  clearMarkers();

  for (var i = 0; i < locations.length; i++) {
    const loc = locations[i].value;
    if (shouldShowForeman(locations[i].key)) {
      let date = displayDate(loc.date);
      var marker = new google.maps.Marker({
        position: loc.coord,
        map: map,
        title: loc.display,
        label: initials(loc.display)
      });
      marker.setTitle(loc.display + " - " + date);
      markers.push(marker);
    }
  }
}

function displayDate(datestring) {
  let date = moment(datestring);
  let today = moment(new Date());

  const fmtYear = date.format('YYYY') === today.format('YYYY') ? "" : "YYYY ";
  const fmtMonth = date.format('YYYY-MM') === today.format('YYYY-MM') ? "" : "MMM ";
  var fmtWeek = date.format('YYYY W') === today.format('YYYY W') ? "" : "ddd ";
  var fmtDay = date.format('YYYY-MM-DD') === today.format('YYYY-MM-DD') ? "" : "DD ";

  fmtWeek = (fmtMonth === "" ? fmtWeek : "");
  fmtDay = (fmtWeek === "" ? fmtDay : "");

  const fmt = fmtYear + fmtMonth + fmtWeek + fmtDay + "HH:mm";

  return date.format(fmt);
}

function shouldShowForeman(fID) {
  for (var i = 0; i < foremen.length; i++) {
    if (foremen[i].key === fID) {
      return foremen[i].beingTracked;
    }
  }
  return true;
}

var first = true;
function displayForemanLocation() {
  let locRef = firebase.database().ref('/' + userID() + '/locations');
  locRef.off();
  locRef.on('value', function(snapshot) {
    clearMarkers();
    locations = [];
    snapshot.forEach(function (child) {
      let loc = child.val();
      locations.push({value: loc, key: child.key});
      if (shouldShowForeman(child.key)) {
        let date = displayDate(loc.date);
        if (first) {
          first = false;
          map.setCenter(loc.coord);
          map.setZoom(15);
        }
        var marker = new google.maps.Marker({
          position: loc.coord,
          map: map,
          title: loc.display,
          label: initials(loc.display)
        });
        marker.setTitle(loc.display + " - " + date);
        markers.push(marker);
      }
    });
  });
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

  var target = document.getElementById('trackerFilter');
  var button = document.getElementById('updateButton');
  if (shouldSpin) {
    if (spinner == null) {
      spinner = new Spinner(opts).spin(target);
      button.style.visibility = "hidden";
    }
  } else {
    button.style.visibility = "visible";
    spinner.stop();
    spinner = null;
  }
}
