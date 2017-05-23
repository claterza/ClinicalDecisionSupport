package logic.index;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import models.BoostedDocumentBuilder;

/**
 * Creates a searchable index.
 * Must provide index path and path to directory containing docs to index.
 */

public class XmlDocIndexer implements Indexer {

    private static StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
    private IndexWriter indexWriter;
    private List<File> fileList = new LinkedList<>();

    //Boost weight constants
    private static final float JOURNAL_TITLE_BOOST = 1.0f;
    private static final float ARTICLE_TITLE_BOOST = 1.0f;
    private static final float ABSTRACT_BOOST = 1.0f;
    private static final float CHEMICAL_LIST_BOOST = 1.0f;
    private static final float MESHHEADING_LIST_BOOST = 1.0f;
    private static final float TYPE_BOOST = 1.0f;

    public static void main(String[] args) throws IOException {
        String indexPath = args[0];
        String corpusDirectory = args[1];
        String modelFilepath = "";
        if (args.length > 2){
        	modelFilepath = args[2];
        }
        XmlDocIndexer indexer;
        try {
            indexer = new XmlDocIndexer(indexPath);
            if (args.length > 2){
            	indexer.indexDirectory(corpusDirectory, modelFilepath);
            } else {
            	indexer.indexDirectory(corpusDirectory);
            }
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
        	System.out.println("Adding file to index: " + f.getName().toLowerCase());
            List<Document> doclist = createDocuments(f);
            for (Document doc : doclist){
            	this.indexWriter.addDocument(doc);
            }
        }
        this.fileList.clear();
    }
    
    public void indexDirectory(String fileName, String modelFilepath) throws IOException {
        addDirectory(new File(fileName));
        
        HashMap<String, HashMap<String, Double>> modelMap = readModelFile(modelFilepath);
        
        int processing = 1;
        int fileListSize = this.fileList.size();
        System.out.println("Total XML files found: " + fileListSize);
        for (File f : this.fileList) {
        	System.out.println("Processing file " + processing + "/" + fileListSize + "\t(" + f.getName().toLowerCase() + ")");
            List<Document> doclist = createDocuments(f, modelMap);
            for (Document doc : doclist){
            	this.indexWriter.addDocument(doc);
            }
        }
        this.fileList.clear();
    }
    
    private List<Document> createDocuments(File f) throws IOException {
    	List<Document> doclist = new ArrayList<Document>();
    	try {
        	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            org.w3c.dom.Document xmldoc = docBuilder.parse (f);
            
            // normalize text representation
            xmldoc.getDocumentElement ().normalize ();

            NodeList listOfArticles = xmldoc.getElementsByTagName("PubmedArticle");
            int totalArticles = listOfArticles.getLength();
            System.out.println("Total # of articles in file : " + totalArticles);

            for(int s=0; s<totalArticles ; s++){
            	Node articleNode = listOfArticles.item(s);
                if(articleNode.getNodeType() == Node.ELEMENT_NODE){
                	
                	Element articleElement = (Element)articleNode;

                    String pmid = getXmlValue(articleElement, "PMID");
                    String jTitle = getXmlValue(articleElement, "Title");
                    String aTitle = getXmlValue(articleElement, "ArticleTitle");
                    String abstractText = getXmlValue(articleElement, "AbstractText");
                    String chemList = getXmlValueList(articleElement, "Chemical", "NameOfSubstance", null);
                    String mhList = getXmlValueList(articleElement, "MeshHeading", "DescriptorName", "QualifierName");
                    String type = "unknown";
                    
                    Document doc = new BoostedDocumentBuilder().journalTitle(jTitle, JOURNAL_TITLE_BOOST).
                            articleTitle(aTitle, ARTICLE_TITLE_BOOST).abstractText(abstractText, ABSTRACT_BOOST).
                            chemicals(chemList, CHEMICAL_LIST_BOOST).headings(mhList, MESHHEADING_LIST_BOOST).
                            type(type, TYPE_BOOST).fileName(pmid).path(f.getPath()).build();
                    doclist.add(doc);
                }
            }
        } catch (Exception exc) {
        	exc.printStackTrace(System.out);
            throw new IOException();
        } 
    	return doclist;
    }
    
