/* 
* 	File:	Graphs.js
*	Author:	Binary Ninjaz (Vincent,Shaun,Letanyan,Ojo)
*
*	Description:	This file contais functions for the data representation on 
*					"graphs.html". It requests and recieves data from firebase
*					databse, and uses google graph APIs 
*/
const baseUrl = 'https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/timedGraphSessions'; //Base URL for accessing firebase
const database = firebase.database();	/* Pointing to database on firebase cloud */
const user = function() { return firebase.auth().currentUser }; /* Function which authenticates user */

/* Function returns the user ID of the selected user */
const userID = function() {
  if (user() !== null) {
    return user().uid ;
  } else {
    return "";
  }
}

var foremen = []; /* Array containing a list of Foremen names */
var workers = []; /* Array containing a list of workers names */
var orchards = []; /* Array containing a list of Orchard names */

$(window).bind("load", () => {
	myFunction(); //This function starts the spinner, as soon as the page loads
	/* The next two lines hide the div that displays the spinner*/
	var divHide = document.getElementById('myChart1');
	divHide.style.visibility = "hidden";
  let succ = () => {
    initPage();
  };
  let fail = () => {
    workers = [];
    orchards = [];
  };
  retryUntilTimeout(succ, fail, 1000);
});

/* Function returns a pointer to the list of workers of the particular user ID */
function workersRef() {
  return database.ref('/' + userID()  + '/workers');
}

/* Function returns a pointer to the list of orchards of the particular user ID */
function orchardsRef() {
  return database.ref('/' + userID()  + '/orchards');
}

/* Function returns a worker pointed to by the callback parameter */
function getWorkers(callback) {
  const ref = firebase.database().ref('/' + userID() + '/workers');
  ref.once('value').then((snapshot) => {
    callback(snapshot);
  });
}

/* Function returns an orchard pointed to by the callback parameter */
function getOrchards(_callback) {
  const ref = firebase.database().ref('/' + userID() + '/orchards');
  ref.once('value').then((snapshot) => {
    _callback(snapshot);
  });
}

/* Function returns a foremen, given a particular key */
function foremanForKey(key) {
  for (var k in foremen) {
    if (foremen[k].key === key) {
      return foremen[k];
    }
  }
  return {value: {name: "Farm", surname: "Owner"}}; //The return value is a JSON object
}

/* Function returns a worker, given a particular key */
function workerForKey(key) {
  for (var k in workers) {
    if (workers[k].key === key) {
      return workers[k];
    }
  }
  return undefined;
}

//calls functions that populate the drop down lists and worker/orchard arrays
//also draws initial graphs on start up
function initPage(){
    initOrchards(); //This function initiates the orchards immediately when the analytics page is accessed (for selction)
    initWorkers();//This function initiates the workers immediately when the analytics page is accessed (for selection)

    //Vincent started working on this function from here
    var ctx = document.getElementById("myChart").getContext('2d');
    var myChart = new Chart(ctx, {
            type: 'radar',
            data: {
                    //The size of the labels array will depend on the start date and the end date.
                    //The labels will be the dates, from the specified start till the specified end.
                    labels: ['Sunday','Monday','Tuesday','Wednesday','Thursady','Friday','Saturday'], //This will contain the dates plotted on each point. (give me the dates)
                    datasets: [{
                    label: "Number of Bags p/day", //These are the number of bags per day since the start date and the end date

                    //The following values in the data array are the number of bags collected each day. from start date to end date
                    data: [0, 0, 0, 0, 0, 0, 0], //The size of this will also depend on the start and the end date
                    pontBackgroundColor: '#4CAF50' //Color of the area 
            }]
            }
    });
    
    google.charts.load("current", {packages:["corechart"]});
    google.charts.setOnLoadCallback(drawChart);
    function drawChart() {
      var data = google.visualization.arrayToDataTable([
        ["Period", "Number of Bags", { role: "style" } ], //This line explains the format of the array object
        ["06:00 - 07:00", 0, "#4CAF50"], //[label: which is the period of 1 hour, Number of bags, color of bar]
        ["07:00 - 08:00", 0, "#4CAF50"],
        ["08:00 - 09:00", 0, "#4CAF50"],
		["09:00 - 10:00", 0, "#4CAF50"],
        ["10:00 - 11:00", 0, "#4CAF50"],
        ["11:00 - 12:00", 0, "#4CAF50"],
		["12:00 - 13:00", 0, "#4CAF50"],
        ["13:00 - 14:00", 0, "#4CAF50"],
        ["14:00 - 15:00", 0, "#4CAF50"],
        ["15:00 - 16:00", 0, "#4CAF50"],
		["16:00 - 17:00", 0, "#4CAF50"],
        ["17:00 - 18:00", 0, "#4CAF50"]
      ]);

      var view = new google.visualization.DataView(data);
      view.setColumns([0, 1,
                       { calc: "stringify",
                         sourceColumn: 1,
                         type: "string",
                         role: "annotation" },
                       2]);
      var nameOfWorker; //This has to be assigned the name of the worker/the id ow the worker
      var options = {
        title: "Number of bags collected per hour", //Teboho Mokoena will be replaced with 'nameOfWorker' variable
        width: 1080, //Setting the width 
        height: 500, //Setting the height
        bar: {groupWidth: "95%"}, //This is the grouping width of the bar graph
        legend: { position: "none" }, //This will be determined by the UX designer
      };
      var chart = new google.visualization.BarChart(document.getElementById("curve_chart"));
      chart.draw(view, options);
  }
}

