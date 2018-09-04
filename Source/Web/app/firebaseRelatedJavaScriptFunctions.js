"use strict";

const user = function() { return firebase.auth().currentUser; };
const userID = function() {
  if (user() !== null) {
    return user().uid;
  } else {
    return "";
  }
};

function initializeFirebase() {
  const config = {
    apiKey: "AIzaSyBTTgAtocwDfror-XZLi4R5rUEphTUK4PU",
    authDomain: "harvest-ios-1522082524457.firebaseapp.com",
    databaseURL: "https://harvest-ios-1522082524457.firebaseio.com",
    projectId: "harvest-ios-1522082524457",
    storageBucket: "harvest-ios-1522082524457.appspot.com",
    messagingSenderId: "167365669216"
  };
  firebase.initializeApp(config);
}

// set up the database
initializeFirebase();

function refreshPage() {
  location.reload();
}

const title = document.getElementsByTagName("title")[0].innerHTML;
if (title !== "Harvest | Login and Register") {
  // This is automatic whenever there's a change to a users authorization
  firebase.auth().onAuthStateChanged(function (user) {
    if (user) {
      // user logged in
      // window.location.href = "HomePage.html";
    } else {
      // user logged out
      window.location.href = "index.html";
    }
  });
}

function retryUntilTimeout(succ, fail, timeout) {
  if (firebase.auth().currentUser) {
    succ()
  } else {
    if (timeout > 0 && timeout < 1000 * 60 * 5) {
      setTimeout(succ, timeout * 2);
    } else {
      alert("Network timeout. Try Reloading the page later.");
    }
    fail();
  }
}

function firebaseRegister(email, password) {
  firebase.auth()
    .createUserWithEmailAndPassword(email, password)
    .then(function (user) { // user details correct
        if(user && user.emailVerified === false){
            user.sendEmailVerification().then(function(){
              verifyEmail(user);
            });
        }
        //document.location.href = "HomePage.html";
    }).catch(function (error) { // some error occured
        const errorCode = error.code;
        const errorMessage = error.message;

        if (errorCode === 'auth/wrong-password') {
          alert('Wrong password.');
        } else {
          alert(errorMessage);
        }
  });
}

function verifyEmail(){
    var mod = document.getElementById("ModalSpace");
    mod.innerHTML = "" +
        "" +
        "<div id='resetModal' class='modal fade' role='dialog'>" +
        "<div class='modal-dialog'>" +
        "" +
        "<div class='modal-content'>" +
        "<div class='modal-header'>" +
        "<h4 class='modal-title'>Email Verification</h4>" +
        "</div>" +
        "<div class='modal-body'>" +
        "<label class='control-label'>This email needs to be verified. Please verify the email in order to log in.</label> " +
        "</div>" +
        "<div class='modal-footer'>" +
        "<div class='col-sm-2 col-sm-offset-5' style='padding: 2px'><button type='button' class='btn btn-success' onclick='sendEmailVerification()'>Resend Email Verification</button</button></div>" +
        "<div class='col-sm-5' style='padding: 2px'><button type='button' class='btn btn-success' data-dismiss='modal' onclick='goToLogin()'>Continue</button></div>" +
        "</div>" +
        "</div>" +
        "" +
        "</div>" +
        "</div>"
    ;

    $('#resetModal  ').modal('show');
}

function sendEmailVerification(){
    firebase.auth().currentUser.sendEmailVerification();
}

function goToLogin(){
    window.location.href = "index.html";
}

function firebaseLogin() {
  const email = document.getElementById("username").value;
  let password = document.getElementById("password").value;

  firebase.auth().signInWithEmailAndPassword(email, password).then(function (user) {
    // user logged in
    document.location.href = "HomePage.html";
  }).catch(function (error) {
    // log in failed
    const errorCode = error.code;
    const errorMessage = error.message;

    alert('Email or password is incorrect. Please try again.');
  });
}

function signOut() {
  firebase.auth().signOut().then(function () {
    document.location.href = "index.html";
  });
}

function sendPasswordResetEmail(emailAddress) {
  firebase.auth().sendPasswordResetEmail(emailAddress).then(function () {
    // Email sent.
  }).catch(function (error) {
    // An error happened.
  });
}

function locationLookup(callback) {
  $.get('http://ip-api.com/json', (data, response) => {
    callback(data, response);
  });
}

function getAdmin(callback) {
  const ref = firebase.database().ref('/' + userID() + '/admin');
  ref.once('value').then((snapshot) => {
    callback(snapshot);
  });
}

function getOrchards(callback) {
  const ref = firebase.database().ref('/' + userID() + '/orchards');
  ref.once('value').then((snapshot) => {
    callback(snapshot);
  });
}

function getWorkers(callback) {
  const ref = firebase.database().ref('/' + userID() + '/workers');
  ref.once('value').then((snapshot) => {
    callback(snapshot);
  });
}

