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

function anyOrchardContainsPoint(orchards, point) {
  for (var orchard in orchards) {
    if (polygonContainsPoint(orchards[orchard].polygon, point)) {
      return orchards[orchard].val;
    }
  }
  return undefined;
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
    const val = snapshot.val();
    const coords = val.coords;
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
      if (orchardIds === undefined || arrayContainsItem(orchardIds, childSnapshot.key)) {
        const val = childSnapshot.val();
        const coords = val.coords;
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
      if (orchardIds === undefined || arrayContainsItem(orchardIds, childSnapshot.key)) {
        const val = childSnapshot.val();
        const coords = val.coords;
        var poly = [];
        for (const k in coords) {
          poly.push({x: coords[k].lng, y: coords[k].lat});
        }
        result.push({polygon: poly, val: val, id: childSnapshot.key});
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
// id0=[String]
// id1=[String]
// ...
// idN=[String]
//
// groupBy=[worker, orchard, foreman, farm]
// startDate=[Double]
// endDate=[Double]
// uid=[String]
exports.collectionsWithinDate = functions.https.onRequest((req, res) => {
  cors(req, res, () => {
    const startDate = req.body.startDate;
    const endDate = req.body.endDate;
    const uid = req.body.uid;
    var groupBy = req.body.groupBy;
    if (groupBy === undefined) {
      groupBy = "orchard"
    }
    
    var ids = [];
    for (var i = 0; i < Object.keys(req.body).length; i++) {
      const key = "id" + i;
      if (req.body[key] !== undefined) {
        ids.push(req.body[key]);
      }
    }
    
    var result = [];
    
    var sessions = admin.database().ref('/' + uid + '/sessions');
    
    if (groupBy === "orchard") {
      orchardPolygons(ids, uid, (polygons) => {
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
          console.log(error);
        });
      });
    } else if (groupBy === "worker") {
      sessions.once('value').then((snapshot) => {
        snapshot.forEach((childSnapshot) => {
          const key = childSnapshot.key;
          const val = childSnapshot.val();
          if (startDate <= val.start_date && val.start_date <= endDate) {
            for (var ckey in val.collections) {
              const collection = val.collections[ckey];
              if (!arrayContainsItem(ids, ckey)) {
                continue;
              }
              for (var pickup in collection) {
                const geopoint = collection[pickup].coord
                const point = {x: geopoint.lng, y: geopoint.lat}; 
                result.push(geopoint);
              }
            }
          }
        });
        
        res.send(result);
        return true;
      }).catch((error) => {
        console.log(error);
      });
    } else if (groupBy === "foreman") {
      sessions.once('value').then((snapshot) => {
        snapshot.forEach((childSnapshot) => {
          const key = childSnapshot.key;
          const val = childSnapshot.val();
          console.log(val.wid + " " + JSON.stringify(ids));
          if (arrayContainsItem(ids, val.wid) && startDate <= val.start_date && val.start_date <= endDate) {
            for (var ckey in val.collections) {
              const collection = val.collections[ckey];
              for (var pickup in collection) {
                const geopoint = collection[pickup].coord
                const point = {x: geopoint.lng, y: geopoint.lat}; 
                result.push(geopoint);
              }
            }
          }
        });
        
        res.send(result);
        return true;
      }).catch((error) => {
        console.log(error);
      });
    } else if (groupBy === "farm") {
      orchardsCooked(undefined, uid, (orchards) => {
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
                  const o = anyOrchardContainsPoint(orchards, point);
                  if (o === undefined || !arrayContainsItem(ids, o.farm)) {
                    continue;
                  }
                  result.push(geopoint);
                }
              }
            }
          });
          res.send(result);
          return true;
        }).catch((error) => {
          console.log(error);
        });
      });
    }
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

function isSameYear(d1, d2) {
  return moment(d1).startOf('year').format('YYYY') === moment(d2).startOf('year').format('YYYY');
}

function isSameMonth(d1, d2) {
  return moment(d1).startOf('month').format('YYYY-MM') === moment(d2).startOf('month').format('YYYY-MM');
}

function isSameDay(d1, d2) {
  return moment(d1).startOf('day').format('YYYY-MM-DD') === moment(d2).startOf('day').format('YYYY-MM-DD');
}

