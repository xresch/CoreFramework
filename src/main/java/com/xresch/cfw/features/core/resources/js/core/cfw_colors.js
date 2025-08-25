
/**************************************************************************************************************
 * Contains the various color functions of CFW.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025
 * @license MIT-License
 **************************************************************************************************************/

/*************************************************************
 * Adds coloring to the element based on the records value and
 * the value added in the fields bgstylefield and textstylefield.
 * @param element the DOM or JQuery element to colorize
 * @param color either one of:
 *      - a CSS color name
 *      - an RGB color code starting with '#'
 *      - CFW color class starting with 'cfw-'
 * @param type either 'bg' | 'text' | 'border' | 'table'
 * @param borderSize Optional size definition for the border.
 *************************************************************/
function cfw_colors_colorizeElement(element, color, type, borderSize){
	
	var $element = $(element);
	if(CFW.utils.isNullOrEmpty(color)){ return; }

	if(type == 'bg'){
		
		//--------------------------------
		// Use shorthand-classes for 
		// common colors to reduce dom size
		switch(color){
			case "cfw-green": 	
			case "green":	
				$element.addClass("bgg");  return; break;
				
			case "cfw-limegreen": 				
			case "limegreen": 				
				$element.addClass("bglg"); return; break;
				
			case "cfw-yellow": 					
			case "yellow": 					
				$element.addClass("bgy"); return; break;
				
			case "cfw-orange": 		
			case "orange": 		
				$element.addClass("bgo"); return; break;
				
			case "cfw-red": 		
			case "red": 		
				$element.addClass("bgr"); return; break;
				
			default: 	
				/* keep null */;
		}
			
		if(color.startsWith("cfw-")){
			$element.addClass("bg-"+color)
		}else{
			$element.css("background-color", color);
		}
		return;
		
		
	} else if( type == "text" ){
		
		if(color.startsWith("cfw-")){
			$element.addClass("text-"+color);
		}else{
			$element.css("color", color);
		}
		
		
	} else if (type == 'border'){
		
		let size = (borderSize == null) ? "1px" : borderSize; 
		
		if(color.startsWith("cfw-")){
			 $element.addClass("border-"+color);
			 $element.css("border-width", size);
			 $element.css("border-style", "solid");
		}else{
			
			$element.css("border", size+" solid "+color);
		}
		return;
	}else if(type == 'table'){
		
		if(color.startsWith("cfw-")){
			$element.addClass("table-"+color);
		}else{
			$element.css("background-color", color);
		}
		return;
	}
			 		
}
		
		
	
/************************************************************************************************
 * Creates a hex color like #112233 from a string like "rgba(112, 54, 210, 1.0)"
 ************************************************************************************************/
