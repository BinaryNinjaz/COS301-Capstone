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

firebase.auth().onAuthStateChanged(function (user) {
  if (user) {
    $(window).bind("load", function() {
      getWorkers();
    });
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

/*function getCollectionsRef(sessionKey) {
  return database.ref('/' + userID() + '/sessions/'+
                sessionKey+'/collections');
}

function workerCollections(sessionKey,workerKey) {
  return database.ref('/' + userID() + '/sessions/'+
                sessionKey+'/collections/'+workerKey);
}

function yieldsRef() {
  return database.ref('/' + userID() + '/sessions');
}
*/


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

function orchardPerformance(start, end, id){
   groupBy = 'orchard';
   period = 'daily';
   startDate = start;
   endDate = end;
   uid = id;
   var params = constructParams(groupBy,period,startDate,endDate,uid);
   sendPostRequest(params);
   //still needs implementation
}

function workerTotalBags(start, end, id){
   groupBy = 'worker';
   period = 'hourly';
   startDate = start;
   endDate = end;
   uid = id;
   var params = constructParams(groupBy,period,startDate,endDate,uid);
   sendPostRequest(params);
   //still needs implementation
}

function constructParams(groupBy,period,startDate,endDate,uid){
    var params = '';
    params = params +'groupBy='+groupBy;
    params = params +'&period='+period;
    params = params +'&startDate='+startDate;
    params = params +'&endDate='+endDate;
    params = params +'&uid='+uid;
    return params;
}

function sendPostRequest(params){
    var http = new XMLHttpRequest();
    http.open('POST', baseUrl, true);
    
    http.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');

    http.onreadystatechange = function() {//Call a function when the state change
        if(http.readyState == 4 && http.status == 200) {
            //do something with http.responseText here
        }
    }
    http.send(params);
}