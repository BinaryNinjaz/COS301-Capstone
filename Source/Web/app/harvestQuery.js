
function isEmptyObject(obj) {
  return Object.keys(obj).length === 0 && obj.constructor === Object;
}

function ignoredWord(word) {
  return arrayContainsEntity(["is", "in", "with", "by", "on", "over", "from", "and", "a", "an"], word.toLowerCase());
}

function propertyWords() {
  return [
    "crop", "name", "company", "email", "phone number", "province", "nearest town",
    "details", "crop", "cultivar", "irrigation type", "id", "type", "assigned orchard",
    "worker name", "worker id", "worker phone number", "worker assigned orchard",
    "foreman name", "foreman id", "foreman phone number", "foreman assigned orchard",
    "orchard name", "orchard crop", "orchard cultivar", "orchard irrigation type",
    "farm name", "farm company", "farm email", "farm phone number", "farm province",
    "farm nearest town", "worker", "orchard", "farm", "foreman"
  ].sort((a, b) => { return b.length - a.length; });
}

function propertyWord(word) {
  return arrayContainsString(propertyWords(), word.toLowerCase());
}

function getRequestedPropertiesFromQuery(queryText) {
  const passes = queryText.trim().split(/(^|\s+)or($|\s+)/i);
  const properties = propertyWords();
  var result = [];
  for (const i in passes) {
    var subResult = [];
    for (const pidx in properties) {
      const prop = properties[pidx];
      if (stringContainsSubstring(passes[i], prop)) {
        if (prop === "worker" || prop === "orchard" || prop === "farm" || prop === "foreman") {
          subResult.push(prop + " name");
        } else {
          subResult.push(prop);
        }
      }
    }
    result.push(subResult);
  }
  return result;
}

function removeKeywordsFromQuery(query) {
  const properties = propertyWords();
  var result = query;
  for (const pidx in properties) {
    const prop = properties[pidx];
    result = removeWordFromString(prop, result);
  }
  return result;
}

function timePeriods() {
  return [
    /(today)/i,
    /(yesterday)/i,
    /(this\sweek)/i,
    /(last\sweek)/i,
    /(this\smonth)/i,
    /(last\smonth)/i,
    /(this\syear)/i,
    /(last\syear)/i,
    /last\s+(\d+)\s+(days?)/i, // last # days
    /last\s+(\d+)\s+(weeks?)/i, // last # weeks
    /last\s+(\d+)\s+(months?)/i, // last # months
    /((?:\d\d\s+)?(?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)(?:\s+\d{4})?)/i, // DD? MMM YYYY?
    /((?:\d\d\s+)?(?:january|february|march|april|may|june|july|august|september|october|november|december)(?:\s+\d{4})?)/i, // DD? MMMM YYYY?
  ];
}

function getTimePeriods(queryText) {
  const passes = queryText.trim().split(/(^|\s+)or(\s+|$)/i);
  const periods = timePeriods();
  var result = [];
  for (const i in passes) {
    var subResult = [];
    for (const pidx in periods) {
      const period = periods[pidx];
      var match = period.exec(passes[i]);
      if (match != null) {
        const prebuilt = timePeriodForInterval(match[0]);
        if (prebuilt !== undefined) {
          subResult.push(prebuilt);
        } else if (match[2] !== undefined) {
          var unit;
          if (match[2] === "days" || match[2] === "day") {
            unit = 'day';
          } else if (match[2] === "weeks" || match[2] === "week") {
            unit = 'week';
          } else if (match[2] === "months" || match[2] === "month") {
            unit = 'month';
          }
          if (match[1] !== undefined) {
            const daterange = timePeriodForRange(match[1], unit);
            if (daterange !== undefined) {
              subResult.push(daterange);
            }
          }
        } else {
          const x = moment(match[0]);
          if (x != undefined) {
            const s = x.startOf('day');
            const e = x.endOf('day');
            subResult.push({start: s, end: e});
          }
        }
        match = period.exec(passes[i]);
      }
    }
    result.push(subResult);
  }
  return result;
}

function timePeriodForInterval(interval) {
  var s;
  var e;
  if (interval === "today") {
    s = moment().startOf('day');
    e = moment().endOf('day');
  } else if (interval === "yesterday") {
    s = moment().subtract(1, 'days').startOf('day');
    e = moment().subtract(1, 'days').endOf('day');
  } else if (interval === "this week") {
    s = moment().startOf('week');
    e = moment().endOf('week');
  } else if (interval === "last week") {
    s = moment().subtract(1, 'weeks').startOf('week');
    e = moment().subtract(1, 'weeks').endOf('week');
  } else if (interval === "this month") {
    s = moment().startOf('month');
    e = moment().endOf('month');
  } else if (interval === "last month") {
    s = moment().subtract(1, 'months').startOf('month');
    e = moment().subtract(1, 'months').endOf('month');
  } else if (interval === "this year") {
    s = moment().startOf('year');
    e = moment().endOf('year');
  } else if (interval === "last year") {
    s = moment().subtract(1, 'years').startOf('year');
    e = moment().subtract(1, 'years').endOf('year');
  } else {
    s = undefined;
  }
  return s !== undefined ? {start: s, end: e} : undefined;
}

function timePeriodForRange(amount, unit) {
  var e = moment().endOf(unit + 's');
  var s = moment().subtract(parseInt(amount), unit + 's').startOf(unit);
  return {start: s, end: e};
}

function removeTimePeriodsFromQuery(query) {
  const periods = timePeriods();
  var result = query;
  for (const pidx in periods) {
    const period = periods[pidx];
    result = result.replace(period, "");
  }
  return result;
}

