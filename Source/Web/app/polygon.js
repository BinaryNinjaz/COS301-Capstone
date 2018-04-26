function minLat(polygon) {
  var result = Infinity;
  for (var i = 0; i < polygon.length; i++) {
    if (polygon[i].lat < result) {
      result= polygon[i].lat;
    }
  }
  return result;
}

function maxLat(polygon) {
  var result = -Infinity;
  for (var i = 0; i < polygon.length; i++) {
    if (polygon[i].lat > result) {
      result= polygon[i].lat;
    }
  }
  return result;
}

function minLng(polygon) {
  var result = Infinity;
  for (var i = 0; i < polygon.length; i++) {
    if (polygon[i].lng < result) {
      result= polygon[i].lng;
    }
  }
  return result;
}

function maxLng(polygon) {
  var result = -Infinity;
  for (var i = 0; i < polygon.length; i++) {
    if (polygon[i].lng > result) {
      result= polygon[i].lng;
    }
  }
  return result;
}

function cenLat(polygon) {
  var mn = minLat(polygon);
  var mx = maxLat(polygon);
  return mn + 0.5 * (mx - mn);
}

function cenLng(polygon) {
  var mn = minLng(polygon);
  var mx = maxLng(polygon);
  return mn + 0.5 * (mx - mn);
}

function disLat(polygon) {
  var mn = minLat(polygon);
  var mx = maxLat(polygon);
  return Math.abs(mx - mn);
}

function disLng(polygon) {
  var mn = minLng(polygon);
  var mx = maxLng(polygon);
  return Math.abs(mx - mn);
}

function bounds(polygon) {
  var bounds = new google.maps.LatLngBounds();
  for (var i = 0; i < polygon.length; i++) {
    bounds.extend(polygon[i]);
  }
  return bounds;
}