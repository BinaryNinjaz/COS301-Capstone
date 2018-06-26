const functions = require('firebase-functions');
const admin = require('firebase-admin');
//const cors = require('cors')({origin: true});
admin.initializeApp();

// ?startDate=[Double]&endDate=[Double]&uid=[String]
exports.sessionsWithinDates = functions.https.onRequest((req, res) => {
  const startDate = req.query.startDate;
  const endDate = req.query.endDate;
  const uid = req.query.uid;
  
  var result = {};
  
  var sessions = admin.database().ref('/' + uid + '/sessions');
  sessions.once('value').then((snapshot) => {
    snapshot.forEach((childSnapshot) => {
      const key = childSnapshot.key;
      const val = childSnapshot.val();
      
      if (startDate <= val.start_date && val.start_date <= endDate) {
        result[key] = val;
      }
    });
    res.send(result);
    return true;
  }).catch((error) => {
    
  });
});

// ?pageNo=[Int]&pageSize=[Int? = 50]&uid=[String]
exports.flattendSessions = functions.https.onRequest((req, res) => {
  const pageNo = req.query.pageNo;
  var pageSize = req.query.pageSize;
  if (pageSize === undefined) {
    pageSize = 50;
  }
  const uid = req.query.uid;
  
  var result = [];
  var count = 0;
  var sessions = admin.database().ref('/' + uid + '/sessions').orderByChild('start_date');
  sessions.once('value').then((snapshot) => {
    const total = snapshot.numChildren();
    
    snapshot.forEach((childSnapshot) => {
      if (count >= total - (pageNo - 1) * pageSize) {
        res.send(result);
        return;
      } else if (count >= total - pageNo * pageSize) {
        const key = childSnapshot.key;
        const val = childSnapshot.val();
        result.unshift({key: key, start_date: val.start_date, wid: val.wid});
      } 
      count++;
    });
    res.send(result);
    return true;
  }).catch((error) => {});
});

// Point: [x: Float, y: Float]
// polygon: [Point]
// point: Point
function polygonContainsPoint(polygon, point) {
  var i = 0;
  var j = polygon.length - 1;
  var c = false
  for (; i < polygon.length; j = i++) {
    const yValid = (polygon[i].y > point.y) !== (polygon[j].y > point.y)
    const xValidCond = (polygon[j].x - polygon[i].x) * (point.y - polygon[i].y) / (polygon[j].y - polygon[i].y) + polygon[i].x;
    
    if (yValid && point.x < xValidCond) {
      c = !c;
    }
  }
  return c;
}

// orchardId: String
// uid: String
// completion: ([Point]) -> ()
function orchardPolygon(orchardId, uid, completion) {
  const coords = admin.database().ref('/' + uid + '/orchards/' + orchardId);
  var result = [];
  coords.once('value').then((snapshot) => {
    const value = snapshot.val();
    const coords = value.coords;
    for (const k in coords) {
      result.push({x: coords[k].lng, y: coords[k].lat});
    }
    completion(result);
    return true;
  }).catch((error) => {});
}

function roundToDaysSince1970(timeinterval) {
  timeinterval -= timeinterval % 86400;
  return timeinterval / 86400.0;
}

// groups all collections by day into summation, from collections that are in polygon.
function summationOfCollections(summation, collections, polygon) {
  for (const wkey in collections) {
    for (const ckey in collections[wkey]) {
      const collection = collections[wkey][ckey];
      const xyCoord = {x: collection.coord.lng, y: collection.coord.lat};
      if (polygonContainsPoint(polygon, xyCoord)) {
        const day = roundToDaysSince1970(collection.date);
        if (summation[day] === undefined) {
          summation[day] = 1;
        } else {
          summation[day] += 1;
        }
      }
    }
  }
}

function highest(summation) {
  var max = 0;
  for (const key in summation) {
    if (max < summation[key]) {
      max = summation[key];
    }
  }
  return max;
}

function lowest(summation) {
  var min = Infinity;
  for (const key in summation) {
    if (min > summation[key]) {
      min = summation[key];
    }
  }
  return min;
}

function sinusoidalRegression(data, x) {
  const period = 365.25;
  const high = highest(data);
  const low = lowest(data);
  const d = (high + low) / 2;
  const a = high - d;
  const b = 2.0 * Math.PI / period;
  const c = Math.asin((low - d) / a) - b;
  
  const y = a * Math.sin(b * x + c) + d;
  
  return {
    definition: {
      a: a,
      b: b,
      c: c,
      d: d,
    },
    expected: y
  }
}


// ?orchardId=[String]&date=[Double]&uid=[String]
exports.expectedYield = functions.https.onRequest((req, res) => {
  const orchardId = req.query.orchardId;
  const timeinterval = req.query.date;
  const uid = req.query.uid;
  
  orchardPolygon(orchardId, uid, (polygon) => {
    const sessions = admin.database().ref('/' + uid + '/sessions');
    sessions.once('value').then((snapshot) => {
      var summation = {};
      snapshot.forEach((childSnapshot) => {
        const val = childSnapshot.val();
        const collections = val.collections;
        summationOfCollections(summation, collections, polygon);
      });
      res.send(sinusoidalRegression(summation, roundToDaysSince1970(timeinterval)));
      return true;
    }).catch((error) => {});
  });
});