function getFarms(callback) {
  const ref = firebase.database().ref('/' + userID() + '/farms');
  ref.once('value').then((snapshot) => {
    callback(snapshot);
  });
}

/// orchards must be set to {} before calling this function
function setOrchards(orchards, completion) {
  getOrchards((snapshot) => {
    snapshot.forEach((childSnapshot) => {
      orchards[childSnapshot.key] = childSnapshot.val();
    });
    completion();
  });
}

/// workers must be set to {} before calling this function.
/// This function also gets the admin as a worker.
function setWorkers(workers, completion) {
  getAdmin((adminSnapshot) => {
    const val = adminSnapshot.val();
    if (val !== null && val !== undefined) {
      const farmOwner = {name: val.firstname, surname: val.lastname, isFarmOwner: true, type: "Foreman"};

      workers[val.uid] = farmOwner;
      getWorkers((snapshot) => {
        snapshot.forEach((childSnapshot) => {
          workers[childSnapshot.key] = childSnapshot.val();
        });
        completion();
      });
    }
  });
}

/// farms must be set to {} before calling this function
function setFarms(farms, completion) {
  getFarms((snapshot) => {
    snapshot.forEach((childSnapshot) => {
      farms[childSnapshot.key] = childSnapshot.val();
    });
    completion();
  });
}

function watchFarms(farms, completion) {
  const ref = firebase.database().ref('/' + userID() + '/farms');
  ref.on('child_added', (snapshot) => {
    farms[snapshot.key] = snapshot.val();
    completion();
  });
  ref.on('child_removed', (snapshot) => {
    delete farms[snapshot.key];
    completion();
  });
  ref.on('child_changed', (snapshot) => {
    farms[snapshot.key] = snapshot.val();
    completion();
  });
}

function watchOrchards(orchards, completion) {
  const ref = firebase.database().ref('/' + userID() + '/orchards');
  ref.on('child_added', (snapshot) => {
    orchards[snapshot.key] = snapshot.val();
    completion();
  });
  ref.on('child_removed', (snapshot) => {
    delete orchards[snapshot.key];
    completion();
  });
  ref.on('child_changed', (snapshot) => {
    orchards[snapshot.key] = snapshot.val();
    completion();
  });
}

function watchWorkers(workers, completion) {
  const ref = firebase.database().ref('/' + userID() + '/workers');
  ref.on('child_added', (snapshot) => {
    workers[snapshot.key] = snapshot.val();
    completion();
  });
  ref.on('child_removed', (snapshot) => {
    delete workers[snapshot.key];
    completion();
  });
  ref.on('child_changed', (snapshot) => {
    workers[snapshot.key] = snapshot.val();
    completion();
  });
}

function searchFarm(farm, searchText, full) {
  var result = {};
  if (farm === undefined || searchText === "") {
    return result;
  }

  const text = searchText.toLowerCase();

  if (farm.name !== undefined && stringContainsWord(farm.name.toLowerCase(), text)) {
    result["Name"] = farm.name;
  }

  if (farm.companyName !== undefined && stringContainsWord(farm.companyName.toLowerCase(), text)) {
    result["Company"] = farm.companyName;
  }

  if (farm.email !== undefined && stringContainsWord(farm.email.toLowerCase(), text)) {
    result["Email"] = farm.email;
  }

  if (farm.contactNumber !== undefined && stringContainsWord(farm.contactNumber.toLowerCase(), text)) {
    result["Phone Number"] = farm.contactNumber;
  }

  if (farm.province !== undefined && stringContainsWord(farm.province.toLowerCase(), text)) {
    result["Province"] = farm.province;
  }

  if (farm.town !== undefined && stringContainsWord(farm.town.toLowerCase(), text)) {
    result["Nearest Town"] = farm.town;
  }

  if (full && farm.further !== undefined && stringContainsWord(farm.further.toLowerCase(), text)) {
    result["Details"] = farm.further;
  }

  return result;
}

function workerIsAssignedToOrchard(worker, orchard) {
  for (const i in worker.orchards) {
    if (worker.orchards[i] === orchard) {
      return true;
    }
  }
  return false;
}

