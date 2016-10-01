from __future__ import print_function
import xml.etree.ElementTree as ET
import sys
import re
import os
import nltk
from difflib import SequenceMatcher
from operator import itemgetter

DAMPING_FACTOR = float(0.85)

def similar(a, b):
    return SequenceMatcher(None, a, b).ratio()

def strip_xml(xmlfield):
	return re.sub(r'<[^<>]*>','',ET.tostring(xmlfield,'utf-8')).strip()

def add_to_dict(d,k,v):
	if k in d:
		d[k] += v
	else:
		d[k] = v

def get_article_info(fpath):
	tree = ET.parse(fpath)
	root = tree.getroot()
	title = ""
	year = ""
	refs = set()

	for tgroup in root.iter('title-group'):
		for atitle in tgroup.iter('article-title'):
			if title == "":
				title += strip_xml(atitle)
	for pdate in root.iter('pub-date'):
		for yr in pdate.iter('year'):
			if year == "":
				year += strip_xml(yr)
#	key = title + ":" + year
	key = title

	for rf in root.iter('ref'):
		reftitle = ""
		refyear = ""
		for atitle in rf.iter('article-title'):
			if reftitle == "":
				reftitle += strip_xml(atitle)
		for yr in rf.iter('year'):
			if refyear == "":
				refyear += strip_xml(yr)
#		refs.add(reftitle+":"+refyear)
		refs.add(reftitle)

	return key, refs

def get_ref_lists(input_dir):
	article_keys = {}
	pageranks = {}
	doc_count = 0

	for root, subdirs, files in os.walk(input_dir):
		for filename in files:
			if (filename.split('.')[-1]=='nxml'):
				try:
					a_key,references = get_article_info(os.path.join(root, filename))
				except:
					print("Could not process file " + filename)
				else:
					doc_count += 1
					print("Doc Count:"+str(doc_count), end='\r')
					sys.stdout.flush()
					article_keys[a_key] = filename.split('.')[0]
					ref_count = len(references)
					if ref_count > 0:
						for ref in references:
							add_to_dict(pageranks,ref,(float(1)/float(ref_count)))

	return pageranks,article_keys,doc_count

def get_pageranks(input_dir):
	initial_pageranks,article_keys,doc_count = get_ref_lists(input_dir)
	pageranks = {}
	for art,pr in initial_pageranks.items():
		if art in article_keys:
			doc_id = article_keys[art]
			final_pr = (pr*DAMPING_FACTOR + (float(1)-DAMPING_FACTOR))/float(doc_count)
			pageranks[doc_id] = final_pr
	return pageranks

def pagerank(input_dir,fpath_out):
	pageranks = get_pageranks(input_dir)
	with open(fpath_out, "w") as fout:
		fout.write("Doc ID\tPageRank Val\n")
		for doc, val in sorted(pageranks.items(), key=itemgetter(1)):
			fout.write(doc + "\t" + str(val) + "\n")


pagerank(sys.argv[1],sys.argv[2])