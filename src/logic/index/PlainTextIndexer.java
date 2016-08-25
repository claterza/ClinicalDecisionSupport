package logic.index;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import models.BoostedDocumentBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Created by claterza on 6/16/16.
 */
public class PlainTextIndexer implements Indexer {

    private static StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
    private IndexWriter indexWriter;
    private List<File> fileList = new LinkedList<>();

    //Boost weight constants
    private static final float TYPE_BOOST = 2.0f;
    private static final float CAT_BOOST = 2.0f;
    private static final float TITLE_BOOST = 3.5f;
    private static final float KEYWORDS_BOOST = 3.0f;
    private static final float ABSTRACT_BOOST = 1.5f;
    private static final float BODY_BOOST = 1.0f;

    public static void main(String[] args) throws IOException {
        String indexPath = args[0];
        String corpusDirectory = args[1];
        PlainTextIndexer indexer;
        try {
            indexer = new PlainTextIndexer(indexPath);
            //indexer.indexDirectory(corpusDirectory);
            indexer.indexDirectoryWithClassifier(corpusDirectory,args[2],args[3],args[4]);
            System.out.println("Added " + corpusDirectory + " to index at " + indexPath);
        } catch (Exception exc) {
        	System.out.println(exc.getMessage());
        	exc.printStackTrace();
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
        // Read model files for type classification
        for (File f : this.fileList) {
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(f);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String linetype = null;
                String line = null;
                String title = "";
                String keywords = "";
                String abstractText = "";
                String bodyText = "";
                String catText = "";
                while((line = bufferedReader.readLine()) != null) {
                    if((line.length() > 2) && (line.substring(0,2).equals("--")) && (line.substring(line.length() - 2).equals("--"))){
                        linetype = line.trim();
                    } else {
                    	if (linetype.equals("--TITLE--")) {
                    		title = line;
                    	} else if (linetype.equals("--KEYWORDS--")) {
                    		keywords = line;
                    	} else if (linetype.equals("--ABSTRACT--")) {
                    		abstractText += " " + line;
                    	} else if (linetype.equals("--BODY--")) {
                    		bodyText += " " + line;
                    	} else if (linetype.equals("--CATEGORIES--")) {
                    		catText = line;
                    	}
                    }
                }
                String types = "";
                
                // build Document and add it to index
                Document doc = new BoostedDocumentBuilder().types(types, TYPE_BOOST).
                    title(title, TITLE_BOOST).keywords(keywords, KEYWORDS_BOOST).
                    abstractText(abstractText, ABSTRACT_BOOST).body(bodyText, BODY_BOOST).
                    categories(catText, CAT_BOOST).fileName(f.getName()).path(f.getPath()).build();
                this.indexWriter.addDocument(doc);
                if (fileReader != null) {
                    bufferedReader.close();
                }
            } catch (Exception exc) {
            	System.out.println(exc.getMessage());
            	exc.printStackTrace();
                throw new IOException();
            }
        }
        this.fileList.clear();
    }
    
    public void indexDirectoryWithClassifier(String fileName, String diagnosisFile, String testFile, String treatmentFile) throws IOException {
        addDirectory(new File(fileName));
        // Read model files for type classification
        List<HashMap<String, HashMap<String, Double>>> modelMaps = getModelMaps(diagnosisFile,testFile,treatmentFile);
        for (File f : this.fileList) {
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(f);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String linetype = null;
                String line = null;
                String title = "";
                String keywords = "";
                String abstractText = "";
                String bodyText = "";
                String catText = "";
                while((line = bufferedReader.readLine()) != null) {
                    if((line.length() > 2) && (line.substring(0,2).equals("--")) && (line.substring(line.length() - 2).equals("--"))){
                        linetype = line.trim();
                    } else {
                    	if (linetype.equals("--TITLE--")) {
                    		title = line;
                    	} else if (linetype.equals("--KEYWORDS--")) {
                    		keywords = line;
                    	} else if (linetype.equals("--ABSTRACT--")) {
                    		abstractText += " " + line;
                    	} else if (linetype.equals("--BODY--")) {
                    		bodyText += " " + line;
                    	} else if (linetype.equals("--CATEGORIES--")) {
                    		catText = line;
                    	}
                    }
                }
                // MaxEnt multi-label Type classifier
                Set<String> tokens = getFeatures((title + " " + keywords + " " + abstractText + " " + bodyText));
                String types = classifyTypes(modelMaps, tokens);
                // build Document and add it to index
                Document doc = new BoostedDocumentBuilder().types(types, TYPE_BOOST).
                    title(title, TITLE_BOOST).keywords(keywords, KEYWORDS_BOOST).
                    abstractText(abstractText, ABSTRACT_BOOST).body(bodyText, BODY_BOOST).
                    categories(catText, CAT_BOOST).fileName(f.getName()).path(f.getPath()).build();
                this.indexWriter.addDocument(doc);
                if (fileReader != null){
                    bufferedReader.close();
                }
            } catch (Exception exc) {
            	System.out.println(exc.getMessage());
            	exc.printStackTrace();
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
    
    private String classifyTypes(List<HashMap<String, HashMap<String, Double>>> modelMaps, Set<String> tokens) {
    	String types = "";
    	for (HashMap<String, HashMap<String, Double>> modelMap : modelMaps) {
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
    		if (!maxLabel.equals("other")) {
    			types += maxLabel + " ";
    		}
    	}
    	return types;
    }
    
    private Set<String> getFeatures(String contents) {
    	Set<String> wordSet = new HashSet<String>();
    	for (String token : contents.split("\\s")) {
    		wordSet.add(token);
    	}
    	return wordSet;
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
    
    private List<HashMap<String, HashMap<String, Double>>> getModelMaps(String diagFilepath, String testFilepath, String treatFilepath) throws IOException {
    	List<HashMap<String, HashMap<String, Double>>> modelMaps = new ArrayList<HashMap<String, HashMap<String, Double>>>();
    	modelMaps.add(readModelFile(diagFilepath));
    	modelMaps.add(readModelFile(testFilepath));
    	modelMaps.add(readModelFile(treatFilepath));
    	return modelMaps;
    }
}
