package com.xresch.cfw.utils;


import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;

import java.lang.instrument.Instrumentation;


public class CFWDump {
    private static CFWDump instance = new CFWDump();
    
    /*************************************************************************
     * 
     * @return
     *************************************************************************/
    private static CFWDump getInstance() {
        return instance;
    }

    /*************************************************************************
     * 
     * @return
     *************************************************************************/
    private class DumpContext {
        int maxDepth = 0;
        int maxArrayElements = 0;
        int callCount = 0;
        HashMap<String, String> ignoreList = new HashMap<String, String>();
        HashMap<Object, Integer> visited = new HashMap<Object, Integer>();
    }
    
    /*************************************************************************
     * 
     * @return
     *************************************************************************/
    public static String dumpObject(Object o) {
        return dumpObject(o, 0, 0, null);
    }
    
    /*************************************************************************
     * 
     * @return
     *************************************************************************/
    public static String dumpObject(Object o, int maxDepth, int maxArrayElements, String[] ignoreList) {
        DumpContext ctx = CFWDump.getInstance().new DumpContext();
        ctx.maxDepth = maxDepth;
        ctx.maxArrayElements = maxArrayElements;

        if (ignoreList != null) {
            for (int i = 0; i < Array.getLength(ignoreList); i++) {
                int colonIdx = ignoreList[i].indexOf(':');
                if (colonIdx == -1)
                    ignoreList[i] = ignoreList[i] + ":";
                ctx.ignoreList.put(ignoreList[i], ignoreList[i]);
            }
        }

        return dump(o, ctx);
    }
    
    /*************************************************************************
     * 
     * @return
     *************************************************************************/
    private static String dump(Object o, DumpContext ctx) {
        if (o == null) {
            return "<null>";
        }

        ctx.callCount++;
        StringBuilder tabs = new StringBuilder();
        for (int k = 0; k < ctx.callCount; k++) {
            tabs.append("\t");
        }
        StringBuilder builder = new StringBuilder();
        Class oClass = o.getClass();

        String oSimpleName = getSimpleNameWithoutArrayQualifier(oClass);

        if (ctx.ignoreList.get(oSimpleName + ":") != null)
            return "<Ignored>";

        if (oClass.isArray()) {
            //buffer.append("\n");
            //buffer.append(tabs.toString().substring(1));
            builder.append(" [");
            int rowCount = ctx.maxArrayElements == 0 ? Array.getLength(o) : Math.min(ctx.maxArrayElements, Array.getLength(o));
            for (int i = 0; i < rowCount; i++) {
            	builder.append("\n");
            	builder.append(tabs.toString());
                try {
                    Object value = Array.get(o, i);
                    builder.append(dumpValue(value, ctx));
                } catch (Exception e) {
                    builder.append(e.getMessage());
                }
                if (i < Array.getLength(o) - 1)
                    builder.append(",");
                
            }
            if (rowCount < Array.getLength(o)) {
            	builder.append("\n");
                builder.append(tabs.toString());
                builder.append(Array.getLength(o) - rowCount + " more array elements...");
                
            }
            builder.append("]");
        } else {
            builder.append("\n");
            builder.append(tabs.toString().substring(1));
            builder.append("{\n");
            builder.append(tabs.toString());
            builder.append("hashCode: " + o.hashCode());
            builder.append("\n");
            while (oClass != null && oClass != Object.class) {
                Field[] fields = oClass.getDeclaredFields();

                if (ctx.ignoreList.get(oClass.getSimpleName()) == null) {
                    if (oClass != o.getClass()) {
                        builder.append(tabs.toString());
                        builder.append("===== Inherited - " + oSimpleName + " =====\n");
                    }

                    for (int i = 0; i < fields.length; i++) {

                        String fSimpleName = getSimpleNameWithoutArrayQualifier(fields[i].getType());
                        String fName = fields[i].getName();

                        fields[i].setAccessible(true);
                        builder.append(tabs.toString());
                        builder.append(fName + "(" + fSimpleName + ")");
                        builder.append("=");

                        if (ctx.ignoreList.get(":" + fName) == null &&
                            ctx.ignoreList.get(fSimpleName + ":" + fName) == null &&
                            ctx.ignoreList.get(fSimpleName + ":") == null) {

                            try {
                                Object value = fields[i].get(o);
                                builder.append(dumpValue(value, ctx));
                            } catch (Exception e) {
                                builder.append(e.getMessage());
                            }
                            builder.append("\n");
                        }
                        else {
                            builder.append("<Ignored>");
                            builder.append("\n");
                        }
                    }
                    oClass = oClass.getSuperclass();
                    oSimpleName = oClass.getSimpleName();
                }
                else {
                    oClass = null;
                    oSimpleName = "";
                }
            }
            builder.append(tabs.toString().substring(1));
            builder.append("}");
        }
        ctx.callCount--;
        return builder.toString();
    }

    /*************************************************************************
     * 
     * @return
     *************************************************************************/
    private static String dumpValue(Object value, DumpContext ctx) {
        if (value == null) {
            return "<null>";
        }
        if (value.getClass().isPrimitive() ||
            value.getClass() == java.lang.Short.class ||
            value.getClass() == java.lang.Long.class ||
            value.getClass() == java.lang.String.class ||
            value.getClass() == java.lang.Integer.class ||
            value.getClass() == java.lang.Float.class ||
            value.getClass() == java.lang.Byte.class ||
            value.getClass() == java.lang.Character.class ||
            value.getClass() == java.lang.Double.class ||
            value.getClass() == java.lang.Boolean.class ||
            value.getClass() == java.util.Date.class ||
            value.getClass().isEnum()) {

            return value.toString();

        } else {

            Integer visitedIndex = ctx.visited.get(value);
            if (visitedIndex == null) {
                ctx.visited.put(value, ctx.callCount);
                if (ctx.maxDepth == 0 || ctx.callCount < ctx.maxDepth) {
                    return dump(value, ctx);
                }
                else {
                    return "<Reached max recursion depth>";
                }
            }
            else {
                return "<Previously visited - see hashCode " + value.hashCode() + ">";
            }
        }
    }


    /*************************************************************************
     * 
     * @return
     *************************************************************************/
    private static String getSimpleNameWithoutArrayQualifier(Class clazz) {
        String simpleName = clazz.getSimpleName();
        int indexOfBracket = simpleName.indexOf('['); 
        if (indexOfBracket != -1)
            return simpleName.substring(0, indexOfBracket);
        return simpleName;
    }
}
