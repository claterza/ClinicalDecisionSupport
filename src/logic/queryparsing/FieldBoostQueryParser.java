package logic.queryparsing;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;

import java.util.HashMap;
import java.util.Map;

public class FieldBoostQueryParser implements TrecQueryParser {

    private Analyzer analyzer;
    private MultiFieldQueryParser parser;

    private static final float TITLE_BOOST = 1.1f;
    private static final float KEYWORDS_BOOST = 1.0f;
    private static final float ABSTRACT_BOOST = 1.0f;
    private static final float BODY_BOOST = 1.0f;

    private static final Map<String, Float> boostMap;
    static {
        boostMap = new HashMap<String, Float>();
        boostMap.put("abstract", ABSTRACT_BOOST);
        boostMap.put("title", TITLE_BOOST);
        boostMap.put("keywords", KEYWORDS_BOOST);
        boostMap.put("body", BODY_BOOST);
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
}