function arrayContainsString(array, string) {
  for (const i in array) {
    if (array[i] === string) {
      return true;
    }
  }
  return false;
}

function buildFormalQuery(queryText) {
  const passes = queryText.trim().split(/(^|\s+)or($|\s+)/i);
  var tokens = [];
  for (const i in passes) {
    const pass = passes[i];
    tokens.push(pass.split(/\s+/g));
  }
  return tokens;
}

function unionOfObjects(objectA, objectB, count) {
  if (isEmptyObject(objectA)) {
    return objectB;
  }
  if (isEmptyObject(objectB)) {
    return objectA;
  }
  var result = {};
  for (const keyA in objectA) {
    for (const keyB in objectB) {
      if (!arrayContainsString(keyA.split("/"), keyB)) {
        const temp = objectA[keyA];
        if (keyB === "Calculation") {
          result[keyA + "/" + keyB] = temp + "<br>" + objectB[keyB];
        } else {
          result[keyA + "/" + keyB] = temp + "<br>" + objectB[keyB];
        }
        delete result[keyA];
      } else {
        if (arrayContainsString(objectA[keyA].split("<br>"), objectB[keyB])) {
          result[keyA] = objectA[keyA];
        } else if (result[keyA] === undefined) {
          if (arrayContainsString(objectA[keyA].split("<br>"), "Calculation") || keyB === "Calculation") {
            result[keyA] = objectA[keyA] + "<br>" + objectB[keyB];
          } else {
            result[keyA] = objectA[keyA] + "<br>" + objectB[keyB];
          }
        }
      }
    }
  }

  console.log("~=", result);
  // filter compound duplicates from compound words
  for (const rKey in result) {
    for (const r2Key in result) {
      if (rKey !== r2Key) {
        if (equalSets(rKey.split("/"), r2Key.split("/"))) {
          delete result[r2Key];
        }
        console.log(stringContainsSubstring(rKey, r2Key))
        if (stringContainsSubstring(rKey, r2Key)) {
          console.log("--------", r2Key);
          delete result[r2Key];
        }
      }
    }
  }

  console.log("==", result);
  return result;
}

function equalSets(setA, setB) {
  console.log(setA, setB);
  if (setA.length !== setB.length) {
    return false;
  }
  for (const keyA in setA) {
    var containsA = false;
    for (const keyB in setB) {
      if (setA[keyA] === setB[keyB]) {
        containsA = true;
        break;
      }
    }
    if (!containsA) {
      return false;
    }
  }
  return true;
}

function mergeObjects(objectA, objectB) {
  var result = {};
  for (const a in objectA) {
    result[a] = objectA[a];
  }
  for (const b in objectB) {
    if (result[b] !== undefined) {
      result[b] = result[b] + " or " + objectB[b];
    } else {
      result[b] = objectB[b];
    }

  }
  return result;
}

function queryEntity(option, ekey, entity, farms, orchards, workers, queryText, full) {
  var result = {};

  const requested = getRequestedPropertiesFromQuery(queryText);
  const periods = getTimePeriods(queryText);
  queryText = removeKeywordsFromQuery(queryText);
  queryText = removeTimePeriodsFromQuery(queryText);
  const query = buildFormalQuery(queryText);
  for (const aQueryIdx in query) {
    const aQuery = query[aQueryIdx];
    var subResult = {};
    var missed = false;
    for (const queryParamIdx in aQuery) {
      const queryParam = aQuery[queryParamIdx];
      var subSubResult;
      if (ignoredWord(queryParam)) {
        continue;
      }
      if (option === "worker") {
        subSubResult = searchWorker(entity, orchards, queryParam, full);
      } else if (option === "orchard") {
        subSubResult = searchOrchard(entity, ekey, farms, orchards, workers, queryParam, full);
      } else if (option === "farm") {
        subSubResult = searchFarm(entity, queryParam, full);
      } else if (option === "session") {
        subSubResult = searchSession(entity, queryParam, farms, orchards, workers, periods[aQueryIdx][0]);
      }

      if (!isEmptyObject(subSubResult)) {
        subResult = unionOfObjects(subResult, subSubResult, aQuery.length);
      } else {
        subResult = {};
        missed = true;
        break;
      }
    }
    if (requested[aQueryIdx] !== undefined && requested[aQueryIdx].length > 0) {
      for (const key in subResult) {
        const keyParts = key.toLowerCase().split("/");
        var removeProp = true;
        for (const reqPropIdx in requested[aQueryIdx]) {
          const reqProp = requested[aQueryIdx][reqPropIdx];
          if (arrayContainsString(keyParts, reqProp)) {
            removeProp = false;
            break;
          }
        }
        if (removeProp) {
          delete subResult[key];
        }
      }
    }
    if (!missed && !isEmptyObject(subResult)) {
      result = mergeObjects(result, subResult);
    }
  }
  return result;
}

function queryWorker(worker, orchards, queryText, full) {
  return queryEntity('worker', '', worker, {}, orchards, {}, queryText, full);
}

function queryOrchard(orchard, okey, farms, orchards, workers, queryText, full) {
  return queryEntity('orchard', okey, orchard, farms, orchards, workers, queryText, full);
}

function queryFarm(farm, queryText, full) {
  return queryEntity('farm', '', farm, {}, {}, {}, queryText, full);
}

function querySession(session, queryText, full) {
  return queryEntity('session', '', session, farms, orchards, workers, queryText, full);
}