function roundDateToRunningPeriod(timeinterval, period, sameYear, sameMonth, sameDay) {
  const fmtYear = sameYear ? '' : 'YYYY ';
  const fmtMonth = sameMonth ? '' : 'MMM ';
  const fmtDay = sameDay ? '' : 'DD';
  const fmt = fmtYear + fmtMonth + fmtDay;
  const date = new Date(timeinterval * 1000);
  if (period === "hourly") {
    return moment(date).startOf('hour').format(fmt + ' HH:mm');
  } else if (period === "daily") {
    return moment(date).startOf('day').format(fmt === '' ? 'ddd' : fmt);
  } else if (period === "weekly") {
    return moment(date).startOf('week').format(fmt === '' ? 'ddd' : fmt);
  } else if (period === "monthly") {
    return moment(date).startOf('month').format(fmtYear + ' MMM');
  } else if (period === "yearly") {
    return moment(date).startOf('day').format('YYYY');
  } else {
    return '';
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

function updateDaysCounter(days, entities, key, accum, period, pickedDate) {
  const date = new Date(pickedDate * 1000);
  const grouper = period === "weekly"
    ? moment(date).startOf('week')
    : period === "monthly"
      ? moment(date).startOf('month')
      : period === "yearly"
        ? moment(date).startOf('year')
        : moment(date).startOf('day');
  if (days[accum] === undefined) {
    days[accum] = {};
  }
  if (days[accum][grouper] === undefined) {
    days[accum][grouper] = 1;
  }
  if (entities[accum] === undefined) {
    entities[accum] = {};
  }
  if (entities[accum][key] === undefined) {
    entities[accum][key] = 1;
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

function averageOfSessionItem(item, key, days, workingOn) {
  var result = {};
  const accums = Object.keys(item);
  for (const iaccum in accums) {
    const accum = accums[iaccum];
    if (result[accum] === undefined) {
      result[accum] = {};
    }
    var len = Object.keys(days[accum]).length;
    if (len === undefined || len === null) { len = 1; }
    var cnt = Object.keys(workingOn[accum]).length;
    if (cnt === undefined || cnt === null) { cnt = 1; }
    result[accum] = (item[accum] / len) / cnt;
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
// groupBy=[worker, orchard, foreman, farm]
// period=[hourly, daily, weekly, monthly, yearly]
// startDate=[Double]
// endDate=[Double]
// mode=[accumTime, accumEntity, running]
// uid=[String]
//
// --------- Result ------------- Mode = accumTime
// result = {avg: {*: #}, p}
//
// where p = {id0: {*: #, *: #, ...}, id1: {*: #, *: #, ...}, ...}
// where * is some values determined by period hourly = [0, 23], daily=[Sunday, ..., Saturday]
// weekly = [0, 52], monthly = [January, ..., December], yearly = [0, Int.max)
// # is total number of bags collected
//
// --------- Result ------------- Mode = accumEntity
// result = {avg: {*: #, ...}, sum: {*: #, ...}}
//
// where * is some values determined by period hourly = YYYY-MM-dd hh, daily= YYYY-MM-dd
// weekly = YYYY-MM-dd, monthly = YYYY-MM, yearly = YYYY
// # is total number of bags collected
// sum is the sum of id0 + id1 + ... + idN at date points determined by period.
//
// --------- Result ------------- Mode = running
// let result = {avg: {*: #, ...}, id0: {*: #, *: #, ...}, id1: {*: #, *: #, ...}, ...}
//
// where * is some values determined by period hourly = YYYY-MM-dd hh, daily= YYYY-MM-dd
// weekly = YYYY-MM-dd, monthly = YYYY-MM, yearly = YYYY
// # is total number of bags collected
//
// ------------------------------ [NOTE: avg is always the average of all entities]
//
exports.timedGraphSessions = functions.https.onRequest((req, res) => {
  cors(req, res, () => {
    const startDate = req.body.startDate;
    const endDate = req.body.endDate;
    const uid = req.body.uid;
    const groupBy = req.body.groupBy;
    const period = req.body.period;
    var mode = req.body.mode;
    if (mode === undefined) {
      mode = "accumTime";
    }
    const isAccumTime = mode === 'accumTime';
    const isAccumEntity = mode === 'accumEntity';
    const isRunning = mode === 'running';
    const sd = new Date(startDate * 1000);
    const ed = new Date(endDate * 1000);
    
    const sameY = isSameYear(sd, ed);
    const sameM = isSameMonth(sd, ed);
    const sameD = isSameDay(sd, ed);
    
    var result = {};
    var allOthers = {avg: {}};
    var days = {};
    var workingOnDays = {};
    
    var ids = [];
    for (var i = 0; i < Object.keys(req.body).length; i++) {
      const ikey = "id" + i;
      if (req.body[ikey] !== undefined) {
        ids.push(req.body[ikey]);
        if (mode !== 'accumEntity') {
          result[req.body[ikey]] = {};
        }
      }
    }
    
    if (groupBy === "worker" || groupBy === "foreman") {
      var sessionsRef = admin.database().ref('/' + uid + '/sessions');
      sessionsRef.once("value").then((snapshot) => {
        snapshot.forEach((childSnapshot) => {
          const key = childSnapshot.key;
          const val = childSnapshot.val();
          
          const foremanKey = val.wid;
          for (const workerKey in val.collections) {
            const collection = val.collections[workerKey];
            for (const pickupKey in collection) {
              const pickup = collection[pickupKey];
              const accum = !isAccumTime
                ? roundDateToRunningPeriod(pickup.date, period, sameY, sameM, sameD)
                : roundDateToPeriod(pickup.date, period);
              const wkey = isAccumEntity
                ? "sum"
                : groupBy === "foreman" ? foremanKey : workerKey;
                
                
              var contained = groupBy === "foreman" && arrayContainsItem(ids, foremanKey)
              || groupBy === "worker" && arrayContainsItem(ids, workerKey)
                
              if (startDate <= pickup.date && pickup.date <= endDate) {
                if (contained) {
                  incrSessionCounter(result, wkey, accum);
                }
                incrSessionCounter(allOthers, "avg", accum);
                updateDaysCounter(days, workingOnDays, wkey, accum, period, pickup.date);
              }
            }
          }
        });
        result["avg"] = averageOfSessionItem(allOthers.avg, "avg", days, workingOnDays);
        res.send(result);
        return true;
      }).catch((err) => {
        console.log(err);
      });
    } else {
      orchardsCooked(undefined, uid, (cookedOrchards) => {
        var sessionsRef = admin.database().ref('/' + uid + '/sessions');
        sessionsRef.once("value").then((snapshot) => {
          snapshot.forEach((childSnapshot) => {
            const key = childSnapshot.key;
            const val = childSnapshot.val();
            
            for (const workerKey in val.collections) {
              const collection = val.collections[workerKey];
              
              for (const pickupKey in collection) {
                const pickup = collection[pickupKey];
                const accum = !isAccumTime
                  ? roundDateToRunningPeriod(pickup.date, period, sameY, sameM, sameD)
                  : roundDateToPeriod(pickup.date, period);
                const pnt = {x: pickup.coord.lng, y: pickup.coord.lat};
                
                var orc = undefined;
                for (const okey in cookedOrchards) {
                  if (polygonContainsPoint(cookedOrchards[okey].polygon, pnt)) {
                    orc = cookedOrchards[okey];
                    break;
                  }
                }
                
                if (orc === undefined 
                || orc.id === undefined 
                || orc.val === undefined 
                || orc.val.farm === undefined) {
                  continue;
                }
                
                // akey is to check if we are actually in an asked for orchard/farm
                // but we will always use pkey to group dat based on the mode.
                const akey = groupBy === "orchard" ? orc.id : orc.val.farm;
                
                const pkey = isAccumEntity
                  ? "sum"
                  : akey;
                
                const contained = arrayContainsItem(ids, akey);
                
                if (startDate <= pickup.date && pickup.date <= endDate) {
                  if (contained) {
                    incrSessionCounter(result, pkey, accum);
                  }
                  incrSessionCounter(allOthers, "avg", accum);
                  updateDaysCounter(days, workingOnDays, pkey, accum, period, pickup.date);
                }
              }
            }
          });
          result["avg"] = averageOfSessionItem(allOthers.avg, "avg", days, workingOnDays);
          res.send(result);
          return true;
        }).catch((err) => {
          console.log(err);
        });
      });
    }
  });
});