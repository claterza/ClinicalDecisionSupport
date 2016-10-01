import sys
from operator import itemgetter

MULTIPLY_FACTOR = float(1000000)

def pagerank_sort(res_fpath,pr_fpath,out_fpath):
	pagerank = {}
	with open(pr_fpath, "r") as pr:
		for li in pr:
			if li.split()[0] != "Doc":
				vals = li.split()
				pagerank[vals[0]] = float(vals[1])*MULTIPLY_FACTOR
		pr.close()
	task_num = 0
	tasks = {}
	with open(res_fpath, "r") as res:
		for li in res:
			vals = li.split()
			if task_num != int(vals[0]):
				if task_num != 0:
					tasks[task_num].sort(key=itemgetter(4),reverse=True)
				task_num = int(vals[0])
				tasks[task_num] = []
			if vals[2] in pagerank:
				vals[4] = float(vals[4])+pagerank[vals[2]]
			else:
				vals[4] = float(vals[4])
			tasks[task_num].append(vals)
		res.close()
	with open(out_fpath, "w") as fo:
		for task in tasks:
			for row in tasks[task]:
				fo.write(" ".join(map(str,row)) + "\n")
		fo.close()


pagerank_sort(sys.argv[1],sys.argv[2],sys.argv[3])