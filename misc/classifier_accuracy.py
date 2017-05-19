import os
import sys
import re
import nltk
import math


def read_gold_standard_file(fpath,classify_type):
	print("Reading gold standard file")
	gold_pos_docs = set()
	gold_neg_docs = set()
	gold_all_docs = set()
	with open(fpath, "r") as fin:
		for li in fin:
			line = li.split()
			topic_id = int(line[0])
			doc_num = line[2]
			if classify_type == "diagnosis":
				if (topic_id <= 10):
					gold_pos_docs.add(doc_num)
				else:
					gold_neg_docs.add(doc_num)
			elif classify_type == "test":
				if (topic_id > 10) and (topic_id <= 20):
					gold_pos_docs.add(doc_num)
				else:
					gold_neg_docs.add(doc_num)
			elif classify_type == "treatment":
				if (topic_id > 20):
					gold_pos_docs.add(doc_num)
				else:
					gold_neg_docs.add(doc_num)
			else:
				print("invalid classifier type")
				return
			gold_all_docs.add(doc_num)
	return gold_pos_docs, gold_neg_docs, gold_all_docs


def get_features(fpath):
	features = set()
	with open(fpath, "r") as fin:
		for li in fin:
			if (len(li) >= 2) and (li[0:2]!="--"):
				for token in nltk.word_tokenize(li.decode('utf-8')):
					clean_token = re.sub(r'\:','',token).lower()
					if clean_token != "":
						features.add(clean_token)
	return features


def read_model_file(fpath):
	print("Reading model file")
	model = {}
	model["pos"] = {}
	model["neg"] = {}
	with open(fpath, "r") as fin:
		current_label = None
		for li in fin:
			line = li.split()
			if (len(line) > 3) and (line[0] == "FEATURES") and (line[1] == "FOR"):
				if line[3] == "other":
					current_label = "neg"
				else:
					current_label = "pos"
			else:
				model[current_label][line[0]] = float(line[1])
	return model


def classify(model,features):
	max_label = None
	max_val = 0
	for model_label in model:
		current_val = 0
		for feature in features:
			if feature in model[model_label]:
				current_val += model[model_label][feature]
		results = math.exp(current_val)
		if current_val > max_val:
			max_val = current_val
			max_label = model_label
	return max_label


def classify_files(input_dir,cpath,gold_standard_docs):
	model = read_model_file(cpath)
	print("Classifying files")
	test_pos = set()
	test_neg = set()
	for root, subdirs, files in os.walk(input_dir):
		for filename in files:
			doc_id = filename.split('.')[0]
			if (filename.split('.')[-1]=='nxml') and (doc_id in gold_standard_docs):
				file_path = os.path.join(root, filename)
				try:
					features = get_features(file_path)
				except:
					print("Could not process file " + filename)
				else:
					label = classify(model,features)
					if label == "pos":
						test_pos.add(doc_id)
					elif label == "neg":
						test_neg.add(doc_id)
	return test_pos, test_neg


def print_accuracy(gold_pos, test_pos, classify_type):
	print("----- Accuracy for classifier " + str(classify_type) + " -----")

	prec_count = 0
	for doc in test_pos:
		if doc in gold_pos:
			prec_count += 1
	precision = float(prec_count)/float(len(test_pos))
	print("Precision:\t" + str(precision))

	rec_count = 0
	for doc in gold_pos:
		if doc in test_pos:
			rec_count += 1
	recall = float(rec_count)/float(len(gold_pos))
	print("Recall:\t\t" + str(recall))

	fmeasure = float(2)*((precision*recall)/(precision+recall))
	print("F-measure:\t" + str(fmeasure))




# python classify_accuracy.py preprocessed_directory gold_standard_filepath classifier_filepath classifier_type

classifier_type = sys.argv[4]

gold_pos_docs, gold_neg_docs, gold_all_docs = read_gold_standard_file(sys.argv[2],classifier_type)

test_pos_docs, test_neg_docs = classify_files(sys.argv[1],sys.argv[3],gold_all_docs)

print_accuracy(gold_pos_docs,test_pos_docs,classifier_type)



