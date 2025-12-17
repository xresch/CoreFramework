/**************************************************************************************************************
 * Contains the various UI functions of CFW.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025
 * @license MIT-License
 **************************************************************************************************************/
/**************************************************************************************
 * Create a table of contents for the h-elements on the page.
 * If you do not want a section and it's subsection to appear in the table of contents,
 * add the class 'toc-hidden' to your header element.
 *
 * @param contentAreaSelector the jQuery selector for the element containing all 
 * the headers to show in the table of contents
 * @param targetSelector the jQuery selector for the resulting element
 * @param headerTag the tag used for the Table Header e.g. h1, h2, h3 (default h1)
 * @return nothing
 *************************************************************************************/
function cfw_ui_createTOC(contentAreaSelector, resultSelector, headerTag){
	
	var target = $(resultSelector);
	var headers = $(contentAreaSelector).find("h1:visible, h2:visible, h3:visible, h4:visible, h5:visible, h6:visible");
	
	var h = "h1";
	if(headerTag != null){
		h = headerTag;
	}
	//------------------------------
	//Loop all visible headers
	var currentLevel = 1;
	var resultHTML = '<'+h+' class="cfw-toc-header">Table of Contents</'+h+'><ul>';
	for(let i = 0; i < headers.length ; i++){
		var head = headers[i];
		var headLevel = head.tagName[1];
		
		//------------------------------
		//increase list depth
		while(currentLevel < headLevel){
			resultHTML += "<ul>";
			currentLevel++;
		}
		//------------------------------
		//decrease list depth
		while(currentLevel > headLevel){
			resultHTML += "</ul>";
			currentLevel--;
		}
		
		//------------------------------
		//Add List Item
		var classes = "";
		if($(head).hasClass('toc-hidden')){ classes="d-none"; }
		
		resultHTML += '<li class="'+classes+'"><a href="#toc_anchor_'+i+'">'+head.innerHTML+'</li>';
		$(head).before('<a name="toc_anchor_'+i+'"></a>');
	}
	
	//------------------------------
	// Close remaining levels
	while(currentLevel > 1){
		resultHTML += "</ul>";
		currentLevel--;
	}
	
	target.html(resultHTML);
	
}


