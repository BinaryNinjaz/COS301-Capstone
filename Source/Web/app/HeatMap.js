const user = function() { return firebase.auth().currentUser };
const userID = function() {
  if (user() !== null) {
    return user().uid 
  } else {
    return ""
  }
}
function yieldsRef() {
  return firebase.database().ref('/' + userID() + '/sessions');
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
  displayHeatMap();
}

function displayHeatMap() {
  yieldsRef().off();
  yieldsRef().on('value', function(snapshot) {
    locations = [];
    snapshot.forEach(function (child) {
      let cols = child.val().collections;
      if (cols !== undefined) {
        for (var key in cols) {
          var coords = cols[key];
          for (var i = 0; i < coords.length; i++) {
            var coord = coords[i].coord;
            var latLng = new google.maps.LatLng(coord.lat, coord.lng);
            locations.push(latLng);
          }
        }
      }
    });
    var g = [
    'rgba(0, 0, 0, 0)',
    'rgba(0, 128, 255, 0.7)',
    'rgba(0, 64, 255, 0.7)',
    'rgba(0, 0, 255, 0.7)',]
    var heatmap = new google.maps.visualization.HeatmapLayer({
      data: locations,
      dissipating: true,
      gradient: g,
      radius: 50,
      map: map
    });
  });
}