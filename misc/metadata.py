import xml.etree.ElementTree as ET
import os
import sys
import re

def read_gold_standard_file(fpath):
	diag_docs = set()
	test_docs = set()
	treat_docs = set()
	total_docs = set()
	doc_count = {}
	with open(fpath, "r") as fin:
		for li in fin:
			line = li.split()
			topic_id = int(line[0])
			doc_num = line[2]
			total_docs.add(doc_num)
			if (topic_id <= 10):
				diag_docs.add(doc_num)
			elif (topic_id > 10) and (topic_id <= 20):
				test_docs.add(doc_num)
			elif (topic_id > 20):
				treat_docs.add(doc_num)
			if (topic_id in doc_count):
				doc_count[topic_id] += 1
			else:
				doc_count[topic_id] = 1
		fin.close()
	return total_docs,diag_docs,test_docs,treat_docs,doc_count

def get_info_from_xml(fpath):
	keywords = []
	abstract = ""
	title = ""
	body = ""
	categories = set()
	tree = ET.parse(fpath)
	root = tree.getroot()
	for kwd in root.iter("kwd"):
		out_kw = ET.tostring(kwd,'utf-8')
		keywords.append(re.sub(r'<[^<>]*>','',out_kw).strip())
	for abst in root.iter('abstract'):
		for p in abst.iter('p'):
			out_abst = ET.tostring(p,'utf-8')
			abstract += re.sub(r'<[^<>]*>','',out_abst).strip() + "\n"
	for titlegroup in root.iter('title-group'):
		for titlestring in titlegroup.iter('article-title'):
			out_title = ET.tostring(titlestring,'utf-8')
			title += re.sub(r'<[^<>]*>','',out_title).strip() + "\n"
	for bdy in root.iter('body'):
		for p in bdy.iter('p'):
			out_body = ET.tostring(p,'utf-8')
			body += re.sub(r'<[^<>]*>','',out_body).strip() + "\n"
	for cat in root.iter("article-categories"):
		for subj in cat.iter('subject'):
			out_subj = ET.tostring(subj,'utf-8')
			categories.add(re.sub(r'<[^<>]*>','',out_subj).strip())
	if not categories:
		categories.add("none")
	return title,keywords,abstract,body,categories

