import os
import sys
import re
import nltk

def read_gold_standard_file(fpath):
	diag_docs = set()
	test_docs = set()
	treatment_docs = set()
	total_docs = set()
	with open(fpath, "r") as fin:
		for li in fin:
			line = li.split()
			topic_id = int(line[0])
			doc_num = line[2]
			if (topic_id <= 10):
				diag_docs.add(doc_num)
			elif (topic_id > 10) and (topic_id <= 20):
				test_docs.add(doc_num)
			elif (topic_id > 20):
				treatment_docs.add(doc_num)
			total_docs.add(doc_num)
		fin.close()
	return diag_docs,test_docs,treatment_docs,total_docs

def get_features(fpath):
	features = set()
	with open(fpath, "r") as fin:
		current_field = None
		for li in fin:
			if not ((li.strip()[0:2]=="--") and (li.strip()[-2:]=="--")) and (current_field == "--CATEGORIES--"):
				for token in nltk.word_tokenize(li.decode('utf-8')):
					clean_token = re.sub(r'\:','',token).lower()
					if clean_token != "":
						features.add(clean_token)
			else:
				current_field = li.strip()
		fin.close()
	return features

def get_vector_lists(input_dir,gold_standard_fpath):
	diag_docs,test_docs,treatment_docs,total_docs = read_gold_standard_file(gold_standard_fpath)
	diag_vectors = []
	test_vectors = []
	treatment_vectors = []

	for root, subdirs, files in os.walk(input_dir):
		for filename in files:
			doc_id = filename.split('.')[0]
			if (filename.split('.')[-1]=='nxml') and (doc_id in total_docs):
				feats = get_features(os.path.join(root, filename))
				if doc_id in diag_docs:
					diag_vectors.append(("diagnosis",feats))
				else:
					diag_vectors.append(("other",feats))
				if doc_id in test_docs:
					test_vectors.append(("test",feats))
				else:
					test_vectors.append(("other",feats))
				if doc_id in treatment_docs:
					treatment_vectors.append(("treatment",feats))
				else:
					treatment_vectors.append(("other",feats))

	return diag_vectors,test_vectors,treatment_vectors

def write_vector_file(fpath, vector_list):
	with open(fpath, 'w') as fo:
		for vector in vector_list:
			fo.write(vector[0])
			for feat in vector[1]:
				fo.write(" " + feat.encode('utf-8') + ":1")
			fo.write("\n")
		fo.close()

def create_vector_files(diag_vectors, test_vectors, treatment_vectors):
	diag_train_len = int(float(len(diag_vectors))*float(0.8))
	test_train_len = int(float(len(test_vectors))*float(0.8))
	treat_train_len = int(float(len(treatment_vectors))*float(0.8))

	write_vector_file("diagnosis_train_vectors.txt",diag_vectors[0:diag_train_len])
	write_vector_file("diagnosis_test_vectors.txt",diag_vectors[diag_train_len:])

	write_vector_file("test_train_vectors.txt",test_vectors[0:test_train_len])
	write_vector_file("test_test_vectors.txt",test_vectors[test_train_len:])

	write_vector_file("treatment_train_vectors.txt",treatment_vectors[0:treat_train_len])
	write_vector_file("treatment_test_vectors.txt",treatment_vectors[treat_train_len:])



diag,test,treatment = get_vector_lists(sys.argv[1],sys.argv[2])
create_vector_files(diag,test,treatment)

