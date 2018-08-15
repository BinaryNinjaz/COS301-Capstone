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

$(window).bind("load", () => {
  updateSpiner(true, "statsHolder");
  var anotherButton = document.getElementById("createAnotherGraphButton");
  anotherButton.style.visibility = "hidden";
  const succ = () => {
    initPage();
  };
  const fail = () => {
    farms = {};
    orchards = {};
    workers = {};
  };
  retryUntilTimeout(succ, fail, 1000);
});

var stats = [];
var farms = {};
var orchards = {};
var workers = {};
function initPage() {
  setFarms(farms, () => {
    setOrchards(orchards, () => {
      setWorkers(workers, () => {
        updateSpiner(false, "statsHolder");
        createNewGraph();
        var anotherButton = document.getElementById("createAnotherGraphButton");
        anotherButton.style.visibility = "visible";
      });
    });
  });
}

function createStat() {
  stats.push({
    interval: 'thisweek',
    startDate: moment().startOf('week'),
    endDate: moment().endOf('week'),
    period: 'daily',
    mode: 'running',
    groupBy: 'worker',
    ids: {farm: [], orchard: [], worker: [], foreman: []}
  });
  return stats[stats.length - 1];
}

function createEntitySelector(id) {
  const event = (param) => { return "onchange='updateGroupBy(" + id + ",\"" + param + "\")'" }

  const fChecked = stats[id].groupBy === "farm" ? [" checked ", " active "] : ["", ""];
  const oChecked = stats[id].groupBy === "orchard" ? [" checked ", " active "] : ["", ""];
  const wChecked = stats[id].groupBy === "worker" ? [" checked ", " active "] : ["", ""];
  const mChecked = stats[id].groupBy === "foreman" ? [" checked ", " active "] : ["", ""];

  return "<div class='btn-group' data-toggle='buttons'>" +
  "  <label class='btn btn-sm btn-info" + fChecked[1] + "'>" +
  "    <input type='radio' autocomplete='off' name='filterEntity' " + event('farm') + fChecked[0] + "> Farms" +
  "  </label>" +
  "  <label class='btn btn-sm btn-info" + oChecked[1] + "'>" +
  "    <input type='radio' autocomplete='off' name='filterEntity' " + event('orchard') + oChecked[0] + "> Orchards" +
  "  </label>" +
  "  <label class='btn btn-sm btn-info" + wChecked[1] + "'>" +
  "    <input type='radio' autocomplete='off' name='filterEntity' " + event('worker') + wChecked[0] + "> Workers" +
  "  </label>" +
  "  <label class='btn btn-sm btn-info" + mChecked[1] + "'>" +
  "    <input type='radio' autocomplete='off' name='filterEntity' " + event('foreman') + mChecked[0] + "> Foremen" +
  "  </label>" +
  "</div>";
}

function createEntitySelectionList(id) {
  const event = (param) => { return "onchange='updateIds(" + id + ", \"" + param + "\")'" };

  var result = "<div id='stat" + id + "EntitySelection'>";

  var items = sortedStatList(0);
  for (const idx in items) {
    const item = items[idx];
    const key = item.key;
    const value = item.value;
    const checked = stats[id].ids[stats[id].groupBy].includes(key) ? " checked " : "";
    result += "<div class='checkbox'>" +
    "  <label>" +
    "    <input type='checkbox' " + checked + event(key) + " > " + value.name + " " + value.surname +
    "  </label>" +
    "</div>";
  }

  result += "</div>";

  return result;
}

function createTimePeriodSelector(id) {
  const event = (param) => { return "onchange='updateTimePeriod(" + id + ", \"" + param + "\")'" };
  const name = "name='timePeriod" + id + "'";

  const hChecked = stats[id].period === "hourly" ? " checked " : "";
  const dChecked = stats[id].period === "daily" ? " checked " : "";
  const wChecked = stats[id].period === "weekly" ? " checked " : "";
  const mChecked = stats[id].period === "monthly" ? " checked " : "";
  const yChecked = stats[id].period === "yearly" ? " checked " : "";

  return "<div>" +
  "  <div class='radio'>" +
  "    <label><input " + hChecked + name + " type='radio' " + event("hourly") + "> Hourly</label>" +
  "  </div>" +
  "  <div class='radio'>" +
  "    <label><input " + dChecked  + name + " type='radio' " + event("daily") + "> Daily</label>" +
  "  </div>" +
  "  <div class='radio'>" +
  "    <label><input " + wChecked  + name + " type='radio' " + event("weekly")  + "> Weekly</label>" +
  "  </div>" +
  "  <div class='radio'>" +
  "    <label><input " + mChecked  + name + " type='radio' " + event("monthly")  + "> Monthly</label>" +
  "  </div>" +
  "  <div class='radio'>" +
  "    <label><input " + yChecked  + name + " type='radio' " + event("yearly")  + "> Yearly</label>" +
  "  </div>" +
  "</div>";
}

