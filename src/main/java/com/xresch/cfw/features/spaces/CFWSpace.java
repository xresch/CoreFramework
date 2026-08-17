package com.xresch.cfw.features.spaces;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWHierarchy;
import com.xresch.cfw.datahandling.CFWHierarchyConfig;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionFetch;
import com.xresch.cfw.features.contextsettings.ContextSettings;
import com.xresch.cfw.features.contextsettings.ContextSettings.ContextSettingsFields;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.spaces.CFWSpace.CFWSpaceFields;
import com.xresch.cfw.features.spaces.CFWSpaceAdminsMap.CFWSpaceUserMapFields;
import com.xresch.cfw.features.spaces.FeatureSpaces.FeatureSpaceDefaults;
import com.xresch.cfw.validation.EmailValidator;
import com.xresch.cfw.validation.ExcludeStringsValidator;
import com.xresch.cfw.validation.LengthValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2026
 * @license MIT
 **************************************************************************************************************/
public class CFWSpace extends CFWObject {
	
	public static String TABLE_NAME = "CFW_SPACES";
	
	public enum CFWSpaceFields{
		PK_ID, 
		UUID,
		TYPE, 
		NAME, 
		ABBREVIATION, 
		DESCRIPTION,
		SHARED_EMAIL, 
		IS_ENABLED,
		CLASSIFICATION,
		IS_FOREIGN_SPACE,
		FK_ID_CTXSETTING_JUNIORSYSTEM
	}
	
	public enum CFWSpaceType{
		ORG,
		SUBORG,
		POST
	}
	
	public enum CFWSpaceClassification{
		NONE,
		ADMIN,
		PRODUCTION
	}
		
	
	public static final CFWHierarchyConfig hierarchyConfig = 
		new CFWHierarchyConfig(
				  CFWSpace.class
				, new Object[] {CFWSpaceFields.ABBREVIATION, CFWSpaceFields.NAME}
				, new Object[] {CFWSpaceFields.PK_ID, CFWSpaceFields.NAME}
				  )
		{
	
		@Override
		public boolean canBeReordered(CFWObject targetParent, CFWObject sortedElement) {
			
			CFWSpace sorted = (CFWSpace)sortedElement;
			CFWSpace target = (CFWSpace)targetParent;
			
			//----------------------------------------
			// Prevent Moving ORG
			if(sorted.type() == CFWSpaceType.ORG
			&& target != null) {
				CFW.Messages.addWarningMessage("Cannot move a root space below another root space.");
				return false;
			}
			
			//----------------------------------------
			// Prevent Moving to Other Space
			if(sorted.type() != CFWSpaceType.ORG) {
				//TODO
			}
			return true;
		}
		
		@Override
		public boolean canAccessHierarchy(String rootElementID) {
			return true;
		}
	};
	
	private CFWField<Integer> id = CFWField.newInteger(FormFieldType.HIDDEN, CFWSpaceFields.PK_ID)
								   .setPrimaryKeyAutoIncrement(this)
								   .setDescription("The id of the space in the local database.")
								   .apiFieldType(FormFieldType.NUMBER)
								   .setValue(null);
	
	// has to be set when inserted into DB
	private CFWField<String> uuid = CFWField.newString(FormFieldType.NONE, CFWSpaceFields.UUID)
			.setDescription("The universally unique identifier(uuid) of this space. Used to identify a space across multiple systems.")
			.setValue(UUID.randomUUID().toString());
	
	private CFWField<String> type = CFWField.newString(FormFieldType.UNMODIFIABLE_TEXT, CFWSpaceFields.TYPE)
			.setDescription("The type of the space.")
//			.addOption(CFWSpaceType.POST, "Post")
//			.addOption(CFWSpaceType.ORG, "Space")
//			.setValue(CFWSpaceType.POST.toString())
			;
	
	private CFWField<String> abbreviation = CFWField.newString(FormFieldType.TEXT, CFWSpaceFields.ABBREVIATION)
			.setDescription("(Optional)The abbreviation used for this space.")
			.addValidator(new LengthValidator(-1, 255))
			.addValidator(new ExcludeStringsValidator(new String[] {",", "\""}))
			;
	
	private CFWField<String> name = CFWField.newString(FormFieldType.TEXT, CFWSpaceFields.NAME)
			.setDescription("The name of the space.")
			.addValidator(new LengthValidator(2, 255))
			.addValidator(new ExcludeStringsValidator(new String[] {",", "\""}))
			;
	