function searchOrchard(orchard, okey, farms, orchards, workers, searchText, full) {
  var result = {};
  if (orchard === undefined || searchText === "") {
    return result;
  }

  const text = searchText.toLowerCase();

  const farm = farms[orchard.farm];
  const farmResults = searchFarm(farm, text, false);
  for (const key in farmResults) {
    result["Farm " + key] = farmResults[key];
  }

  if (orchard.name !== undefined && stringContainsWord(orchard.name.toLowerCase(), text)) {
    result["Name"] = orchard.name;
  }

  if (orchard.crop !== undefined && stringContainsWord(orchard.crop.toLowerCase(), text)) {
    result["Crop"] = orchard.crop;
  }

  for (const i in orchard.cultivars) {
    if (orchard.cultivars[i] !== undefined && stringContainsWord(orchard.cultivars[i].toLowerCase(), text)) {
      result["Cultivar"] = orchard.cultivars[i];
      break;
    }
  }

  if (orchard.irrigation !== undefined && stringContainsWord(orchard.irrigation.toLowerCase(), text)) {
    result["Irrigation Type"] = orchard.irrigation;
  }

  if (full && orchard.further !== undefined && stringContainsWord(orchard.further.toLowerCase(), text)) {
    result["Details"] = orchard.further;
  }

  if (full) {
    for (const wkey in workers) {
      if (workerIsAssignedToOrchard(workers[wkey], okey)) {
        const workerResults = searchWorker(workers[wkey], orchards, text, false);
        for (const key in workerResults) {
          result["Worker " + key] = workerResults[key];
        }
      }
    }
  }

  return result;
}

function searchWorker(worker, orchards, searchText, full) {
  var result = {};
  if (worker === undefined || searchText === "") {
    return result;
  }

  const text = searchText.toLowerCase();

  const name = worker.name + " " + worker.surname;

  if (stringContainsSubstring(name.toLowerCase(), text)) {
    result["Name"] = name;
  }

  if (worker.idNumber !== undefined && stringContainsWord(worker.idNumber.toLowerCase(), text)) {
    result["ID"] = worker.idNumber;
  }
  if (worker.phoneNumber !== undefined && stringContainsWord(worker.phoneNumber.toLowerCase(), text)) {
    result["Phone Number"] = worker.phoneNumber;
  }
  if (full && worker.info !== undefined && stringContainsWord(worker.info.toLowerCase(), text)) {
    result["Details"] = worker.info;
  }
  if (full && worker.type !== undefined && stringContainsWord(worker.type.toLowerCase(), text)) {
    result["Type"] = worker.type;
  }
  if (full && worker.orchards !== undefined) {
    for (const idx in worker.orchards) {
      const orchardId = worker.orchards[idx];
      const orchard = (orchards || [])[orchardId];
      if (orchard.name !== undefined && stringContainsWord(orchard.name.toLowerCase(), text)) {
        result["Assigned Orchard"] = orchard.name;
      }
    }
  }

  return result;
}

function orchardAtPoint(orchards, x, y) {
  for (const orchardId in orchards) {
    const orchard = orchards[orchardId];
    var xs = [];
    var ys = [];
    for (const pointId in orchard.coords) {
      const point = orchard.coords[pointId];
      xs.push(point.lng);
      ys.push(point.lat);
    }
    if (polygonContainsPoint(xs, ys, x, y)) {
      return {value: orchard, key: orchardId};
    }
  }
  return undefined;
}

function arrayContainsEntity(array, item) {
  for (var i in array) {
    if (array[i] === item) {
      return true;
    }
  }
  return false;
}

function searchSession(session, searchText, farms, orchards, workers, period) {
  var result = {};

  const text = searchText.toLowerCase();

  const foreman = workers[session.wid];
  if (foreman !== undefined) {
    const foremanResults = searchWorker(foreman, orchards, text, false);
    for (const key in foremanResults) {
      result["Foreman " + key] = foremanResults[key];
    }
  }

  var orchardsForSession = [];
  for (const workerId in session.collections) {
    const worker = workers[workerId];
    var points = session.collections[workerId];

    if (worker !== undefined) {
      const workerResults = searchWorker(worker, orchards, text, false);
      for (const wkey in workerResults) {
        const collected = " (" + (points.length | 0) + ")";
        if (result["Worker " + wkey] !== undefined) {
          result["Worker " + wkey] = result["Worker " + wkey] + ", " + workerResults[wkey] + collected;
        } else {
          result["Worker " + wkey] = workerResults[wkey] + collected;
        }
      }
    }

    for (const pidx in points) {
      const point = points[pidx];
      const o = orchardAtPoint(orchards, point.coord.lng, point.coord.lat);
      if (o !== undefined && !arrayContainsEntity(orchardsForSession, o.key)) {
        orchardsForSession.push(o.key);
        const orchardResult = searchOrchard(o.value, o.key, farms, orchards, workers, text, false);
        for (const okey in orchardResult) {
          result["Orchard " + okey] = orchardResult[okey];
        }
      }
    }
  }

  if (searchText === "sum" || searchText === "total") {
    result["Calculation"] = "Total bags collected: " + sessionSum(session);
  } else if (searchText === "avg" || searchText === "average") {
    const avg = sessionAvg(session);
    if (avg !== undefined && avg !== NaN) {
      result["Calculation"] = "Average bags collected per worker: " + avg;
    }
  } else if (searchText === "countWorkers" || searchText === "count") {
    result["Calculation"] = "Workers in Session: " + sessionCountWorkers(session);
  } else if (searchText === "countOrchards") {
    result["Calculation"] = "Orchards in Session: " + orchardsForSession.length | 0;
  } else if (searchText === "max" || searchText === "best") {
    const max = sessionMax(session, workers);
    if (max !== undefined) {
      result["Calculation"] = "Best Worker: " + max;
    }
  } else if (searchText === "min" || searchText === "worst") {
    const min = sessionMin(session, workers);
    if (min !== undefined) {
      result["Calculation"] = "Worst Worker: " + min;
    }
  } else if (searchText === "duration" || searchText === "length") {
    result["Calculation"] = "Duration: " + sessionDuration(session);
  } else if (searchText === "mode" || searchText === "common") {
    const mode = sessionMode(session);
    if (mode !== undefined) {
      result["Calculation"] = "Common Amount Collection: " + mode;
    }
  } else if (searchText === "stdev" || searchText === "stddev") {
    const stdev = sessionSTDDEV(session);
    if (stdev !== undefined) {
      result["Calculation"] = "Standard Deviation: " + stdev;
    }
  }

  if (period !== undefined) {
    const date = moment(session.start_date);

    const cd = date.get('date');
    const cm = date.get('month');
    const cy = date.get('year');

    const sd = period.start.get('date');
    const sm = period.start.get('month');
    const sy = period.start.get('year');

    const ed = period.end.get('date');
    const em = period.end.get('month');
    const ey = period.end.get('year');

    if (period.start.toDate().getTime() <= date.toDate().getTime() && date.toDate().getTime() <= period.end.toDate().getTime()) {
      if (text === "") {
        result[date.format("dddd, DD MMMM YYYY")] = "";
      }
    } else {
      result = {};
    }
  }

  return result;
}

