/* jshint esversion: 6 */
/* jshint -W014 */

const functions = require('firebase-functions');
const admin = require('firebase-admin');
const cors = require('cors')({origin: true});
const moment = require('moment');
admin.initializeApp();

// Point: [x: Float, y: Float]
// polygon: [Point]
// point: Point
function polygonContainsPoint(polygon, point) {
  if (polygon === undefined) {
    return false;
  }
  var i = 0;
  var j = polygon.length - 1;
  var c = false;
  for (; i < polygon.length; j = i++) {
    const yValid = (polygon[i].y > point.y) !== (polygon[j].y > point.y);
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

function roundSince1970(date, period) {
  const d = date.clone();
  const startOfTime = moment(0);
  if (period === "hourly") {
    return d.diff(startOfTime, 'hour');
  } else if (period === "daily") {
    return d.diff(startOfTime, 'day');
  } else if (period === "weekly") {
    return d.diff(startOfTime, 'week');
  } else if (period === "monthly") {
    return d.diff(startOfTime, 'month');
  } else if (period === "yearly") {
    return d.diff(startOfTime, 'year');
  }
  return d.diff(startOfTime, 'day');
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

function periodStep(period) {
  if (period === "hourly") {
    return 24;
  } else if (period === "daily") {
    return 364.25;
  } else if (period === "weekly") {
    return 54;
  } else if (period === "monthly") {
    return 12;
  } else if (period === "yearly") {
    return 1;
  }
  return 364.25;
}

function sinusoidalRegression(data, periodType) {
  const period = periodStep(periodType);
  const high = highest(data);
  const low = lowest(data);
  const d = (high + low) / 2;
  const a = high - d;
  const b = 2.0 * Math.PI / period;
  const c = Math.asin((low - d) / a) - b;

  return {
    a: a,
    b: b,
    c: c,
    d: d,
  };
}

function randomBool() {
  return Math.random() < 0.5;
}

function randomChromosome(limit) {
  return {
    a: Math.random() * limit.a,
    b: Math.random() * limit.b,
    c: Math.random() * limit.c,
    d: Math.random() * limit.d
  };
}

function mutateChromosome(chromosome, limit) {
  const rChromo = randomChromosome(limit);
  return {
    a: randomBool() ? chromosome.a : rChromo.a,
    b: randomBool() ? chromosome.b : rChromo.b,
    c: randomBool() ? chromosome.c : rChromo.c,
    d: randomBool() ? chromosome.d : rChromo.d
  };
}

function crossChromosome(x, y) {
  return {
    a: randomBool() ? x.a : y.a,
    b: randomBool() ? x.b : y.b,
    c: randomBool() ? x.c : y.c,
    d: randomBool() ? x.d : y.d
  };
}

function crossChromosomesWithChance(chromosomes, prob) {
  const len = chromosomes.length;
  for (var i = 0; i < len / 2 - 1; i += 1) {
    const p = Math.random();
    if (p > prob) {
      const a = chromosomes[i * 2];
      const b = chromosomes[i * 2 + 1];
      const c = crossChromosome(a, b);
      chromosomes.push(c);
    }
  }
}

function mutateChromosomesWithChance(chromosomes, limit, prob) {
  const len = chromosomes.length;
  for (var i = 0; i < len; i += 1) {
    const p = Math.random();
    if (p > prob) {
      const a = chromosomes[i];
      const c = mutateChromosome(a, limit);
      chromosomes.push(c);
    }
  }
}

function selectChromosomes(chromosomes, tourneySize, data) {
  var result = [];
  var evals = [];
  for (const ci in chromosomes) {
    const chromosome = chromosomes[ci];
    const fitness = evaluateFitness(chromosome, data);

    for (var i = 0; i < Math.min(result.length, tourneySize); i += 1) {
      if (fitness < evals[i]) {
        if (i < result.length - 1) {
          evals.splice(i, 0, fitness);
          result.splice(i, 0, chromosome);
        } else {
          evals.push(fitness);
          result.push(chromosome);
        }
        if (result.length > tourneySize) {
          result.pop();
          evals.pop();
        }
      }
    }
    if (result.length < tourneySize) {
      evals.push(fitness);
      result.push(chromosome);
    }
  }
  return result;
}

function evaluateFitness(chromosome, data) {
  var error = 0.0;
  const f = (x) => {
    return chromosome.a * Math.sin(chromosome.b * x + chromosome.c) + chromosome.d;
  };

  for (const xi in data) {
    const yi = data[xi];
    const y = f(xi);
    error += (y - yi) * (y - yi);
  }
  return error;
}

function evolvePopulation(size, generations, data, period) {
  const limit = sinusoidalRegression(data, period);
  var chromosomes = [];
  for (var i = 0; i < size; i += 1) {
    chromosomes.push(randomChromosome(limit));
  }
  chromosomes.push(limit);

  for (var j = 0; j < generations; j += 1) {
    mutateChromosomesWithChance(chromosomes, limit, 0.1);
    crossChromosomesWithChance(chromosomes, 0.4);
    const ts = selectChromosomes(chromosomes, size, data);
    chromosomes = ts;
  }

  var best = limit;
  var bestE = evaluateFitness(best, data);

  for (var k = 0; k < chromosomes.length; k += 1) {
    const e = evaluateFitness(chromosomes[k], data);
    if (e < bestE) {
      best = chromosomes[k];
      bestE = e;
    }
  }

  return best;
}

// -------- POST Body --------
//
// id0=[String]
// id1=[String]
// ...
// idN=[String]
//
// groupBy=[worker, orchard, foreman, farm]
// startDate=[D MMM YYYY HH:mm ZZ]
// endDate=[D MMM YYYY HH:mm ZZ]
// uid=[String]
exports.collectionsWithinDate = functions.https.onRequest((req, res) => {
  cors(req, res, () => {
    const startDate = moment(req.body.startDate);
    const endDate = moment(req.body.endDate);
    const uid = req.body.uid;
    var groupBy = req.body.groupBy;
    if (groupBy === undefined) {
      groupBy = "orchard";
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
            if (startDate.isSameOrBefore(moment(val.start_date)) && moment(val.start_date).isSameOrBefore(endDate)) {
              for (var ckey in val.collections) {
                const collection = val.collections[ckey];
                for (var pickup in collection) {
                  const geopoint = collection[pickup].coord;
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
          if (startDate.isSameOrBefore(moment(val.start_date)) && moment(val.start_date).isSameOrBefore(endDate)) {
            for (var ckey in val.collections) {
              const collection = val.collections[ckey];
              if (!arrayContainsItem(ids, ckey)) {
                continue;
              }
              for (var pickup in collection) {
                const geopoint = collection[pickup].coord;
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
          if (arrayContainsItem(ids, val.wid) && startDate.isSameOrBefore(moment(val.start_date)) && moment(val.start_date).isSameOrBefore(endDate)) {
            for (var ckey in val.collections) {
              const collection = val.collections[ckey];
              for (var pickup in collection) {
                const geopoint = collection[pickup].coord;
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
            if (startDate.isSameOrBefore(moment(val.start_date)) && moment(val.start_date).isSameOrBefore(endDate)) {
              for (var ckey in val.collections) {
                const collection = val.collections[ckey];
                for (var pickup in collection) {
                  const geopoint = collection[pickup].coord;
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

function roundDateToPeriod(date, period) {
  if (period === "hourly") {
    return date.format("H");
  } else if (period === "daily") {
    return date.format("eeee");
  } else if (period === "weekly") {
    return date.format("dddd");
  } else if (period === "monthly") {
    return date.format("MMMM");
  } else if (period === "yearly") {
    return date.format("YYYY");
  } else {
    return "";
  }
}

function isSameYear(d1, d2) {
  return d1.clone().startOf('year').format('YYYY') === d2.clone().startOf('year').format('YYYY');
}

function isSameMonth(d1, d2) {
  return d1.clone().startOf('month').format('YYYY-MM') === d2.clone().startOf('month').format('YYYY-MM');
}

function isSameDay(d1, d2) {
  return d1.clone().startOf('day').format('YYYY-MM-DD') === d2.clone().startOf('day').format('YYYY-MM-DD');
}

function roundDateToRunningPeriod(date, period, sameYear, sameMonth, sameDay) {
  const fmtYear = sameYear ? '' : 'YYYY ';
  const fmtMonth = sameMonth ? '' : 'MMM ';
  const fmtDay = sameDay ? '' : 'DD';
  const fmt = fmtYear + fmtMonth + fmtDay;
  if (period === "hourly") {
    return date.clone().startOf('hour').format(fmt + ' HH:mm');
  } else if (period === "daily") {
    return date.clone().startOf('day').format(fmt === '' ? 'ddd' : fmt);
  } else if (period === "weekly") {
    return date.clone().startOf('week').format(fmt === '' ? 'ddd' : fmt);
  } else if (period === "monthly") {
    return date.clone().startOf('month').format(fmtYear + 'MMM');
  } else if (period === "yearly") {
    return date.clone().startOf('day').format('YYYY');
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

function updateDaysCounter(days, entities, key, accum, period, date) {
  const grouper = period === "weekly"
    ? date.clone().startOf('week')
    : period === "monthly"
      ? date.clone().startOf('month')
      : period === "yearly"
        ? date.clone().startOf('year')
        : date.clone().startOf('day');
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

function sinusoidalOfSessionItems(items, period) {
  var result = {};
  const keys = Object.keys(items);
  for (const ikey in keys) {
    const key = keys[ikey];
    result[key] = evolvePopulation(100, 25, items[key], period);
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
// startDate=[D MMM YYYY HH:mm ZZ]
// endDate=[D MMM YYYY HH:mm ZZ]
// mode=[accumTime, accumEntity, running]
// uid=[String]
//
// --------- Result ------------- Mode = accumTime
// result = {avg: {*: #}, p, exp: {*: {a: #, b: #, c: #, d: #}, ...}}
//
// where p = {id0: {*: #, *: #, ...}, id1: {*: #, *: #, ...}, ...}
// where * is some values determined by period hourly = [0, 23], daily=[Sunday, ..., Saturday]
// weekly = [0, 52], monthly = [January, ..., December], yearly = [0, Int.max)
// # is total number of bags collected
//
// --------- Result ------------- Mode = accumEntity
// result = {avg: {*: #, ...}, sum: {*: #, ...}, exp: {sum: {a: #, b: #, c: #, d: #}}}
//
// where * is some values determined by period hourly = YYYY MMM dd HH:mm, daily= YYYY MMM DD
// weekly = YYYY MMM DD, monthly = YYYY MMM, yearly = YYYY
// # is total number of bags collected
// sum is the sum of id0 + id1 + ... + idN at date points determined by period.
//
// --------- Result ------------- Mode = running
// let result = {avg: {*: #}, p, exp: {*: {a: #, b: #, c: #, d: #}, ...}}
//
// where p = {id0: {*: #, *: #, ...}, id1: {*: #, *: #, ...}, ...}
// where * is some values determined by period hourly = YYYY MMM dd HH:mm, daily= YYYY MMM DD
// weekly = YYYY MMM DD, monthly = YYYY MMM, yearly = YYYY
// # is total number of bags collected
//
// ------------------------------ NOTE:
// + avg is always the average of all entities
// + running and accumEntity * are truncated if time doesnt overlap into different bases.
//   example: for daily if your startDate and endDate are within the same month then YYYY
//   and MM are discarded. If your startDate and endDate go into different months but are in
//   the same year then only YYYY is dropped. This applies for all dates in running and accumEntity.
// + for running and accumEntity when the same YYYY MM and DD is asked for it returns ddd
// + exp components are constants in the function: a * sin(b * x + c) + d where x is the only variable
exports.timedGraphSessions = functions.https.onRequest((req, res) => {
  cors(req, res, () => {
    const startDate = moment(req.body.startDate);
    const endDate = moment(req.body.endDate);
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

    const sameY = isSameYear(startDate, endDate);
    const sameM = isSameMonth(startDate, endDate);
    const sameD = isSameDay(startDate, endDate);

    var result = {};
    var allOthers = {avg: {}}; // all collections in the requested period
    var all = {}; // all collections ever for requested ids
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
              const pdate = moment(pickup.date);
              const accum = !isAccumTime
                ? roundDateToRunningPeriod(pdate, period, sameY, sameM, sameD)
                : roundDateToPeriod(pdate, period);
              const wkey = isAccumEntity
                ? "sum"
                : groupBy === "foreman" ? foremanKey : workerKey;


              var contained = groupBy === "foreman" && arrayContainsItem(ids, foremanKey)
              || groupBy === "worker" && arrayContainsItem(ids, workerKey);

              if (startDate.isSameOrBefore(pdate) && pdate.isSameOrBefore(endDate)) {
                if (contained) {
                  incrSessionCounter(result, wkey, accum);
                }
                incrSessionCounter(allOthers, "avg", accum);
                updateDaysCounter(days, workingOnDays, wkey, accum, period, pdate);
              }
              if (contained) {
                incrSessionCounter(all, wkey, roundSince1970(pdate, period));
              }
            }
          }
        });
        result.avg = averageOfSessionItem(allOthers.avg, "avg", days, workingOnDays);
        result.exp = sinusoidalOfSessionItems(all, period);
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
                const pdate = moment(pickup.date);
                const accum = !isAccumTime
                  ? roundDateToRunningPeriod(pdate, period, sameY, sameM, sameD)
                  : roundDateToPeriod(pdate, period);
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
                const pkey = isAccumEntity ? "sum" : akey;
                const contained = arrayContainsItem(ids, akey);

                if (startDate.isSameOrBefore(pdate) && pdate.isSameOrBefore(endDate)) {
                  if (contained) {
                    incrSessionCounter(result, pkey, accum);
                  }
                  incrSessionCounter(allOthers, "avg", accum);
                  updateDaysCounter(days, workingOnDays, pkey, accum, period, pdate);
                }
                if (contained) {
                  incrSessionCounter(all, pkey, roundSince1970(pdate, period));
                }
              }
            }
          });
          result.avg = averageOfSessionItem(allOthers.avg, "avg", days, workingOnDays);
          result.exp = sinusoidalOfSessionItems(all, period);
          res.send(result);
          return true;
        }).catch((err) => {
          console.log(err);
        });
      });
    }
  });
});


exports.archiveWorker = functions.database.ref('/{uid}/workers/{wid}')
  .onDelete((snapshot, context) => {
    const oldValue = snapshot.val();
    const oldKey = context.params.wid;
    snapshot.ref.parent.parent.child('archived/workers/' + oldKey).update(oldValue);
  });

exports.archiveOrchard = functions.database.ref('/{uid}/orchards/{oid}')
  .onDelete((snapshot, context) => {
    const oldValue = snapshot.val();
    const oldKey = context.params.oid;
    snapshot.ref.parent.parent.child('archived/orchards/' + oldKey).update(oldValue);
  });

exports.archiveFarm = functions.database.ref('/{uid}/farms/{fid}')
  .onDelete((snapshot, context) => {
    const oldValue = snapshot.val();
    const oldKey = context.params.fid;
    snapshot.ref.parent.parent.child('archived/farms/' + oldKey).update(oldValue);
  });

exports.archiveSession = functions.database.ref('/{uid}/sessions/{sid}')
  .onDelete((snapshot, context) => {
    const oldValue = snapshot.val();
    const oldKey = context.params.sid;
    snapshot.ref.parent.parent.child('archived/sessions/' + oldKey).update(oldValue);
  });
