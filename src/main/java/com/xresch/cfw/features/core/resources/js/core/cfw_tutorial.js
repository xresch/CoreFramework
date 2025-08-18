/**************************************************************************************************************
 * Contains the various tutorial functions of CFW.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2025
 * @license MIT-License
 **************************************************************************************************************/

 /*************************************************************************************
 * Starts a bundle of steps.
 * 
 * @param jqueryObjectOrId the object to start the tutorial
 *************************************************************************************/
function cfw_tutorial_bundleStart(jqueryObjectOrId){
	
	//---------------------------------------------
	// Add Button to Menubar 
	if($('#cfwMenuButtons-Tutorials').length == 0){
		var tutsButton = $('<li class="cfw-button-menuitem">'
			+'<a class="dropdown-item" onclick="cfw_tutorial_drawStartpage()" id="cfwMenuButtons-Tutorials">'
				+'<div class="cfw-fa-box">'
					+'<i class="fas fa-graduation-cap"></i>'
				+'</div><span class="cfw-menuitem-label">Tutorials</span></a>'
			+'</li>'
			)
		$('.navbar-right').prepend(tutsButton);
	}	
	
	//---------------------------------------------
	// Start Bundle 
	CFW.tutorial.data.currentBundle = {
		  startingObject: jqueryObjectOrId
		, steps: []
	}
}

/*************************************************************************************
 * Starts a bundle of steps.
 * 
 * @param jqueryObjectOrId the object to start the tutorial
 *************************************************************************************/
function cfw_tutorial_bundleEnd(){
	
	CFW.tutorial.data.bundles.push(CFW.tutorial.data.currentBundle);
	CFW.tutorial.data.currentBundle = null;
}

/*************************************************************************************
 * Plays a bundle and starts by showing the first step.
 * 
 * @param jqueryObjectOrId the object to start the tutorial
 *************************************************************************************/
function cfw_tutorial_bundlePlay(event, index){
	
	event.stopPropagation();
	
	CFW.tutorial.data.bundlePlaying = index;
	CFW.tutorial.data.currentStep = 0;
	
	var bundleToPlay = CFW.tutorial.data.bundles[index];
	var steps = bundleToPlay.steps;
	
	if(steps != null && steps.length > 0){
		
		cfw_tutorial_reset();
		$('#cfw-tuts-btn-prev').removeClass('d-none');
		$('#cfw-tuts-btn-next').removeClass('d-none');
		
		cfw_tutorial_drawStep(steps[0]);

	}else{
		CFW.ui.addToastWarning("Seems this tutorial does not contain any steps");
	}

}

/*************************************************************************************
 * Adds a step to the current bundle.
 * 
 * @param jqueryObjectOrId the object to start the tutorial
 *************************************************************************************/
function cfw_tutorial_addStep(stepData){
	
	var defaultValues = {
		  selector: null
		, clickable: false
		// useful to wait for an element to appear, like a modal panel
		, drawDelay: 0
		, text: null
		, beforeDraw: null
	}
	
	var finalStep = Object.assign({}, defaultValues, stepData);

	CFW.tutorial.data.currentBundle.steps.push(finalStep);
}


/*************************************************************************************
 * Adds a step to the current bundle.
 * 
 * @param jqueryObjectOrId the object to start the tutorial
 *************************************************************************************/
function cfw_tutorial_nextStep(){
	
	var bundleIndex = CFW.tutorial.data.bundlePlaying;
	var bundleToPlay = CFW.tutorial.data.bundles[bundleIndex];
	var currentStepIndex = CFW.tutorial.data.currentStep;
	
	if( (currentStepIndex+1) < bundleToPlay.steps.length ){
		
		cfw_tutorial_drawStep(bundleToPlay.steps[currentStepIndex+1]);
		CFW.tutorial.data.currentStep += 1;

	}else if( (currentStepIndex+1) == bundleToPlay.steps.length ) {
		
		cfw_tutorial_drawStep({
			  selector: null
			, clickable: true
			, text: "You have reached the end of the tutorial."
			, drawDelay: 500
			, beforeDraw: null
		});
		CFW.tutorial.data.currentStep += 1;
	}else{
		cfw_tutorial_drawStartpage();
		return;
	}
	
	
}

/*************************************************************************************
 * 
 *************************************************************************************/
