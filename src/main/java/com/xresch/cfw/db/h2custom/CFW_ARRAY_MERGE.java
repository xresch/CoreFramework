package com.xresch.cfw.db.h2custom;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import org.h2.value.Value;
import org.h2.value.ValueNumeric;

/************************************************************************
 * Merges integer Arrays into a single array
 ************************************************************************/
public class CFW_ARRAY_MERGE implements org.h2.api.Aggregate{
    
	public static final String FUNCTION_NAME = "CFW_INT_ARRAY_MERGE";
	
	private static BigDecimal[] EMPTY_BIGDEC_ARRAY = new BigDecimal[0];
	
	TreeSet<Object> values = new TreeSet<>();
	int i = 0;
    @Override
    public void init(java.sql.Connection cnctn) throws java.sql.SQLException {
        // I ignored this
    }

    @Override
    public int getInternalType(int[] ints) throws java.sql.SQLException {
    	if(ints.length != 1) {
    		new SQLException(FUNCTION_NAME+": Function only accepts a single parameter."); 
    	}
    	if (ints[0] != org.h2.value.Value.ARRAY
    	&&  ints[0] != org.h2.value.Value.NULL) {
    		new SQLException(FUNCTION_NAME+": Function only accepts values of type ARRAY."); 
    	}
    	
    	return org.h2.value.Value.ARRAY;
    }

    @Override
    public void add(Object o) throws java.sql.SQLException {
    	
    	if(o == null || !o.getClass().isArray()) {
    		return;
    	}
    	Object[] array = (Object[])o;
    	for(Object current : array) {
    		values.add(current);
    	}

    }

    @Override
    public Object getResult() throws java.sql.SQLException {
    	if (values == null) {
            return null;
        }
    	
        return values.toArray(EMPTY_BIGDEC_ARRAY);
        
//    	Collection<Value> valuesInt = new ArrayList<>();
//    	
//    	for(Object value : values) {
//    		valuesInt.add(ValueNumeric.get((BigDecimal)value));
//    	}
//        
//        return valuesInt.toArray(Value.EMPTY_VALUES);
    }
}