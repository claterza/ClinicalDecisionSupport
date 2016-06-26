package logic.search;

import logic.queryparsing.TrecQueryParser;
import logic.queryparsing.FieldBoostQueryParser;

import models.InputQueries;
import models.TrecQuery;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Paths;

public class FieldBoostSearchEngine implements SearchEngine {

    private static final int TOP_N = 1000;
    private static final double MIN_SCORE = 30;

    public static void main(String[] args) throws Exception {
        String indexFileName = args[0];
        String queryFileName = args[1];
        String outputFileName = args[2];
        FieldBoostSearchEngine searchEngine = new FieldBoostSearchEngine();
        searchEngine.searchWithQueryFile(indexFileName, queryFileName, outputFileName);
    }

    public void searchWithQueryFile(String indexFileName,
                               String queryFileName,
                               String outputFileName) throws Exception {
        // initialize query objects and files
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexFileName)));
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        InputQueries inputQueries = new InputQueries(queryFileName);
        TrecQueryParser queryParser = new FieldBoostQueryParser();
        PrintStream searchOutput = new PrintStream(new File(outputFileName));
        // parse each query and write to output file
        for (TrecQuery trecQuery : inputQueries) {
            Query query = queryParser.parseQueryText(trecQuery.query);
            trecSearch(indexSearcher, query, searchOutput, trecQuery.topicID);
        }
        indexReader.close();
    }

    private static void trecSearch(IndexSearcher indexSearcher, Query query,
                                  PrintStream searchOutput, String topicID) throws IOException {
        TopDocs results = indexSearcher.search(query, TOP_N);
        ScoreDoc[] hits = results.scoreDocs;
        int totalHits = results.totalHits;
        int loopStop = (totalHits < TOP_N) ? totalHits : TOP_N;
        for (int i = 0; i < loopStop; i++) {
            Document doc = indexSearcher.doc(hits[i].doc);
            String topic_no = topicID;
            String pmcid = doc.get("filename").replace(".nxml","");
            int rank = i+1;
            double score = hits[i].score; //probably needs improvement
            String explanation = indexSearcher.explain(query, 0).getDescription();
            System.out.println(query + " Explanation " + explanation + " " + score);
            String runName = "test_run";
            String res = topic_no + " Q0 " + pmcid + " " + rank + " " + score + " " + runName;
            if (score > MIN_SCORE){
                searchOutput.println(res);
                // output format: TOPIC_NO  Q0  PMCID  RANK  SCORE  RUN_NAME
            }
        }
    }
}
