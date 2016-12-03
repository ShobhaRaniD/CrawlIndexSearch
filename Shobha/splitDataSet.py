import random
import sys
import os
from shutil import copyfile

data = sys.argv[1]
devDir = sys.argv[2]
trainDir = sys.argv[3]

for dirPath, dirNames, files in os.walk(data, topdown=False):
	random.shuffle(files)
	testRange = int((25*len(files))/100)
	testData = files[:testRange]
	trainData = files[testRange:]
	for name in testData:
		dst = devDir + '/' +  name
		src = dirPath + '/'+name 
		copyfile(src, dst)
	for name in trainData:
		dst = trainDir + '/' +  name
		src = dirPath + '/'+name 
		copyfile(src, dst)