/* This function loads all available orchards in the database, for graph filtering */
function initOrchards(){
    var orchardSelect = document.getElementById('orchardSelect');
    getOrchards((orchardsSnap) => {
        orchards=[];
        orchardsSnap.forEach((orchard) => {
          const val = orchard.val();
          const k = orchard.key;
          orchards.push({key: k, value: val});
          var option = document.createElement("option");
          option.text = val.name;
		  //console.log(option);
          orchardSelect.options.add(option);
        });
    });
	
}

/* This function loads all available workers in the database, for graph filtering */
function initWorkers(){
   var workerSelect = document.getElementById('workerSelect');
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
            var wName = w.name + ' ' + w.surname;
            var option = document.createElement("option");
            option.text = wName;
            workerSelect.options.add(option);
          }
        });
    });
	myFunction2();//This function stops the spinner, it is here because it (initWorkers) is the last function called in initPage()
					//This means that when this line executes, resources are ready
}

//takes information chosen by user for orchard filter to pass to orchard performance function
function filterOrchard(){
    var name = document.getElementById('orchardSelect').value;
    var week = document.getElementById('weekSelect').value; //format e.g: 2018-W17
    if(name!== '' && week!==''){
        var start = new Date(week);
        var end = new Date(start.getFullYear(),start.getMonth(),start.getDate()+6);
        var id = getOrchardId(name);  
		myFunction(); //This function activates the spinner to signify fetching of resources
		var canvasHide = document.getElementById('myChart');
		canvasHide.style.visibility = "hidden";
		/* updateSpinerOrchard is no longer active because the div is not visible*/
		updateSpinerOrchard(true); //This calls the spinner when filtering the orchard for the graphs
        orchardPerformance(start, end, id);
    }else{
        window.alert("Some fields in the orchard filter appear to be blank. \n"
        +"Please enter them to continue."); //Appropriate error message when loading fails
    }
}

/* This function returns the Orchard ID when given the name of the orchard */
function getOrchardId(name){
    var id='';
    for (var k in orchards) {
        if(name===orchards[k].value.name){
            return orchards[k].key;
        }
    }
    return id;
}

//takes information chosen by user for worker filter to pass to worker performance function
//date to test function 2018/
function filterWorker(){
    var name = document.getElementById('workerSelect').value;
    var date = document.getElementById('workerDateSelect').value;
    if(name!== '' && date!== ''){
        var start = new Date(date);
        var end=new Date(date);
        end.setHours(18);
        end.setMinutes(0);
        start.setHours(6);
        start.setMinutes(0);
        var id = getWorkerId(name);
		myFunction(); //This calls the function which shows that resources are loading
		/*The next two line are unnecessary but they show the user that they have pressed the button */
		var divHide = document.getElementById('curve_chart');
		divHide.style.visibility = "hidden";
        workerPerformance(start, end, id);
    }else{
        window.alert("Some fields in the worker filter appear to be blank. \n"
        +"Please enter them to continue."); //Appropriate error message when loading of workers fails 
    }
}