function createTimeIntervalSelector(id) {
  const event = (param) => { return "onchange='updateTimeInterval(" + id + ", \"" + param + "\")'"};
  const name = "name='timeInterval" + id + "'";

  const checked0 = stats[id].interval === "today" ? " checked " : "";
  const checked1 = stats[id].interval === "yesterday" ? " checked " : "";
  const checked2 = stats[id].interval === "thisweek" ? " checked " : "";
  const checked3 = stats[id].interval === "lastweek" ? " checked " : "";
  const checked4 = stats[id].interval === "thismonth" ? " checked " : "";
  const checked5 = stats[id].interval === "lastmonth" ? " checked " : "";
  const checked6 = stats[id].interval === "thisyear" ? " checked " : "";
  const checked7 = stats[id].interval === "lastyear" ? " checked " : "";
  const checked8 = stats[id].interval === "betweenexactdates" ? " checked " : "";

  return "<div>" +
  "  <div class='radio'>" +
  "    <label><input " + checked0 + name + " type='radio' " + event("today") + "> Today</label>" +
  "  </div>" +
  "  <div class='radio'>" +
  "    <label><input " + checked1 + name + " type='radio' " + event("yesterday") + "> Yesterday</label>" +
  "  </div>" +
  "  <div class='radio'>" +
  "    <label><input " + checked2 + name + " type='radio' " + event("thisweek") + "> This Week</label>" +
  "  </div>" +
  "  <div class='radio'>" +
  "    <label><input " + checked3 + name + " type='radio' " + event("lastweek") + "> Last Week</label>" +
  "  </div>" +
  "  <div class='radio'>" +
  "    <label><input " + checked4 + name + " type='radio' " + event("thismonth") + "> This Month</label>" +
  "  </div>" +
  "  <div class='radio'>" +
  "    <label><input " + checked5 + name + " type='radio' " + event("lastmonth") + "> Last Month</label>" +
  "  </div>" +
  "  <div class='radio'>" +
  "    <label><input " + checked6 + name + " type='radio' " + event("thisyear") + "> This Year</label>" +
  "  </div>" +
  "  <div class='radio'>" +
  "    <label><input " + checked7 + name + " type='radio' " + event("lastyear") + "> Last Year</label>" +
  "  </div>" +
  "  <div class='radio'>" +
  "    <label><input " + checked8 + name + " type='radio' " + event("betweenexactdates") + "> Between Exact Dates</label>" +
  "  </div>" +
  "</div>";
}

function createExactDateSelector(id) {
  const statId = "stat" + id;
  const start = stats[id].startDate.format('YYYY-MM-DD');
  const end = stats[id].endDate.format('YYYY-MM-DD');
  const event = (param) => { return "onchange='updateDate(" + id + ", \"" + param + "\", this)'" };

  const isHidden = stats[id].interval === "betweenexactdates" ? "" : " hidden ";

  return "<div id='" + statId + "Date' " + isHidden + ">" +
  "  <h5>Start From:</h5>" +
  "    <input type='date' class='form-control' id='startDatePicker' " + event("start") + " value='" + start + "'>" +
  "  <h5>End At:</h5>" +
  "    <input type='date' class='form-control' id='endDatePicker' " + event("end") + " value='" + end + "'>" +
  "</div>";
}

