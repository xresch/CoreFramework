
/**************************************************************************************************************
 * Contains the various formatter methods.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025
 * @license MIT-License
 **************************************************************************************************************/

/**************************************************************************************
 * Create a timestamp string
 * @param epoch unix epoch milliseconds since 01.01.1970
 * @return timestamp as string
 *************************************************************************************/
function cfw_format_epochToTimestamp(epoch){
	
	//--------------------------
	// Checks
	if(CFW.utils.isNullOrEmpty(epoch)){ return ""; }
	if( isNaN(epoch) ){ return ""; }
	if(typeof epoch == "string"){ epoch = parseInt(epoch) }
  

	return new  moment(epoch).format('YYYY-MM-DD HH:mm:ss');

}

/**************************************************************************************
 * Create a date string Like 2022-02-22
 * @param epoch unix epoch milliseconds since 01.01.1970
 * @return date as string
 *************************************************************************************/
function cfw_format_epochToDate(epoch){
	
	//--------------------------
	// Checks
	if(CFW.utils.isNullOrEmpty(epoch)){ return ""; }
	if( isNaN(epoch) ){ return ""; }
	if(typeof epoch == "string"){ epoch = parseInt(epoch) }
	
	//--------------------------
	// Format
	var a = new Date(epoch);
	var year 		= a.getFullYear();
	var month 	= a.getMonth()+1 < 10 	? "0"+(a.getMonth()+1) : a.getMonth()+1;
	var day 		= a.getDate() < 10 		? "0"+a.getDate() : a.getDate();
	
	var time = year + '-' + month + '-' + day ;
	return time;
}

/**************************************************************************************
 * Create a short date string like "Wed 6 Aug 25 23:00".
 * 
 * @param epoch unix epoch milliseconds since 01.01.1970
 * @return date as string
 *************************************************************************************/
function cfw_format_epochToShortDate(epoch){
	
	//--------------------------
	// Checks
	if(CFW.utils.isNullOrEmpty(epoch)){ return ""; }
	if( isNaN(epoch) ){ return ""; }
	if(typeof epoch == "string"){ epoch = parseInt(epoch) }
  
	const date = new Date(epoch);

	const weekday = date.toLocaleDateString('en-US', { weekday: 'short' }); // "Wed"
	const day = date.getDate(); // 6
	const month = date.toLocaleDateString('en-US', { month: 'short' }); // "Aug"
	const year = String(date.getFullYear()).slice(-2); // "25"
	const time = date.toLocaleTimeString('en-US', {
		hour: '2-digit',
		minute: '2-digit',
		hour12: false
	}); // "23:00"

	return `${weekday} ${day} ${month} ${year} ${time}`;
	
}


/**************************************************************************************
 * Create a clock string in the format HH:mm:ss from an epoch time
 * @param epoch milliseconds of epoch date and time
 * @return clock string
 *************************************************************************************/
function cfw_format_millisToClock(epoch){

	//--------------------------
	// Checks
	if(CFW.utils.isNullOrEmpty(epoch)){ return ""; }
	if( isNaN(epoch) ){ return ""; }
	if(typeof epoch == "string"){ epoch = parseInt(epoch) }
	

	return moment.utc(epoch).format("HH:mm:ss");
  
  
  return "";
}


/**************************************************************************************
 * Create a duration string in the format # d ##h ##m ##.#s 
 * @param timeValue amount of time
 * @param timeUnit unit of time, one of: ns | us | ms | s | m | h (Default: ms)
 * @return clock string
 *************************************************************************************/
function cfw_format_timeToDuration(timeValue, timeUnit){
	
	var millisValue = timeValue;
	
	//--------------------------------
	// If null, assume is milliseconds
	if(timeUnit != null && timeValue != null ){
		switch(timeUnit){
			case 'ns':	millisValue = timeValue / 1000000; break
			case 'us':	millisValue = timeValue / 1000; break
			//case 'ms':	millisValue = timeValue; break
			case 's':	millisValue = timeValue * 1000; break
			case 'm':	millisValue = timeValue * 1000 * 60; break
			case 'h':	millisValue = timeValue * 1000 * 60 * 60; break
			default: 	millisValue = timeValue; 
		}
	}
	
	return cfw_format_millisToDuration(millisValue);
}

/**************************************************************************************
 * Create a duration string in the format # d ##h ##m ##.#s 
 * @param epoch milliseconds
 * @return clock string
 *************************************************************************************/