/* This function returns the worker ID, given the name of a particular worker */
function getWorkerId(name){
    var id='';
    for (var k in workers) {
        var fullname = workers[k].value.name+' '+workers[k].value.surname;
        if(name===fullname){
            return workers[k].key;
        }
    }
    return id;
}

//converts a date to seconds since epoch
function dateToSeconds(date){ return date.getTime() / 1000 ; }

//post request for orchard
function orchardPerformance(start, end, id){
   const groupBy = 'orchard';
   const period = 'daily';
   const startDate = dateToSeconds(start);
   const endDate = dateToSeconds(end);
   //baseUrl is set to 'https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/timedGraphSessions'
   var keys = {};
   keys.id0 = id;
   keys.groupBy = groupBy;
   keys.period = period;
   keys.startDate = startDate;
   keys.endDate = endDate;
   keys.uid = userID();
   $.post(baseUrl, keys, (data, status) => {
        changeOrchardGraph(data);
    });
}

///This function gets worker performance data to represent as graphical statistics
function workerPerformance(start, end, id){
   const groupBy = 'worker';
   const period = 'hourly';
   const startDate = dateToSeconds(start);
   const endDate = dateToSeconds(end);
   //baseUrl is set to 'https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/timedGraphSessions'
   var keys = {};
   keys.id0 = id;
   keys.groupBy = groupBy;
   keys.period = period;
   keys.startDate = startDate;
   keys.endDate = endDate;
   keys.uid = userID();
   $.post(baseUrl, keys, (data, status) => {
        changeWorkerGraph(data);
    });
}

///This function updates orchard graph based on user input
function changeOrchardGraph(data){
	myFunction2();
	var canvasHide = document.getElementById('myChart');
	var divHide = document.getElementById('myChart1');
	canvasHide.style.visibility = "visible"; //Shows the graph
	divHide.style.visibility = "hidden"; //Hides the spinner div
	updateSpinerOrchard(false); /* This function call stops the spinner */
    console.log(data); // can be removed, just used to view json object
    var name = document.getElementById('orchardSelect').value;
    var key = getOrchardId(name);
    var values = data[key];
    var sevenDays = ["Sunday","Monday","Tuesday","Wednesday","Thursady","Friday","Saturday"];
    var data = [];
    for(var i=0;i<7;i++){
        if(values[sevenDays[i]] === undefined){
            data.push(0);
        }else{
            data.push(values[sevenDays[i]]);
        }
    }
    var ctx = document.getElementById("myChart").getContext('2d');
    var myChart = new Chart(ctx, {
        type: 'radar',
        data: {
                //The size of the labels array will depend on the start date and the end date.
                //The labels will be the dates, from the specified start till the specified end.
                labels: ['Sunday','Monday','Tuesday','Wednesday','Thursady','Friday','Saturday'], //This will contain the dates plotted on each point. (give me the dates)
                datasets: [{
                    label: "Number of Bags p/day", //These are the number of bags per day since the start date and the end date

                    //The following values in the data array are the number of bags collected each day. from start date to end date
                    data: data, //The size of this will also depend on the start and the end date
                    pointBackgroundColor: '#4CAF50' //Color of the area 
                }]
            }
    });
}