var LOADER_MESSAGES = [
   '<i class="fa fa-cog fa-spin fa-fw margin-bottom"></i> Loading...',
   '<i class="fa fa-cogs fa-spin fa-fw margin-bottom"></i> Something is going on here... ',
   '<i class="fa fa-certificate fa-spin fa-fw margin-bottom"></i> IT IS SPINNING!!! ',
   '<i class="fa fa-dog fa-beat fa-fw margin-bottom"></i> Woof! Woof! Bark!!!',
   '<i class="fa fa-coffee fa-spin fa-fw margin-bottom"></i> You might want to go grab a coffee... ',
   '<i class="fa fa-copyright fa-spin fa-fw margin-bottom"></i> This could take a while... ',
   '<i class="fa fa-crosshairs fa-spin fa-fw margin-bottom"></i> Target locked, manifest rendering... ',
   '<i class="fa fa-cube fa-spin fa-fw margin-bottom"></i> Algorithm is running... ',
   '<i class="fa fa-clover fa-bounce fa-fw margin-bottom"></i>&nbsp;<i class="fa fa-clover fa-bounce margin-bottom" style="--fa-animation-delay: 333ms"></i>&nbsp;<i class="fa fa-clover fa-bounce fa-fw margin-bottom" style="--fa-animation-delay: 666ms"></i> Good luck with that... ',
   '<i class="fa fa-exclamation-circle fa-spin fa-fw margin-bottom"></i> Attention! Content is being generated... ',
   '<i class="fa fa-football-ball fa-spin fa-fw margin-bottom"></i>Yay! It\'s a football! ',
   '<i class="fa fa-globe fa-spin fa-fw margin-bottom"></i> Sending Requests around the world... ',
   '<i class="fa fa-hourglass fa-spin fa-fw margin-bottom"></i> Processing in Progress... ',
   '<i class="fa fa-republican fa-beat fa-fw margin-bottom"></i> Manipulating elections... <i class="fa fa-democrat fa-beat fa-fw margin-bottom"></i>',
   '<i class="fa fa-cog fa-spin fa-fw margin-bottom"></i><i class="fa fa-cog fa-spin margin-bottom"></i><i class="fa fa-cog fa-spin fa-fw margin-bottom"></i> More gears for more loading... ',
   '<i class="fa fa-bug fa-spin fa-fw margin-bottom"></i> Loading potential buggy content... ',
   '<i class="fa fa-horse fa-spin fa-fw margin-bottom"></i> Feeding ze unicorns... ',
   '<i class="fa fa-volleyball-ball fa-beat fa-fw margin-bottom"></i> Serving the ball...',
   '<i class="fa fa-bacon fa-spin fa-fw margin-bottom"></i> Frying some bacon... ',
   '<i class="fas fa-plane-arrival fa-bounce fa-fw margin-bottom"></i> Landing the plane... ',
   '<i class="fa fa-ice-cream fa-spin fa-fw margin-bottom"></i> Melting the ice cream... ', 
   '<i class="fa fa-truck-monster fa-spin fa-fw margin-bottom"></i> Whoah, look at it go!',
   '<i class="fa fa-bell fa-spin fa-fw margin-bottom"></i> Waking up the minions...',
   '<i class="fa fa-egg fa-bounce fa-fw margin-bottom"></i>&nbsp;<i class="fa fa-egg fa-bounce margin-bottom" style="--fa-animation-delay: 333ms"></i>&nbsp;<i class="fa fa-egg fa-bounce fa-fw margin-bottom" style="--fa-animation-delay: 666ms"></i> Boiling some eggs... ',
   '<i class="fab fa-windows fa-spin fa-fw margin-bottom"></i> Still faster than Windows...',
   '<i class="fas fa-star fa-beat fa-fw margin-bottom"></i> Gazing at stars... ',
   '<i class="fa fa-music fa-beat fa-fw margin-bottom"></i> Playing elevator music...',
   '<i class="fa fa-wrench fa-spin fa-fw margin-bottom"></i> Doing the needful...',
   '<i class="fa fa-puzzle-piece fa-spin fa-fw margin-bottom"></i> Putting pieces together...',
   '<i class="fa fa-hand-scissors fa-spin fa-fw margin-bottom"></i> Rock... paper... siccors!',
   '<i class="fa fa-snowflake fa-spin fa-fw margin-bottom"></i> Counting snowflakes... ',
   '<i class="fa fa-dice-one fa-spin fa-fw margin-bottom"></i>&nbsp;<i class="fa fa-dice-three fa-spin margin-bottom" style="--fa-animation-delay: 333ms"></i>&nbsp;<i class="fa fa-dice-six fa-spin fa-fw margin-bottom" style="--fa-animation-delay: 666ms"></i> Throwing the dices... ',
   '<i class="fab fa-creative-commons-zero fa-spin fa-fw margin-bottom"></i> Dividing by zero... ',
   '<i class="fa fa-utensil-spoon fa-spin fa-fw margin-bottom"></i> Bending the spoon... ',
   '<i class="fas fa-microphone-lines fa-shake fa-fw margin-bottom"></i> Dropping the microphone...', 
   '<i class="fa fa-binoculars fa-spin fa-fw margin-bottom"></i> Observing the birds... ',
   '<i class="fa fa-mask fa-spin fa-fw margin-bottom"></i> Hiding the secrets... ',
   '<i class="fa fa-tooth fa-bounce fa-fw margin-bottom"></i>&nbsp;<i class="fa fa-tooth fa-bounce margin-bottom" style="--fa-animation-delay: 333ms"></i>&nbsp;<i class="fa fa-tooth fa-bounce fa-fw margin-bottom" style="--fa-animation-delay: 666ms"></i> Brushing the teeth... ',
   '<i class="fa fa-dragon fa-bounce fa-fw margin-bottom"></i> Taming the dragon...',
   '<i class="fas fa-peace fa-beat fa-fw margin-bottom"></i> Conducting peace negotiations... ',
   '<i class="fa fa-yin-yang fa-spin fa-fw margin-bottom"></i> Channeling the chi...',
   '<i class="fa fa-toilet-paper fa-bounce fa-fw margin-bottom"></i> Flushing...',
   '<i class="fas fa-cat fa-bounce fa-fw margin-bottom"></i> Waiting for nine lives... ',
   '<i class="fa fa-location-arrow fa-spin fa-fw margin-bottom"></i> Searching for something I can display to you... ',
   '<i class="fas fa-dungeon fa-bounce fa-fw margin-bottom"></i> Conquering the dungeon... ',
   '<i class="fa fa-feather fa-spin fa-fw margin-bottom"></i> Wingardium Leviosaaa!!!', 
   '<i class="fa fa-spider fa-bounce fa-fw margin-bottom"></i>&nbsp;<i class="fa fa-spider fa-bounce margin-bottom" style="--fa-animation-delay: 333ms"></i>&nbsp;<i class="fa fa-person-running fa-bounce fa-fw margin-bottom" style="--fa-animation-delay: 666ms"></i> Running from the spiders... ',
   '<i class="fa fa-magic-wand-sparkles fa-spin fa-fw margin-bottom"></i> Doing the magic...', 
   '<i class="fa fa-paperclip fa-spin fa-fw margin-bottom"></i> Clipping the papers...', 
   '<i class="fas fa-circle-radiation fa-beat fa-fw margin-bottom"></i> Measuring radiation levels... ',
   '<i class="fa fa-ghost fa-bounce fa-fw margin-bottom"></i> Haunting the house...', 
   '<i class="fas fa-microphone-lines fa-bounce fa-fw margin-bottom"></i> Making an announcement...', 
   '<i class="fa fa-chess-rook fa-bounce fa-fw margin-bottom"></i>&nbsp;<i class="fa fa-chess-king fa-bounce margin-bottom" style="--fa-animation-delay: 333ms"></i>&nbsp;<i class="fa fa-chess-knight fa-bounce fa-fw margin-bottom" style="--fa-animation-delay: 666ms"></i> Moving into checkmate... ',
   '<i class="fa fa-spaghetti-monster-flying fa-bounce fa-fw margin-bottom"></i> Praying to pasta...', 
   '<i class="fas fa-heartbeat fa-beat fa-fw margin-bottom"></i> Measuring heart beat...',
   '<i class="fas fa-car-rear fa-bounce fa-fw margin-bottom"></i> Driving to the destination...',
   '<i class="fa fa-hurricane fa-spin fa-spin-reverse fa-fw margin-bottom"></i> Preparing for windy days...',
   '<i class="fas fa-bus fa-bounce fa-fw margin-bottom"></i> Waiting for the bus...',
   '<i class="fa fa-bowling-ball fa-spin fa-fw margin-bottom"></i> Throwing a strike...',
   '<i class="fa fa-microchip fa-bounce fa-fw margin-bottom"></i>&nbsp;<i class="fa fa-microchip fa-bounce margin-bottom" style="--fa-animation-delay: 333ms"></i>&nbsp;<i class="fa fa-microchip fa-bounce fa-fw margin-bottom" style="--fa-animation-delay: 666ms"></i> Heating up the microchips... ',
   '<i class="fas fa-location-dot fa-bounce fa-fw margin-bottom"></i> Marking the location...',
   '<i class="fas fa-face-grin-hearts fa-beat fa-fw margin-bottom"></i> Admiring the beauty...',
   '<i class="fas fa-bell fa-shake fa-fw margin-bottom"></i> Ringing the bell...',
   '<i class="fas fa-pizza-slice fa-beat fa-fw margin-bottom"></i> Ordering fast food...',
   '<i class="fas fa-parachute-box fa-shake fa-fw margin-bottom"></i> Dropping supply boxes...', 
   '<i class="fa fa-bacteria fa-bounce fa-fw margin-bottom"></i>&nbsp;<i class="fa fa-bacterium fa-bounce margin-bottom" style="--fa-animation-delay: 333ms"></i>&nbsp;<i class="fa fa-bacteria fa-bounce fa-fw margin-bottom" style="--fa-animation-delay: 666ms"></i> Sterilization in progress... ',
   '<i class="fas fa-thumbtack fa-bounce fa-fw margin-bottom"></i> Searching the needle in the haystack...', 
   '<i class="fas fa-shield fa-bounce fa-fw margin-bottom"></i> Repelling the attacks...', 
   ];

