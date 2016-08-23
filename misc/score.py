import sys
import re
from operator import itemgetter

def read_topic_file(fpath):
	topics = {}
	with open(fpath, "r") as fin:
		for li in fin:
			values = li.split()
			topic_num = int(values[0])
			if topic_num not in topics:
				topics[topic_num] = []
			topics[topic_num].append(values[2])
	return topics

def get_results(topics_in,topics_gold):
	precision = {}
	recall = {}
	r_precision = {}
	p_10 = {}

	for topic in topics_in:
		topic_precision = 0
		for doc in topics_in[topic]:
			if doc in topics_gold[topic]:
				topic_precision += 1
		precision[topic] = float(topic_precision)/float(len(topics_in[topic]))
		topic_recall = 0
		for doc in topics_gold[topic]:
			if doc in topics_in[topic]:
				topic_recall += 1
		recall[topic] = float(topic_recall)/float(len(topics_gold[topic]))
		r_prec = 0
		for doc in topics_in[topic][0:len(topics_gold[topic])]:
			if doc in topics_gold[topic]:
				r_prec += 1
		r_precision[topic] = float(r_prec)/float(len(topics_gold[topic]))
		p_10_val = 0
		for doc in topics_in[topic][0:10]:
			if doc in topics_gold[topic]:
				p_10_val += 1
		p_10[topic] = float(p_10_val)/float(10)


	return precision,recall,r_precision,p_10

def output_results(precision,recall,r_prec,p_10):
	print("Topic#\tPrecision\tRecall\tR-prec")
	#print("Topic#\tPrecision\tRecall\tR-prec\tP@10")

	precision_total = float(0)
	recall_total = float(0)
	r_prec_total = float(0)
	p_10_total = float(0)

	for topic in sorted(precision):
		precision_total += precision[topic]
		recall_total += recall[topic]
		r_prec_total += r_prec[topic]
		p_10_total += p_10[topic]
		print(str(topic) + "\t" + str(precision[topic]) + "\t" + str(recall[topic]) + "\t" + str(r_prec[topic]))
		#print(str(topic) + "\t" + str(precision[topic]) + "\t" + str(recall[topic]) + "\t" + str(r_prec[topic]) + "\t" + str(p_10[topic]))

	print("---------------------------------------")
	print("Average Precision: " + str(precision_total/float(len(precision))))
	print("Average Recall: " + str(recall_total/float(len(recall))))
	print("Average R-prec: " + str(r_prec_total/float(len(r_prec))))
	#print("Average P@10: " + str(p_10_total/float(len(p_10))))



topics_in = read_topic_file(sys.argv[1])
topics_gold = read_topic_file(sys.argv[2])
prec,rec,rprec,p10 = get_results(topics_in,topics_gold)
output_results(prec,rec,rprec,p10)
