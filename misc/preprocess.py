import xml.etree.ElementTree as ET
import os
import sys
import re

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

def read_and_preprocess_files(input_dir,output_dir):
	total_count = 0
	title_count = 0
	abst_count = 0
	kwd_count = 0
	body_count = 0
	categories_count = 0

	for root, subdirs, files in os.walk(input_dir):
		for filename in files:
			if (filename.split('.')[-1]=='nxml'):
				file_path = os.path.join(root, filename)
				output_path = re.sub(input_dir,output_dir,file_path)
				try:
					title,keywords,abstract,body,categories = get_info_from_xml(file_path)
				except:
					print("Could not process file " + filename)
				else:
					if not os.path.exists(os.path.dirname(output_path)):
						try:
							os.makedirs(os.path.dirname(output_path))
						except OSError as exc: # Guard against race condition
							print(exc)
							raise
					with open(output_path, 'w') as fo:
						total_count += 1

						if (title!=""):
							fo.write("--TITLE--\n" + title)
							title_count += 1
						if categories:
							fo.write("--CATEGORIES--\n")
							fo.write(" ".join(categories) + "\n")
							categories_count += 1
						if keywords:
							fo.write("--KEYWORDS--\n")
							fo.write(" ".join(keywords) + "\n")
							kwd_count += 1
						if (abstract!=""):
							fo.write("--ABSTRACT--\n" + abstract)
							abst_count += 1
						if (body!=""):
							fo.write("--BODY--\n" + body)
							body_count += 1
						fo.close()

	print("Total Documents: " + str(total_count))
	print("Titles: " + str(title_count) + " (" + str(float(100)*(float(title_count)/float(total_count))) + "%)")
	print("Categories: " + str(categories_count) + " (" + str(float(100)*(float(categories_count)/float(total_count))) + "%)")
	print("Keywords: " + str(kwd_count) + " (" + str(float(100)*(float(kwd_count)/float(total_count))) + "%)")
	print("Abstracts: " + str(abst_count) + " (" + str(float(100)*(float(abst_count)/float(total_count))) + "%)")
	print("Bodies: " + str(body_count) + " (" + str(float(100)*(float(body_count)/float(total_count))) + "%)")

read_and_preprocess_files(sys.argv[1],sys.argv[2])