/*******************************************************************************
 * Set if the Loading animation is visible or not.
 * 
 * The following example shows how to call this method to create a proper rendering
 * of the loader:
 * 	
 *  CFW.ui.toggleLoader(true);
 *	window.setTimeout( 
 *	  function(){
 *	    // Do your stuff
 *	    CFW.ui.toggleLoader(false);
 *	  }, 100);
 *
 * @param isVisible true or false
 * @param targetID the id of the element where the overlay should be loaded.
 * 				   If undefined, the loader is added to the full body.
 * @param text the text to be displayed while loading, default is "Loading..."
 * @param icon font awesome icon that should be spinning, will only have an
 * 			effect if text is not null. Default: "fa-cog"
 ******************************************************************************/
function cfw_ui_toggleLoader(isVisible, targetID, text, icon){
	
	let loaderID;
	let target;
	let cssClass;

	//------------------------------------
	// Set Defaults
	if(text == null){	
		icon = "";
		let randomIndex = Math.floor(Math.random() * (LOADER_MESSAGES.length) );
		text = LOADER_MESSAGES[randomIndex];

	}else{
		 if(icon == null){	
			icon = '<i class="fa fa-spin fa-1x fa-fw '+icon+' margin-bottom"></i>'; 
		}else{
			icon = '<i class="fa fa-cog fa-spin fa-1x fa-fw margin-bottom"></i>'; 
		}
	}
	
	//------------------------------------
	// Get Target
	if(targetID != null){
		loaderID = "cfw-loader-"+targetID;
		target = $("#"+targetID);
		cssClass = "cfw-loader-custom";
	}else{
		loaderID = "cfw-loader-body";
		target = $("body");
		cssClass = "cfw-loader-body";
	}
	
	//------------------------------------
	// Get Existing loader
	let loader = $("#"+loaderID);

	//------------------------------------
	// Create if not Exists
	if(loader.length == 0){
		loader = $('<div id="'+loaderID+'" class="'+cssClass+'">'
				+'<div>'
					+icon
					+'<span class="m-0"></span>'
				+'</div>'
			+'</div>');	
		
		var bgColor = $('body').css("background-color");
		loader.css("background-color", bgColor);
				
		target.prepend(loader);
	}
	
	//------------------------------------
	// Toggle visibility
	if(isVisible){
		var parent = loader.parent();
		loader.find("span").html(text);
		loader.css("display", "flex");
		
		if(targetID != null){
			loader.width( parent.width() );
			loader.height( parent.height() );
		}
	}else{
		loader.css("display", "none");
	}
	
}
var LOADER_QUOTES = [
     '<p><b>What do you call a magic dog?</b></p>  <p>A labracadabrador.</p>'
   , '<p><b>Why did the frog take the bus to work today?</b></p>  <p>His car got toad away.</p>'
   , '<p><b>What did one hat say to the other?</b></p>  <p>You wait here. I\'ll go on a head.</p>'
   , '<p><b>What did the buffalo say when his son left for college?</b></p>  <p>Bison.</p>'
   , '<p><b>What is an astronaut\'s favorite part on a computer?</b></p>  <p>The space bar.</p>'
   , '<p><b>Why did the yogurt go to the art exhibition?</b></p>  <p>Because it was cultured.</p>'
   , '<p><b>Did you hear about the two people who stole a calendar?</b></p>  <p>They each got six months.</p>'
   , '<p><b>What do cows do on date night?</b></p>  <p>Go to the moo-vies.</p>'
   , '<p><b>What do cows say when they hear a bad joke?</b></p>  <p>I am not amoosed.</p>'
   , '<p><b>What did 0 say to 8?</b></p>  <p>Nice belt.</p>'
   , '<p><b>Did you hear about the guy who invented the knock-knock joke?</b></p>  <p>He won the &quot;no-bell&quot; prize.</p>'
   , '<p><b>Hear about the new restaurant called Karma?</b></p>  <p>There\'s no menu: You get what you deserve.</p>'
   , '<p><b>Why don\'t scientists trust atoms?</b></p>  <p>Because they make up everything.</p>'
   , '<p><b>Why can\'t you explain puns to kleptomaniacs?</b></p>  <p>They always take things literally.</p>'
   , '<p><b>Why are ghosts such bad liars?</b></p>  <p>Because they are easy to see through.</p>'
   , '<p><b>What do you call an alligator in a vest?</b></p>  <p>An investigator.</p>'
   , '<p><i>&quot;Always forgive your enemies; nothing annoys them so much.&quot;</i></p>  <p>- Oscar Wilde</p>'
   , '<p><i>&quot;Before you criticize someone, walk a mile in their shoes. That way, you\'ll be a mile from them, and you\'ll have their shoes.&quot;</i></p>  <p>- Jack Handy</p>'
   , '<p><i>&quot;If I\'m not back in five minutes, just wait longer.&quot;</i></p>  <p>- Ace Ventura</p>'
   , '<p><i>&quot;Do not take life too seriously. You will never get out of it alive.&quot;</i></p>  <p>- Elbert Hubbard</p>'
   , '<p><i>&quot;Clothes make the man. Naked people have little or no influence in society.&quot;</i></p>  <p>- Mark Twain</p>'
   , '<p><i>&quot;If you think you are too small to make a difference, try sleeping with a mosquito.&quot;</i></p>  <p>- Dalai Lama</p>'
   , '<p><i>&quot;Remember, today is the tomorrow you worried about yesterday.&quot;</i></p>  <p>- Dale Carnegie</p>'
   , '<p><i>&quot;The best thing about the future is that it comes one day at a time.&quot;</i></p>  <p>- Abraham Lincoln</p>'

];

 /*******************************************************************************
 * .
 * 
 * The following example shows how to call this method to create a proper rendering
 * of the loader:
 * 	
 *  CFW.ui.toggleLoaderQuotes(true);
 *	window.setTimeout( 
 *	  function(){
 *	    // Do your stuff
 *	    CFW.ui.toggleLoaderQuotes(false);
 *	  }, 100);
 *
 * @param isVisible true or false
 * @param targetID the id of the element where the overlay should be loaded.
 * 				   If undefined, the loader is added to the full body.
 ******************************************************************************/
