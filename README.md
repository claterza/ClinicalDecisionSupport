# PrecisionMedicine

TODO:
- optimize boost weights (NN?)
- query expansion (negex, metamap)

**---SYSTEM RUN OVERVIEW---**

**-SETUP-**

Download TREC document collection (curr. 2017): http://trec-cds.appspot.com/2017.html#documents

Download TREC topics file (curr. 2015): http://trec-cds.appspot.com/2017.html#topics
	
	
**-MAXENT TYPE CLASSIFIER-**

Model file for treatment/unknown -type classifier (based on past TREC data) is included in repo.
To recreate file, follow the below steps.

1. Generate gold standard type vector files (1 training, 1 test):

		python create_vectors.py [preprocessed_directory] [gold_standard_eval_file]

2. Generate mallet vector files (requires mallet):

		mallet import-svmlight --input treatment_train_vectors.txt --output treatment_train.vectors
		mallet import-svmlight --input treatment_test_vectors.txt --output treatment_test.vectors --use-pipe-from ./treatment_train.vectors
	
3. Generate model files:

		vectors2classify --training-file treatment_train.vectors --testing-file treatment_test.vectors --trainer MaxEnt --output-classifier treatment1 > treatment_stdout --report train:accuracy test:accuracy
	
	
**-INDEX (ABSTRACTS)-**

Create document index:

	XmlDocIndexer.java [output_index_directory] [document_directory] [classifier_model_file (optional)]


**-INDEX (TRIALS)-**
**TBD**
Create clinical trial index:

	XmlTrialIndexer.java [output_index_directory] [document_directory]

	
**-QUERY-**

Make sure you have a valid TREC query XML file; format below.

	<topics task="2017 TREC Precision Medicine">
 	 <topic number="1" type="test">
  	  <disease>Acute lymphoblastic leukemia</disease>
	  <variant>ABL1, PTPN11</variant>
	  <demographic>12-year-old male</demographic>
	  <other>No relevant factors</other>
	 </topic>
	 ...
	</topics>
	
	
**-SEARCH (ABSTRACTS)-**
**NEEDS TO BE UPDATED FOR TREC2017**
Search using Lucene:

	FieldBoostSearchEngine.java [index_directory] [query_file] [output_lucene_file]


**-PAGERANK (ABSTRACTS)-**
**NEEDS TO BE UPDATED FOR TREC2017**
1. Generate document set pagerank scores:

		python pagerank.py [collection_xml_directory] [output_pagerank_file]
	
2. Add scaled pagerank scores to Lucene scores:

		python pagerank_sort.py [lucene_file] [pagerank_file] [output_final_score_file]
	

**-SEARCH (TRIALS)-**
**NEEDS TO BE UPDATED FOR TREC2017**
Search using Lucene:

	FieldBoostTrialSearchEngine.java [index_directory] [query_file] [output_lucene_file]
	
	
**-EVALUATE-**

This is the first year of the new TREC PM task, so there is no way to directly evaluate the results.
Evaluation needs to be done manually, using personal relevancy judgements (see Evaluation discussion
on http://trec-cds.appspot.com/2017.html for more details)
	