function cfw_format_millisToDuration(epoch){
	
	//--------------------------
	// Checks
	if(CFW.utils.isNullOrEmpty(epoch)){ return ""; }
	if( isNaN(epoch) ){ return ""; }
	if(typeof epoch == "string"){ epoch = parseInt(epoch) }
	
	//--------------------------
	// Format
	var isNegative = (epoch < 0);
	epoch = Math.abs(epoch);
	
	var milliseconds = parseInt((epoch % 1000) / 100);
	var seconds = Math.floor((epoch / 1000) % 60);
	var minutes = Math.floor((epoch / (1000 * 60)) % 60);
	var hours = Math.floor((epoch / (1000 * 60 * 60)) % 24);
	var days = Math.floor( epoch / (1000 * 60 * 60 * 24) );
	

	
	hours = (hours < 10) ? "0" + hours : hours;
	minutes = (minutes < 10) ? "0" + minutes : minutes;
	seconds = (seconds < 10) ? "0" + seconds : seconds;
	
	var clockString = seconds + "." + milliseconds + "s";
	
	if(minutes != "00" 
	|| hours != "00" 
	|| days != 0
	){
		clockString = minutes + "m "+clockString;
	}
	
	if(hours != "00" 
	|| days != 0){
		clockString = hours + "h " +clockString;
		
	}
	
	if(days != 0){
		clockString = days + "d " +clockString;
	}
	
	if(isNegative){
		clockString = "-" +clockString;
	}
	return clockString;
	
}

/**************************************************************************************
 * Create a duration string in the format HH:MM:SS 
 * @param epoch milliseconds
 * @return clock string
 *************************************************************************************/
function cfw_format_millisToDurationClock(epoch){
	
	//--------------------------
	// Checks
	if(CFW.utils.isNullOrEmpty(epoch)){ return ""; }
	if( isNaN(epoch) ){ return ""; }
	if(typeof epoch == "string"){ epoch = parseInt(epoch) }
	
	//--------------------------
	// Format
	var isNegative = (epoch < 0);
	epoch = Math.abs(epoch);
	
	var seconds = Math.floor((epoch / 1000) % 60);
	var minutes = Math.floor((epoch / (1000 * 60)) % 60);
	var hours = Math.floor((epoch / (1000 * 60 * 60)));

	hours = (hours < 10) ? "0" + hours : hours;
	minutes = (minutes < 10) ? "0" + minutes : minutes;
	seconds = (seconds < 10) ? "0" + seconds : seconds;
	
	var clockString = hours + ":" + minutes + ":" +seconds ;
	
	if(isNegative){
		clockString = "-" +clockString;
	}
	
	return clockString;
	
	    

}

/**************************************************************************************
 * Create a short label for the time range. Parameters cannot be null.
 * 
 * @param earliestMillis unix epoch milliseconds since 01.01.1970
 * @param latestMillis unix epoch milliseconds since 01.01.1970
 * @return string 
 *************************************************************************************/
