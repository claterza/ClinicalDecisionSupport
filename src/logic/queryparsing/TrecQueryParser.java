package logic.queryparsing;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;

public interface TrecQueryParser {
	Query parseQueryText(String queryText) throws ParseException;
    Query parseQueryText(String[] query) throws ParseException;
}