function cfw_ui_toggleLoaderQuotes(isVisible, targetID){
	
	let loaderID;
	let target;
	let cssClass;

	let randomIndex = Math.floor(Math.random() * (LOADER_QUOTES.length) );
	let quote = LOADER_QUOTES[randomIndex];
	
	//------------------------------------
	// Get Target
	if(targetID != null){
		loaderID = "cfw-quotesloader-"+targetID;
		target = $("#"+targetID);
		cssClass = "cfw-loader-custom";
	}else{
		loaderID = "cfw-quotesloader-body";
		target = $("body");
		cssClass = "cfw-loader-body";
	}
	
	//------------------------------------
	// Get Existing loader
	let loader = $("#"+loaderID);

	//------------------------------------
	// Create if not Exists
	if(loader.length == 0){
		loader = $('<div id="'+loaderID+'" class="'+cssClass+'">'
				+'<div class="d-flex flex-column flex-align-center">'
					+'<i class="fa fa-cog fa-spin fa-2x fa-fw margin-bottom"></i>'
					+'<div class="mt-1 text-center">'+quote+'</div>'
				+'</div>'
			+'</div>');	
		
		var bgColor = $('body').css("background-color");
		loader.css("background-color", bgColor);
				
		target.prepend(loader);
	}
	
	//------------------------------------
	// Toggle visibility
	if(isVisible){
		var parent = loader.parent();
		loader.css("display", "flex");
		loader.find(".text-center").html(quote);
		if(targetID != null){
			loader.width( parent.width() );
			loader.height( parent.height() );
		}
	}else{
		loader.css("display", "none");
	}
	
}


/*******************************************************************************
 * Returns the html for a Loader with the class cfw-inline-loader.
 ******************************************************************************/
function cfw_ui_createLoaderHTML(){
	
	return '<div class="cfw-inline-loader">'
			+'<div class="d-flex flex-column flex-align-center">'
				+'<i class="fa fa-cog fa-spin fa-2x fa-fw margin-bottom"></i>'
				+'<p class="m-0">Loading</p>'
			+'</div>'
		+'</div>';
	
}

/*******************************************************************************
 * Function to show fixed dropdown menus.
 * Example HTML:
	<div class="dropdown dropright">
		<button id="chartSettingsButton" class="btn btn-sm btn-primary dropdown-toggle" type="button" onclick="CFW.ui.toggleDropdownMenuFixed(event, this)" >
			Chart Settings
		</button>
		<div id="${fieldID}-DROPDOWNMENU" class="dropdown-menu-fixed col-sm-12" onclick="event.stopPropagation();">
			... your dropdown content ...
		</div>
	</div>
 ******************************************************************************/
function cfw_ui_toggleDropdownMenuFixed(event, buttonElement){
	
	event.stopPropagation();
	
	var parent = $(buttonElement).closest('.dropdown');
	var isShown = parent.hasClass('show');
	// close any visible before opening new one
	cfw_ui_closeAllDropdownMenuFixed();
	
	if(!isShown){
		parent.addClass('show');
	}
}

/*******************************************************************************
 * 
 ******************************************************************************/
function cfw_ui_closeAllDropdownMenuFixed(){
	
	$('.dropdown-menu-fixed')
		.closest('.dropdown')
		.removeClass('show');
}


/**************************************************************************************
 * Add an alert message to the message section.
 * Ignores duplicated messages.
 * @param type the type of the alert: INFO, SUCCESS, WARNING, ERROR
 * @param message
 *************************************************************************************/
function cfw_ui_addAlert(type, message){
	
	var clazz = "";
	
	switch(type.toLowerCase()){
		
		case "success": clazz = "alert-success"; break;
		case "info": 	clazz = "alert-info"; break;
		case "warning": clazz = "alert-warning"; break;
		case "error": 	clazz = "alert-danger"; break;
		case "severe": 	clazz = "alert-danger"; break;
		case "danger": 	clazz = "alert-danger"; break;
		default:	 	clazz = "alert-info"; break;
		
	}
	
	var htmlString = '<div class="alert alert-dismissible '+clazz+'" role=alert>'
		+ '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>'
		+ message
		+"</div>\n";
	
	//----------------------------------------------
	// Add if not already exists
	var messages = $("#cfw-messages");
	
	if (messages.html().indexOf(message) <= 0) {
		messages.append(htmlString);
	}
	
}

/**************************************************************************************
 * Create a new Toast.
 * @param toastTitle the title for the toast
 * @param toastBody the body of the toast (can be null)
 * @param style bootstrap style like 'info', 'success', 'warning', 'danger'
 * @param (optional)delay in milliseconds for autohide
 * @return nothing
 *************************************************************************************/
