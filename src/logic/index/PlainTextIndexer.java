package logic.index;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.LinkedList;
/**
 * Created by claterza on 6/16/16.
 */
public class PlainTextIndexer implements Indexer {

    private static StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
    private IndexWriter indexWriter;
    private List<File> fileList = new LinkedList<>();

    //Boost weight constants
    private static final float TITLE_BOOST = 3.5f;
    private static final float KEYWORDS_BOOST = 3.0f;
    private static final float ABSTRACT_BOOST = 1.5f;

    public static void main(String[] args) throws IOException {
        String indexPath = args[0];
        String corpusDirectory = args[1];
        PlainTextIndexer indexer;
        try {
            indexer = new PlainTextIndexer(indexPath);
            indexer.indexDirectory(corpusDirectory);
            System.out.println("Added " + corpusDirectory + " to index at " + indexPath);
        } catch (Exception exc) {
            throw new IOException();
        }
        indexer.indexWriter.close();
    }

    public PlainTextIndexer(String indexDirectoryName) throws IOException {
        FSDirectory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryName));
        IndexWriterConfig config = new IndexWriterConfig(this.standardAnalyzer);
        this.indexWriter = new IndexWriter(indexDirectory, config);
    }

    public void indexDirectory(String fileName) throws IOException {
        addDirectory(new File(fileName));
        for (File f : this.fileList) {
            FileReader fileReader = null;
            try {
                Document doc = new Document();
                fileReader = new FileReader(f);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                int counter = 0;
                String line = null;
                String title = "";
                String keywords = "";
                String abstractText = "";
                while((line = bufferedReader.readLine()) != null) {
                    counter++;
                    if(counter==1){
                        title = line;
                    } else if (counter==3){
                        keywords = line;
                    } else {
                        abstractText += " " + line;
                    }
                }
                Field titleField = new TextField("title", title, Field.Store.YES);
                Field keywordsField = new TextField("keywords", keywords, Field.Store.YES);
                Field abstractField = new TextField("abstract", abstractText, Field.Store.YES);
                titleField.setBoost(TITLE_BOOST);
                keywordsField.setBoost(KEYWORDS_BOOST);
                abstractField.setBoost(ABSTRACT_BOOST);
                doc.add(titleField);
                doc.add(keywordsField);
                doc.add(abstractField);
                doc.add(new StringField("path", f.getPath(), Field.Store.YES));
                doc.add(new StringField("filename", f.getName(), Field.Store.YES));
                this.indexWriter.addDocument(doc);
                if (fileReader != null){
                    bufferedReader.close();
                }
            } catch (Exception exc) {
                throw new IOException();
            }
        }
        this.fileList.clear();
    }

    private void addDirectory(File directory) {
        if (!directory.exists()) {
            System.out.println("Can't find directory " + directory);
        }
        for (File f : directory.listFiles()) {
            String fileName = f.getName().toLowerCase();
            if (fileName.endsWith(".nxml")) {
                this.fileList.add(f);
            } else if (f.isDirectory() ){
                addDirectory(f);
            } else {
                System.out.println("Only .nxml files can be added to this index.");
            }
        }
    }
}
