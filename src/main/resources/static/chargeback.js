/****************************HashTable Functionality**************************/

function HashTable(obj)
{
    this.length = 0;
    this.items = {};
    for (var p in obj) {
        if (obj.hasOwnProperty(p)) {
            this.items[p] = obj[p];
            this.length++;
        }
    }

    this.addItem = function(key, value)
    {
        var previous = undefined;
        if (this.hasItem(key)) {
            previous = this.items[key];
        }
        else {
            this.length++;
        }
        this.items[key] = value;
        return previous;
    }

    this.getItem = function(key) {
        return this.hasItem(key) ? this.items[key] : undefined;
    }

    this.hasItem = function(key)
    {
        return this.items.hasOwnProperty(key);
    }
   
    this.removeItem = function(key)
    {
        if (this.hasItem(key)) {
            previous = this.items[key];
            this.length--;
            delete this.items[key];
            return previous;
        }
        else {
            return undefined;
        }
    }

    this.keys = function()
    {
        var keys = [];
        for (var k in this.items) {
            if (this.hasItem(k)) {
                keys.push(k);
            }
        }
        return keys;
    }

    this.values = function()
    {
        var values = [];
        for (var k in this.items) {
            if (this.hasItem(k)) {
                values.push(this.items[k]);
            }
        }
        return values;
    }

    this.each = function(fn) {
        for (var k in this.items) {
            if (this.hasItem(k)) {
                fn(k, this.items[k]);
            }
        }
    }

    this.clear = function()
    {
        this.items = {}
        this.length = 0;
    }
}
var getYesterdaysDate = function(){
	return moment().add(-1, 'days').format("YYYY-MM-DD");
}
/************************************HashTable Ends******************************************/

var colors = [

"#00ffff", "#c8c811", "#000000", "#0000ff", "#a52a2a", "#00ffff",
		"#846156", "#008b8b", "#a9a9a9", "#006400", "#bdb76b", "#8b008b",
		"#556b2f", "#ff8c00", "#9932cc", "#8b0000", "#e9967a", "#715e7a",
		"#ff00ff", "#ffd700", "#008000", "#4b0082", "#f0e68c", "#add8e6",
		"#3beaff", "#90ee90", "#d3d3d3", "#ffb6c1", "#84846d", "#00ff00",
		"#ff00ff", "#800000", "#000080", "#808000", "#ffa500", "#ffc0cb",
		"#800080", "#145690", "#ff0000", "#c0a3a3",  "#607b1b", "#6894d2" ];

var memoryConv=["B","KB","MB","GB","TB" ];
var memoryFig=[1024,1048576];
var h= new HashTable({one:1});

var selectedTab = "summaryCostTab";

var getcolorArray = function(labeList) {
	var ids = [];
	var colorArray = [];
	for (var i = 0; i < colors.length; ++i) {
		ids.push(colors[i]);
	}
	for (var i = 0; i < labeList.length; i++) {
		var random = Math.round(Math.random() * ids.length);
		var color = ids.splice(random, 1);
		colorArray.push(color[0])
	}
	console.log(colorArray);
	return colorArray;
}

var getOrganisations = function(){
	
	$.ajax({
		url : "getOrgList",
		success : function(data) {
			console.log(" Org List::" + data)
			populateOrgDropDown(data);
		}
	});
}

var populateOrgDropDown = function(vals){
	 $("#OrgSelect").empty();
	 $.each(vals, function(index, value){
		 $("#OrgSelect").append("<option>" + value + "</option>");
	 });
	 $("#OrgSelect").change(function(){
		var selectedOrg = $( "#OrgSelect option:selected" ).text();
		clearAllCharts();

		console.log(selectedOrg);
		 $.ajax({
				url : "getSpaceList/" + selectedOrg,
				success : function(data) {
					console.log(" Space List::" + data)
					populateSpaceDropDown(data);
				}
			});
		 
	 });
	 if($( "#OrgSelect option:selected" ).text() ==="" || $( "#OrgSelect option:selected" ).text() === undefined || $( "#OrgSelect option:selected" ).text() === null){
			return;
		}else{
			 $.ajax({
					url : "getSpaceList/" + $( "#OrgSelect option:selected" ).text(),
					success : function(data) {
						console.log(" Space List::" + data)
						populateSpaceDropDown(data);
					}
				});
		}
}


