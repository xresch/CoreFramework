package com.xresch.cfw.db;

/**************************************************************************************************************
 * Class used to create SQL statements for a CFWObject.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWLuceneQuery {
	private StringBuilder luceneQuery = new StringBuilder();
	private CFWSQL initialSQL ;
	
	public CFWLuceneQuery(CFWSQL initialSQL) {
		this.initialSQL = initialSQL;
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWLuceneQuery custom(String custom) {
		luceneQuery.append(custom);
		return this;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWLuceneQuery and() {
		luceneQuery.append(" AND ");
		return this;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWLuceneQuery or() {
		luceneQuery.append(" OR ");
		return this;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWLuceneQuery not () {
		luceneQuery.append(" NOT ");
		return this;
	}
	
		
}