	private CFWField<String> description = CFWField.newString(FormFieldType.TEXTAREA, CFWSpaceFields.DESCRIPTION)
			.setDescription("(Optional) Description of this space.")
			.addValidator(new LengthValidator(-1, 255));
	

	private CFWField<String> classification = CFWField.newString(FormFieldType.SELECT, CFWSpaceFields.CLASSIFICATION)
			.setDescription("The classification of the space. Used to evaluate ratios between admininistration and production.")
			.addOption(CFWSpaceClassification.NONE, "None")
			.addOption(CFWSpaceClassification.ADMIN, "Administration")
			.addOption(CFWSpaceClassification.PRODUCTION, "Production")
			.setValue(CFWSpaceClassification.NONE.toString())
			;
	
	private CFWField<String> email = CFWField.newString(FormFieldType.EMAIL, CFWSpaceFields.SHARED_EMAIL)
			.setDescription("(Optional) A mailbox that can be used to contact this space(a shared mailbox, not a personal one).")
			.addValidator(new LengthValidator(-1, 255))
			.addValidator(new EmailValidator());

	
	private CFWField<Boolean> isEnabled = CFWField.newBoolean(FormFieldType.BOOLEAN, CFWSpaceFields.IS_ENABLED)
					.setDescription("Defines if the space is enabled or disabled. Disabled spaces will be hidden in the UI.")
					.setValue(true);
	
	private CFWField<Boolean> isForeignSpace = CFWField.newBoolean(FormFieldType.HIDDEN, CFWSpaceFields.IS_FOREIGN_SPACE)
					.setDescription("Defines if the space is part of the local system or imported from a foreign system.")
					.setValue(false);
	
	private CFWField<Integer> foreignKeyCtxSettingsJuniorSystem = CFWField.newInteger(FormFieldType.HIDDEN, CFWSpaceFields.FK_ID_CTXSETTING_JUNIORSYSTEM)
			.setForeignKeyCascade(this, ContextSettings.class, ContextSettingsFields.PK_ID)
			.setDescription("(Optional)The id of the junior systems context settings.")
			.apiFieldType(FormFieldType.NUMBER)
			.setValue(null);
	
	public CFWSpace() {
		initializeFields();
	}
	
	public CFWSpace(String username) {
		initializeFields();
	}
	
	public CFWSpace(ResultSet result) throws SQLException {
		initializeFields();
		this.mapResultSet(result);
	}
		
