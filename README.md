# ClinicalDecisionSupport

TODO:
- polish XML parsing in XmlDocIndexer
- optimize boost weights (NN?)
- Better query formation

---SYSTEM RUN OVERVIEW---

-SETUP-
Download TREC document collection (curr. 2015): http://trec-cds.appspot.com/2015.html#documents
Download TREC topics file (curr. 2015): http://trec-cds.appspot.com/2015.html#topics
Download TREC gold standard evaluation file (curr. 2015): http://trec-cds.appspot.com/qrels-treceval-2015.txt

Preprocess document collection:
	python preprocess.py [collection_xml_directory] [output_preprocessed_directory]
	
	
-MAXENT CLASSIFIER-
Generate gold standard type vector files:
	python create_vectors.py [preprocessed_directory] [gold_standard_eval_file]
	# generates six files: 1 training and 1 testing file for each type (diagnosis/test/treatment)

Generate mallet vector files (requires mallet):
	mallet import-svmlight --input diagnosis_train_vectors.txt --output diagnosis_train.vectors
	mallet import-svmlight --input diagnosis_test_vectors.txt --output diagnosis_test.vectors --use-pipe-from ./diagnosis_train.vectors
	# run again for test-type files, and again for treatment-type files
	
Generate model files:
	vectors2classify --training-file diagnosis_train.vectors --testing-file diagnosis_test.vectors --trainer MaxEnt --output-classifier diag1 > diag_stdout --report train:accuracy test:accuracy
	# run again for test-type files, and again for treatment-type files
	
	
-INDEX-
Create document index:
	PlainTextIndexer.java [output_index_directory] [preprocessed_directory] [diagnosis_model_file] [test_model_file] [treatment_model_file]
	
	
-SEARCH-
Use TREC topics file to create query file. Format below.
	topicID=X
	type  # test, treatment, or diagnosis
	(diagnosis +) querystring # uses either topic summary or topic description. modified using preferred query build method (UMLS, normalization, etc).
	
	
-SEARCH-
Search using Lucene:
	FieldBoostSearchEngine.java [index_directory] [query_file] [output_lucene_file]


-PAGERANK-
Generate document set pagerank scores:
	python pagerank.py [collection_xml_directory] [output_pagerank_file]
	
Add scaled pagerank scores to Lucene scores:
	python pagerank_sort.py [lucene_file] [pagerank_file] [output_final_score_file]
	
	
-EVALUATE-
Get infAP, infNDCG, and P@10 evals (+ other misc evals):
	perl sample_eval.pl [gold_standard_eval_file] [final_score_file]
	
Get R-prec eval (+ other misc evals):
	python score.py [gold_standard_eval_file] [final_score_file]
	