package com.xresch.cfw.features.query.tutorial;

import java.util.ArrayList;
/**************************************************************************************************************
 * 
 * <ASTQuery> ::= <ASTCommand>+ 
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class ASTQuery extends ASTElement{
	
	ArrayList<ASTElement> commandList = new ArrayList<ASTElement>();
	public ASTQuery(ArrayList<ASTElement> commandList) {
		super(ASTQuery.class);
	}

}