function cfw_ui_addToast(toastTitle, toastBody, style, delay){
	
	var body = $("body");
	var toastsID = 'cfw-toasts';
	
	//--------------------------------------------
	// Create Toast Wrapper if not exists
	//--------------------------------------------
	var toastDiv = $("#"+toastsID);
	if(toastDiv.length == 0){
	
		var toastWrapper = $(
				'<div id="cfw-toasts-wrapper" aria-live="polite" aria-atomic="true">'
			  + '  <div id="'+toastsID+'"></div>'
			  + '</div>');
				
		body.prepend(toastWrapper);
		toastDiv = $("#"+toastsID);
	}

	//--------------------------------------------
	// Prepare arguments
	//--------------------------------------------
	
	if(style == null){
		style = "primary";
	}
	
	var clazz;
	switch(style.toLowerCase()){
	
		case "success": clazz = "success"; break;
		case "info": 	clazz = "info"; break;
		case "warning": clazz = "warning"; break;
		case "error": 	clazz = "danger"; break;
		case "severe": 	clazz = "danger"; break;
		case "danger": 	clazz = "danger"; break;
		default:	 	clazz = style; break;
		
	}
	
	var autohide = 'data-autohide="false"';
	if(delay != null){
		autohide = 'data-autohide="true" data-delay="'+delay+'"';
	}
	//--------------------------------------------
	// Check if message is already shown
	//--------------------------------------------
	let hash = 	CFW.utils.hash(""+toastTitle + toastBody + style);
	
	if(toastDiv.find("div[data-hash='"+hash+"']").not('.hide').length > 0){
		return;
	}
	
	//--------------------------------------------
	// Create Toast 
	//--------------------------------------------
	
	var toastHTML = 
		      '<div class="toast bg-'+clazz+' text-light" data-hash="'+hash+'" role="alert" aria-live="assertive" aria-atomic="true" data-animation="true" '+autohide+'>'
			+ '  <div class="toast-header bg-'+clazz+' text-light">'
			  + '	<strong class="mr-auto word-break-word"></strong>'
			  + '	<button type="button" class="ml-2 mb-auto close" data-dismiss="toast" aria-label="Close">'
			  + '	  <span aria-hidden="true">&times;</span>'
			  + '	</button>'
			+ '  </div>' 
			+ '  <div class="toast-body"></div>'
			+ '</div>';
	
	var toast = $(toastHTML);
	
	//-------------------------------------
	// Handle Title
	if(toastTitle != null){
		
		if(toastTitle.length > 500){
			toastTitle = toastTitle.substring(0, 500) + "... (truncated)";
		}
		toast.find('.toast-header strong').append(toastTitle);	
	}else{
		toast.find('.toast-header').remove();
	}
	//-------------------------------------
	// Handle Body
	if(toastBody != null){
		
		if(toastBody.length > 500){
			toastBody = toastBody.substring(0, 500) + "... (truncated)";
		}
		toast.find('.toast-body').text(toastBody);	
	}else{
		toast.find('.toast-body').remove();
	}
	
	toastDiv.append(toast);
	toast.toast('show');
}

/**************************************************************************************
 * Create a model with content.
 * @param modalTitle the title for the modal
 * @param modalBody the body of the modal
 * @param jsCode to execute on modal close
 * @param size 'small', 'regular', 'large'
 * @param keepOnOutsideClick (obsolete, now default true)
 * @return nothing
 *************************************************************************************/
function cfw_ui_showModal(modalTitle
		, modalBody
		, jsCodeOrFunction
		, size
		, keepOnOutsideClick){
	
	var modalID = 'cfw-default-modal';
	var modalHeaderClass = '';
	var modalHeaderType = 'h3';
	var modalDialogClass = 'modal-xl';
	var backdrop = true;
	var modalSettings = {};
	
	var style = ''
	if(size == 'small'){
		modalID = 'cfw-small-modal';
		style = 'style="z-index: 1155; display: block; top: 25px;"';
		modalHeaderClass = 'p-2';
		modalDialogClass = 'modal-lg';
		modalHeaderType = 'h4';
		backdrop = false;
	}else if(size == 'large'){
		modalID = 'cfw-large-modal';
		style = 'style="z-index: 1045; display: block; top: -10px;"';
		modalDialogClass = 'modal-xxl-cfw';
	}
	
	//if(keepOnOutsideClick){
	modalSettings.backdrop = 'static';
	modalSettings.keyboard = false;

	
	CFW.global.lastOpenedModal = modalID;
	var modal = $("#"+modalID);
	if(modal.length == 0){
	
		modal = $(
				'<div id="'+modalID+'" class="modal fade" '+style+' tabindex="-1" role="dialog">'
				+ '  <div class="modal-dialog '+modalDialogClass+'" role="document">'
				+ '    <div class="modal-content">'
				+ '      <div class="modal-header '+modalHeaderClass+'">'
				+ '        <'+modalHeaderType+' class="modal-title">Title</'+modalHeaderType+'>'
				+ '        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times</span></button>'
				+ '      </div>'
				+ '      <div class="modal-body" >'
				+ '      </div>'
				+ '      <div class="modal-footer">'
				+ '         <button id="'+modalID+'-closebutton" type="button" class="btn btn-sm btn-primary" data-dismiss="modal">Close</button>'
				+ '      </div>'
				+ '      <div class="modal-bg-blur"></div>'
				+ '    </div>'
				+ '  </div>'
				+ '</div>');

		modal
		.modal(modalSettings)
		.draggable({
			backdrop: backdrop,	   
		    handle: ".modal-header",
		}); 
		// remove position relative to show content in background
		modal.css('position', '');
		
		// Prevent other modals to close the modal
//		modal.on('hidden.bs.modal', function (e) {
//			  if(e.target.id != modalID) { // ignore events which are raised from child modals
//			    return false;
//			  }
//			  // your code
//			});
		
//		modal.modal({
//		    backdrop: 'static',
//		    keyboard: false
//		});
		$('body').prepend(modal);
		
		//-------------------------------------
		// Reset Scroll Position
		modal.on('hide.bs.modal', function () {
			modal.find('.modal-body').scrollTop(0);
		});	

	}

	//---------------------------------
	// Add Callback
	if(jsCodeOrFunction != null){

		modal.on('hidden.bs.modal', function () {
			cfw_utils_executeCodeOrFunction(jsCodeOrFunction);
			$("#"+modalID).off('hidden.bs.modal');
		});	
	}
	
	//---------------------------------
	// ShowModal
	modal.find(".modal-title").html("").append(modalTitle);
	modal.find('.modal-body').html("").append(modalBody);
	
	modal.modal('show');

}

/**************************************************************************************
 * Create a model with content.
 * @param modalTitle the title for the modal
 * @param modalBody the body of the modal
 * @param jsCodeOrFunction to execute on modal close
 * @param keepOnOutsideClick set true to keep the Modal open when clicking on the backdrop
 * @return nothing
 *************************************************************************************/
function cfw_ui_showModalMedium(modalTitle, modalBody, jsCodeOrFunction, keepOnOutsideClick){
	cfw_ui_showModal(modalTitle, modalBody, jsCodeOrFunction, 'regular', keepOnOutsideClick);
}
	
