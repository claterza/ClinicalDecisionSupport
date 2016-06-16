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
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.LinkedList;

import org.w3c.dom.*;
import javax.xml.parsers.*;

/**
 * Creates a searchable index.
 * Must provide index path and path to directory containing docs to index.
 */

public class XmlDocIndexer implements Indexer {

    private static StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
    private IndexWriter indexWriter;
    private List<File> fileList = new LinkedList<>();

    //Boost weight constants
    private static final float CONTENTS_BOOST = 1.0f;
    private static final float TITLE_BOOST = 3.5f;
    private static final float KEYWORDS_BOOST = 3.0f;
    private static final float ABSTRACT_BOOST = 2.5F;

    public static void main(String[] args) throws IOException {
        String indexPath = args[0];
        String corpusDirectory = args[1];
        XmlDocIndexer indexer;
        try {
            indexer = new XmlDocIndexer(indexPath);
            indexer.indexDirectory(corpusDirectory);
            System.out.println("Added " + corpusDirectory + " to index at " + indexPath);
        } catch (Exception exc) {
            throw new IOException();
        }
        indexer.indexWriter.close();
    }

    public XmlDocIndexer(String indexDirectoryName) throws IOException {
        FSDirectory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryName));
        IndexWriterConfig config = new IndexWriterConfig(this.standardAnalyzer);
        this.indexWriter = new IndexWriter(indexDirectory, config);
    }

    public void indexDirectory(String fileName) throws IOException {
        addDirectory(new File(fileName));
        for (File f : this.fileList) {
            Document doc = createDocument(f);
            this.indexWriter.addDocument(doc);
        }
        this.fileList.clear();
    }

    private Document createDocument(File f) throws IOException {
        FileReader fileReader = null;
        try {

            NamedNodeMap attributes = getXmlAttributes(f);
            org.apache.lucene.document.Document doc = new Document();
            fileReader = new FileReader(f);
            // Create fields and boost them
            Field contentsField = new TextField("contents",
                    attributes.getNamedItem("contents").getNodeValue(),
                    Field.Store.YES);
            Field titleField = new TextField("title",
                    attributes.getNamedItem("title").getNodeValue(),
                    Field.Store.YES);
            Field keywordsField = new TextField("keywords",
                    attributes.getNamedItem("keywords").getNodeValue(),
                    Field.Store.YES);
            Field abstractField = new TextField("abstract",
                    attributes.getNamedItem("abstract").getNodeValue(),
                    Field.Store.YES);
            Field pathField = new TextField("path", f.getPath(), Field.Store.YES);
            Field filename = new StringField("filename", f.getName(), Field.Store.YES);
            contentsField.setBoost(CONTENTS_BOOST);
            titleField.setBoost(TITLE_BOOST);
            keywordsField.setBoost(KEYWORDS_BOOST);
            abstractField.setBoost(ABSTRACT_BOOST);
            // Add boosted fields to document
            doc.add(contentsField);
            doc.add(titleField);
            doc.add(keywordsField);
            doc.add(abstractField);
            doc.add(pathField);
            doc.add(filename);
            return doc;
        } catch (Exception exc) {
            throw new IOException();
        } finally {
            fileReader.close();
        }
    }

    private NamedNodeMap getXmlAttributes(File f) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        org.w3c.dom.Document xmlDoc = dBuilder.parse(f);
        Element element = xmlDoc.getDocumentElement();
        return element.getAttributes();
    }

    private void addDirectory(File directory) {
        if (!directory.exists()) {
            System.out.println("Can't find directory " + directory);
        }
        for (File f : directory.listFiles()) {
            String fileName = f.getName().toLowerCase();
            if (fileName.endsWith(".nxml")) {
                this.fileList.add(f);
            } else {
                System.out.println("Only .nxml files can be added to this index.");
            }
        }
    }

}