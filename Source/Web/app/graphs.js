const baseUrl = 'https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/timedGraphSessions';
const database = firebase.database();
const user = function() { return firebase.auth().currentUser };
const userID = function() {
  if (user() !== null) {
    return user().uid ;
  } else {
    return "";
  }
}

function yieldsRef() {
  return database.ref('/' + userID() + '/sessions');
}

firebase.auth().onAuthStateChanged(function (user) {
  if (user) {
    $(window).bind("load", function() {
      getWorkers();
    });
  } else {
    sessions = [];
  }
});

function workersRef() {
  return database.ref('/' + userID()  + '/workers');
}

var foremen = [];
var workers = [];
function getWorkers() {
  workersRef().once('value').then(function (snapshot) {
      snapshot.forEach(function (worker) {
        const w = worker.val();
        const k = worker.key;
        if (w.type === "Foreman") {
          foremen.push({key: k, value: w});
        } else {
          workers.push({key: k, value: w});
        }
      });
  });
}

function getCollectionsRef(sessionKey) {
  return database.ref('/' + userID() + '/sessions/'+
                sessionKey+'/collections');
}

function workerCollections(sessionKey,workerKey) {
  return database.ref('/' + userID() + '/sessions/'+
                sessionKey+'/collections/'+workerKey);
}


function foremanForKey(key) {
  for (var k in foremen) {
    if (foremen[k].key === key) {
      return foremen[k];
    }
  }
  return {value: {name: "Farm", surname: "Owner"}};
}

function workerForKey(key) {
  for (var k in workers) {
    if (workers[k].key === key) {
      return workers[k];
    }
  }
  return undefined;
}

function initPage(){
    getWorkers();
}

var groupBy;
var period;
var startDate;
var endDate;
var uid;

function orchardPerformance(){
   groupBy = 'orchard';
   period = 'daily';
   //still needs implementation
}

function workerTotalBags(){
   groupBy = 'worker';
   period = 'hourly';
   //still needs implementation
}