function cfw_colors_RGB2HEX(rgb) {
    if(CFW.utils.isNullOrEmpty(rgb) ){
		return rgb;
	}
    
    if (/^#[0-9A-F]{6}$/i.test(rgb)) return rgb;

    rgb = rgb.match(/^rgba?\((\d+),\s*(\d+),\s*(\d+).*$/);
   
    function hex(x) {
        return ("0" + parseInt(x).toString(16)).slice(-2);
    }
    
    return "#" + hex(rgb[1]) + hex(rgb[2]) + hex(rgb[3]);
}

/************************************************************************************************
 * Creates an RGBA string like "rgba(112, 54, 210, 1.0)" from a hex color like "#33FF55".
 ************************************************************************************************/
function cfw_colors_HEX2RGBA(hex, alpha = 1) {
  const r = parseInt(hex.slice(1, 3), 16);
  const g = parseInt(hex.slice(3, 5), 16);
  const b = parseInt(hex.slice(5, 7), 16);
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

/************************************************************************************************
 * Creates an RGBA string like "rgba(112, 54, 210, 1.0)" from a cfw color class like "cfw-green".
 ************************************************************************************************/
function cfw_colors_cfwColorToRGBA(colorName, alpha = 1) {
	
	const temp = document.createElement("div");
	temp.classList.toggle("text-"+colorName);

	var workspace = CFW.ui.getWorkspace();
	workspace.append(temp);

	const rgb = getComputedStyle(temp).color;
	$(temp).remove();

	const [r, g, b] = rgb.match(/\d+/g);
	return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

/************************************************************************************************
 * Creates an RGBA string like "rgba(112, 54, 210, 1.0)" from a named color.
 ************************************************************************************************/
function cfw_colors_nameToRGBA(colorName, alpha = 1) {
	
	const temp = document.createElement("div");
	temp.style.color = colorName;
	var workspace = CFW.ui.getWorkspace();
	workspace.append(temp);

	const rgb = getComputedStyle(temp).color;
	$(temp).remove();

	const [r, g, b] = rgb.match(/\d+/g);
	return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

/************************************************************************************************
 * Creates an RGB string like "rgba(112, 54, 210, 1.0)" from a color.
 * some inputs will be returned unchanged.
 ************************************************************************************************/
function cfw_colors_colorToRGBA(color, alpha = 1) {
	
	//----------------------------
	// Empty
	if(CFW.utils.isNullOrEmpty(color)){ return ""; }
	
	//----------------------------
	// RGBA
	if(color.startsWith("rgba")){ return color; }
	
	//----------------------------
	// HSLA
	if(color.startsWith("hsla")){ return color; }
	
	//----------------------------
	// Hex Color
	if(color.startsWith("#")){ return cfw_colors_HEX2RGBA(color, alpha); }
	//----------------------------
	// Hex Color
	if(color.startsWith("cfw-")){ return cfw_colors_cfwColorToRGBA(color, alpha); }

	//----------------------------
	// Named color
	return cfw_colors_nameToRGBA(color, alpha = 1);
}

	
/************************************************************************************************
 * Creates a random RGB string like "rgba(112, 54, 210, 1.0)" 
 ************************************************************************************************/
function cfw_colors_randomRGB() {
	var r = Math.floor(Math.random()*255);
	var g = Math.floor(Math.random()*255);
	var b = Math.floor(Math.random()*255);
	
	return 'rgba('+r+','+g+','+b+', 1.0)';
	
}


/************************************************************************************************
 * Creates a random HSL string like "hsla(112, 54, 210, 1.0)" 
 * @param minS The minimum saturation in percent
 * @param maxS The maximum saturation in percent
 * @param minL The minimum Lightness in percent
 * @param maxL The maximum Lightness in percent
 ************************************************************************************************/
function cfw_colors_randomHSL(minS, maxS, minL, maxL) {
	var h = CFW.utils.randomInt(0,256);
	var s = CFW.utils.randomInt(minS, maxS);
	var l = CFW.utils.randomInt(minL, maxL);
	
	return 'hsla('+h+','+s+'%,'+l+'%, 1.0)';
}

/************************************************************************************************
 * Creates a random HSL string like "hsla(112, 54, 210, 1.0)" for the given Hue.
 * @param hue the hue or any integer value (remainder for hue % 360 will be used)
 * @param minS The minimum saturation in percent
 * @param maxS The maximum saturation in percent
 * @param minL The minimum Lightness in percent
 * @param maxL The maximum Lightness in percent
 ************************************************************************************************/
function cfw_colors_randomSL(hue, minS, maxS, minL, maxL) {
	
	var s = CFW.utils.randomInt(minS, maxS);
	var l = CFW.utils.randomInt(minL, maxL);
	
	return 'hsla('+hue % 360+','+s+'%,'+l+'%, 1.0)';
}

/************************************************************************************************
 * Returns the style class string for the enum constant in CFWState.CFWStateOption in java.
 * @param cfwStateOption 
 ************************************************************************************************/
function cfw_colors_getCFWStateStyle(cfwStateOption) {
	
	if(CFW.utils.isNullOrEmpty(cfwStateOption)){
		return CFW.style.none;
	}
	
	//--------------------
	// Add Colors
	switch(cfwStateOption.trim().toUpperCase()){
		case "GREEN": 			return CFW.style.green; 		
		case "LIMEGREEN": 		return CFW.style.limegreen;  					
		case "YELLOW": 			return CFW.style.yellow;  		
		case "ORANGE": 			return CFW.style.orange; 		
		case "RED": 			return CFW.style.red; 			
		case "DISABLED": 		return CFW.style.disabled; 		
		case "NOT_EVALUATED":	return CFW.style.notevaluated; 	
		case "NONE":			return CFW.style.none; 			
		default:				return CFW.style.none; 			
	}
	
	return style;
}
/************************************************************************************************
 * Returns the style class string that indicates the status that is worse.
 * If both are null, returns CFW.style.green.
 * @param thresholdClassOne threshold class 
 * @param thresholdClassTwo threshold class 
 ************************************************************************************************/
function cfw_colors_getThresholdWorse(thresholdClassOne, thresholdClassTwo ) {
	

	if(thresholdClassOne == null){
		if(thresholdClassTwo != null){
			return thresholdClassTwo;
		}else{
			return CFW.style.green;
		}
	}
	
	// increase performance
	if(thresholdClassOne == thresholdClassTwo){
		return thresholdClassOne;
	}
	
	if(thresholdClassOne != CFW.style.red){
		switch(thresholdClassTwo){
			
			case CFW.style.red: 
				return CFW.style.red;
			break;
			
			
			case CFW.style.orange: 
				return CFW.style.orange;
			break;
			
			
			case CFW.style.yellow: 
				if(thresholdClassOne != CFW.style.red
				&& thresholdClassOne != CFW.style.orange){
					return CFW.style.yellow;
				}
			break;
			
			
			case CFW.style.limegreen: 
				if(thresholdClassOne != CFW.style.red
				&& thresholdClassOne != CFW.style.orange
				&& thresholdClassOne != CFW.style.yellow
				){
					return CFW.style.limegreen;
				}
			break;
			
			case CFW.style.green: 
				if(thresholdClassOne != CFW.style.red
				&& thresholdClassOne != CFW.style.orange
				&& thresholdClassOne != CFW.style.yellow
				&& thresholdClassOne != CFW.style.limegreen
				){
					return CFW.style.green;
				}
			break;
			
			default:
				// do nothing
			break;
		}
	}

	return thresholdClassOne;
}

/************************************************************************************************
 * Used to find the direction of the threshold.
 * 
 * @return true if it is High to Low, false otherwise.
 ************************************************************************************************/
function cfw_colors_getThresholdDirection(
			  tExellent
			, tGood
			, tWarning
			, tEmergency
			, tDanger
		){
	
	var isHighToLow = true;
	var thresholds = [tExellent, tGood, tWarning, tEmergency, tDanger];
	var firstDefined = null;

	for(let i = 0; i < thresholds.length; i++){
		var current = thresholds[i];
		if (!CFW.utils.isNullOrEmpty(current)){
			if(firstDefined == null){
				firstDefined = current;
			}else{
				if(current != null && firstDefined < current ){
					isHighToLow = false;
				}
				break;
			}
		}
	}	
	
	return isHighToLow;	
}
/************************************************************************************************
 * Returns a cfw style string for the value based on the defined thresholds e.g CFW.style.green.
 * You can use the string for creating a class like: 
 *   "bg-cfw-green"
 *   "text-cfw-green"
 *   "border-cfw-green"
 *   "table-cfw-green"
 *   
 * If all the thresholds are null/undefined or the value is NaN returns an CFW.style.none.
 * You can define thresholds values increasing from Excellent to Danger, or from Danger to Excellent.
 * You can let thresholds undefined/null to skip the color. Values below the lowest threshold value
 * will result in CFW.style.notevaluated.
 * 
 * If isDisabled is set to "true", returns CFW.style.disabled.
 * 
 * @param value the value that should be thresholded
 * @param tExellent the threshold for excellent
 * @param tGood the threshold for good
 * @param tWarning the threshold for warning
 * @param tEmergency the threshold for emergency
 * @param tDanger the threshold for danger
 * @param isDisabled define if the thresholding is disabled
 * @param evaluateLower if true, values below the lowest threshold are evaluated as the lowest color.
 *        If false, values are not evaluated
 ************************************************************************************************/
function cfw_colors_getThresholdStyle(
		  value
		, tExellent
		, tGood
		, tWarning
		, tEmergency
		, tDanger
		, isDisabled
		, evaluateLower) {
	
	//---------------------------
	// Initial Checks
	if(isDisabled) { return CFW.style.disabled; }
	
	if(isNaN(value)
	|| (
		CFW.utils.isNullOrEmpty(tExellent)
	   && CFW.utils.isNullOrEmpty(tGood)
	   && CFW.utils.isNullOrEmpty(tWarning)
	   && CFW.utils.isNullOrEmpty(tEmergency)
	   && CFW.utils.isNullOrEmpty(tDanger)
	   )
	){
		return CFW.style.none;
	}

	//---------------------------
	// Find Threshold direction
	var isHighToLow = cfw_colors_getThresholdDirection(tExellent, tGood, tWarning, tEmergency, tDanger);

	//---------------------------
	// Set Colors for Thresholds
	var styleString = CFW.style.notevaluated;
	
	if(isHighToLow){
		if 		(!CFW.utils.isNullOrEmpty(tExellent) 	&& value >= tExellent) 	{ styleString = CFW.style.green; } 
		else if (!CFW.utils.isNullOrEmpty(tGood) 		&& value >= tGood) 		{ styleString = CFW.style.limegreen; } 
		else if (!CFW.utils.isNullOrEmpty(tWarning) 	&& value >= tWarning) 	{ styleString = CFW.style.yellow; } 
		else if (!CFW.utils.isNullOrEmpty(tEmergency) 	&& value >= tEmergency) { styleString = CFW.style.orange; } 
		else if (!CFW.utils.isNullOrEmpty(tDanger) 		&& value >= tDanger)  	{ styleString = CFW.style.red; } 
		else{ 
			if(evaluateLower){
				styleString = CFW.style.red; 
			}else{
				styleString = CFW.style.notevaluated; 
			}
		} 
	}else{
		if 		(!CFW.utils.isNullOrEmpty(tDanger) 		&& value>= tDanger)  	{ styleString = CFW.style.red; } 
		else if (!CFW.utils.isNullOrEmpty(tEmergency) 	&& value>= tEmergency) 	{ styleString = CFW.style.orange; } 
		else if (!CFW.utils.isNullOrEmpty(tWarning) 	&& value>= tWarning) 	{ styleString = CFW.style.yellow; } 
		else if (!CFW.utils.isNullOrEmpty(tGood) 		&& value>= tGood) 		{ styleString = CFW.style.limegreen; } 
		else if (!CFW.utils.isNullOrEmpty(tExellent) 	&& value>= tExellent) 	{ styleString = CFW.style.green; } 	
		else{ 
			if(evaluateLower){
				styleString = CFW.style.green; 
			}else{
				styleString = CFW.style.notevaluated; 
			}
		}
	}
	
	return styleString;
}

/************************************************************************************************
 * Returns a colored indicator for the given threshold.
 * 
 * @param value the value that should be thresholded
 * @param tExellent the threshold for excellent
 * @param tGood the threshold for good
 * @param tWarning the threshold for warning
 * @param tEmergency the threshold for emergency
 * @param tDanger the threshold for danger
 * @param isDisabled define if the thresholding is disabled
 * @param evaluateLower if true, values below the lowest threshold are evaluated as the lowest color.
 *        If false, values are not evaluated
 ************************************************************************************************/
function cfw_colors_getThresholdIndicator(
		  value
		, tExellent
		, tGood
		, tWarning
		, tEmergency
		, tDanger
		, isDisabled
		, evaluateLower) {
			
	let style = CFW.colors.getThresholdStyle(
		  value
		, tExellent
		, tGood
		, tWarning
		, tEmergency
		, tDanger
		, isDisabled
		, evaluateLower);
	
	var isHighToLow = CFW.colors.getThresholdDirection(tExellent, tGood, tWarning, tEmergency, tDanger);
	
	return CFW.colors.getThresholdIndicatorForStyle(style, isHighToLow);

}

/************************************************************************************************
 * Returns a colored indicator for the given threshold style and direction.
 * 
 * @param style one of the styles returned by cfw_colors_getThresholdStyle()
 * @param isHighToLow value returned by cfw_colors_getThresholdDirection()
 ************************************************************************************************/
function cfw_colors_getThresholdIndicatorForStyle(style, isHighToLow){

	var iconClass = "fa-arrow-up";
	var rotateBy = 0;

	if 		(style == CFW.style.green ) 	{ (isHighToLow) ? rotateBy = 0 : rotateBy = 180; } 
	else if (style == CFW.style.limegreen ) { (isHighToLow) ? rotateBy = 45 : rotateBy = 135; } 
	else if (style == CFW.style.yellow ) 	{  rotateBy = 90; } 
	else if (style == CFW.style.orange ) 	{ (isHighToLow) ? rotateBy = 135 : rotateBy = 45; } 
	else if (style == CFW.style.red ) 		{ (isHighToLow) ? rotateBy = 180 : rotateBy = 0;} 
	else if (style == CFW.style.notevaluated ) 		{ iconClass = "fa-minus"; } 
	else{ 
		rotateBy = 90;
	} 
	
	let indicator = $('<i class="fas ">');	
	
	CFW.colors.colorizeElement(indicator, style, "text");
	
	indicator.addClass(iconClass);
	if(rotateBy > 0){ indicator.addClass('rotate-'+rotateBy); }
	
	return indicator;	
}

/************************************************************************************************
 * Evaluates two threshholds going in opposite directions, for example:
 *   - tGreen is 10, everything >=10 will be green
 *   - tRed is -10, everything <=-10 will be red
 *   - 
 * 
 * Returns a cfw style string for the value based on the defined thresholds e.g CFW.style.green.
 * You can use the string for creating a class like: 
 *   "bg-cfw-green"
 *   "text-cfw-green"
 *   "border-cfw-green"
 *   "table-cfw-green"
 *   
 * If all the thresholds are null/undefined or the value is NaN returns an CFW.style.none.
 * You can define thresholds values increasing from tGreen to tRed, or from tRed to tGreen.
 * 
 * If isDisabled is set to "true", returns CFW.style.disabled.
 * 
 * @param value the value that should be thresholded
 * @param tGreen the threshold for excellent
 * @param tRed the threshold for danger
 * @param isDisabled define if the thresholding is disabled
 ************************************************************************************************/
function cfw_colors_getSplitThresholdStyle(
		  value
		, tGreen
		, tRed
		, isDisabled
		) {

	//---------------------------
	// Initial Checks
	if(isDisabled) { return CFW.style.disabled; }
	
	if(isNaN(value)
	|| (
		CFW.utils.isNullOrEmpty(tGreen)
	   && CFW.utils.isNullOrEmpty(tRed)
	   )
	){
		return CFW.style.none;
	}

	//---------------------------
	// Find Threshold direction
	var direction = 'HIGH_TO_LOW';
	var thresholds = [tGreen, tRed];
	
	if(tGreen < tRed){
		direction = 'LOW_TO_HIGH';
	}

	//---------------------------
	// Set Colors for Thresholds
	var styleString = CFW.style.notevaluated;
	
	if(direction == 'HIGH_TO_LOW'){
		if 		(!CFW.utils.isNullOrEmpty(tGreen) 	&& value >= tGreen) 	{ styleString = CFW.style.green; } 
		else if (!CFW.utils.isNullOrEmpty(tRed) 		&& value <= tRed)  	{ styleString = CFW.style.red; } 
	}else{
		if 		(!CFW.utils.isNullOrEmpty(tRed) 		&& value >= tRed)  	{ styleString = CFW.style.red; } 
		else if (!CFW.utils.isNullOrEmpty(tGreen) 	&& value <= tGreen) 	{ styleString = CFW.style.green; } 	
	}
	
	return styleString;
}
