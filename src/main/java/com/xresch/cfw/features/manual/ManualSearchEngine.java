package com.xresch.cfw.features.manual;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.memory.MemoryIndex;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

import com.google.gson.JsonObject;

public class ManualSearchEngine {
	
    private static final int SNIPPET_SIZE = 400;
    private static final int SNIPPET_SIZE_HALF = SNIPPET_SIZE / 2;
	private static final Map<ManualPage, MemoryIndex> indexMap = new HashMap<>();
    private static final StandardAnalyzer analyzer = new StandardAnalyzer();

    
	/*****************************************************************************
	 * Add a ManualPage to the in-memory index
	 *****************************************************************************/
    public static void addPage(ManualPage page) {
        MemoryIndex memoryIndex = new MemoryIndex();
        memoryIndex.addField("content", page.getContentPlaintext(), analyzer);
        indexMap.put(page, memoryIndex);
    }

	/*****************************************************************************
	 *  Search through indexed pages and return matching snippets
	 *****************************************************************************/
 
    public static NavigableMap<Float, JsonObject> searchManual(String searchQuery) {
        TreeMap<Float, JsonObject> sortedResults = new TreeMap<>();

        QueryParser parser = new QueryParser("content", analyzer);
        Query query;
		
        query = parser.createBooleanQuery("content", searchQuery);

        if (query == null) return sortedResults;

        for (Map.Entry<ManualPage, MemoryIndex> entry : indexMap.entrySet()) {
            ManualPage page = entry.getKey();
            MemoryIndex memoryIndex = entry.getValue();

            float score = memoryIndex.search(query);

            if (score > 0.0f) {
                String snippet;
				try {
					snippet = getHighlightedSnippet(page.getContentPlaintext(), query);
				} catch (Exception e) {
					snippet = extractSnippet(page.getContentPlaintext(), searchQuery);
				}
				
				JsonObject object = new JsonObject();
				object.addProperty("score", score );
				object.addProperty("title", page.getLabel() );
				object.addProperty("path", page.resolvePath(null) );
				object.addProperty("snippet", snippet );
				sortedResults.put(score, object);
            }
        }
        return sortedResults.reversed();
    }
    
	/*****************************************************************************
	 *  Search through indexed pages and return matching snippets with highlighting
	 *****************************************************************************/
    private static String getHighlightedSnippet(String text, Query query) throws Exception {
       
    	SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<b class=\"badge badge-warning\" >", "</b>");
        QueryScorer scorer = new QueryScorer(query);
        Highlighter highlighter = new Highlighter(formatter, scorer);
        highlighter.setTextFragmenter(new SimpleFragmenter(SNIPPET_SIZE)); // Snippet length

        TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(text));
        String highlighted = highlighter.getBestFragment(tokenStream, text);
        
        return (highlighted != null) ? highlighted + "..." : text.substring(0, Math.min(text.length(), 200)) + "...";
    }
    
	/*****************************************************************************
	 *  Search through indexed pages and return matching snippets.
	 *****************************************************************************/
    private static String extractSnippet(String text, String searchQuery) {
        int index = text.toLowerCase().indexOf(searchQuery.toLowerCase());

        if (index == -1) {
            return text.substring(0, Math.min(text.length(), SNIPPET_SIZE)) + "...";
        }

        int snippetStart = Math.max(0, index - SNIPPET_SIZE_HALF);
        int snippetEnd = Math.min(text.length(), index + searchQuery.length() + SNIPPET_SIZE_HALF);

        String before = text.substring(snippetStart, index);
        String match = text.substring(index, index + searchQuery.length());
        String after = text.substring(index + searchQuery.length(), snippetEnd);

        return before + "<b class=\"badge badge-warning\" >" + match + "</b>" + after + "...";
    }
    

}