function createModeSelector(id) {
  const event = (param) => { return "onchange='updateMode(" + id + ", \"" + param + "\")'"; };
  const statId = (param) => { return "id='stat" + id + param + "'"; };

  const runningChecked = stats[id].mode === "running" ? [" checked ", " active "]: ["", ""];
  const accumEntityChecked = stats[id].mode === "accumEntity" ? [" checked ", " active "] : ["", ""];
  const accumTimeChecked = stats[id].mode === "accumTime" ? [" checked ", " active "] : ["", ""];

  const accumEntityTitle = stats[id].groupBy.charAt(0).toUpperCase() + stats[id].groupBy.substr(1, stats[id].groupBy.length);
  var accumTimeTitle = "";
  const per = stats[id].period;
  if (per === "hourly") {
    accumTimeTitle = "Hour";
  } else if (per === "daily") {
    accumTimeTitle = "Day";
  } else if (per === "weekly") {
    accumTimeTitle = "Week";
  } else if (per === "monthly") {
    accumTimeTitle = "Month";
  } else if (per === "yearly") {
    accumTimeTitle = "Year";
  }

  return "<div class='btn-group' data-toggle='buttons'>" +
  "  <label " + statId('running') + " class='btn btn-sm btn-info " + runningChecked[1] + "'>" +
  "    <input type='radio' autocomplete='off' name='filterEntity' " + event('running') + runningChecked[0] + "> None" +
  "  </label>" +
  "  <label " + statId('accumEntity') + " class='btn btn-sm btn-info" + accumEntityChecked[1] + "'>" +
  "    <input type='radio' autocomplete='off' name='filterEntity' " + event('accumEntity') + accumEntityChecked[0] + "> By " + accumEntityTitle +
  "  </label>" +
  "  <label " + statId('accumTime') + " class='btn btn-sm btn-info" + accumTimeChecked[1] + "'>" +
  "    <input type='radio' autocomplete='off' name='filterEntity' " + event('accumTime') + accumTimeChecked[0] + "> By " + accumTimeTitle +
  "  </label>" +
  "</div>";
}

function createUpdateGraphButton(id) {
  const event = "onclick='updateGraph(" + id + ")'";

  return "<button type='button' class='btn btn-success statUpdateButton' id='updateButton' " + event + ">" +
  "  Update Graph" +
  "</button>";
}

function createNewGraph() {
  var statsHolder = document.getElementById("statsHolder");
  createStat();
  statsHolder.innerHTML = "";

  for (var idx in stats) {
    var div = document.getElementById("stat" + idx + "Graph")
    if (div !== undefined && div !== null) {
      var ctx = div.getContext('2d');
      if (charts[idx] !== undefined) {
        charts[idx].destroy();
      }
    }
  }

  for (var idx in stats) {
    displayStat(stats[idx], idx);
  }

  for (var idx in stats) {
    var ctx = document.getElementById("stat" + idx + "Graph").getContext('2d');
    var config = chartObjectData[idx];
    charts[idx] = new Chart(ctx, config);
  }
}

function displayStat(stat, id) {
  var statsHolder = document.getElementById("statsHolder");
  const statId = "stat" + id;

  statsHolder.innerHTML += "<div id='" + statId + "' class='container statView'>" +
  "  <div class='row'>" +
  "    <div class='col-sm-4'>" +
  "      <div class='scrollView statFilter'>" +
  "        <h5>Compare: </h5>" +
             createEntitySelector(id) +
             createEntitySelectionList(id) +
  "        <h5>Time Period: </h5>" +
             createTimePeriodSelector(id) +
  "        <h5>Time Interval: </h5>" +
             createTimeIntervalSelector(id) +
             createExactDateSelector(id) +
  "        <h5>Accumulation: </h5>" +
             createModeSelector(id) +
  "      </div>" +
         createUpdateGraphButton(id) +
  "    </div>" +
  "    <div id='" + statId + "GraphHolder' class='col-sm-8 detailsView statGraph'>" +
  "      <canvas id='" + statId + "Graph'>" +
  "      </canvas>"
  "    </div>" +
  "  </div>" +
  "</div>";
}

function updateTimePeriod(id, period) {
  stats[id].period = period;
  var accumTime = document.getElementById("stat" + id + "accumTime");
  const message = (param) => { return "<input type='radio' autocomplete='off' name='filterEntity' onchange='updateMode(" + id + ", \"accumTime\")'> By " + param };
  if (period === "hourly") {
    accumTime.innerHTML = message("Hour");
  } else if (period === "daily") {
    accumTime.innerHTML = message("Day");
  } else if (period === "weekly") {
    accumTime.innerHTML = message("Week");
  } else if (period === "monthly") {
    accumTime.innerHTML = message("Month");
  } else if (period === "yearly") {
    accumTime.innerHTML = message("Year");
  }
}

