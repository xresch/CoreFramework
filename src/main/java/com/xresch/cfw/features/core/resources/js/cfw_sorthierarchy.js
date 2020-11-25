
/**************************************************************************************************************
 * This file contains various javascript examples.
 * For your convenience, this code is highly redundant for eassier copy&paste.
 * @author Reto Scheiwiller, (c) Copyright 2019 
 **************************************************************************************************************/

var SORTHIERARCHY_URL = "./sorthierarchy";
var JSEXAMPLES_LAST_OPTIONS = null;
var URL_PARAMS=CFW.http.getURLParams();


/******************************************************************
 * Delete
 ******************************************************************/
function jsexamples_duplicate(id){
	
	params = {action: "duplicate", item: "person", id: id};
	CFW.http.getJSON(SORTHIERARCHY_URL, params, 
		function(data) {
			if(data.success){
				CFW.cache.clearCache();
				jsexamples_draw(JSEXAMPLES_LAST_OPTIONS);
			}
	});
}

/******************************************************************
 *
 * @param data as returned by CFW.http.getJSON()
 * @return 
 ******************************************************************/
function cfw_sorthierarchy_printSortableHierarchy(data){
	
	var $parent = $("#cfw-container");
	$parent.html("");
	$parent.append("<h1>Sort Hierarchy<h1>");
	
	//--------------------------------
	// Button	
	
	//--------------------------------
	// Table
	
	if(data.payload != undefined){
		
		var resultCount = data.payload.length;
		if(resultCount == 0){
			CFW.ui.addToastInfo("Hmm... seems there aren't any people in the list.");
			return;
		}
				
		for(key in data.payload){
			cfw_sorthierarchy_printHierarchyElement(0, $parent, data.payload[key])
		}

		//$parent.append(renderResult);
		
	}else{
		CFW.ui.addAlert('error', 'Something went wrong and no items can be displayed.');
	}
}

/******************************************************************
*
* @param data as returned by CFW.http.getJSON()
* @return 
******************************************************************/
function cfw_sorthierarchy_printHierarchyElement(level, $parent, object){
	//--------------------------------------
	// Create Draggable element
	var draggableItem = $('<div id="sortable-item-'+object.PK_ID+'" class="cfw-draggable" draggable="true">')
	var draggableHeader = $('<div id="sortable-header-'+object.PK_ID+'" class="cfw-draggable-handle card-header">'+object.NAME+'</div>');
	
	draggableItem.on('dragstart', function(e){
		var draggable = $('.cfw-draggable.dragging');
		if(draggable.length == 0){
			$(this).addClass('dragging');
		}
	});
	
	draggableItem.on('dragend', function(e){
		$(this).removeClass('dragging');
	});
	
	draggableItem.on('dragover', function(e){
		
		e.preventDefault();
		
		var draggable = $('.cfw-draggable.dragging');
		
		//--------------------------------------
		// Get Closests Drop Target
		var droptargetElements = $('.cfw-draggable:not(.dragging) .cfw-droptarget ').toArray();

		var dropTarget = droptargetElements.reduce(function(closest, currentTarget) {
			let box = $(currentTarget).closest('.cfw-draggable').get(0).getBoundingClientRect();
			//let offset = e.clientY - box.top - box.height / 2;
			let offset = e.clientY - box.top;
			
			if (offset < 0 && offset > closest.offset) {
				return { offset: offset, element: $(currentTarget) };
			} else {
				return closest;
			}
			
		}, { offset: Number.NEGATIVE_INFINITY }).element;
		



		//var dropTarget = dropTargetParent.find('> ul');
		console.log("===========================");
		console.log(draggable);
		console.log(dropTarget);
		
		//--------------------------------------
		// Append: make sure droptarget is not 
		// a child of draggable
	    if (dropTarget != null 
	    && draggable.find('#'+dropTarget.attr('id')).length == 0) {
	    	//draggable.detach();
	    	dropTarget.append(draggable);
	    }
	});
	
	//--------------------------------------
	// Create Children
	var childlist = $('<div id="children-'+object.PK_ID+'" class="cfw-droptarget pl-4">')

	for(key in object.children){
		cfw_sorthierarchy_printHierarchyElement(level++, childlist, object.children[key]);
	}
	
	//--------------------------------------
	// Append 
	draggableItem.append(draggableHeader)
	draggableItem.append(childlist);
	$parent.append(draggableItem);
}



//const draggables = document.querySelectorAll('.draggable')
//const containers = document.querySelectorAll('.container')
//
//draggables.forEach(draggable => {
//  draggable.addEventListener('dragstart', () => {
//    draggable.classList.add('dragging')
//  })
//
//  draggable.addEventListener('dragend', () => {
//    draggable.classList.remove('dragging')
//  })
//})
//
//containers.forEach(container => {
//  container.addEventListener('dragover', e => {
//    e.preventDefault()
//    const afterElement = getDragAfterElement(container, e.clientY)
//    const draggable = document.querySelector('.dragging')
//    if (afterElement == null) {
//      container.appendChild(draggable)
//    } else {
//      container.insertBefore(draggable, afterElement)
//    }
//  })
//})
//
//function getDragAfterElement(container, y) {
//  const draggableElements = [...container.querySelectorAll('.draggable:not(.dragging)')]
//
//  return draggableElements.reduce((closest, child) => {
//    const box = child.getBoundingClientRect()
//    const offset = y - box.top - box.height / 2
//    if (offset < 0 && offset > closest.offset) {
//      return { offset: offset, element: child }
//    } else {
//      return closest
//    }
//  }, { offset: Number.NEGATIVE_INFINITY }).element
//}
	


/******************************************************************
 * Main function
 ******************************************************************/
function cfw_sorthierarchy_draw(){
	
	CFW.ui.toogleLoader(true);
	
	window.setTimeout( 
	function(){
		
		CFW.http.fetchAndCacheData(SORTHIERARCHY_URL, {type: URL_PARAMS.type, action: "fetch", item: "hierarchy", rootid: 111}, "hierarchy", cfw_sorthierarchy_printSortableHierarchy);
			
		CFW.ui.toogleLoader(false);
	}, 50);
}