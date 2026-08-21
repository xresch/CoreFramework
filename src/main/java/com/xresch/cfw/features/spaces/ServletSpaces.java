package com.xresch.cfw.features.spaces;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWForm;
import com.xresch.cfw.datahandling.CFWFormHandler;
import com.xresch.cfw.datahandling.CFWHierarchy;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.spaces.CFWSpace.CFWSpaceType;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.utils.CFWRandom;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2026
 * @license MIT
 **************************************************************************************************************/
public class ServletSpaces extends HttpServlet
{
	private static final String FIELDNAME_SENIOR_SELECTOR = "JSON_SENIOR_SELECTOR";
	private static final String FIELDNAME_USER_SELECTOR = "JSON_USER_SELECTOR";
	private static final String FIELDNAME_ADMIN_SELECTOR = "JSON_ADMIN_SELECTOR";
	private static final String[] excludedFields = new String[] {FIELDNAME_SENIOR_SELECTOR, FIELDNAME_USER_SELECTOR, FIELDNAME_ADMIN_SELECTOR};
	
	private static Logger logger = CFWLog.getLogger(ServletSpaces.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	public ServletSpaces() {
	
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		HTMLResponse html = new HTMLResponse("Spaces");
		String spaceID = request.getParameter("spaceid"); //id of the root space
		String action = request.getParameter("action");
		String item = request.getParameter("item");
		
		//--------------------------------
		// Can be done without Permissions
		if(action != null) {

			if( action.equals("fetch")
			&& item.equals("spacesforuser")){
				JSONResponse jsonResponse = new JSONResponse();		
				jsonResponse.setPayload(CFW.DB.Spaces.getSpaceListForUserAsJsonWithBreadcrumbs());
				return;
			}

			if( action.equals("update") ) {
			
				if(item.equals("selectedspaceid")
				&& spaceID != null){
					JSONResponse jsonResponse = new JSONResponse();	
					int spaceInteger = Integer.parseInt(spaceID);
					if(CFW.DB.Spaces.checkCurrentUserHasAccessToSpace(spaceInteger)) {
						CFW.Context.Request.getSessionData().setSpaceID(spaceInteger);
					}else {
						CFWSpace space = CFW.DB.Spaces.getFromCache(spaceInteger);
						if(space != null) {
							CFW.Messages.addWarningMessage("The current user does not have access to the Space '"+space.name()+"'.");
							jsonResponse.setSuccess(false);
						}else {
							CFW.Messages.addWarningMessage("The space does not exist: " + spaceInteger + "");
							jsonResponse.setSuccess(false);
						}
					}
					return;
				}
				
				String filterSpaceInclusive = request.getParameter("filterSpaceInclusive");
				if(item.equals("filterSpaceInclusive")
				&& filterSpaceInclusive != null){
					JSONResponse jsonResponse = new JSONResponse();	
					CFW.Context.Request.getSessionData().setFilterSpaceInclusive( Boolean.parseBoolean(filterSpaceInclusive) );
					return;
				}
			}
			
		}
		
		//--------------------------------
		// Needs Permissions
		if(CFW.Context.Request.hasPermission(FeatureSpaces.PERMISSION_SPACES_VIEWER)
		|| CFW.Context.Request.hasPermission(FeatureSpaces.PERMISSION_SPACES_ADMIN)
		) {
			
			//createForms();

			if(action == null) {

				FeatureSpaces.addSpacesCommonJS(html);
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureSpaces.PACKAGE_RESOURCE, "om_spaces.js");
				html.addJavascriptCode("om_spaces_initialDraw();");
				
				//--------------------
				// Add Data
				
				boolean canCreateSpaces =
					   CFW.Context.Request.hasPermission(FeatureSpaces.PERMISSION_SPACES_ADMIN)
					|| CFW.Context.Request.hasPermission(FeatureSpaces.PERMISSION_SPACES_CREATE);
				
				html.addJavascriptData("canCreateSpaces", canCreateSpaces);
				html.addJavascriptData("isSpacesAdmin", CFW.Context.Request.hasPermission(FeatureSpaces.PERMISSION_SPACES_ADMIN) );
				
				//--------------------
				// Add Types
				CFWSpaceType[] typesArray = CFWSpaceType.values();
				StringBuilder builder = new StringBuilder();
				for(CFWSpaceType type : typesArray) {
					
					if(type == CFWSpaceType.ROOT_SPACE) {
						continue;
					}
					builder.append(type.toString()+",");
				}
				builder.deleteCharAt(builder.length()-1);
				
				html.addJavascriptData("types", builder.toString());
				
				//--------------------
				// Various
		        response.setContentType("text/html");
		        response.setStatus(HttpServletResponse.SC_OK);
			}else {
				handleDataRequest(request, response);
			}
		}else {
			CFWMessages.accessDenied();
		}
        
    }
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void handleDataRequest(HttpServletRequest request, HttpServletResponse response) {
		
		//-----------------------------
		// Prepare Variables
		String action = request.getParameter("action");
		String item = request.getParameter("item");
	
		String ID = request.getParameter("id"); //id of the space
		String spaceID = request.getParameter("spaceid"); //id of the root space
			
		JSONResponse jsonResponse = new JSONResponse();		

		//-----------------------------
		// Check is Admin
		Integer selectedSpaceID = CFW.Context.Request.getSelectedSpaceID();
		
		boolean isAdminForSelectedSpace = 
				   CFW.DB.SpaceAdminMap.checkIsCurrentUserAdminOfSelectedSpace(selectedSpaceID)
				|| CFW.DB.SpaceAdminGroupsMap.checkIsCurrentUserAdminByGroupOfSelectedSpace(selectedSpaceID);
		
		jsonResponse.addCustomAttribute("isAdminForSelectedSpace", isAdminForSelectedSpace);
		
		//-----------------------------
		// Handle Request
		switch(action.toLowerCase()) {
		
			case "fetch": 			
				switch(item.toLowerCase()) {
					
					case "spaceslist": 		jsonResponse.setPayload(CFWDBSpaces.getHierarchyForSpaceAsJson(spaceID));
	  										break;
	  				
					default: 				CFW.Messages.itemNotSupported(item);
											break;
				}
				break;
			
			case "fetchpartial": 			
				switch(item.toLowerCase()) {
					case "spaceslist": 		String pagesize = request.getParameter("pagesize");
											String pagenumber = request.getParameter("pagenumber");
											String filterquery = request.getParameter("filterquery");
											String sortby = request.getParameter("sortby");
											String isAscendingString = request.getParameter("isascending");
											boolean isAscending = (isAscendingString == null || isAscendingString.equals("true")) ? true : false;
											
											jsonResponse.getContent().append(CFWDBSpaces.getPartialSpaceListAsJSON(pagesize, pagenumber, filterquery, sortby, isAscending));
	  										break;
	  										
					default: 				CFW.Messages.itemNotSupported(item);
											break;
				}
				break;	
			
			case "delete": 			
				switch(item.toLowerCase()) {

					case "space": 		deleteSpace(jsonResponse, ID);
										break;  
										
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;	
				
			case "duplicate": 			
				switch(item.toLowerCase()) {

					case "space": 	 	duplicateSpace(jsonResponse, ID);
										break;  
										
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;	
			case "getform": 			
				switch(item.toLowerCase()) {
				
					case "createspace": 	String type = request.getParameter("type");	
											createAddSpaceForm(jsonResponse, type, spaceID);
											break;
				
					case "editspace": 	createEditForm(jsonResponse, ID);
											break;
					
					default: 			CFW.Messages.itemNotSupported(item);
										break;
				}
				break;
						
			default: 			CFW.Messages.actionNotSupported(action);
								break;
								
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void deleteSpace(JSONResponse jsonResponse, String ID) {
		
		CFWHierarchy.deleteWithChildren(CFWSpace.hierarchyConfig, Integer.parseInt(ID));	
	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void duplicateSpace(JSONResponse jsonResponse, String id) {
		CFWDBSpaces.duplicateByID(id);
	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void addSeniorSelectorField(CFWSpace space, int spaceid) {
		
		if(space == null || space.type() == CFWSpaceType.ROOT_SPACE) {
			return;
		}
		
		try {
			Integer parentID = CFWHierarchy.getParentID(space);
			
			//--------------------------------------
			// Create Field
			CFWField<LinkedHashMap<String,String>> parentSelector = 
					CFWField.newTagsSelector(FIELDNAME_SENIOR_SELECTOR)
							.setDescription("Select the senior, leave empty if this element should be made a root space. Start typing to get suggestions.")
							.setLabel("Senior Space")
							.addAttribute("maxTags", "1")
							.setAutocompleteHandler(new CFWAutocompleteHandler(10,2) {
								
								public AutocompleteResult getAutocompleteData(HttpServletRequest request, String inputValue, int cursorPosition) {
									
									return CFW.DB.Spaces.autocompleteSpaceForRootSpace(inputValue, spaceid, this.getMaxResults());
								}
							});
			
			//--------------------------------------
			// Set Selected Value
			LinkedHashMap<String,String> selectedValue = new LinkedHashMap<>();
			if(parentID != null) {
				CFWSpace parent = CFWDBSpaces.selectByID(parentID);
				if(parent != null) {
					selectedValue.put(""+parent.id(), parent.createSpaceLabel());
				}
			}
			
			parentSelector.setValue(selectedValue);
			
			//--------------------------------------
			// Prepend before all other fields
			space.addFieldAfter(parentSelector, null);
			
		}catch(IllegalArgumentException e) {
			new CFWLog(logger).severe(e);
		}
		
	}
	
	
	/******************************************************************
	 * Return senior ID if selected, or null if not selected or 
	 * senior selector field is not present.
	 ******************************************************************/
	private Integer getSeniorID(CFWObject object) {
		
		Integer parentID = null;
		
		//--------------------------
		// Handle ParentID
		if(object.getFields().containsKey(FIELDNAME_SENIOR_SELECTOR)) {
			
			//--------------------------
			// Get Selection
			CFWField<LinkedHashMap<String,String>> parentSelector = object.getField(FIELDNAME_SENIOR_SELECTOR);
			LinkedHashMap<String,String> selectedMap = parentSelector.getValue();
			
			if(selectedMap != null && !selectedMap.isEmpty()) {
				String parentIDString = selectedMap.keySet().toArray(new String[] {})[0];
				parentID = Integer.parseInt(parentIDString);
			}
		}
		
		return parentID;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private boolean isSeniorValid(CFWSpace space, Integer seniorID) {
		//--------------------------
		// Validate Parent Selected
		if(space.type() != CFWSpaceType.ROOT_SPACE
		&& seniorID == null) {
			CFW.Messages.addErrorMessage("Please select a Senior Space.");
			return false;
		}
		
		return true;
	}
//	/******************************************************************
//	 *
//	 ******************************************************************/
//	private boolean updateUsersAndAdmins(CFWSpace space, CFWObject origin) {
//		boolean success = true;
//				
//		//--------------------------
//		// Update Selected Users
//		if(origin.getFields().containsKey(FIELDNAME_USER_SELECTOR)) {
//			CFWField<LinkedHashMap<String,String>> usersSelector = origin.getField(FIELDNAME_USER_SELECTOR);
//			LinkedHashMap<String,String> selectedUsers= usersSelector.getValue();
//			
//			if( !CFW.DB.SpaceUserMap.updateUserSpaceAssignments(space, selectedUsers) ){
//				success = false;
//				CFW.Messages.addErrorMessage("Error while updating user assignments.");
//			}
//		}
//		
//		//--------------------------
//		// Update Selected Admins
//		if(origin.getFields().containsKey(FIELDNAME_ADMIN_SELECTOR)) {
//			CFWField<LinkedHashMap<String,String>> adminsSelector = origin.getField(FIELDNAME_ADMIN_SELECTOR);
//			LinkedHashMap<String,String> selectedAdmins = adminsSelector.getValue();
//			
//			if( !CFW.DB.SpaceAdminMap.updateAdminSpacesAssignments(space, selectedAdmins) ){
//				success = false;
//				CFW.Messages.addErrorMessage("Error while updating user assignments.");
//			}
//		}
//		
//		return success;
//	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	private boolean checkCanCreateSpaces() {
				
		return CFW.Context.Request.hasPermission(FeatureSpaces.PERMISSION_SPACES_ADMIN)
		|| CFW.Context.Request.hasPermission(FeatureSpaces.PERMISSION_SPACES_CREATE);
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createAddSpaceForm(JSONResponse json, String type, String spaceid) {
		CFWSpaceType spaceType = CFWSpaceType.valueOf(type);
		//--------------------------------------
		// Create ContextSettings Form
		if(
			  ( spaceType == CFWSpaceType.ROOT_SPACE && checkCanCreateSpaces() )
			|| CFW.DB.SpaceAdminMap.checkIsCurrentUserAdminOfSelectedSpace(spaceid)
		) {
			
			//--------------------------------
			// Create settings instance 
			CFWSpace space = new CFWSpace();
			space.type(spaceType);
			
			//--------------------------------
			// Add Fields
			addSeniorSelectorField(space, Integer.parseInt(spaceid));	
			space.updateSelectorFields();
			
			//--------------------------------
			// Create Form
			CFWForm form = space.toForm("cfwCreateSpaceForm"+CFWRandom.stringAlphaNumSpecial(12),
																	"{!cfw_core_add!}");
			
			form.setFormHandler(new CFWFormHandler() {
				
				@SuppressWarnings("unchecked")
				@Override
				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
									
					//--------------------------
					// Validate and Create Object
					if(origin.mapRequestParameters(request)) {
						
						//--------------------------
						// Check Can Create Spaces
						if(space.type() == CFWSpaceType.ROOT_SPACE
						&& !(CFW.Context.Request.hasPermission(FeatureSpaces.PERMISSION_SPACES_ADMIN)
						|| CFW.Context.Request.hasPermission(FeatureSpaces.PERMISSION_SPACES_CREATE))) {
							CFW.Messages.noPermission();
							return;
						}
						
						//--------------------------
						// Handle ParentID
						Integer seniorID = null;
						
						if(origin.getFields().containsKey(FIELDNAME_SENIOR_SELECTOR)) {
							
							seniorID = getSeniorID(origin);
							
							if(!isSeniorValid(space, seniorID)) {
								return;
							}

						}
						
						
						//--------------------------
						// Insert into DB
						Integer createdPrimary = CFWHierarchy.create(
														  seniorID
														, origin
														, (Object[])excludedFields);
						
						//--------------------------
						// Set Users
						if(createdPrimary != null) {
							
							space.id(createdPrimary);	
							CFW.Messages.addSuccessMessage("Created!");
							
														
							//----------------------------------------
							// Update Seniors and Users
							space.saveSelectorFields();
							
							//----------------------------------------
							// Reset Caches
							CFW.DB.Spaces.resetCaches();
						}

					}
					
				}
			});
			
			form.appendToPayload(json);
			json.setSuccess(true);	
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	private void createEditForm(JSONResponse json, String ID) {

		CFWSpace space = CFWDBSpaces.selectByID(Integer.parseInt(ID));
		Integer rootid = CFWHierarchy.getRootID(space);
		Integer currentSeniorID = CFWHierarchy.getParentID(space);
		
		addSeniorSelectorField(space, rootid);
		space.updateSelectorFields();
		
		if(space != null) {
			
			CFWForm editSpaceForm = space.toForm("cfwEditSpaceForm"+ID, "Update Space");
			
			editSpaceForm.setFormHandler(new CFWFormHandler() {
				
				@SuppressWarnings({"unchecked" })
				@Override
				public void handleForm(HttpServletRequest request, HttpServletResponse response, CFWForm form, CFWObject origin) {
								
					//--------------------------
					// Validate and Create Object
					if(origin.mapRequestParameters(request)) {
						
						//--------------------------
						// Update values in DB
						boolean success = origin.updateWithout(excludedFields);

						//--------------------------
						// Handle Seniors
						Integer seniorID = null;
						
						if(origin.getFields().containsKey(FIELDNAME_SENIOR_SELECTOR)) {
							
							seniorID = getSeniorID(origin);
							
							
							if(isSeniorValid(space, seniorID)) {
								//--------------------------
								// Update Senior if changed
								if(currentSeniorID != null 
								&& seniorID != null
								&& currentSeniorID.intValue() != seniorID.intValue()) {
									Integer childID = ((CFWSpace)origin).id();
									success = CFWHierarchy.updateParent(origin.getHierarchyConfig(), seniorID, childID);
									if(!success) {
										CFW.Messages.addErrorMessage("Error while updating Senior.");
									}
								}
							}

						}

						//--------------------------
						// Update Users And Admins
						success &= space.saveSelectorFields();

						if(success) {
							CFW.Messages.addSuccessMessage("Saved!");
						}else {
							CFW.Messages.addErrorMessage("Error while updating.");
						}
						
						//----------------------------------------
						// Reset Caches
						CFW.DB.Spaces.resetCaches();
					}
				}
			});
			
			editSpaceForm.appendToPayload(json);
			
		}

	}
}