function sessionSum(session) {
  var result = 0;
  for (const workerId in session.collections) {
    var points = session.collections[workerId];
    result += points.length;
  }
  return result;
}

function sessionAvg(session) {
  const sum = sessionSum(session);
  const cnt = sessionCountWorkers(session);
  return Math.round(sum / cnt * 100) / 100;
}

function sessionCountWorkers(session) {
  var result = 0;
  var count = 0;
  const collections = session.collections;
  if (collections === undefined) {
    return undefined;
  }
  for (const workerId in collections) {
    count++;
  }
  return count;
}

function sessionMax(session, workers) {
  var max = 0;
  var result = undefined;
  for (const workerId in session.collections) {
    var points = session.collections[workerId];
    if (points.length > max) {
      max = points.length;
      result = workers[workerId].name + " " + workers[workerId].surname + " (" + max + ")";
    }
  }
  return result;
}

function sessionMin(session, workers) {
  var min = Infinity;
  var result = undefined;
  for (const workerId in session.collections) {
    var points = session.collections[workerId];
    if (points.length < min) {
      min = points.length;
      result = workers[workerId].name + " " + workers[workerId].surname + " (" + min + ")";
    }
  }
  return result;
}

function sessionDuration(session) {
  const start = moment(session.start_date, "d MMM yyyy HH:mm ZZ");
  const end = moment(session.end_date, "d MMM yyyy HH:mm ZZ");
  const hours = end.diff(start, 'hours');
  const minutes = end.diff(start, 'minutes') - hours * 60;

  const hplural = hours === 1 ? "" : "s";
  const mplural = minutes === 1 ? "" : "s";

  const hourMessage = hours !== 0 ? (hours + " hour") + hplural : "";
  const minuteMessage = minutes !== 0 ? (minutes + " minute") + mplural : "";

  const conj = hourMessage !== "" && minuteMessage !== "" ? " and " : "";

  return hourMessage === "" && minuteMessage === "" ? "0 minutes" : hourMessage + conj + minuteMessage;
}

function sessionMode(session) {
  var counts = {};
  for (const workerId in session.collections) {
    const points = session.collections[workerId];
    const count = points.length;
    if (counts[count] === undefined) {
      counts[count] = 1;
    } else {
      counts[count] += 1;
    }
  }

  var commonAmount = undefined;
  var max = 0;
  for (const id in counts) {
    if (commonAmount === undefined) {
      commonAmount = id;
      max = counts[id];
    } else if (counts[id] > max) {
      commonAmount = id;
      max = counts[id];
    }
  }

  return commonAmount;
}

function sessionSTDDEV(session) {
  var avg = sessionAvg(session);
  var diff = 0.0;
  for (const workerId in session.collections) {
    diff += Math.pow((session.collections[workerId].length - avg), 2);
  }
  const count = sessionCountWorkers(session);
  if (count === undefined) {
    return undefined;
  }
  return Math.floor((Math.sqrt(diff / (count - 1)) * 10000)) / 10000.0;
}