/**************************************************************************************
 * Create a model with content.
 * @param modalTitle the title for the modal
 * @param modalBody the body of the modal
 * @param jsCode to execute on modal close
 * @param keepOnOutsideClick set true to keep the Modal open when clicking on the backdrop
 * @return nothing
 *************************************************************************************/
function cfw_ui_showModalSmall(modalTitle, modalBody, jsCodeOrFunction, keepOnOutsideClick){
	cfw_ui_showModal(modalTitle, modalBody, jsCodeOrFunction, 'small', keepOnOutsideClick);
}
	
/**************************************************************************************
 * Create a model with content.
 * @param modalTitle the title for the modal
 * @param modalBody the body of the modal
 * @param jsCode to execute on modal close
 * @param keepOnOutsideClick set true to keep the Modal open when clicking on the backdrop
 * @return nothing
 *************************************************************************************/
function cfw_ui_showModalLarge(modalTitle, modalBody, jsCodeOrFunction, keepOnOutsideClick){
	cfw_ui_showModal(modalTitle, modalBody, jsCodeOrFunction, 'large', keepOnOutsideClick);
}	

/**************************************************************************************
 * Create a model with content.
 * @param modalID the ID of the modal
 * @param modalBody the body of the modal
 * @param jsCode to execute on modal close
 * @return nothing
 *************************************************************************************/
function cfw_ui_reopenModal(modalID){
	
	var modal = $("#"+modalID);
	
	if(modal.length != 0){
		modal.modal('show');
	}
		
}

/**************************************************************************************
 * Show a modal with support Info
 *************************************************************************************/
function cfw_ui_showSupportInfoModal(){
		
	var modalContent = $('<div>');
	
	//=========================================
	// Javascript Data
	//=========================================
	modalContent.append('<h2>Javascript Data</h2>');
	
	var rendererParams = {
			data: [JSDATA],
			customizers: {
				starttime: function(record, value) { return (value != null ) ? CFW.format.epochToTimestamp(value) : "" }
			},
			rendererSettings: {
				table: {
					filterable: false,
					narrow: true,
					verticalize: true
				},
			},
		};
	
	var result = CFW.render.getRenderer('table').render(rendererParams);
	modalContent.append(result);
	
	//=========================================
	// Toast Messages
	//=========================================
	var toastDiv = $("#cfw-toasts");
	if(toastDiv.length != 0){
		modalContent.append('<h2>Toast Messages</h2>');
		var clone = toastDiv.clone();
		clone.find('.close').remove();
		clone.find('.toast')
			.attr('data-autohide', 'false')
			.removeClass('hide')
			.addClass('show');
		modalContent.append(clone.find('.toast').clone());
	}
	
	//=========================================
	// Show Modal
	cfw_ui_showModalMedium("Support Info", modalContent);
}

/**************************************************************************************
 * Create a confirmation modal panel that executes the function passed by the argument
 * @param message the message to show
 * @param confirmLabel the text for the confirm button
 * @param jsCode the javascript to execute when confirmed
 * @return nothing
 *************************************************************************************/
function cfw_ui_confirmExecute(message, confirmLabel, jsCodeOrFunction){
	
	var body = $("body");
	var modalID = 'cfw-confirm-dialog';
	
	
	var modal = $('<div id="'+modalID+'" class="modal fade" tabindex="-1" role="dialog">'
				+ '  <div class="modal-dialog" role="document">'
				+ '    <div class="modal-content">'
				+ '      <div class="modal-header">'
				+ '        '
				+ '        <h3 class="modal-title">Confirm</h3>'
				+ '      </div>'
				+ '      <div class="modal-body">'
				+ '        <p>'+message+'</p>'
				+ '      </div>'
				+ '      <div class="modal-footer">'
				+ '      </div>'
				+ '    </div>'
				+ '  </div>'
				+ '</div>');

	modal.modal();
	
	//set focus on confirm button
	modal.on('shown.bs.modal', function(event) {
		$('#cfw-confirmButton').get(0).focus();
	});
	
	body.prepend(modal);	
	
	var closeButton = $('<button type="button" class="close"><span aria-hidden="true">&times</span></button>');
	closeButton.attr('onclick', 'cfw_ui_confirmExecute_Execute(this, \'cancel\')');
	closeButton.data('modalID', modalID);
	
	var cancelButton = $('<button type="button" class="btn btn-primary">Cancel</button>');
	cancelButton.attr('onclick', 'cfw_ui_confirmExecute_Execute(this, \'cancel\')');
	cancelButton.data('modalID', modalID);
	
	var confirmButton = $('<button id="cfw-confirmButton" type="button" class="btn btn-primary">'+confirmLabel+'</button>');
	confirmButton.attr('onclick', 'cfw_ui_confirmExecute_Execute(this, \'confirm\')');
	confirmButton.data('modalID', modalID);
	confirmButton.data('jsCode', jsCodeOrFunction);
	
	modal.find('.modal-header').append(closeButton);
	modal.find('.modal-footer').append(cancelButton).append(confirmButton);
	
	modal.modal('show');

}


function cfw_ui_confirmExecute_Execute(sourceElement, action){
	
	var $source = $(sourceElement);
	var modalID = $source.data('modalID');
	var jsCode = $source.data('jsCode');
	
	var modal = $('#'+modalID);
		
	//remove modal
	modal.modal('hide');
	modal.remove();
	$('.modal-backdrop').remove();
	$('body').removeClass('modal-open');
	modal.remove();
	
	if(action == 'confirm'){
		CFW.utils.executeCodeOrFunction(jsCode);
	}
}
/******************************************************************
 * Creates a hidden workspace in the DOM tree.
 * This is needed for ChartJS, needs a DOM element to use
 * getComputedStyle.
 * @param 
 * @return object
 ******************************************************************/
function cfw_ui_getWorkspace() {

	var workspace = $('#cfw-hidden-workspace');
	
	if(workspace.length == 0){
		workspace = $('<div id="cfw-hidden-workspace" style="display: none;">');
		$('body').append(workspace);				
	}
	
	return workspace;
}
/******************************************************************
 * Waits for an element to appear.
 * Usage Example:
 	waitForElm('.some-class').then((element) => {
	    console.log('Element is ready');
	    console.log(element.textContent);
	});
 * @param selector a querySelector (JQuery objects not supported)
 * @return object
 ******************************************************************/
