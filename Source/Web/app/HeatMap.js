var map;
function initMap() {
  navigator.geolocation.getCurrentPosition(function(loc) {
    map = new google.maps.Map(document.getElementById('map'), {
      center: {lat: loc.coords.latitude, lng: loc.coords.longitude },
      zoom: 14
    });
  });
  map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: -25, lng: 28 },
    zoom: 14,
    mapTypeId: 'terrain'
  });
}

const yieldsRef = firebase.database().ref('/yields');
function displayHeatMap() {
  yieldsRef.on('value', function(snapshot) {
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
    var heatmap = new google.maps.visualization.HeatmapLayer({
      data: locations,
      dissipating: false,
      map: map
    });
  });
}