function cfw_format_timerangeToString(earliestMillis, latestMillis){

	//-----------------------------------
	// Checks
	if(typeof earliestMillis == "string"){ earliestMillis = parseInt(earliestMillis); }
	if(typeof latestMillis == "string"){ latestMillis = parseInt(latestMillis); }
    
    //-----------------------------------
	// Now Values
	const nowDate 		= new Date();
	const nowYear 		= String(nowDate.getFullYear()).slice(-2); // e.g. "25"
	const nowMonth 		= nowDate.toLocaleDateString('en-US', { month: 'short' }); // e.g. "Aug"
	const nowDay 		= nowDate.getDate(); // e.g. 6

	
  	//-----------------------------------
	// Earliest Values
	const earliestDate 		= new Date(earliestMillis);
	const earliestYear 		= String(earliestDate.getFullYear()).slice(-2); // e.g. "25"
	const earliestMonth 	= earliestDate.toLocaleDateString('en-US', { month: 'short' }); // e.g. "Aug"
	const earliestDay 		= earliestDate.getDate(); // e.g. 6
	const earliestTime 		= earliestDate.toLocaleTimeString('en-US', {
		hour: '2-digit',
		minute: '2-digit',
		hour12: false
	}); // e.g. "23:00"
	
	//-----------------------------------
	// Latest Values
	const latestDate 		= new Date(latestMillis);
	const latestYear 		= String(latestDate.getFullYear()).slice(-2); // e.g. "25"
	const latestMonth 		= latestDate.toLocaleDateString('en-US', { month: 'short' }); // e.g. "Aug"
	const latestDay 		= latestDate.getDate(); // e.g. 6
	const latestTime 		= latestDate.toLocaleTimeString('en-US', {
		hour: '2-digit',
		minute: '2-digit',
		hour12: false
	}); // e.g. "23:00"
	
	
	//-----------------------------------
	// Evaluate what is Displayed
	
	let displayYear = false;
	if( earliestYear != latestYear
	|| nowYear != earliestYear
	|| nowYear != latestYear
	){
		displayYear = true;
	}
	
	let displayMonth = displayYear; // display month if year is displayed
	if( earliestMonth != latestMonth
	|| nowMonth != earliestMonth
	|| nowMonth != latestMonth
	){
		displayMonth = true;
	}
	
	let displayDay = displayMonth; // display day if month is displayed
	if( earliestDay != latestDay
	|| nowDay != earliestDay
	|| nowDay != latestDay
	){
		displayDay = true;
		displayMonth = true; // always display month if day is displayed
	}
		
	let displayTime = false; 
	if( !displayYear){
		displayTime = true;
	}
	
	
	//-----------------------------------
	// Evaluate what is Displayed
	let earliestFormatted = "";
	let latestFormatted = "";
	
	if(displayYear){
		earliestFormatted 	= " " + earliestYear;
		latestFormatted 	= " " + latestYear;
	}
	
	if(displayMonth){
		earliestFormatted 	= ` ${earliestMonth} ${earliestFormatted}`;
		latestFormatted 	= ` ${latestMonth} ${latestFormatted}`;
	}
	
	if(displayDay){
		earliestFormatted 	= ` ${earliestDay} ${earliestFormatted}`;
		latestFormatted 	= ` ${latestDay} ${latestFormatted}`;
	}
	
	if(displayTime){
		earliestFormatted 	= `${earliestFormatted} ${earliestTime}`;
		latestFormatted 	= `${latestFormatted} ${latestTime}`;
	}
		
	return `${earliestFormatted} to ${latestFormatted}`;

}


/**************************************************************************************
 * Create a timestamp string
 * @param epoch unix epoch milliseconds since 01.01.1970
 * @return timestamp as string
 *************************************************************************************/
function cfw_format_cfwSchedule(scheduleData){

	if(scheduleData == null || scheduleData.timeframe == null){
		return "";
	}
	
	var result = "<div>";
	
	if( !CFW.utils.isNullOrEmpty(scheduleData.timeframe.startdatetime) ) { result += '<span><b>Start:&nbsp</b>'+CFW.format.epochToTimestamp(scheduleData.timeframe.startdatetime) +'</span><br/>'; }
	
	if (scheduleData.timeframe.endtype == "RUN_FOREVER"){ result += '<span><b>End:&nbsp</b> Run Forever</span><br/>'; }
	else if (scheduleData.timeframe.endtype == "EXECUTION_COUNT"){ result += '<span><b>End:&nbsp</b>'+scheduleData.timeframe.executioncount+' execution(s)</span><br/>'; }
	else if( !CFW.utils.isNullOrEmpty(scheduleData.timeframe.enddatetime) ) { result += '<span><b>End:&nbsp</b>'+CFW.format.epochToTimestamp(scheduleData.timeframe.enddatetime) +'</span><br/>'; }

	//values: EVERY_X_MINUTES, EVERY_X_DAYS, EVERY_WEEK, CRON_EXPRESSION
	if (scheduleData.interval.intervaltype == "EVERY_X_MINUTES"){ result += '<span><b>Interval:&nbsp</b> Every '+scheduleData.interval.everyxminutes+' minute(s)</span><br/>'; }
	else if (scheduleData.interval.intervaltype == "EVERY_X_DAYS"){ result += '<span><b>Interval:&nbsp</b> Every '+scheduleData.interval.everyxdays+' day(s)</span><br/>'; }
	else if (scheduleData.interval.intervaltype == "CRON_EXPRESSION"){ result += '<span><b>Interval:&nbsp</b> Cron Expression "'+scheduleData.interval.cronexpression+'"</span><br/>'; }
	else if (scheduleData.interval.intervaltype == "EVERY_WEEK"){ 
		result += '<span><b>Interval:&nbsp</b> Every week on '; 
		let days = "";
		if(scheduleData.interval.everyweek.MON){ days += "MON/"; }
		if(scheduleData.interval.everyweek.TUE){ days += "TUE/"; }
		if(scheduleData.interval.everyweek.WED){ days += "WED/"; }
		if(scheduleData.interval.everyweek.THU){ days += "THU/"; }
		if(scheduleData.interval.everyweek.FRI){ days += "FRI/"; }
		if(scheduleData.interval.everyweek.SAT){ days += "SAT/"; }
		if(scheduleData.interval.everyweek.SUN){ days += "SON/"; }
		
		days = days.substring(0, days.length-1);
		result += days;
	}
	
  return result;
}

