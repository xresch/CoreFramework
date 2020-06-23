
/********************************************************************
 * CFW.JS Tests
 * ============
 * Test javascript for testing.
 *
 *@author Reto Scheiwiller, 2019
 ********************************************************************/

/********************************************************************
 * Executes the tests cases.
 * 
 ********************************************************************/
function cfw_test_createTable(parent){
	
	//---------------------------------
	// Create Table
	var cfwTable = new CFWTable();
	cfwTable.addHeaders(["FirstCol","2ndCol","III Column"]);
	cfwTable.addHeader("AnotherCol");
	
	cfwTable.addRow('<tr>'
			+'<td>A</td>'
			+'<td>B</td>'
			+'<td>C</td>'
			+'<td>D</td>'
			+'</tr>');
	
	cfwTable.addRows('<tr>'
			+'<td>1</td>'
			+'<td>2</td>'
			+'<td>3</td>'
			+'<td>4</td>'
			+'</tr>'
			+'<tr>'
			+'<td>E</td>'
			+'<td>F</td>'
			+'<td>G</td>'
			+'<td>H</td>'
			+'</tr>');
	
	//---------------------------------
	// Add to Page
	var resultDiv = $('<div id="createTableTest">');
	resultDiv.html("<h1> Test new CFWTable()</h1>");
	cfwTable.appendTo(resultDiv);
	parent.append(resultDiv);
	
	//---------------------------------
	// Verify

	if((thCount = $('#createTableTest th').length) != 4){
		CFW.ui.addAlert("error", "Table header count expected 4 but was "+thCount);
	}
	
	if((tdCount = $('#createTableTest td').length) != 12){
		CFW.ui.addAlert("error", "Table cell count expected 12 but was "+tdCount);
	}
	
}

/********************************************************************
 * Minimizing Test
 * 
 ********************************************************************/
function mockupForRemoveCommentsTest(){ 
	
	/*
	 *  Minimize-Test: Multi Line
	 *  
	 */
	/*******************************
	 *  Minimize-Test: Multi Line 2
	 *  
	 *******************************/
	
	/*
	 *  Minimize-Test: Multi Line before
	 *  
	 */ if (true){ return; }
	 
	 if (true){ return; } /*
	 *  Minimize-Test: Multi Line after
	 *  
	 */ 
	 
	//-------------------------------
	// Single line Comments
	//-------------------------------
	 
	/* single line block comment */
	 
	/** before inline **/ if (true){ return 1; }
	 if (true){ return 2; } /** after inline **/
	/** before and after inline **/ if (true){ return 3; } /** before and after inline **/
	
	//-------------------------------
	// In String Comments
	//-------------------------------
	var doubleQuote = "before /* blabla */ after";
	var singleQuote = 'before /* blabla */ after';
	var doubleQuote = "start only /* ";
	var singleQuote = 'end only */';
	var doubleQuote = "before /* blabla */ after" + "more text";
	var doubleQuote = "before /* " + " */ splitted";
	var singleQuote = "before /* blabla */ after" + /*between strings*/ "bla" + /*between strings*/ + "";
	//-------------------------------
	// Crazy Combination tests
	//-------------------------------
	
	/** block before
	 **/ /* before and after inline */ var singleQuote = "before /* blabla */ after" + /*between strings*/ 'bla' + /*between strings*/ + ""; /** before and after inline **/ var anotherVar = false;	/** block after
	**/
}
/********************************************************************
 * Executes the tests cases.
 * 
 ********************************************************************/
function run(){ 
	
	CFW.ui.addAlert("info", "================ TEST ISSUES BELOW THIS LINE =================");
	parent = $("#cfw-content");
	
	cfw_test_createTable(parent);
	
}

run();