var clearAllCharts = function(){
	
	var vals = ["summaryCost","memoryCost", "cpuCost","diskCost", "memory", "cpu","disk"];
	$.each(vals, function(value, index) {
		if(h.getItem(value) != undefined){
			h.getItem(value).destroy();
		}
	})
}
	
var populateSpaceDropDown = function(vals){
	
	$("#OrgSpace").empty();
	var y = document.getElementsByClassName('active tablinks');
	console.log(y);
	 $.each(vals, function(index, value){
		 $("#OrgSpace").append("<option>" + value + "</option>");
	 });
	
	 $("#OrgSpace").change(function(){
			var selectedSpace = $( "#OrgSpace option:selected" ).text();
			clearAllCharts();
			displayBasedOnTab(selectedTab);
			 
		 });
	 
	displayBasedOnTab(selectedTab);
	
}


var displayBasedOnTab = function(name){

	console.log(name);
	switch(name){
	case "summaryCostTab":
		clearAllCharts();
		getSummaryCostDetails();
		break;
		
	case "memoryCostTab":
		clearAllCharts();
		getMemoryCostDetails();
		break;
		
	case "cpuCostTab":
		clearAllCharts();
		getCPUCostDetails();
		break;
		
	case "diskCostTab":
		clearAllCharts();
		getDiskCostDetails();
		break;
		
	case "memoryTab":
		clearAllCharts();
		getMemoryUsageDetails();
		break;
	case "diskTab":
		clearAllCharts();
		getDiskUsageDetails();
		break;
	case "cpuTab":
		clearAllCharts();
		getCPUUsageDetails();
		break;
	}

}

var periodChange = function(){
	$("#PreiodSelect").change(function(){
		displayBasedOnTab(selectedTab);
		 
	 });
}
var getStartDate = function(){
	if($("#PreiodSelect option:selected").text()==="Yesterday"){
		return  getYesterdaysDate();
		var endDate = getYesterdaysDate();
	}else if($("#PreiodSelect option:selected").text()==="Month"){
		return  moment().subtract(1,'months').startOf('month').format("YYYY-MM-DD");
	}else{
		return  getYesterdaysDate();
	}
	
	
	 
	
}


var getEndDate = function(){
	
	if($("#PreiodSelect option:selected").text()==="Yesterday"){
		return getYesterdaysDate();
	}else{
	return moment().subtract(1,'months').endOf('month').format("YYYY-MM-DD");
	}
}

var getSummaryCostDetails = function() {
	var start = getStartDate();
	var end = getEndDate();
	console.log("End Date ::" + end)
	$('#spinner').show();
	$.ajax({
		url : "getCostDetails/COST/SUMMARY/" + start + "/" + end,
		success : function(data) {
			 $('#spinner').hide();
			populateChartDetails(data, "summaryCost", 'pie',"cost");
			
		}
		
	});

}

var getMemoryCostDetails = function() {
	var start = getStartDate();
	var end = getEndDate();
	$('#spinner').show();
	$.ajax({
		url : "getCostDetails/COST/MEM/"  + start + "/" + end,
		success : function(data) {
			 $('#spinner').hide();
			populateChartDetails(data, "memoryCost", 'pie',"cost");
		}
	});

}

var getCPUCostDetails = function() {
	var start = getStartDate();
	var end = getEndDate();
	$('#spinner').show();
	$.ajax({
		url : "getCostDetails/COST/CPU/"  + start + "/" + end,
		success : function(data) {
			 $('#spinner').hide();
			populateChartDetails(data, "cpuCost", 'pie',"cost");
		}
	});

}



var getDiskCostDetails = function() {
	var start = getStartDate();
	var end = getEndDate();
	$('#spinner').show();

	$.ajax({
		url : "getCostDetails/COST/DISK/"  + start + "/" + end,
		success : function(data) {
			$('#spinner').hide();
			populateChartDetails(data, "diskCost", 'pie',"cost");
		}
	});

}

