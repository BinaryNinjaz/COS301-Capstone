const locationsRef = firebase.database().ref('/locations');
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

function displayForemanLocation() {
  locationsRef.off();
  locationsRef.on('value', function(snapshot) {
    locations = [];
    snapshot.forEach(function (child) {
      let loc = child.val();
      
      var marker = new google.maps.Marker({
        position: loc.coord,
        map: map,
        title: loc.display,
        label: initials(loc.display)
      });
      marker.setTitle(loc.display);
    });
  });
}