def get_metadata(input_dir,gold_standard_fpath):
	total_docs,diag_docs,test_docs,treat_docs,ind_doc_count = read_gold_standard_file(gold_standard_fpath)

	total_doc_count = 0
	total_title_count = 0
	total_abst_count = 0
	total_kwd_count = 0
	total_body_count = 0
	total_categories = {}

	diag_doc_count = 0
	diag_title_count = 0
	diag_abst_count = 0
	diag_kwd_count = 0
	diag_body_count = 0
	diag_categories = {}

	test_doc_count = 0
	test_title_count = 0
	test_abst_count = 0
	test_kwd_count = 0
	test_body_count = 0
	test_categories = {}

	treat_doc_count = 0
	treat_title_count = 0
	treat_abst_count = 0
	treat_kwd_count = 0
	treat_body_count = 0
	treat_categories = {}

	just_diag = 0
	just_test = 0
	just_treat = 0
	diag_test = 0
	diag_treat = 0
	test_treat = 0
	diag_test_treat = 0

	for root, subdirs, files in os.walk(input_dir):
		for filename in files:
			doc_id = filename.split('.')[0]
			if (filename.split('.')[-1]=='nxml') and (doc_id in total_docs):
				file_path = os.path.join(root, filename)
				try:
					title,keywords,abstract,body,categories = get_info_from_xml(file_path)
				except:
					print("Could not process file " + filename)
				else:
					total_doc_count += 1

					diag_flag = False
					test_flag = False
					treat_flag = False
					if (doc_id in diag_docs):
						diag_flag = True
					if (doc_id in test_docs):
						test_flag = True
					if (doc_id in treat_docs):
						treat_flag = True

					if diag_flag:
						diag_doc_count += 1
						if (title!=""):
							diag_title_count += 1
						if keywords:
							diag_kwd_count += 1
						if (abstract!=""):
							diag_abst_count += 1
						if (body!=""):
							diag_body_count += 1
						for cat in categories:
							if (cat in diag_categories):
								diag_categories[cat] += 1
							else:
								diag_categories[cat] = 1
					if test_flag:
						test_doc_count += 1
						if (title!=""):
							test_title_count += 1
						if keywords:
							test_kwd_count += 1
						if (abstract!=""):
							test_abst_count += 1
						if (body!=""):
							test_body_count += 1
						for cat in categories:
							if (cat in test_categories):
								test_categories[cat] += 1
							else:
								test_categories[cat] = 1
					if treat_flag:
						treat_doc_count += 1
						if (title!=""):
							treat_title_count += 1
						if keywords:
							treat_kwd_count += 1
						if (abstract!=""):
							treat_abst_count += 1
						if (body!=""):
							treat_body_count += 1
						for cat in categories:
							if (cat in treat_categories):
								treat_categories[cat] += 1
							else:
								treat_categories[cat] = 1
					if (diag_flag and not test_flag and not treat_flag):
						just_diag += 1
					if (test_flag and not treat_flag and not diag_flag):
						just_test += 1
					if (treat_flag and not test_flag and not diag_flag):
						just_treat += 1
					if (diag_flag and test_flag and not treat_flag):
						diag_test += 1
					if (diag_flag and treat_flag and not test_flag):
						diag_treat += 1
					if (test_flag and treat_flag and not diag_flag):
						test_treat += 1
					if (diag_flag and test_flag and treat_flag):
						diag_test_treat += 1
					if (title!=""):
						total_title_count += 1
					if keywords:
						total_kwd_count += 1
					if (abstract!=""):
						total_abst_count += 1
					if (body!=""):
						total_body_count += 1
					for cat in categories:
						if (cat in total_categories):
							total_categories[cat] += 1
						else:
							total_categories[cat] = 1

	print("----------------------")
	print("Diagnosis-Only Documents: " + str(just_diag))
	print("Test-Only Documents: " + str(just_test))
	print("Treatment-Only Documents: " + str(just_treat))
	print("Diagnosis+Test Documents: " + str(diag_test))
	print("Diagnosis+Treatment Documents: " + str(diag_treat))
	print("Test+Treatment Documents: " + str(test_treat))
	print("Diagnosis+Test+Treatment Documents: " + str(diag_test_treat))
	print("----------------------")
	print("Total Unique Documents: " + str(total_doc_count))
	print("Total Titles: " + str(total_title_count) + " (" + str(float(100)*(float(total_title_count)/float(total_doc_count))) + "%)")
	print("Total Keywords: " + str(total_kwd_count) + " (" + str(float(100)*(float(total_kwd_count)/float(total_doc_count))) + "%)")
	print("Total Abstracts: " + str(total_abst_count) + " (" + str(float(100)*(float(total_abst_count)/float(total_doc_count))) + "%)")
	print("Total Bodies: " + str(total_body_count) + " (" + str(float(100)*(float(total_body_count)/float(total_doc_count))) + "%)")
	#print("---Total Categories---")
	#for cat, cat_count in total_categories.iteritems():
	#	print(cat + "\t" + str(cat_count))
	print("# Unique Total Categories: " + str(len(total_categories)))
	if "none" in total_categories:
		print("# None Total Categories: " + str(total_categories["none"]))
	else:
		print("# None Total Categories: 0")
	print("----------------------")
	print("Diagnosis Unique Documents: " + str(diag_doc_count))
	print("Diagnosis Titles: " + str(diag_title_count) + " (" + str(float(100)*(float(diag_title_count)/float(diag_doc_count))) + "%)")
	print("Diagnosis Keywords: " + str(diag_kwd_count) + " (" + str(float(100)*(float(diag_kwd_count)/float(diag_doc_count))) + "%)")
	print("Diagnosis Abstracts: " + str(diag_abst_count) + " (" + str(float(100)*(float(diag_abst_count)/float(diag_doc_count))) + "%)")
	print("Diagnosis Bodies: " + str(diag_body_count) + " (" + str(float(100)*(float(diag_body_count)/float(diag_doc_count))) + "%)")
	#print("---Diagnosis Categories---")
	#for cat, cat_count in diag_categories.iteritems():
	#	print(cat + "\t" + str(cat_count))
	print("# Unique Diagnosis Categories: " + str(len(diag_categories)))
	if "none" in diag_categories:
		print("# None Diagnosis Categories: " + str(diag_categories["none"]))
	else:
		print("# None Diagnosis Categories: 0")
	print("----------------------")
	print("Test Unique Documents: " + str(test_doc_count))
	print("Test Titles: " + str(test_title_count) + " (" + str(float(100)*(float(test_title_count)/float(test_doc_count))) + "%)")
	print("Test Keywords: " + str(test_kwd_count) + " (" + str(float(100)*(float(test_kwd_count)/float(test_doc_count))) + "%)")
	print("Test Abstracts: " + str(test_abst_count) + " (" + str(float(100)*(float(test_abst_count)/float(test_doc_count))) + "%)")
	print("Test Bodies: " + str(test_body_count) + " (" + str(float(100)*(float(test_body_count)/float(test_doc_count))) + "%)")
	#print("---Test Categories---")
	#for cat, cat_count in test_categories.iteritems():
	#	print(cat + "\t" + str(cat_count))
	print("# Unique Test Categories: " + str(len(test_categories)))
	if "none" in test_categories:
		print("# None Test Categories: " + str(test_categories["none"]))
	else:
		print("# None Test Categories: 0")
	print("----------------------")
	print("Treatment Unique Documents: " + str(treat_doc_count))
	print("Treatment Titles: " + str(treat_title_count) + " (" + str(float(100)*(float(treat_title_count)/float(treat_doc_count))) + "%)")
	print("Treatment Keywords: " + str(treat_kwd_count) + " (" + str(float(100)*(float(treat_kwd_count)/float(treat_doc_count))) + "%)")
	print("Treatment Abstracts: " + str(treat_abst_count) + " (" + str(float(100)*(float(treat_abst_count)/float(treat_doc_count))) + "%)")
	print("Treatment Bodies: " + str(treat_body_count) + " (" + str(float(100)*(float(treat_body_count)/float(treat_doc_count))) + "%)")
	#print("---Treatment Categories---")
	#for cat, cat_count in treat_categories.iteritems():
	#	print(cat + "\t" + str(cat_count))
	print("# Unique Treatment Categories: " + str(len(treat_categories)))
	if "none" in treat_categories:
		print("# None Treatment Categories: " + str(treat_categories["none"]))
	else:
		print("# None Treatment Categories: 0")
	print("----------------------")
	for docid, docid_count in ind_doc_count.iteritems():
		print("DocID: " + str(docid) + "\t" + "NumDocs: " + str(docid_count))
	print("----------------------")

get_metadata(sys.argv[1],sys.argv[2])