	private void initializeFields() {
		this.setTableName(TABLE_NAME);
		
		this.setHierarchyConfig(hierarchyConfig);
		
		this.addFields(id, 
				uuid,
				type,
				abbreviation, 
				name, 
				description,
				classification,
				email,
				isEnabled,
				isForeignSpace,
				foreignKeyCtxSettingsJuniorSystem
				);
	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public void initDB() {

	}
	
	/**************************************************************************************
	 * 
	 **************************************************************************************/
	public ArrayList<APIDefinition> getAPIDefinitions() {
		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
		
		String[] inputFields = 
				new String[] {
						CFWSpaceFields.PK_ID.toString(), 
						CFWSpaceFields.UUID.toString(), 
						CFWSpaceFields.TYPE.toString(), 
						CFWSpaceFields.CLASSIFICATION.toString(), 
						CFWSpaceFields.SHARED_EMAIL.toString(),
						CFWSpaceFields.NAME.toString(),
						CFWSpaceFields.ABBREVIATION.toString(),
						CFWSpaceFields.DESCRIPTION.toString(),
						CFWSpaceFields.IS_ENABLED.toString(),
				};
		
		String[] outputFields = 
				new String[] {
						CFWSpaceFields.PK_ID.toString(), 
						CFWSpaceFields.UUID.toString(), 
						CFWSpaceFields.TYPE.toString(), 
						CFWSpaceFields.CLASSIFICATION.toString(), 
						CFWSpaceFields.SHARED_EMAIL.toString(),
						CFWSpaceFields.NAME.toString(),
						CFWSpaceFields.ABBREVIATION.toString(),
						CFWSpaceFields.DESCRIPTION.toString(),
						CFWSpaceFields.IS_ENABLED.toString(),
						CFWSpaceFields.IS_FOREIGN_SPACE.toString(),
						CFWSpaceFields.FK_ID_CTXSETTING_JUNIORSYSTEM.toString()
				};

		//----------------------------------
		// fetchJSON
		APIDefinitionFetch fetchDataAPI = 
				new APIDefinitionFetch(
						this.getClass(),
						this.getClass().getSimpleName(),
						"fetchData",
						inputFields,
						outputFields
				);
		
		apis.add(fetchDataAPI);
		
		return apis;
	}
	
	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public String createSpaceLabel() {
		return "["+this.abbreviation()+"] "+this.name();
	}

	/*****************************************************************************
	 *  
	 *****************************************************************************/
	public String createBreadcrumbsString() {
		
		StringBuilder builder = new StringBuilder();
		
		LinkedHashMap<Integer, CFWObject> parentList = CFWHierarchy.getParentsAsFlatList(this);
		if(parentList.isEmpty()) {
			return "";
		}
		
		
		CFWSpace current = null;
		
		for(CFWObject object : parentList.values()) {
			current = (CFWSpace)object;
			builder.append(current.abbreviation()+" / ");
		}
		
		return builder.substring(0, builder.length()-2) + current.name();

	}
	
	
	
	
	
	public Integer id() {
		return id.getValue();
	}
	
	public CFWSpace id(Integer value) {
		this.id.setValue(value);
		return this;
	}
	
	public String uuid() {
		return uuid.getValue();
	}
	
	public CFWSpace uuid(String value) {
		this.uuid.setValue(value);
		return this;
	}
	
	public CFWSpaceType type() {
		if(type.getValue() == null) { return null; }
		return CFWSpaceType.valueOf(type.getValue());
	}
	
	public CFWSpace type(CFWSpaceType value) {
		this.type.setValue(value.toString());
		return this;
	}
	
	public CFWSpaceClassification classification() {
		if(classification.getValue() == null) { return null; }
		return CFWSpaceClassification.valueOf(classification.getValue());
	}
	
	public CFWSpace classification(CFWSpaceClassification value) {
		this.classification.setValue(value.toString());
		return this;
	}
	
	public String name() {
		return name.getValue();
	}
	
	public CFWSpace name(String value) {
		this.name.setValue(value);
		return this;
	}
	
	public String abbreviation() {
		return abbreviation.getValue();
	}
	
	public CFWSpace abbreviation(String value) {
		this.abbreviation.setValue(value);
		return this;
	}
	
	public String description() {
		return description.getValue();
	}
	
	public CFWSpace description(String value) {
		this.description.setValue(value);
		return this;
	}
	
	public String email() {
		return email.getValue();
	}
	
	public CFWSpace email(String value) {
		this.email.setValue(value);
		return this;
	}
	
	public boolean isEnabled() {
		return isEnabled.getValue();
	}
	
	public CFWSpace isEnabled(boolean value) {
		this.isEnabled.setValue(value);
		return this;
	}
	
	public boolean isForeignSpace() {
		return isForeignSpace.getValue();
	}
	
	public CFWSpace isForeignSpace(boolean value) {
		this.isForeignSpace.setValue(value);
		return this;
	}
	
	public Integer foreignKeyCtxSettingsJuniorSystem() {
		return foreignKeyCtxSettingsJuniorSystem.getValue();
	}
	
	public CFWSpace foreignKeyCtxSettingsJuniorSystem(Integer value) {
		this.foreignKeyCtxSettingsJuniorSystem.setValue(value);
		return this;
	}
	
	/**********************************************************************************
	 * If the spaces feature is active, returns a selector field that is a foreign key
	 * of the CFWSpace object. If it is inactive, this method returns nothing.
	 * @param parent object this field should be assigned too
	 * @param isHidden if the field is hidden.
	 * @return 
	 **********************************************************************************/
	public static CFWField<Integer> createSpaceSelectorField(CFWObject parent, boolean isHidden) {
		
		if( ! FeatureCore.isFeatureActive(FeatureSpaces.FEATURE_NAME) ) {
			return null;
		}
		
		CFWField<Integer> field;
		
		if(isHidden) {
			field = CFWField.newInteger(FormFieldType.HIDDEN, CFWSpaceUserMapFields.FK_ID_SPACE);
		}else {
			field = CFWField.newInteger(FormFieldType.SELECT, CFWSpaceUserMapFields.FK_ID_SPACE);
		}
		
		field.setColumnDefinition("INT DEFAULT "+ FeatureSpaceDefaults.DEFAULT.id() )
			.setForeignKeyCascade(parent, CFWSpace.class, CFWSpaceFields.PK_ID)
			.setDescription("The space this entity belongs to.")
			.setOptions(CFW.DB.Spaces.getSpaceListForUserOptions())
			.apiFieldType(FormFieldType.SELECT);
		
		return field;
	}
	
	
}