function cfw_ui_waitForAppear(selector, timeoutMillis) {
	
	if(timeoutMillis == null){
		timeoutMillis = 5000;
	}
	var starttime = Date.now();
	
    return new Promise((resolve, reject) => {
	
        if (document.querySelector(selector)) {
            return resolve(document.querySelector(selector));
        }

        const observer = new MutationObserver(mutations => {
			
            if (document.querySelector(selector)) {
                resolve(document.querySelector(selector));
                observer.disconnect();
            }

			if((Date.now() - starttime) > timeoutMillis){
				reject(selector);
			}
        });

        observer.observe(document.body, {
            childList: true,
            subtree: true
        });
    });
}


/*********************************************************************************
* Creates a printView by opening a new window and returns a jQueryDiv where you 
* can put the content inside which you want to print.
* @param title title at the top of the page(optional)
* @param description after the title(optional)
* @param doLandscape true for landscape, false for portrait
* @return jQueryElement a div you can write the content to print to.
*********************************************************************************/
function cfw_ui_createPrintView(title, description, doLandscape){
	
	//--------------------------
	// Create Window
	var hostURL = CFW.http.getHostURL();
	var printView = window.open();

	var printBox = printView.document.createElement("div");
	printBox.id = "cfw-manual-printview-box";
	printView.document.body.appendChild(printBox);
	
	//--------------------------
	// Copy Styles
	var stylesheets = $('link[rel="stylesheet"]');
	for(let i = 0; i < stylesheets.length; i++){
		let href = stylesheets.eq(i).attr('href');
		if(!CFW.utils.isNullOrEmpty(href)){
			let cssLink = printView.document.createElement("link");
			cssLink.rel = "stylesheet";
			cssLink.media = "screen, print";
			cssLink.href = hostURL+href;
			printView.document.head.appendChild(cssLink);
		}
	}	
	
	//--------------------------
	// Override Bootstrap Style
	let cssLink = printView.document.createElement("link");
	cssLink.rel = "stylesheet";
	cssLink.media = "screen, print";
	cssLink.href = hostURL+"/cfw/jarresource?pkg=com.xresch.cfw.features.core.resources.css&file=bootstrap-theme-bootstrap.css";
	printView.document.head.appendChild(cssLink);
		
	//--------------------------
	// Override Code Style
	let cssCodestyleLink = printView.document.createElement("link");
	cssCodestyleLink.rel = "stylesheet";
	cssCodestyleLink.media = "screen, print";
	cssCodestyleLink.href = hostURL+"/cfw/jarresource?pkg=com.xresch.cfw.features.core.resources.css&file=highlightjs_arduino-light.css";
	printView.document.head.appendChild(cssCodestyleLink);
	
	//--------------------------
	// Copy Scripts
	var javascripts = $('#javascripts script');

	for(let i = 0; i < javascripts.length; i++){

		let source = javascripts.eq(i).attr('src');
		if(!CFW.utils.isNullOrEmpty(source) && source.startsWith('/cfw')){
			let script = printView.document.createElement("script");
			script.src = hostURL+source;
			printView.document.head.appendChild(script);
		}
	}
	
	var parent = $(printBox);
		
	//--------------------------
	//Create CSS
	
	var cssString = `<style media="print, screen">
		html, body {
			margin: 0px;
			padding: 0px;
			print-color-adjust: exact;
			font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";
		}
		table, pre, code, p { page-break-inside:auto }
		tr    { page-break-inside:avoid; page-break-after:auto }
		#paper {
			padding: 20mm;
			width: 100%;
			border-collapse: collapse;
		}
		img{
			padding-bottom: 5mm;
		}
		h1{
			page-break-before: always;
		}
		.page-break{
			page-break-before: always;
		}
		.cfw-toc-header{ page-break-before: avoid; }
		h1 {font-size: 32px;}
		h2 {font-size: 30px;}
		h3 {font-size: 28px;}
		h4 {font-size: 26px;}
		h5 {font-size: 24px;}
		h6 {font-size: 22px;}
		div, p, span, table {font-size: 20px;}
		h1, h2, h3, h4, h5, h6{
			padding-top: 5mm;
		}
		#print-toc > h1, #doc-title, h1 + div > h1,  h1 + div > .page-break, h2 + div > .page-break, h3 + div > .page-break, h4 + div > .page-break{
			page-break-before: avoid;
		}
		.cfw-sticky-th{ position: unset; }
		th, .cfw-sticky-th, th.bg-dark {
			color: black !important;
			background: transparent !important;
		}
		@page{ size: ` + (doLandscape ? 'landscape' : 'portrait') + `; }

	</style>
	
	`
		;
	
	parent.append(cssString);
	
	//---------------------------------
	// Add Title
	if(!CFW.utils.isNullOrEmpty(title)){
		var titleElement = printView.document.createElement("title");
		titleElement.innerHTML = title;
		printView.document.head.appendChild(titleElement);
		
		parent.append('<h1 class="text-center pt-4 mt-4">'+title+'</h1>');
	}
	
	//---------------------------------
	// Add Description
	if(!CFW.utils.isNullOrEmpty(description)){		
		parent.append('<p class="text-center pb-4 mb-4">'+description+'</p>');
	}
	return parent;
}

/**************************************************************************************
 * Opens a Modal to store JSON data in the File Manager.
 * 
 * @param name the name for the stored file
 * @param jsonArray array of json objects
 * @param sourceElementOrID the elment or ID (with #) that is the source of the action
 * @param nameSuggestionsArray array of strings for name suggestions
 *************************************************************************************/