///This function updates worker graph based on user input
function changeWorkerGraph(data){
	myFunction2(); //This function de-activates the spinner to signify that resources have arrived
	var divHide = document.getElementById('curve_chart');
	divHide.style.visibility = "visible";
    console.log(data); // can be removed, just used to view json object
    var name = document.getElementById('workerSelect').value;
    var key = getWorkerId(name);
    var values = data[key];
    var hourly = ["6","7","8","9","10","11","12","13","14","15","16","17","18"];
    var hours = ["06:00 - 07:00",
                "07:00 - 08:00",
                "08:00 - 09:00",
                "09:00 - 10:00",
                "10:00 - 11:00",
                "11:00 - 12:00",
                "12:00 - 13:00",
                "13:00 - 14:00",
                "14:00 - 15:00",
                "15:00 - 16:00",
                "16:00 - 17:00",
                "17:00 - 18:00"];
    //var data = {["Period", "Number of Bags", { role: "style" } ]};
    var temp = [
        ["Period", "Number of Bags", { role: "style" } ],
    ];
    for(var i=0;i<hourly.length;i++){
        if(values[hourly[i]] === undefined){
            temp.push([hours[i], 0, "#4CAF50"]);
        }else{
            //data.push(values[sevenDays[i]]);
            temp.push([hours[i], values[hourly[i]], "#4CAF50"]);
        }
    }
	
    google.charts.load("current", {packages:["corechart"]});
    google.charts.setOnLoadCallback(drawChart);
    function drawChart() {
        var data1 = google.visualization.arrayToDataTable(temp);
	var view = new google.visualization.DataView(data1);
	view.setColumns([0, 1,
                       { calc: "stringify",
                         sourceColumn: 1,
                         type: "string",
                         role: "annotation" },
                       2]);
	var options = {
            title: "Number of bags collected per hour", //Teboho Mokoena will be replaced with 'nameOfWorker' variable
            width: 1200, //Setting the width 
            height: 500, //Setting the height
            bar: {groupWidth: "95%"}, //This is the grouping width of the bar graph
            legend: { position: "none" }, //This will be determined by the UX designer
        };
        var chart = new google.visualization.BarChart(document.getElementById("curve_chart"));
        chart.draw(view, options);
    }
}

///edits value of end date label when a starting date is picked
function changeLabel(){
    var start = new Date(document.getElementById('weekSelect').value);
    var end = new Date(start.getFullYear(),start.getMonth(),start.getDate()+6);
    document.getElementById('endDate').value = formatDate(end);
}

///formats a date to yyyy-mm-dd, used in changeLabel() function
function formatDate(date) {
    var d = new Date(date),
        month = '' + (d.getMonth() + 1),
        day = '' + d.getDate(),
        year = d.getFullYear();

    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;

    return [year, month, day].join('-');
}
/* This function shows the spinner while still waiting for resources*/
var spinnerOrchard;
function updateSpinerOrchard(shouldSpin) {
  var opts = {
	lines: 8, // The number of lines to draw
	length: 37, // The length of each line
	width: 10, // The line thickness
	radius: 45, // The radius of the inner circle
	scale: 1, // Scales overall size of the spinner
	corners: 1, // Corner roundness (0..1)
	color: '#4CAF50', // CSS color or array of colors
	fadeColor: 'transparent', // CSS color or array of colors
	speed: 1, // Rounds per second
	rotate: 0, // The rotation offset
	animation: 'spinner-line-fade-quick', // The CSS animation name for the lines
	direction: 1, // 1: clockwise, -1: counterclockwise
	zIndex: 2e9, // The z-index (defaults to 2000000000)
	className: 'spinner', // The CSS class to assign to the spinner
	top: '50%', // Top position relative to parent
	left: '50%', // Left position relative to parent
	shadow: '0 0 1px transparent', // Box-shadow for the lines
	position: 'absolute' // Element positioning
  };

  var target = document.getElementById('myChart1'); //This is where the spinner is gonna show
  if (shouldSpin) {
	spinnerOrchard = new Spinner(opts).spin(target); //The class and corresponding css are defined in spin.js and spin.css
  } else {
	spinnerOrchard.stop(); //This line stops the spinner. 
	spinnerOrchard = null;
  }
}

//This function is needed to display spinner
function myFunction(){ 
	var target = document.getElementById('cover-spin');
	target.style.display = "inline"; //This line shows the spinner
}
//This function is needed to stop the spinner
function myFunction2(){
	var target = document.getElementById('cover-spin');
	target.style.display = "none"; //This line simply hides the spinner
}