/**************************************************************************************
 * Splits up CFWSchedule data and adds the following fields to each record:
 * SCHEDULE_START, SCHEDULE_END, SCHEDULE_INTERVAL
 * The above fields contain a formatted string representing the value defined by the schedule.
 * This method is useful to display schedule definitions better in tables.
 * 
 * @param dataArray containing the records with a schedule field
 * @param fieldname the name of the schedule field
 *************************************************************************************/
function cfw_format_splitCFWSchedule(dataArray, fieldname){
	
	for(let index in dataArray){

		let currentRecord = dataArray[index];
		let scheduleData = currentRecord[fieldname];
		
		if( scheduleData != null && scheduleData.timeframe != null){ 

			if( !CFW.utils.isNullOrEmpty(scheduleData.timeframe.startdatetime) ) { currentRecord.SCHEDULE_START = CFW.format.epochToTimestamp(scheduleData.timeframe.startdatetime); }
			
			if (scheduleData.timeframe.endtype == "RUN_FOREVER"){ currentRecord.SCHEDULE_END = "Run Forever"; }
			else if (scheduleData.timeframe.endtype == "EXECUTION_COUNT"){ currentRecord.SCHEDULE_END = "After "+scheduleData.timeframe.executioncount+" execution(s)"; }
			else if( !CFW.utils.isNullOrEmpty(scheduleData.timeframe.enddatetime) ) { currentRecord.SCHEDULE_END = CFW.format.epochToTimestamp(scheduleData.timeframe.enddatetime); }

			//values: EVERY_X_MINUTES, EVERY_X_DAYS, EVERY_WEEK, CRON_EXPRESSION
			if (scheduleData.interval.intervaltype == "EVERY_X_MINUTES"){ currentRecord.SCHEDULE_INTERVAL = 'Every '+scheduleData.interval.everyxminutes+' minute(s)'; }
			else if (scheduleData.interval.intervaltype == "EVERY_X_DAYS"){ currentRecord.SCHEDULE_INTERVAL = 'Every '+scheduleData.interval.everyxdays+' day(s)'; }
			else if (scheduleData.interval.intervaltype == "CRON_EXPRESSION"){ currentRecord.SCHEDULE_INTERVAL = 'CRON: "'+scheduleData.interval.cronexpression+'"'; }
			else if (scheduleData.interval.intervaltype == "EVERY_WEEK"){ 
				
				let days = "";
				if(scheduleData.interval.everyweek.MON){ days += "MON/"; }
				if(scheduleData.interval.everyweek.TUE){ days += "TUE/"; }
				if(scheduleData.interval.everyweek.WED){ days += "WED/"; }
				if(scheduleData.interval.everyweek.THU){ days += "THU/"; }
				if(scheduleData.interval.everyweek.FRI){ days += "FRI/"; }
				if(scheduleData.interval.everyweek.SAT){ days += "SAT/"; }
				if(scheduleData.interval.everyweek.SUN){ days += "SON/"; }
				
				days = days.substring(0, days.length-1);
				currentRecord.SCHEDULE_INTERVAL = 'Every week on '+days;
			}
			
		}else{
			currentRecord.SCHEDULE_START = "&nbsp;";
			currentRecord.SCHEDULE_END = "&nbsp;";
			currentRecord.SCHEDULE_INTERVAL = "&nbsp;";
		}
		
	}
}

/**************************************************************************************
 * Takes values and converts them into a horizontal box plot.
 * @param values object like 
 * 		{
 * 			  "start":3.437
 * 			, "min":4.517
 * 			, "low":5.12
 * 			, "high":4.887
 * 			, "max":5.529
 * 			, "end":7.061
 * 		}
 * @param color a CSS color used for the boxplot
 * @param isEpochTime set to true to convert the values to a timestamp for the popover
 *************************************************************************************/
