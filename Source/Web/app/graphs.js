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
      getOrchards();
    });
  } 
});

function workersRef() {
  return database.ref('/' + userID()  + '/workers');
}

function orchardsRef() {
  return database.ref('/' + userID()  + '/orchards');
}

var foremen = [];
var workers = [];
var orchards = [];

function getWorkers(callback) {
  const ref = firebase.database().ref('/' + userID() + '/workers');
  ref.once('value').then((snapshot) => {
    callback(snapshot);
  });
}

function getOrchards(_callback) {
  const ref = firebase.database().ref('/' + userID() + '/orchards');
  ref.once('value').then((snapshot) => {
    _callback(snapshot);
  });
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

//below adds worker and orchard names to the drop down lists
function populateLists(){
    var workerSelect = document.getElementById('workerSelect');
    for (var k in workers) {
       var wName = workers[k].value.name + ' ' + workers[k].value.surname;
       var option = document.createElement("option");
       option.text = wName;
       console.log(wName);
       workerSelect.options.add(option);
    }
    var orchardSelect = document.getElementById('orchardSelect');
    for (var k in orchards) {
       var option = document.createElement("option");
       option.text = orchards[k].value.name;
       console.log(orchards[k].value.name);
       orchardSelect.options.add(option);
    }
}

function initPage(){
    getOrchards((orchardsSnap) => {
        orchards=[];
        orchardsSnap.forEach((orchard) => {
          const val = orchard.val();
          const k = orchard.key;
          orchards.push({key: k, value: val})
        });
        initWorkers();
    }); 
}

function initWorkers(){
   getWorkers((workersSnap) => {
        foremen = [];
        workers = [];
        workersSnap.forEach((worker) => {
          const w = worker.val();
          const k = worker.key;
          if (w.type === "Foreman") {
            foremen.push({key: k, value: w});
          } else {
            workers.push({key: k, value: w});
          }
        });
        populateLists();
    });
}

function filterOrchard(){
    var orchardName = document.getElementById('orchardSelect').value;
    var start = document.getElementById('startDateSelect').value;
    var end = document.getElementById('endDateSelect').value;
    if(name!== '' && start!== '' && end !== ''){
        var id = getOrchardId(orchardName);
        orchardPerformance(start, end, id);
    }else{
        window.alert("Some fields in the orchard filter appear to be blank. \n"
        +"Please enter them to continue");
    }
}

function getOrchardId(name){
    
}

function filterWorker(){
    
}

function getWorkerId(name){
    
}

var groupBy;
var period;
var startDate;
var endDate;
var uid;

//converts a date to seconds since epoch
function dateToSeconds(date){ return Math.floor( date.getTime() / 1000 ) }

function orchardPerformance(start, end, id){
   groupBy = 'orchard';
   period = 'daily';
   startDate = dateToSeconds(start);
   endDate = dateToSeconds(end);
   uid = id;
   var params = constructParams(groupBy,period,startDate,endDate,uid);
   var response = sendPostRequest(params);
   //still needs implementation
}

function workerPerformance(start, end, id){
   groupBy = 'worker';
   period = 'hourly';
   startDate = dateToSeconds(start);
   endDate = dateToSeconds(end);
   uid = id;
   var params = constructParams(groupBy,period,startDate,endDate,uid);
   var response = sendPostRequest(params);
   //still needs implementation
   //console.log(response);
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

    http.onreadystatechange = function() {//Call a function when the state changes
        if(http.readyState == 4 && http.status == 200) {
            return http.responseText;
        }
    }
    http.send(params);
}