function cfw_tutorial_drawStartpage(){
	
	//Make sure anything previous tutorials are closed 
	cfw_tutorial_close();
	
	let parent = $('body');

	cfw_tutorial_objectAppendOverlays(parent)
	
	//-----------------------------
	// Overlays
	for(let index in CFW.tutorial.data.bundles){
		let bundle = CFW.tutorial.data.bundles[index];
		let object = $(bundle.startingObject);

		if(object.length == 0){
			continue;
		}
		
		cfw_tutorial_objectHighlight(object, true);
		
		var playBundleOverlay = 
			$('<div class="cfw-tuts-playbundle" onclick="cfw_tutorial_bundlePlay(event,'+index+')">');
			
		object.append(playBundleOverlay);
	}
	
	//-----------------------------
	// Controls
	var controls = $('<div class="cfw-tuts-controls">'
						+'<div class="d-flex justify-content-center w-100">'
						+'<a id="cfw-tuts-btn-home" class="btn btn-sm  btn-primary mr-2" onclick="cfw_tutorial_drawStartpage()"><i class="fas fa-home"></i></a>'
						+'<a id="cfw-tuts-btn-close" class="btn btn-sm  btn-primary mr-2" onclick="cfw_tutorial_close()"><i class="fas fa-times"></i></a>'
						+'<a id="cfw-tuts-btn-next" class="btn btn-sm  btn-primary mr-2 d-none" onclick="cfw_tutorial_nextStep()">Next</a>'
					+'</div>'
				);
	
	parent.append(controls);
	
	//-----------------------------
	// Controls	
	if(!CFW.tutorial.data.toastWasShown){
		CFW.ui.addToastInfo("Click one of the highlighted elements to start a tutorial.")
		CFW.tutorial.data.toastWasShown = true;
	}
}

/*************************************************************************************
 * 
 *************************************************************************************/
function cfw_tutorial_drawStep(stepData){
	
	var selector = stepData.selector;
	
	//----------------------------------
	// Call beforeDraw
	if(stepData.beforeDraw != null){
		stepData.beforeDraw();
	}
	
	if(selector != null){
		//----------------------------------
		// Handle Selector
		CFW.ui.waitForAppear(selector).then(function(element){

			//----------------------------------
			// Handle Modals
			cfw_tutorial_objectAppendOverlays($('.modal-dialog'));
			
			cfw_tutorial_reset();
			
			CFW.ui.toggleLoader(true);	
			
			window.setTimeout(function(){
				
				//----------------------------------
				// Highlight and Popover
				cfw_tutorial_objectHighlight($(selector), stepData.clickable);
				cfw_tutorial_objectPopover($(selector), stepData.text);
				
				CFW.ui.toggleLoader(false);	
			}, stepData.drawDelay);
		})
		return;
	}else{
		cfw_tutorial_reset();
		
		//----------------------------------
		// Show TextBox Textbox
		window.setTimeout(function(){
			var textbox = $('<div class="cfw-tuts-textbox">');
			textbox.append(stepData.text);
			$('body').append(textbox);
		}, stepData.drawDelay);
		
	}
	
}

/*************************************************************************************
 * 
 *************************************************************************************/
function cfw_tutorial_objectHighlight(objectToHighlight, clickable){
	
	if(objectToHighlight != null){
		
		//only highlight the first
		var object =  $($(objectToHighlight)[0]);
		
		if(object.length > 0){
			
			var clazz = 'cfw-tuts-highlight';
			if(clickable){
				clazz += '-clickable'; 
			}
			object.addClass(clazz)
			object.get(0).scrollIntoView();	  
			window.scrollBy(0, -90);
		}
	}
	
}

/*************************************************************************************
 * 
 *************************************************************************************/
function cfw_tutorial_objectPopover(objectToAddPopover, content){
	
	// only popover the first
	var object =  $($(objectToAddPopover)[0]);
	
	if(object != null){
		var bounds = object[0].getBoundingClientRect();
		
		object.addClass('tutorial-popover')
		
		//-----------------------------
		// Marking Popover
		object
			.popover({
				  //title: "title"
				  content: content
				, container: "body"
				, sanitize: false
				, html: true
				, placement: 'auto'
				, boundary: 'window'
				, animation: true
			})
			.popover('show')
	}
}

/*************************************************************************************
 * 
 *************************************************************************************/
function cfw_tutorial_objectAppendOverlays(targetObject){

	if(targetObject != null){
		var object =  $(targetObject);
		
		if(object.find('.cfw-tuts-backdrop').length == 0){
			var backdrop = $('<div class="cfw-tuts-backdrop">');
			object.append(backdrop);
		}
		
		if(object.find('.cfw-tuts-clickblock').length == 0){
			var clickblock = $('<div class="cfw-tuts-clickblock">');
			object.append(clickblock);
		}
		
	}
}


/*************************************************************************************
 * 
 *************************************************************************************/
function cfw_tutorial_reset(){
			
	$('.tutorial-popover')
		.popover('hide')
		.removeClass('.tutorial-popover');
			
	$('.popover').remove();
	$('.cfw-tuts-highlight').removeClass('cfw-tuts-highlight');
	$('.cfw-tuts-highlight-clickable').removeClass('cfw-tuts-highlight-clickable');
	$('.cfw-tuts-playbundle').remove();
	$('.cfw-tuts-textbox').remove();
}

/*************************************************************************************
 * 
 *************************************************************************************/
function cfw_tutorial_close(){
	cfw_tutorial_reset();
	
	$('.modal').modal('hide');
		
	$('.cfw-tuts-controls').remove();
	$('.cfw-tuts-clickblock').remove();
	$('.cfw-tuts-backdrop').remove();
	
}