function updateTimeInterval(id, interval) {
  var datePickers = document.getElementById("stat" + id + "Date");
  datePickers.hidden = interval !== "betweenexactdates";
  stats[id].interval = interval;
}

function updateGroupBy(id, groupBy) {
  stats[id].groupBy = groupBy;
  var accumEntity = document.getElementById("stat" + id + "accumEntity");
  const message = (param) => { return "<input type='radio' autocomplete='off' name='filterEntity' onchange='updateMode(" + id + ", \"accumEntity\")'> By " + param };
  var struct;
  if (groupBy === "farm") {
    accumEntity.innerHTML = message("Farm");
    struct = farms;
  } else if (groupBy === "orchard") {
    accumEntity.innerHTML = message("Orchard");
    struct = orchards;
  } else if (groupBy === "worker") {
    accumEntity.innerHTML = message("Worker");
    struct = workers;
  } else if (groupBy === "foreman") {
    accumEntity.innerHTML = message("Foreman");
    struct = workers;
  }

  var selectionList = document.getElementById("stat" + id + "EntitySelection");
  selectionList.innerHTML = "";
  const event = (param) => { return "onchange='updateIds(" + id + ", \"" + param + "\")'" };
  const formatter = formatterForGroup(stats[id].groupBy);

  const items = sortedStatList(id);
  for (const idx in items) {
    const item = items[idx];
    const value = item.value;
    const key = item.key;
    const isChecked = stats[id].ids[groupBy].includes(key) ? "checked" : "";
    selectionList.innerHTML += "<div class='checkbox'>" +
    "  <label>" +
    "    <input " + isChecked + " type='checkbox' " + event(key) + " > " + formatter(key, value) +
    "  </label>" +
    "</div>";
  }
}

function updateMode(id, mode) {
  stats[id].mode = mode;
}

function updateDate(id, option, sender) {
  if (option === "start") {
    stats[id].startDate = moment(sender.value);
  } else if (option === "end") {
    stats[id].endDate = moment(sender.value);
  }
}

function updateIds(id, key) {
  const stat = stats[id];
  const idx = stats[id].ids[stat.groupBy].indexOf(key);
  if (idx !== -1) {
    stats[id].ids[stat.groupBy].splice(idx, 1);
  } else {
    stats[id].ids[stat.groupBy].push(key);
  }
}

var charts = {};
function updateGraph(id) {
  var ctx = document.getElementById("stat" + id + "Graph").getContext('2d');

  timedGraphSessions(id, (data) => {
    if (charts[id] !== undefined) {
      charts[id].destroy();
    }
    var config = chartObjectForStat(id, data);
    charts[id] = new Chart(ctx, config);
  });
}

function timedGraphSessions(id, completion) {
  const stat = stats[id];
  const uid = userID();
  const timePeriod = timePeriodForStat(stat);

  var keys = {
  startDate: timePeriod.start,
  endDate: timePeriod.end,
  uid: uid,
  mode: stat.mode,
  groupBy: stat.groupBy,
  period: stat.period,
  offset: "120"
  };

  for (var idx in stat.ids[stat.groupBy]) {
    keys["id" + idx] = stat.ids[stat.groupBy][idx];
  }

  var graphDiv = "stat" + id + "GraphHolder";
  updateSpiner(true, graphDiv);

  $.post(baseUrl, keys, (data, status) => {
    updateSpiner(false, graphDiv);
    completion(data);
  });
}

function timePeriodForStat(stat) {
  var s;
  var e;
  const interval = stat.interval;
  if (interval === "today") {
    s = moment().startOf('day');
    e = moment().endOf('day');
  } else if (interval === "yesterday") {
    s = moment().subtract(1, 'days').startOf('day');
    e = moment().subtract(1, 'days').endOf('day');
  } else if (interval === "thisweek") {
    s = moment().startOf('week');
    e = moment().endOf('week');
  } else if (interval === "lastweek") {
    s = moment().subtract(1, 'weeks').startOf('week');
    e = moment().subtract(1, 'weeks').endOf('week');
  } else if (interval === "thismonth") {
    s = moment().startOf('month');
    e = moment().endOf('month');
  } else if (interval === "lastmonth") {
    s = moment().subtract(1, 'months').startOf('month');
    e = moment().subtract(1, 'months').endOf('month');
  } else if (interval === "thisyear") {
    s = moment().startOf('year');
    e = moment().endOf('year');
  } else if (interval === "lastyear") {
    s = moment().subtract(1, 'years').startOf('year');
    e = moment().subtract(1, 'years').endOf('year');
  } else if (interval === "betweenexactdates") {
    s = stat.startDate;
    e = stat.endDate;
  }
  return {
    start: s.toDate().getTime() / 1000.0,
    end: e.toDate().getTime() / 1000.0
  }
}