function cfw_format_boxplot(values, color, isEpochTime){
	
	var boxplotDiv = $('<div class="w-100 h-100 d-flex align-items-center"></div>');

	let popover = true;
	
	//----------------------------------
	// Check
	if(values == null){	return boxplotDiv; }
	
	let start = values.start;
	let min = values.min;
	let low = values.low;
	let median = values.median;
	let high = values.high;
	let max = values.max;
	let end = values.end;
	
	//----------------------------------
	// Convert Time
	if(isEpochTime){
		values.start = CFW.format.epochToTimestamp(values.start);
		values.min = CFW.format.epochToTimestamp(values.min);
		values.low = CFW.format.epochToTimestamp(values.low);
		values.median = CFW.format.epochToTimestamp(values.median);
		values.high = CFW.format.epochToTimestamp(values.high);
		values.max = CFW.format.epochToTimestamp(values.max);
		values.end = CFW.format.epochToTimestamp(values.end);
	}
	
	
	//----------------------------------
	// Initialize
	
	if(CFW.utils.isNullOrEmpty(color)){	color = "cfw-blue"; }
	
	if(low == null){	 low = (min != null) ? min : 0; }
	if(high == null){	 high = (max != null) ? max : 0; }
	if(low > high){
		// swap low and hi if reversed
		let temp = low;
		low = high;
		high = temp;
		
	}
	
	if(min == null ){	min = low; }
	if(max == null ){	max = high; }

	
	if(start == null){start = min; }
	if(end == null){	end = max; }
	
	//----------------------------------
	// Calculate Absolute Sizes
	let totalWidth 			= end - start;
	let minOffset 			= min - start;
	let minWhiskerWidth 	= low - min;
	let boxWidth 			= high - low;
	let maxWhiskerWidth 	= max - high;

	//----------------------------------
	// Calculate Percentages
	let tickWidthPerc = 0.5;
	
	let minOffsetPerc  			= (minOffset 		/ totalWidth) * 100 - (tickWidthPerc / 2);
	let minWhiskerWidthPerc  	= (minWhiskerWidth  / totalWidth) * 100 - (tickWidthPerc / 2);
	let boxWidthPerc  			= (boxWidth 		/ totalWidth) * 100;
	let maxWhiskerWidthPerc  	= (maxWhiskerWidth  / totalWidth) * 100 - (tickWidthPerc / 2);
	
	//----------------------------------
	// Create a Boxplot Div
		
	//=====================================
	// Add Boxes
	
	let minOffsetDiv 		= $('<div>&nbsp;</div>');	
	let minTickDiv 			= $('<div>&nbsp;</div>');	
	let minWhiskerWidthDiv	= $('<div>&nbsp;</div>');	
	let boxDiv				= $('<div></div>');	
	let maxWhiskerWidthDiv	= $('<div>&nbsp;</div>');	
	let maxTickDiv			= $('<div>&nbsp;</div>');	
	
	minOffsetDiv.css(		"width", minOffsetPerc+'%');
	minTickDiv.css(			"width", tickWidthPerc+'%');
	minTickDiv.css(			"max-width", '2px');
	minWhiskerWidthDiv.css(	"width", minWhiskerWidthPerc+'%');	
	boxDiv.css(				"width", boxWidthPerc+'%');
	maxWhiskerWidthDiv.css(	"width", maxWhiskerWidthPerc+'%');
	maxTickDiv.css(			"width", tickWidthPerc+'%');
	maxTickDiv.css(			"max-width", '2px');

	minTickDiv.css(	"height", '75%');
	minWhiskerWidthDiv.css(	"height", '1px');
	boxDiv.css(	"height", '100%');
	maxWhiskerWidthDiv.css(	"height", '1px');
	maxTickDiv.css(	"height", '75%');

		
	//=====================================
	// Add Median
	
	if(median == null || median == undefined){
		boxDiv.append("&nbsp;");
	}else{
		medianOffset = median - low;
		let medianOffsetPerc = (medianOffset / boxWidth) * 100;
		
		let medianDiv = $('<div class="h-100">&nbsp;</div>');	
		medianDiv.css("width", medianOffsetPerc+'%');
		medianDiv.css("border-right", "2px solid white")
		boxDiv.append(medianDiv);
	}	  
	   
	//=====================================
	// Add Colors
	CFW.colors.colorizeElement(minTickDiv		 , color, "bg")
	CFW.colors.colorizeElement(minWhiskerWidthDiv, color, "bg")
	CFW.colors.colorizeElement(boxDiv		 	 , color, "bg")
	CFW.colors.colorizeElement(maxWhiskerWidthDiv, color, "bg")
	CFW.colors.colorizeElement(maxTickDiv		 , color, "bg")
	
	
	
	//=====================================
	// Add Colors
	if(popover){

		//-------------------------------
		// Simplify Data shown in popup
		let clone = _.clone(values);
		if(CFW.utils.isNullOrEmpty(clone.sart) 	|| clone.start == clone.min){	delete clone.start; }
		if(CFW.utils.isNullOrEmpty(clone.low) 	|| clone.low == clone.min)  {	delete clone.low; }
		if( CFW.utils.isNullOrEmpty(clone.min))								{	delete clone.min; }
		
		if(CFW.utils.isNullOrEmpty(clone.end) 	|| clone.end == clone.max)	{	delete clone.end; }
		if(CFW.utils.isNullOrEmpty(clone.high) 	|| clone.high == clone.max)	{	delete clone.high; }
		if(CFW.utils.isNullOrEmpty(clone.max))								{	delete clone.max; }
		
		if( CFW.utils.isNullOrEmpty(clone.median) ){	delete clone.median; }

		//-------------------------------
		// Show Popup
		let popoverSettings = Object.assign({}, cfw_renderer_common_getPopoverDefaults());
		popoverSettings.content = CFW.format.objectToHTMLList(clone);
		boxplotDiv.popover(popoverSettings);
		
	}
	
	//=====================================
	// Create Boxplot
	boxplotDiv.append(minOffsetDiv);
	
	if(min != low){
		boxplotDiv.append(minTickDiv);   
		boxplotDiv.append(minWhiskerWidthDiv);
	}  
	
	boxplotDiv.append(boxDiv); 
	 	
	if(max != high){	
		boxplotDiv.append(maxWhiskerWidthDiv);  
		boxplotDiv.append(maxTickDiv);   
	}

	return boxplotDiv;
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_format_fieldNameToLabel(fieldname){
	
	if(fieldname == null){
		return "";
	}
 	var regex = /[-_]/;
	var splitted = fieldname.split(regex);
	
	var result = '';
	for(let i = 0; i < splitted.length; i++) {
		result += (CFW.format.capitalize(splitted[i]));
		
		//only do if not last
		if(i+1 < splitted.length) {
			result += " ";
		}
	}
	
	return result;
}
/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_format_csvToObjectArray(csvString, delimiter){
	
 	var lines = csvString.trim().split(/\r\n|\r|\n/);
 	
 	//------------------------------
 	// Check has at least one record
 	if(lines.length < 2){
 		return [];
 	}
 	 	
 	//------------------------------
 	// Get Headers
 	var headers = lines[0].trim().split(delimiter);
 	
 	//------------------------------
 	// Create Objects
 	var resultArray = [];
 	
 	for(let i = 1; i < lines.length; i++){
 		var line = lines[i];
 		var values = line.split(delimiter);
 		var object = {};
 		
 	 	for(var j = 0; j < headers.length && j < values.length; j++){
 	 		var header = headers[j];
 	 		object[header] = values[j].trim();
 	 	}
 	 	resultArray.push(object);
 	 	
 	}
	
	return resultArray;
}


/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_format_capitalize(string) {
	 if(string == null) return '';
	 return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase();
}

/**************************************************************************************
 * Adds separator to numbers
 *************************************************************************************/
function cfw_format_numberSeparators(value, separator, eachDigit) {
	
	if(value == null) return '';
	if(isNaN(value)) return value;
	if(value == true || value == false) return value;

	var separator = (separator == null) ?  "'" : separator;
	var eachDigit = (eachDigit == null) ?  3 : eachDigit;
	
	var stringValue = ""+value;
	var startingPos = stringValue.lastIndexOf('.')-1;
	var resultString = stringValue.substring(startingPos+1);
	
	//if no decimal point found, do this
	if(startingPos == -2){	
		startingPos = stringValue.length-1;
		var resultString = "";
	}
	
	var position = 0;
	for(let i = startingPos; i >= 0; i--){
		position++;
		
		let char = stringValue.charAt(i);

		//handle minus in negative number
		if(char == "-" && resultString.startsWith(separator) ){ 
			resultString = char + resultString.substring(1); 
			break;
		}
		
		resultString = char + resultString;
		
		if(position % 3 == 0 && i > 0){
			resultString = separator + resultString;
		}
	}
	
		
	
	
	return resultString;
}

/**************************************************************************************
 * Formats numbers as kilos, megas, gigas and terras.
 * Formats tiny values with a placeholder for nulls like:
 * 	- original: 0.0000001234
 *  - formatted: 0.0{6}1234
 * 
 * @param value the value to format
 * @param decimals number of decimal places
 * @param addBlank if true, adds a blank between number and the K/M/G/T
 * @param isBytes if true, adds "B" to the resulting format
 **************************************************************************************/
function cfw_format_numbersInThousands(value, decimals, addBlank, isBytes) {
	
	//------------------------------
	// Sanitize
	if(typeof decimals == 'string' ){
		if(isNaN(decimals)){
			decimals = 1;
		}else{
			decimals = parseInt(decimals);
		}
	}
	
	blankString = (addBlank) ? "&nbsp;" : "";
	bytesString = (isBytes) ? "B" : "";
	
	if(isNaN(value)){
		return value;
	}
	
	//------------------------------
	// Check is tiny Value
	if (value != 0 && value < 1 && value > -1 ) {
		
		//---------------------------------------
		// Analyze Value
		let stringValue = ""+value;

		if(stringValue.includes("e")){
			// Handle scientific madness
			value = value.toFixed( parseInt(stringValue.split('-')[1]) + decimals );
			
			stringValue = ""+value;

		}
		
		let splittedString = stringValue.split('.');
		let regularPart = splittedString[0];
		let decimalsPart = splittedString[1];
		
		//---------------------------------------
		// Count Zeros
				let zeroCount = 0;

		for( ; zeroCount < decimalsPart.length; zeroCount++ ){
			if(decimalsPart.charAt(zeroCount) != '0'){
				break;
			}
		}

		//---------------------------------------
		// Format number
		let subZeroDecimals = 3 + decimals;
		if(zeroCount < 3){
			return value.toFixed(zeroCount + subZeroDecimals);
		}else{
						
			let valuePart = decimalsPart.substring(zeroCount);
			if ( valuePart.length > subZeroDecimals ){
				valuePart = valuePart.substring(0, subZeroDecimals);
			}
			let formatted = regularPart + ".0{"+ (zeroCount) +"}"+valuePart;
			return formatted;
		}
		
		
	} else if (value < 1000) {			return value.toFixed(decimals) 				+ blankString + bytesString; }
	else if (value < 1000000) {			return (value / 1000).toFixed(decimals) 	+ blankString + "K"+bytesString; }
	else if (value < 1000000000) {		return (value / 1000000).toFixed(decimals) 	+ blankString + "M"+bytesString; }
	else if (value < 1000000000000) {	return (value / 1000000000).toFixed(decimals) + blankString + "G"+bytesString; }
	else {								return (value / 1000000000000).toFixed(decimals) + blankString + "T"+bytesString; }  
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_format_badgesFromArray(stringArray, addLineBreaks) {
	
	if(stringArray == null) return '';

	let badgesHTML = '<div>';
	
		for(id in stringArray){
			badgesHTML += '<span class="badge badge-primary m-1">'+stringArray[id]+'</span>';
			if(addLineBreaks) { badgesHTML += '</br>'; }
		}
		
	badgesHTML += '</div>';
	
	return badgesHTML;
}

/**************************************************************************************
 * 
 *************************************************************************************/
function cfw_format_badgesFromObjectValues(object, addLineBreaks) {
	
	if(object == null) return '';

	let badgesHTML = '<div>';
	
		for(fieldname in object){
			let value = object[fieldname];
			badgesHTML += '<span class="badge badge-primary m-1">'+value+'</span>';
			if(addLineBreaks) { badgesHTML += '</br>'; }
		}
		
	badgesHTML += '</div>';
	
	return badgesHTML;
}




/**************************************************************************************
 * Converts the input fields of a form to an array of parameter objects.
 * Additionally to $().serializeArray(), it changes string representations of 
 * numbers and booleans to the proper types
 * 
 * { 
 * 		fieldname: "fieldvalue",
 * 		fieldname: "fieldvalue",
 * 		fieldname: "fieldvalue",
 * 		...
 * }
 *
 * @param formOrID either ID of form or a JQuery object
 * @param numbersAsStrings define if number should be converted to strings. Needed to
 *        keep leading zeros.
 *
 *************************************************************************************/
function cfw_format_formToParams(formOrID, numbersAsStrings){
	
	var paramsObject = cfw_format_formToObject(formOrID, numbersAsStrings);

	for(var name in paramsObject){
		var value = paramsObject[name];
		
		if(value != null && typeof value === 'object'){
			paramsObject[name] = JSON.stringify(value);
		}
	}
	
	return paramsObject;
}
/**************************************************************************************
 * Converts the input fields of a form to an array of parameter objects.
 * Additionally to $().serializeArray(), it changes string representations of 
 * numbers and booleans to the proper types
 * 
 * { 
 * 		fieldname: "fieldvalue",
 * 		fieldname: "fieldvalue",
 * 		fieldname: "fieldvalue",
 * 		...
 * }
 *
 * @param formOrID either ID of form or a JQuery object
 * @param numbersAsStrings define if number should be converted to strings. Needed to
 *        keep leading zeros.
 *
 *************************************************************************************/
function cfw_format_formToObject(formOrID, numbersAsStrings){
	
	var paramsArray = cfw_format_formToArray(formOrID, numbersAsStrings);
	
	var object = {};
	for(let i in paramsArray){
		var name = paramsArray[i].name;
		var value = paramsArray[i].value;
		object[name] = value;
	}
	
	return object;
}
/**************************************************************************************
 * Converts the input fields of a form to an array of parameter objects.
 * Additionally to $().serializeArray(), it changes string representations of 
 * numbers and booleans to the proper types
 * [
 * {name: "fieldname", value: "fieldvalue"},
 * {name: "fieldname", value: "fieldvalue"},
 * ...
 * ]
 *
 * @param formOrID either ID of form or a JQuery object
 * @param numbersAsStrings define if number should be converted to strings. Needed to
 *        keep leading zeros.
 *
 *************************************************************************************/
function cfw_format_formToArray(formOrID, numbersAsStrings){
	
	var paramsArray = $(formOrID).serializeArray();
	
	//---------------------------
	// Convert String Values
	for(let i in paramsArray){
		let name = paramsArray[i].name;
		let current = paramsArray[i].value;
		
		paramsArray[i].value = cfw_utils_convertStringsToType(name, current, numbersAsStrings);

	}

	//---------------------------
	// Handle Tags Selector
	var tagsselector = $(formOrID).find('.cfw-tags-selector');
	if(tagsselector.length > 0){
		tagsselector.each(function(){
			let current = $(this);
			let name = current.attr('name');
			
			//---------------------------
			// Find in parameters
			for(let i in paramsArray){
				if(paramsArray[i].name == name){
					
					//---------------------------
					// Create object
					let items = current.tagsinput('items');
					var object = {};
					for (var j in items){
						let value = items[j].value;
						let label = items[j].label;
						object[value] = label;
					}
					//---------------------------
					// Change params
					paramsArray[i].value = object;
					break;
				}
			}
		});
	}

	return paramsArray;
}

/**************************************************************************************
 * Creates an HTML ul-list out of an object
 * @param object a json object to convert to HTML
 * @param style the list style, e.g. "bullets"
 * @param paddingLeft the padding of the list
 * @param DoLabelize turn the fieldname into a camel-case label
 * @return html string
 *************************************************************************************/
function cfw_format_objectToHTMLList(object, style, paddingLeft, doLabelize){
	
	if(style == null){	style = "bullets";	}
	if(paddingLeft == null){	paddingLeft = "20px"; }
	
	
	var htmlString = '<ul style="padding-left: '+paddingLeft+';">';
	if(style == "numbers"){
		htmlString = '<ol style="padding-left: '+paddingLeft+';">';
	}else if(style == "none"){
		htmlString = '<ul style="list-style-type: none; padding-left: '+paddingLeft+';"">';
	}
	
	if(Array.isArray(object)){
		for(let i = 0; i < object.length; i++ ){
			var currentItem = object[i];
			
			if(currentItem == null){
				htmlString += '<li>null</li>';
			}else if(typeof currentItem == "object"){
				htmlString += '<li><b>Object:&nbsp;</b>'
					+ cfw_format_objectToHTMLList(currentItem, style, paddingLeft, doLabelize)
				+'</li>';
			}else{
				htmlString += '<li>'+currentItem+'</li>';
			}
			
		}
	}else if(typeof object == "object"){
		
		for(var key in object){
			var currentValue = object[key];
			var key = (!doLabelize) ? key : CFW.format.fieldNameToLabel(key);
			
			if(currentValue == null){
				htmlString += '<li><strong>'+key+':&nbsp;</strong>null</li>';
			}else if(typeof currentValue == "object"){
				htmlString += '<li><strong>'+key+':&nbsp;</strong>'
					+ cfw_format_objectToHTMLList(currentValue, style, paddingLeft, doLabelize)
				+'</li>';
			}else{
				htmlString += '<li><strong>'+key+':&nbsp;</strong>'
					+ currentValue
				+'</li>';
			}
			
		}
	}
	htmlString += '</ul>';
	
	return htmlString;
		
}