var getMemoryUsageDetails = function() {
	if($( "#OrgSelect option:selected" ).text() ==="" || $( "#OrgSelect option:selected" ).text() === undefined || $( "#OrgSelect option:selected" ).text() === null){
		return;
	}
	if($( "#OrgSpace option:selected" ).text() ==="" || $( "#OrgSpace option:selected" ).text() === undefined || $( "#OrgSpace option:selected" ).text() === null){
		return;
	}
	$('#spinner').show();

	$.ajax({
		url : "getResourceDetails/USED/MEM/" + $( "#OrgSelect option:selected" ).text() + "/" +$( "#OrgSpace option:selected" ).text(),
		success : function(data) {
			$('#spinner').hide();

			populateChartDetails(data, "memory", 'pie');
		}
	});

}

/*Getting Unused Memory Details*/
var getUnusedDetails = function() {
	if($( "#OrgSelect option:selected" ).text() ==="" || $( "#OrgSelect option:selected" ).text() === undefined || $( "#OrgSelect option:selected" ).text() === null){
		return;
	}
	if($( "#OrgSpace option:selected" ).text() ==="" || $( "#OrgSpace option:selected" ).text() === undefined || $( "#OrgSpace option:selected" ).text() === null){
		return;
	}
	$('#spinner').show();

	$.ajax({
		url : "getResourceDetails/UNUSED/MEM/" + $( "#OrgSelect option:selected" ).text() + "/" +$( "#OrgSpace option:selected" ).text(),
		success : function(data) {
			$('#spinner').hide();

			populateChartDetails(data, "unusedMemory",'pie');
		}
	});

}
var getCPUUsageDetails = function() {
	if($( "#OrgSelect option:selected" ).text() ==="" || $( "#OrgSelect option:selected" ).text() === undefined || $( "#OrgSelect option:selected" ).text() === null){
		return;
	}
	if($( "#OrgSpace option:selected" ).text() ==="" || $( "#OrgSpace option:selected" ).text() === undefined || $( "#OrgSpace option:selected" ).text() === null){
		return;
	}
	$('#spinner').show();

	$.ajax({
		url : "getResourceDetails/USED/CPU/" + $( "#OrgSelect option:selected" ).text() + "/" +$( "#OrgSpace option:selected" ).text(),
		success : function(data) {
			$('#spinner').hide();
			populateChartDetails(data, "cpu", 'pie');
		}
	});

}

var getFreeCPUUsageDetails = function() {
	if($( "#OrgSelect option:selected" ).text() ==="" || $( "#OrgSelect option:selected" ).text() === undefined || $( "#OrgSelect option:selected" ).text() === null){
		return;
	}
	if($( "#OrgSpace option:selected" ).text() ==="" || $( "#OrgSpace option:selected" ).text() === undefined || $( "#OrgSpace option:selected" ).text() === null){
		return;
	}
	$('#spinner').show();

	$.ajax({
		url : "getResourceDetails/UNUSED/CPU/" + $( "#OrgSelect option:selected" ).text() + "/" +$( "#OrgSpace option:selected" ).text(),
		success : function(data) {
			$('#spinner').hide();
			populateChartDetails(data, "freeCPU", 'pie');
		}
	});

}
/* Getting CPU Usage*/

/* Getting Disk Usage*/
var getDiskUsageDetails = function() {
	if($( "#OrgSelect option:selected" ).text() ==="" || $( "#OrgSelect option:selected" ).text() === undefined || $( "#OrgSelect option:selected" ).text() === null){
		return;
	}
	if($( "#OrgSpace option:selected" ).text() ==="" || $( "#OrgSpace option:selected" ).text() === undefined || $( "#OrgSpace option:selected" ).text() === null){
		return;
	}
	$('#spinner').show();

	$.ajax({
		url : "getResourceDetails/USED/DISK/" + $( "#OrgSelect option:selected" ).text() + "/" +$( "#OrgSpace option:selected" ).text(),
		success : function(data) {
			$('#spinner').hide();

			populateChartDetails(data, "disk", 'pie');
		}
	});

}


/* Utility function to create a String Array*/

var getlabelsArray = function(labeList) {
	var labelsArray = [];
	for (var i = 0; i < labeList.length; i++) {
		labelsArray.push(labeList[i]);
	}
	return labelsArray;
};

/* Utility Function to create an Integer Array*/

var getdataArray = function(data, chartType, id) {
	var dataArray = [];
	var divisor=1;
	if(chartType==='bar' && id==='disk'){
		divisor = 1024*1024;
	}
	for (var i = 0; i < data.length; i++) {
		dataArray.push(parseFloat(data[i])/divisor);
	}
	return dataArray;
};