function accumTimeDates(period) {
  var result = [];
  if (period === "hourly") {
    for (var i = 0; i < 24; i += 1) {
      result.push(String(i));
    }
  } else if (period === "daily") {
    result = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
  } else if (period === "weekly") {
    for (var i = 0; i < 54; i += 1) {
      result.push(String(i));
    }
  } else if (period === "monthly") {
    result = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
  } else if (period === "yearly") {
    result = ["2018", "2019", "2020", "2021", "2022"];
  }
  return result;
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
    return moment(date).startOf('month').format(fmtYear + 'MMM');
  } else if (period === "yearly") {
    return moment(date).startOf('day').format('YYYY');
  } else {
    return '';
  }
}

function roundSince1970(timeinterval, period) {
  const d = moment(new Date(timeinterval * 1000));
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

function dateLabelsForTimeInterval(start, end, period) {
  const s = new Date(start * 1000.0);
  const e = new Date(end * 1000.0);
  const sameYear = isSameYear(s, e);
  const sameMonth = isSameMonth(s, e);
  const sameDay = isSameDay(s, e);

  var result = [];
  var step = 'day';

  if (period === 'hourly') {
    step = 'hour';
  } else if (period === 'daily') {
    step = 'day';
  } else if (period === 'weekly') {
    step = 'week';
  } else if (period === 'monthly') {
    step = 'month';
  } else if (period === 'yearly') {
    step = 'year';
  }

  var i = start;
  while (i < end) {
    result.push(roundDateToRunningPeriod(i, period, sameYear, sameMonth, sameDay));
    i = moment(new Date(i * 1000.0)).add(1, step).toDate().getTime() / 1000.0;
  }

  return result;
}

var chartObjectData = [];
function chartObjectForStat(id, response) {
  const stat = stats[id];
  var data = {datasets: []};
  const timeInterval = timePeriodForStat(stat);

  var labels;
  if (stat.mode === "accumTime") {
    labels = accumTimeDates(stat.period);
  } else {
    labels = dateLabelsForTimeInterval(timeInterval.start, timeInterval.end, stat.period);
  }

  data["labels"] = labels;
  const usedColors = fillEntityStatData(stat, data, labels, response);

  if (response.avg !== undefined) {
    data.datasets.push({
      data: pollPlotData(labels, response.avg),
      label: "Overall Average",
      borderColor: 'black',
      borderDash: [10,5]
    });
  }

  if (response.sum !== undefined) {
    data.datasets.push({
      data: pollPlotData(labels, response.sum),
      label: "Sum of Selected",
      borderColor: colorForIndex(0)
    });
    if (response.avg.sum !== undefined) {
      data.datasets.push({
        data: pollPlotData(labels, response.avg.sum),
        label: "Overall Average",
        borderColor: 'black',
        borderDash: [10,5]
      });
    }
  }

  fillEntityExpectedData(stat, data, timeInterval.start, timeInterval.end, labels, usedColors, response.exp);

  const chartData = {
    type: 'line',
    data: data,
    options: {
      responsive: true,
      legend: {
        labels: {
          fontColor: "white"
        },
        position: 'bottom'
      },
      scales: {
        yAxes: [{
          ticks: {
            fontColor: "white",
            beginAtZero: true,
            min: 0
          }
        }],
        xAxes: [{
          ticks: {
            fontColor: "white",
            beginAtZero: true
          }
        }]
      }
    }
  }

  if (id >= chartObjectData.length) {
    chartObjectData.push(chartData);
  } else {
    chartObjectData[id] = chartData;
  }
  return chartData;
}

function fillEntityStatData(stat, data, labels, source) {
  const formatter = formatterForGroup(stat.groupBy);
  const entities = entitiesForGroup(stat.groupBy);
  const colors = harvestColorful;
  var colorIdx = 0;
  var colorsUsed = {};

  for (const kidx in stat.ids[stat.groupBy]) {
    const key = stat.ids[stat.groupBy][kidx];
    var plotData = [];
    const info = source[key];
    const plottedData = pollPlotData(labels, info);
    const item = entities[key];

    if (info !== undefined && item !== undefined) {
      colorsUsed[key] = colorForIndex(colorIdx);
      data.datasets.push({
        data: plottedData,
        label: formatter(key, item),
        borderColor: colorsUsed[key]
      });
      colorIdx += 1;
    }
  }

  return colorsUsed;
}

function pollPlotData(labels, info) {
  var plotData = [];
  for (const lidx in labels) {
    const lbl = labels[lidx];
    if (info === undefined) {
      plotData.push(0);
      continue;
    }
    const point = info[lbl];
    if (point === undefined) {
      plotData.push(0);
    } else {
      plotData.push(point);
    }
  }
  return plotData;
}

function fillEntityExpectedData(stat, data, start, end, labels, usedColors, source) {
  const formatter = formatterForGroup(stat.groupBy);
  const entities = entitiesForGroup(stat.groupBy);

  for (const kidx in stat.ids[stat.groupBy]) {
    const key = stat.ids[stat.groupBy][kidx];
    var plotData = [];
    const info = source[key];
    const plottedData = pollExpectedPlotData(start, end, stat.period, labels, info);
    const item = entities[key];

    if (info !== undefined && item !== undefined) {
      data.datasets.push({
        data: plottedData,
        label: formatter(key, item) + " (exp)",
        borderColor: colorWithAlpha(usedColors[key], 0.5)
      });
    }
  }
}

function pollExpectedPlotData(start, end, period, labels, func) {
  const a = func.a;
  const b = func.b;
  const c = func.c;
  const d = func.d;

  const s = roundSince1970(start, period);
  const e = roundSince1970(end, period);

  const interval = labels.length;

  const diff = e - s;
  const move = diff / interval;

  var x = s;
  var result = [];

  while (x < e) {
    result.push(Math.round(a * Math.sin(b * x + c) + d));
    x += move;
  }

  return result;
}

var spinner = {};
function updateSpiner(shouldSpin, targetId) {
  var opts = {
		lines: 8, // The number of lines to draw
		length: 37, // The length of each line
		width: 10, // The line thickness
		radius: 20, // The radius of the inner circle
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
		shadow: '0 0 1px transparent', // Box-shadow for the lines
  };

  var target = document.getElementById(targetId); //This is where the spinner is gonna show
  if (shouldSpin) {
		if (spinner[targetId] == null) {
		  spinner[targetId] = new Spinner(opts).spin(target);
		}
  } else {
	  spinner[targetId].stop();
	  spinner[targetId] = null;
    delete spinner[targetId];
  }
}

function formatterForGroup(group) {
  if (group === "farm") {
    return (key, item) => { return item.name };
  } else if (group === "orchard") {
    return (key, item) => {
      const farm = farms[item.farm];
      return (farm === undefined ? key : farm.name) + " - " + item.name;
    }
  } else if (group === "worker" || group === "foreman") {
    return (key, item) => {
      return item.name + " " + item.surname
    };
  } else {
    return (key, item) => { return "?" };
  }
}

function entitiesForGroup(group) {
  if (group === "farm") {
    return farms;
  } else if (group === "orchard") {
    return orchards;
  } else if (group === "worker") {
    var result = {};
    for (const key in workers) {
      if (workers[key].type === "Worker") {
        result[key] = workers[key];
      }
    }
    return result;
  } else if (group === "foreman") {
    var result = {};
    for (const key in workers) {
      if (workers[key].type === "Foreman") {
        result[key] = workers[key];
      }
    }
    return result;
  }
}

function sortedStatList(id) {
  var result = [];
  const stat = stats[id];
  const items = entitiesForGroup(stat.groupBy);
  for (const key in items) {
    result.push({key: key, value: items[key]});
  }
  result = result.sort((a, b) => {
    formatter = stat.groupBy === "worker" || stat.groupBy === "foreman"
      ? (k, w) => { return w.surname + " " + w.name }
      : formatterForGroup(stat.groupBy);
    return formatter(a.key, a.value).localeCompare(formatter(b.key, b.value));
  });
  return result;
}
