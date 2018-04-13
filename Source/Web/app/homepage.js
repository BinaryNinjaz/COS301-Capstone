const locationsRef = firebase.database().ref('/locations');
var locations = [];

var map;
function initMap() {
  navigator.geolocation.getCurrentPosition(function(loc) {
    map = new google.maps.Map(document.getElementById('map'), {
      center: {lat: loc.coords.latitude, lng: loc.coords.longitude },
      zoom: 14
    });
  });
}

function displayForemanLocation() {
  locationsRef.on('value', function(snapshot) {
    locations = [];
    snapshot.forEach(function (child) {
      let loc = child.val();
      
      var marker = new google.maps.Marker({
        position: loc.coord,
        map: map
      });
    });
  });
}
displayForemanLocation();