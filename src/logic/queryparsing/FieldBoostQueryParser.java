package logic.queryparsing;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;

import java.util.HashMap;
import java.util.Map;

public class FieldBoostQueryParser implements TrecQueryParser {

    private Analyzer analyzer;
    private MultiFieldQueryParser parser;

    private static final float TITLE_BOOST = 2.0f;
    private static final float KEYWORDS_BOOST = 1.5f;
    private static final float ABSTRACT_BOOST = 1.8f;
    private static final float BODY_BOOST = 1.0f;
    private static final float TYPE_BOOST = 4.0f;
    private static final float CAT_BOOST = 1.0f;

    private static final Map<String, Float> boostMap;
    static {
        boostMap = new HashMap<String, Float>();
        boostMap.put("types", TYPE_BOOST);
        boostMap.put("abstract", ABSTRACT_BOOST);
        boostMap.put("title", TITLE_BOOST);
        boostMap.put("keywords", KEYWORDS_BOOST);
        boostMap.put("body", BODY_BOOST);
        boostMap.put("categories", CAT_BOOST);
    }

    private static final String[] docFields;
    static {
        docFields = boostMap.keySet().toArray(new String[boostMap.size()]);
    }

    public FieldBoostQueryParser() {
        this.analyzer= new StandardAnalyzer();
        this.parser = new MultiFieldQueryParser(docFields, analyzer, boostMap);
    }

    public Query parseQueryText(String queryText) throws ParseException {
        return this.parser.parse(queryText);
    }

    public Query parseQueryText(String[] query) throws ParseException {
    	String[] fields = {"types","abstract","title","keywords","body","categories"};
    	BooleanClause.Occur[] flags = {BooleanClause.Occur.MUST,
    			BooleanClause.Occur.SHOULD,
    			BooleanClause.Occur.SHOULD,
    			BooleanClause.Occur.SHOULD,
    			BooleanClause.Occur.SHOULD,
    			BooleanClause.Occur.SHOULD};
        return this.parser.parse(query, fields, flags, analyzer);
    }
}
