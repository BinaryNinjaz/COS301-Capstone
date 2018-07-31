const functions = require('firebase-functions');
const admin = require('firebase-admin');
const cors = require('cors')({origin: true});
const moment = require('moment');
admin.initializeApp();

// ?startDate=[Double]&endDate=[Double]&uid=[String]
exports.sessionsWithinDates = functions.https.onRequest((req, res) => {
  cors(req, res, () => {
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
});

// ?pageNo=[Int]&pageSize=[Int? = 50]&uid=[String]
exports.flattendSessions = functions.https.onRequest((req, res) => {
  cors(req, res, () => {
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
});

// Point: [x: Float, y: Float]
// polygon: [Point]
// point: Point
function polygonContainsPoint(polygon, point) {
  if (polygon === undefined) {
    return false;
  }
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

function anyPolygonContainsPoint(polygons, point) {
  for (var polygon in polygons) {
    if (polygonContainsPoint(polygons[polygon], point)) {
      return true;
    }
  }
  return false;
}

function arrayContainsItem(array, item) {
  for (var a in array) {
    if (String(array[a]) === String(item)) {
      return true;
    }
  }
  return false;
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

// orchardIds: [String]
// uid: String
// completion: ([[Point]]) -> ()
function orchardPolygons(orchardIds, uid, completion) {
  const coords = admin.database().ref('/' + uid + '/orchards/');
  var result = [];
  coords.once('value').then((snapshot) => {
    snapshot.forEach((childSnapshot) => {
      if (arrayContainsItem(orchardIds, childSnapshot.key)) {
        const value = childSnapshot.val();
        const coords = value.coords;
        var poly = [];
        for (const k in coords) {
          poly.push({x: coords[k].lng, y: coords[k].lat});
        }
        result.push(poly);
      }
    });
    completion(result);
    return true;
  }).catch((error) => {});
}

function orchardsCooked(orchardIds, uid, completion) {
  const orchardsRef = admin.database().ref('/' + uid + '/orchards/');
  var result = [];
  orchardsRef.once('value').then((snapshot) => {
    snapshot.forEach((childSnapshot) => {
      if (arrayContainsItem(orchardIds, childSnapshot.key)) {
        const value = childSnapshot.val();
        const coords = value.coords;
        var poly = [];
        for (const k in coords) {
          poly.push({x: coords[k].lng, y: coords[k].lat});
        }
        result.push({polygon: poly, val: value, id: childSnapshot.key});
      }
    });
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
  cors(req, res, () => {
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
});

// -------- POST Body --------
//
// orchardId0=[String]
// orchardId1=[String]
// ...
// orchardIdN=[String]
//
// startDate=[Double]
// endDate=[Double]
// uid=[String]
exports.orchardCollectionsWithinDate = functions.https.onRequest((req, res) => {
  cors(req, res, () => {
    const startDate = req.body.startDate;
    const endDate = req.body.endDate;
    const uid = req.body.uid;
    
    var oids = [];
    for (var i = 0; i < Object.keys(req.body).length; i++) {
      const okey = "orchardId" + i;
      if (req.body[okey] !== undefined) {
        oids.push(req.body[okey]);
      }
    }
    
    var result = [];
    
    orchardPolygons(oids, uid, (polygons) => {
      var sessions = admin.database().ref('/' + uid + '/sessions');
      sessions.once('value').then((snapshot) => {
        snapshot.forEach((childSnapshot) => {
          const key = childSnapshot.key;
          const val = childSnapshot.val();
          if (startDate <= val.start_date && val.start_date <= endDate) {
            for (var ckey in val.collections) {
              const collection = val.collections[ckey];
              for (var pickup in collection) {
                const geopoint = collection[pickup].coord
                const point = {x: geopoint.lng, y: geopoint.lat}; 
                if (anyPolygonContainsPoint(polygons, point)) {
                  result.push(geopoint);
                }
              }
            }
          }
        });
        
        res.send(result);
        return true;
      }).catch((error) => {
        
      });
    });
  });
});

function roundDateToHour(timeinterval) {
  const date = new Date(timeinterval * 1000);
  return date.getUTCHours();
}

function roundDateToDay(timeinterval) {
  const date = new Date(timeinterval * 1000);
  const day = date.getUTCDay();
  switch (day) {
  case 0: return "Sunday";
  case 1: return "Monday";
  case 2: return "Tuesday";
  case 3: return "Wednesday";
  case 4: return "Thursday";
  case 5: return "Friday";
  case 6: return "Saturday";
  default: return "";
  }
}

function roundDateToWeek(timeinterval) {
  const date = new Date(timeinterval * 1000);
  var d = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
  d.setUTCDate(d.getUTCDate() + 4 - (d.getUTCDay()||7));
  var yearStart = new Date(Date.UTC(d.getUTCFullYear(),0,1));
  var weekNo = Math.ceil(( ( (d - yearStart) / 86400000) + 1)/7);
  return weekNo;
}

function roundDateToMonth(timeinterval) {
  const date = new Date(timeinterval * 1000);
  const month = date.getUTCMonth();
  switch (month) {
  case 0: return "January";
  case 1: return "February";
  case 2: return "March";
  case 3: return "April";
  case 4: return "May";
  case 5: return "June";
  case 6: return "July";
  case 7: return "August";
  case 8: return "September";
  case 9: return "October";
  case 10: return "November";
  case 11: return "December";
  default: return "";
  }
}

function roundDateToYear(timeinterval) {
  const date = new Date(timeinterval * 1000);
  const year = date.getUTCFullYear();
  return year
}

function roundDateToPeriod(timeinterval, period) {
  if (period === "hourly") {
    return roundDateToHour(timeinterval);
  } else if (period === "daily") {
    return roundDateToDay(timeinterval);
  } else if (period === "weekly") {
    return roundDateToWeek(timeinterval);
  } else if (period === "monthly") {
    return roundDateToMonth(timeinterval);
  } else if (period === "yearly") {
    return roundDateToYear(timeinterval);
  } else {
    return "";
  }
}

function incrSessionCounter(counter, key, accum) {
  if (key === "" || accum === "") {
    return;
  }
  
  if (counter[key] !== undefined) {
    if (counter[key][accum] !== undefined) {
      counter[key][accum] += 1;
    } else {
      counter[key][accum] = 1;
    }
  } else {
    counter[key] = {};
    counter[key][accum] = 1;
  }
}

function updateDaysCounter(days, key, accum, period, pickedDate) {
  const date = new Date(pickedDate * 1000);
  const grouper = period === "weekly"
    ? moment(date).startOf('week')
    : period === "monthly"
      ? moment(date).startOf('month')
      : period === "yearly"
        ? moment(date).startOf('year')
        : moment(date).startOf('day');
  if (days[key] === undefined) {
    days[key] = {};
  }
  if (days[key][accum] === undefined) {
    days[key][accum] = {};
  }
  if (days[key][accum][grouper] === undefined) {
    days[key][accum][grouper] = 1;
  }
}

function averageOfSessionCounter(counter, days) {
  var result = {};
  var keys = Object.keys(counter);
  for (const ikey in keys) {
    const key = keys[ikey];
    const accums = Object.keys(counter[key]);
    for (const iaccum in accums) {
      const accum = accums[iaccum];
      if (result[key] === undefined) {
        result[key] = {};
      }
      var len = Object.keys(days[key][accum]).length;
      if (len === undefined || len === null) { len = 1; }
      result[key][accum] = counter[key][accum] / len;
    }
  }
  return result;
}

// -------- POST Body --------
//
// id0=[String]
// id1=[String]
// ...
// idN=[String]
//
// groupBy=[worker, orchard, foreman]
// period=[hourly, daily, weekly, monthly, yearly]
// startDate=[Double]
// endDate=[Double]
// avgRange=[all, inclusive, onlybefore] default = onlybefore
// uid=[String]
//
// --------- Result -------------
// let p = {id0: {*: #, *: #, ...}, id1: {*: #, *: #, ...}, ...}
// where * is some values determined by period hourly = [0, 23], daily=[Sunday, ..., Saturday]
// weekly = [0, 52], monthly = [January, ..., December], yearly = [0, Int.max)
// # is total number of bags collected
//
// result = {avg: p, p}
exports.timedGraphSessions = functions.https.onRequest((req, res) => {
  cors(req, res, () => {
    const startDate = req.body.startDate;
    const endDate = req.body.endDate;
    const uid = req.body.uid;
    const groupBy = req.body.groupBy;
    const period = req.body.period;
    var avgRange = req.body.avgRange;
    if (avgRange === undefined) {
      avgRange = "onlybefore";
    }
    
    var result = {};
    var all = {};
    var days = {};
    
    var ids = [];
    for (var i = 0; i < Object.keys(req.body).length; i++) {
      const ikey = "id" + i;
      if (req.body[ikey] !== undefined) {
        ids.push(req.body[ikey]);
        result[req.body[ikey]] = {};
      }
    }
    
    if (groupBy !== "orchard") {
      var sessionsRef = admin.database().ref('/' + uid + '/sessions');
      sessionsRef.once("value").then((snapshot) => {
        snapshot.forEach((childSnapshot) => {
          const key = childSnapshot.key;
          const val = childSnapshot.val();
          
          const foremanKey = val.wid;
          for (const workerKey in val.collections) {
            if (!(groupBy === "foreman" && arrayContainsItem(ids, foremanKey)
            || groupBy === "worker" && arrayContainsItem(ids, workerKey))) {
              continue;
            }
            const collection = val.collections[workerKey];
            for (const pickupKey in collection) {
              const pickup = collection[pickupKey];
              const accum = roundDateToPeriod(pickup.date, period);
              const wkey = groupBy === "foreman" ? foremanKey : workerKey;
              if (startDate <= pickup.date && pickup.date <= endDate) {
                incrSessionCounter(result, wkey, accum);
              }
              
              if (avgRange === "all") {
                incrSessionCounter(all, wkey, accum);
                updateDaysCounter(days, wkey, accum, period, pickup.date);
              } else if (avgRange === "inclusive" && pickup.date <= endDate) {
                incrSessionCounter(all, wkey, accum);
                updateDaysCounter(days, wkey, accum, period, pickup.date);
              } else if (avgRange === "onlybefore" && pickup.date <= startDate) {
                incrSessionCounter(all, wkey, accum);
                updateDaysCounter(days, wkey, accum, period, pickup.date);
              }
            }
          }
        });
        result["avg"] = averageOfSessionCounter(all, days);
        res.send(result);
        return true;
      }).catch((err) => {
        console.log(err);
      });
    } else {
      orchardsCooked(ids, uid, (cookedOrchards) => {
        var sessionsRef = admin.database().ref('/' + uid + '/sessions');
        sessionsRef.once("value").then((snapshot) => {
          snapshot.forEach((childSnapshot) => {
            const key = childSnapshot.key;
            const val = childSnapshot.val();
            
            for (const workerKey in val.collections) {
              const collection = val.collections[workerKey];
              
              for (const pickupKey in collection) {
                const pickup = collection[pickupKey];
                const accum = roundDateToPeriod(val.start_date, period);
                const pnt = {x: pickup.coord.lng, y: pickup.coord.lat};
                
                var orckey = undefined;
                for (const okey in cookedOrchards) {
                  if (polygonContainsPoint(cookedOrchards[okey].polygon, pnt)) {
                    orckey = cookedOrchards[okey].id;
                    break;
                  }
                }
                if (orckey === undefined) {
                  continue;
                }
                if (startDate <= pickup.date && pickup.date <= endDate) {
                  incrSessionCounter(result, orckey, accum);
                }
                
                if (avgRange === "all") {
                  incrSessionCounter(all, orckey, accum);
                  updateDaysCounter(days, orckey, accum, period, pickup.date);
                } else if (avgRange === "inclusive" && pickup.date <= endDate) {
                  incrSessionCounter(all, orckey, accum);
                  updateDaysCounter(days, orckey, accum, period, pickup.date);
                } else if (avgRange === "onlybefore" && pickup.date <= startDate) {
                  incrSessionCounter(all, orckey, accum);
                  updateDaysCounter(days, orckey, accum, period, pickup.date);
                }
              }
            }
          });
          result["avg"] = averageOfSessionCounter(all, days);
          res.send(result);
          return true;
        }).catch((err) => {
          console.log(err);
        });
      });
    }
  });
});