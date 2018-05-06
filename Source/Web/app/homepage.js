const user = function() { return firebase.auth().currentUser };
const userID = function() { 
  if (user() !== null) {
    return user().uid
  } else {
    return ""
  }
}

var markers = [];

function locationsRef() {
  return firebase.database().ref('/' + userID() + '/locations');
}

firebase.auth().onAuthStateChanged(function (user) {
  if (user) {
    initMap();
  }
});

var locations = [];

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
  displayForemanLocation();
}

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

function displayDate(timestamp) {
  let date = new Date(timestamp * 1000);
  let today = new Date();
  
  let Y = date.getYear();
  let M = date.getMonth();
  let D = date.getDate();
  
  let dw = date.getDay();
  
  let h = date.getHours();
  let m = date.getMinutes();
  
  var t = h + ":" + m;
  
  // FIXME doesn't work must research js date stuff >:(
  // Must show more date info progressivly as needed
  /*
  if (Y < today.getYear()) {
    t = Y + "/" + M + "/" + D + " " + t;
  } else if (M < today.getMonth() || D < today.getDate()) {
    t = M + "/" + D + " " + t;
  }
  */
  
  return t;
}

function displayForemanLocation() {
  let locRef = firebase.database().ref('/' + userID() + '/locations');
  locRef.off();
  locRef.on('value', function(snapshot) {
    clearMarkers();
    locations = [];
    snapshot.forEach(function (child) {
      let loc = child.val();
      let date = displayDate(loc.date);
      var marker = new google.maps.Marker({
        position: loc.coord,
        map: map,
        title: loc.display,
        label: initials(loc.display)
      });
      marker.setTitle(loc.display + " - " + date);
      markers.push(marker);
    });
  });
}
