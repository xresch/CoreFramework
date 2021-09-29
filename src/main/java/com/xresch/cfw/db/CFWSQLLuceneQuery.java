package com.xresch.cfw.db;

import com.google.common.base.Strings;
import com.xresch.cfw.datahandling.CFWObject;

/**************************************************************************************************************
 * Class used to create SQL Lucene statements.
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWSQLLuceneQuery {
	private StringBuilder luceneQuery = new StringBuilder();
	private CFWSQL initialSQL;
	
	public CFWSQLLuceneQuery(CFWSQL initialSQL) {
		this.initialSQL = initialSQL;
	}
	
	/****************************************************************
	 * Builds a new CFWSQL instance with a query for fetching the
	 * fulltext result.
	 * Total number of results will be returned in the column
	 * TOTAL_RECORDS.
	 * @param pageSize max number of results, use 0 for all results
	 * @param pageNumber the page that should be fetched.
	 ****************************************************************/
	public CFWSQL build(int pageSize, int pageNumber) {
		return build(null, true, pageSize, pageNumber);
	}
	
	/****************************************************************
	 * Builds a new CFWSQL instance with a query for fetching the
	 * fulltext result.
	 * Total number of results will be returned in the column
	 * TOTAL_RECORDS.
	 * @param sortbyColumn name of the column to sort, can be null or empty string
	 * @param sortAscending true or false
	 * @param pageSize max number of results, use 0 for all results
	 * @param pageNumber the page that should be fetched.
	 ****************************************************************/
	public CFWSQL build(String sortbyColumn, boolean sortAscending, int pageSize, int pageNumber) {
		
		int limit = pageSize < 0 ? 0 : pageSize;
		int offset = pageSize*(pageNumber-1);
		
		
		CFWObject initialObject = initialSQL.getObject();
		CFWSQL fulltextSQL = new CFWSQL(initialObject);
		
//		SELECT T.*, , (SELECT COUNT(*) FROM FTL_SEARCH_DATA('Vic* OR Vik* OR Dion* OR FIRSTNAME:Victoria', 0, 0)) AS TOTAL_RECORDS
//		FROM EXAMPLE_PERSON T 
//		JOIN FTL_SEARCH_DATA('Vic* OR Vik* OR Dion* OR FIRSTNAME:Victoria', 0, 0) FT
//		ON T.PK_ID=FT.KEYS[1];
		fulltextSQL
			.custom(initialSQL.getStatementString().replace("FROM", ", (SELECT COUNT(*) FROM FTL_SEARCH_DATA(?, 0, 0)) AS TOTAL_RECORDS FROM")
				  , luceneQuery.toString())
			.custom("JOIN FTL_SEARCH_DATA(?, ?, ?) FT", luceneQuery.toString(), limit, offset)
			.custom("ON T."+initialObject.getPrimaryField().getName()+"=FT.KEYS[1]");
		
		//--------------------------------
		// Order By
		if( !Strings.isNullOrEmpty(sortbyColumn) ) {
			if(sortAscending) {
				fulltextSQL.orderby(sortbyColumn);
			}else {
				fulltextSQL.orderbyDesc(sortbyColumn);
			}

		}
		
		return fulltextSQL;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWSQLLuceneQuery field(Object fieldname, String query) {
		luceneQuery.append(" "+fieldname+":")
		.append("\""+query+"\" ");
		return this;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWSQLLuceneQuery rangeInclusive(Object fieldname, String from, String to) {
		luceneQuery.append(" "+fieldname+":")
		.append("["+from+" TO "+to+"] ");
		return this;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWSQLLuceneQuery rangeExclusive(Object fieldname, String from, String to) {
		luceneQuery.append(" "+fieldname+":")
		.append("{"+from+" TO "+to+"} ");
		return this;
	}
	
	/****************************************************************
	 * Add a fuzzy search to the query.
	 * @param singleWord 
	 * @param similarity between 0 and 1, higher values indicate
	 *                   higher similarity (standard is 0.5)
	 ****************************************************************/
	public CFWSQLLuceneQuery fuzzy(String singleWord, float similarity) {
		luceneQuery.append(" "+singleWord+"~").append(similarity+" ");
		return this;
	}
	
	/****************************************************************
	 * Add a proximity search to the query.
	 * @param phrase consisting of two or more words
	 * @param proximity in number of words
	 ****************************************************************/
	public CFWSQLLuceneQuery proximity(String phrase, int proximity) {
		luceneQuery.append(" \""+phrase+"\"~").append(proximity+" ");
		return this;
	}
		
	/****************************************************************
	 * Add a boosted search to the query.
	 * @param phrase consisting of two or more words
	 * @param proximity in number of words
	 ****************************************************************/
	public CFWSQLLuceneQuery boost(String phrase, int boostValue) {
		luceneQuery.append(" \""+phrase+"\"^").append(boostValue+" ");
		return this;
	}
		
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWSQLLuceneQuery custom(String custom) {
		luceneQuery.append(custom);
		return this;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWSQLLuceneQuery and() {
		luceneQuery.append(" AND ");
		return this;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWSQLLuceneQuery or() {
		luceneQuery.append(" OR ");
		return this;
	}
	
	/****************************************************************
	 * 
	 ****************************************************************/
	public CFWSQLLuceneQuery not() {
		luceneQuery.append(" NOT ");
		return this;
	}
	
	
	
		
}