    private List<Document> createDocuments(File f, HashMap<String, HashMap<String, Double>> modelMap) throws IOException {
    	List<Document> doclist = new ArrayList<Document>();
    	try {
        	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            org.w3c.dom.Document xmldoc = docBuilder.parse (f);
            
            // normalize text representation
            xmldoc.getDocumentElement ().normalize ();

            NodeList listOfArticles = xmldoc.getElementsByTagName("PubmedArticle");
            int totalArticles = listOfArticles.getLength();
            System.out.println("Total # of articles in file : " + totalArticles);
            
            for(int s=0; s<totalArticles ; s++){
            	Node articleNode = listOfArticles.item(s);
                if(articleNode.getNodeType() == Node.ELEMENT_NODE){                	
                	Element articleElement = (Element)articleNode;

                    String pmid = getXmlValue(articleElement, "PMID");
                    String jTitle = getXmlValue(articleElement, "Title");
                    String aTitle = getXmlValue(articleElement, "ArticleTitle");
                    String abstractText = getXmlValue(articleElement, "AbstractText");
                    String chemList = getXmlValueList(articleElement, "Chemical", "NameOfSubstance", null);
                    String mhList = getXmlValueList(articleElement, "MeshHeading", "DescriptorName", "QualifierName");
                    Set<String> tokens = getFeatures((jTitle + " " + aTitle + " " + abstractText + " " + chemList + " " + mhList));
                    String type = classifyType(modelMap, tokens);
                    
                    Document doc = new BoostedDocumentBuilder().journalTitle(jTitle, JOURNAL_TITLE_BOOST).
                            articleTitle(aTitle, ARTICLE_TITLE_BOOST).abstractText(abstractText, ABSTRACT_BOOST).
                            chemicals(chemList, CHEMICAL_LIST_BOOST).headings(mhList, MESHHEADING_LIST_BOOST).
                            type(type, TYPE_BOOST).fileName(pmid).path(f.getPath()).build();
                    doclist.add(doc);
                }
            }
        } catch (Exception exc) {
        	exc.printStackTrace(System.out);
            throw new IOException();
        } 
    	return doclist;
    }
    
    
    private String getXmlValueList(Element articleElement, String category, String field, String qualifier) {
    	List<String> outputList = new ArrayList<String>();
    	
    	NodeList listOfItems = articleElement.getElementsByTagName(category);
        int totalItems = listOfItems.getLength();

        for(int t=0; t<totalItems ; t++){
        	List<String> descList = new ArrayList<String>();
        	Element categoryElement = (Element)listOfItems.item(t);
            NodeList itemList = categoryElement.getElementsByTagName(field);
            if (itemList != null && itemList.getLength() > 0) {
            	Element itemElement = (Element)itemList.item(0);
            	descList.add(itemElement.getFirstChild().getNodeValue().trim());
            }
            	
            if (qualifier != null){
            	NodeList listOfQuals = categoryElement.getElementsByTagName(qualifier);
            	if (listOfQuals != null && listOfQuals.getLength() > 0){
            		for(int q=0; q<listOfQuals.getLength() ; q++){
            			Element qualifierElement = (Element)listOfQuals.item(q);
            			if (qualifierElement != null){
            	            descList.add(qualifierElement.getFirstChild().getNodeValue().trim());
            			}
            		}
            	}
            }
            outputList.add(String.join(", ", descList));
        }
    	
        String output = String.join(" | ", outputList);
//        System.out.println(category + " : " + output);
    	return output;
    }
    
    private String getXmlValue(Element articleElement, String field) {
    	String output = "";
    	NodeList itemList = articleElement.getElementsByTagName(field);
    	if (itemList != null && itemList.getLength() > 0) {
    		Element itemElement = (Element)itemList.item(0);
    		output = itemElement.getFirstChild().getNodeValue().trim();
//    		System.out.println(field + " : " + output);
    	}
    	return output;
    }
    

    private void addDirectory(File directory) {
        if (!directory.exists()) {
            System.out.println("Can't find directory " + directory);
        }
        for (File f : directory.listFiles()) {
            String fileName = f.getName().toLowerCase();
            if (fileName.endsWith(".xml")) {
                this.fileList.add(f);
            } else if (f.isDirectory() ){
                addDirectory(f);
            } else {
                System.out.println("Only .xml files can be added to this index.");
            }
        }
    }

    private String classifyType(HashMap<String, HashMap<String, Double>> modelMap, Set<String> tokens) {
    	String maxLabel = null;
    	Double maxLabelValue = 0.0;
    	for (HashMap.Entry<String, HashMap<String, Double>> model : modelMap.entrySet()) {
    		String label = model.getKey();
    		HashMap<String, Double> features = model.getValue();
    		Double Y = 0.0;
    		for (String token : tokens) {
    			Double featureValue = features.get(token);
    			if (featureValue != null) {
    				Y += featureValue;
    			}
    		}
    		Double results = Math.exp(Y);
    		if (results > maxLabelValue) {
    			maxLabelValue = results;
    			maxLabel = label;
    		}
    	}
    	return maxLabel;
    }
    
    private Set<String> getFeatures(String contents) {
    	Set<String> wordSet = new HashSet<String>();
    	for (String token : contents.split("\\s")) {
    		String tok = cleanToken(token);
    		if (tok != ""){
    			wordSet.add(tok);
    		}
    	}
    	return wordSet;
    }
    
    private String cleanToken(String token){
    	return token.replaceAll("[.,|:;()]", "").toLowerCase();
    }
    
    private HashMap<String, HashMap<String, Double>> readModelFile(String modelFilepath) throws IOException {
    	HashMap<String, HashMap<String, Double>> outerMap = new HashMap<String, HashMap<String, Double>>();
    	FileReader fileReader = null;
        try {
        	fileReader = new FileReader(new File(modelFilepath));
	        BufferedReader bufferedReader = new BufferedReader(fileReader);
	        String line = null;
	        HashMap<String, Double> innerMap = null;
	        String currentLabel = null;
	        while((line = bufferedReader.readLine()) != null) {
	        	String[] tokens = line.split(" ");
	        	if (tokens[0].equals("FEATURES")) {
	        		if (currentLabel != null) {
	        			outerMap.put(currentLabel,innerMap);
	        		}
	        		innerMap = new HashMap<String, Double>();
	        		currentLabel = tokens[3];
	        	} else {
	        		innerMap.put(tokens[1], Double.parseDouble(tokens[2]));
	        	}
	        }
	        outerMap.put(currentLabel,innerMap);
	        if (fileReader != null){
                bufferedReader.close();
            }
        }  catch (Exception exc) {
        	System.out.println(exc.getMessage());
        	exc.printStackTrace();
            throw new IOException();
        }
        return outerMap;
    }
}