function cfw_ui_storeJsonDataModal(jsonArray, sourceElementOrID, nameSuggestionsArray) {
	
	if(nameSuggestionsArray == null){
		nameSuggestionsArray = [];
	}
	
	//==============================================
	// Create Name Field
	
	let wrapperID ='cfw-storeJsonData-wrapper';
	let fieldID ='cfw-storeJsonData-name';
	let nameField = $(`<input class="col-9" id="${fieldID}" type="text" placeholder="Name" >`);	
	
	//==============================================
	// Create Button
	let button = $('<button class="col-3 btn btn-sm btn-primary">Store to File Manager</button>');
	button.click(
		function() {
			let name = nameField.val();
			cfw_http_postStoreJsonData(name, jsonArray);
		});
	
	//==============================================
	// Get UI Suggestions
	
	let sourceElement = $(sourceElementOrID);

	sourceElement.closest('.grid-stack-item-content')
				 .find('.cfw-dashboard-widget-title')
				 .each(function(){
					 let widgetTitle = $(this).text();
					 nameSuggestionsArray.push(widgetTitle);
				 });
				 
	$('#dashboardName span')
		.each(function(){
			 let dashboardTitle = $(this).text();
			 nameSuggestionsArray.push(dashboardTitle);
		 });	
	
	$("button[data-toggle='dropdown']")
		.each(function(){
			 let buttonText = $(this).text();
			 nameSuggestionsArray.push(buttonText);
		 });	
		 	 
		 		 	 
	//==============================================
	// Get Time Suggestions
	
	let timeframePickers = $(".cfw-timeframepicker-wrapper > input");
	timeframePickers.each(function(){
		let selectedTime = JSON.parse( $(this).val() );
		
		//---------------------
		// earliest
		let earliest = selectedTime.earliest;
		let earliestDate = CFW.format.epochToDate(earliest);
		let earliestDateShort = CFW.format.epochToShortDate(earliest);
		let earliestTimestamp = CFW.format.epochToTimestamp(earliest);
		
		//---------------------
		// latest
		let latest = selectedTime.latest;
		let latestDate = CFW.format.epochToDate(latest);
		let latestDateShort = CFW.format.epochToShortDate(latest);
		let latestTimestamp = CFW.format.epochToTimestamp(latest);
		
		if(earliestDate == latestDate){
			nameSuggestionsArray.push( earliestDate );
		}
			

		//---------------------
		// earliest to latest
		
		if(earliestDate != latestDate){
			nameSuggestionsArray.push( earliestDate +" to " + latestDate );
			nameSuggestionsArray.push( earliestDateShort +" to " + latestDateShort );
			nameSuggestionsArray.push( earliestTimestamp +" to " + latestTimestamp );
		}else{
			// <Date> <Time> to <Time>
			latestTimestamp = latestTimestamp.replace(latestDate+" ", "");
			nameSuggestionsArray.push( earliestTimestamp +" to " + latestTimestamp );
			
			// <Time> to <Time>
			earliestTimestamp = earliestTimestamp.replace(earliestDate+" ", "");
			nameSuggestionsArray.push( earliestTimestamp +" to " + latestTimestamp );
			
			// <DateShort> <Time> to <Time>
			let latestTime = latestDateShort.split(" ");
			latestTime = latestTime[latestTime.length - 1];
			nameSuggestionsArray.push( earliestDateShort +" to " + latestTimestamp );
			
		}
		
		
		//---------------------
		// duration
		let duration = latest - earliest;
		nameSuggestionsArray.push( CFW.format.millisToClock(duration) );
		nameSuggestionsArray.push( CFW.format.millisToDuration(duration) );
		nameSuggestionsArray.push( CFW.format.millisToDurationClock(duration) );

		//---------------------
		// duration
		nameSuggestionsArray.push(selectedTime.offset);
	
	});
	
	//==============================================
	// Get Parameter Suggestions
	let params = CFW.http.getURLParamsDecoded();
	
	cfw_ui_storeJsonDataModal_createNameSuggestionsRecursively(nameSuggestionsArray, params);
	
	//==============================================
	// Filter Suggestions Array
	nameSuggestionsArray = _.uniq(nameSuggestionsArray);
	nameSuggestionsArray = _.map(nameSuggestionsArray, _.trim);
	_.remove(nameSuggestionsArray, function(item) {
		  return CFW.utils.isNullOrEmpty(item);
	});
	
	//==============================================
	// create name suggestions Buttons
	let suggestionsDiv = $('<div class="p-2 d-flex align-items-stretch flex-wrap">');

	for(let i in nameSuggestionsArray){
		let suggestion = nameSuggestionsArray[i];
		
		let button = $('<button class="col-4 btn btn-sm btn-primary m-0">'+suggestion+'</button>');
		button.click(
			function() {
				let name = nameField.val();
				if(CFW.utils.isNullOrEmpty(name)){
					nameField.val(suggestion);
				}else{
					nameField.val(name +" "+suggestion);
				}
			});
			
		suggestionsDiv.append(button);
		
	}
	
	//==============================================
	// Create Wrapper
	let wrapper = $(`<div class="row" id="${wrapperID}">
		<p>Select a name for the file that will be stored in the file manager.</p>
	</div>`);	
	wrapper.append(nameField);
	wrapper.append(button);
	wrapper.append('<p class="w-100 mt-4">Suggestions - click to append to name: </p>');
	wrapper.append(suggestionsDiv);
	
	//==============================================
	// Show the Modal
	CFW.ui.showModalMedium(
			  CFWL("cfw_common_storeJsonData", "Store Data to File Manager")
			, wrapper
			);
}

/**************************************************************************************
 * Add name suggestions recursively.
 * 
 * @param nameSuggestionsArray array of strings for name suggestions
 * @param jsonObject the object to parse through
 *************************************************************************************/
function cfw_ui_storeJsonDataModal_createNameSuggestionsRecursively(nameSuggestionsArray, jsonObject) {
	
	for(let key in jsonObject){
		
		let paramValue = jsonObject[key];
		
		// skip these bastards
		if(key == "earliest" 
		|| key == "latest" 
		|| key == "offset" 
		|| key == "query" 
		){
			continue;
		}
		
		try{
			if(typeof paramValue == "string" && ( paramValue.startsWith("{") || paramValue.startsWith("[") ) ){
				paramValue = JSON.parse(paramValue);
			}
		}catch(e){ /* do nothing */ }
		
		if(Array.isArray(paramValue)){
			continue;
		}
		
		if(typeof paramValue == "object" ){
			cfw_ui_storeJsonDataModal_createNameSuggestionsRecursively(nameSuggestionsArray, paramValue);
		}else{
			
			nameSuggestionsArray.push(paramValue);
		}
	}
}