var getClientName = function(){
	
	$.ajax({
		url : "getClient",
		success : function(data) {
			console.log("Client Name::" + data);
			if(data=='Kroger'){
				document.getElementById("clientImg").src="1px.png";
			}else{
				document.getElementById("clientImg").src="1px.png";
			}
		}
	});
}

/* Function to populate chart Details */
var populateChartDetails = function(data, id, chartType, utilizationBy) {
	
	console.log("id ---- " +id);
	
	
	var canvasId = document.getElementById(id);
	var colorArray = getcolorArray(data.label);
	var chartData = {
		labels : getlabelsArray(data.label),
		datasets : [ {
			data : getdataArray(data.data, chartType, id),
			backgroundColor : colorArray,
			hoverBackgroundColor : colorArray
		} ]
	};
	
	var ctx = canvasId.getContext("2d");
	var midX = canvasId.width / 2;
	var midY = canvasId.height / 2;
console.log("chartData.data : " + chartData);
    
if(h.getItem(id) != undefined){
	h.getItem(id).destroy();
}
var chartLegend = true;
if(chartType==='bar'){
	chartLegend=false;
}

    var pieChart = new Chart(canvasId, {
		type : chartType,
		data : chartData,
		options : {
			responsive : false,
			//onAnimationProgress:  drawSegmentValues,
				tooltips : {
				callbacks : {
					label : function(tooltipItem, data) {
						var dataset = data.datasets[tooltipItem.datasetIndex];
						var total = dataset.data.reduce(function(previousValue,
								currentValue, currentIndex, array) {
							return previousValue + currentValue;
						});
						var currentValue = dataset.data[tooltipItem.index];
						var returnElement;
						if(utilizationBy=="cost"){
							return "$"+currentValue;
						}else if(!id.toUpperCase().includes("CPU")){
							if(currentValue>=  memoryFig[1]){
								return ((currentValue/parseInt(memoryFig[1])).toFixed(2) + memoryConv[2]);
							}else if(currentValue>=memoryFig[0] && currentValue<  memoryFig[1] ){
								return (currentValue + memoryConv[1]);
							}else if(currentValue<memoryFig[0]){
								return (currentValue + memoryConv[0]);
							} 
						}else{
							var precentage = Math.floor(((currentValue / total) * 100) + 0.5);
							return precentage + "%";
						}
						
					}
				}
			},
    legend:{
        display:false
    }
			
		}
	});
 h.addItem(id, pieChart);
	var radius = pieChart.outerRadius;
 
	 var legend = pieChart.generateLegend();
	 console.log("legend ---- " +legend);
	document.getElementById(id+"Legend").innerHTML = legend;
}




function getDropDownList( id, optionList) {
	
		var selectElement= document.getElementById(id);
		for (var i = 0; i<=optionList.length-1; i++){
		    var opt = document.createElement('option');
		    opt.value = optionList[i];
		    opt.innerHTML = optionList[i];
		    selectElement.appendChild(opt);
		}
}
function openTab(evt, className, linkClassName, tabName) {
	var i, tabcontent, tablinks;
	console.log("className : " + className);
	console.log("linkClassName : " + linkClassName);
	console.log("tabName : " + tabName);
  //  var tab = document.getElementsByName(tabName);
    selectedTab = tabName;
    document.getElementById(tabName).style.visibility  = 'visible';
    tabcontent = document.getElementsByClassName(className);
    
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }
    tablinks = document.getElementsByClassName(linkClassName);
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }
    document.getElementById(tabName).style.display = "block";
    evt.currentTarget.className += " active";
    
}

defaultSelectedTab = function(tabName,className,linkClassName,activeElement){
	selectedTab = tabName;
	console.log(" document.getElementById(tabName).className :----- " + document.getElementById(tabName).className);
	displayBasedOnTab(selectedTab);
	document.getElementById(tabName).style.visibility  = 'visible';
    tabcontent = document.getElementsByClassName(className);
    
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }
    tablinks = document.getElementsByClassName(linkClassName);
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }
    document.getElementById(tabName).style.display = "block";
    if(!document.getElementById(activeElement).className.includes(" active")){
    	document.getElementById(activeElement).className=  document.getElementById(activeElement).className + " active";
	}
    
    expandPanelBody = function(elementName){
    	document.getElementById(tabName).style.width="1000px";
    	
    }
}