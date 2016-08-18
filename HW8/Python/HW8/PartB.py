'''
Created on Aug 14, 2016

@author: shabbirhussain
'''
import nltk
import numpy as np
import lda
from HW8.ElasticClient import TermVectors
from sklearn.cluster import KMeans

import sys
class DevNull(object): 
    def write(self, data): pass
#sys.stderr = DevNull()

nltk.data.path = ['/Users/shabbirhussain/Data/nltk_data']

N_TOP_WORDS = 8
N_ITER = 10
N_TOPICS = 200
MAX_DOCS = 100000
N_CLUSTERS = 25

HOST       = "elastichost:9200"
INDEX_NAME = "ap_dataset"
DOC_TYPE   = "document"
FIELD_NAME = "TEXT" 

BASE_PATH = '/Users/shabbirhussain/Data/IRData/topic_model/'
QDOC_PATH = BASE_PATH + 'doclist.txt'
QREL_PATH = BASE_PATH + 'qrels.txt'

def getRelDocMap():
    qDoc = {}
    
    # load qrel into map
    f = open(QREL_PATH, 'r')
    for line in f:
        tokens = line.split()
        if(int(tokens[3]) == 1):
            qDoc[tokens[2]] = tokens[0]
        
    return qDoc

def getAllDocs():
    docs = {}
    
    # load qres file into map 
    f = open(QDOC_PATH, 'r')
    for i, line in enumerate(f):
        if(i>MAX_DOCS): break
        tokens = line.split()
        docs[tokens[1]] = i
        
    return docs

def runLDA(termVector):
    #X = lda.datasets.load_reuters()
    #vocab = lda.datasets.load_reuters_vocab()
    #titles = lda.datasets.load_reuters_titles()
    
    X = termVector.getTermMatrix()

    model = lda.LDA(n_topics=N_TOPICS, n_iter=N_ITER, random_state=1)
    model.fit(X)  # model.fit_transform(X) is also available
    return model

def clusterDocs(X): 
    print "Topic Matrix dim={}".format(X.shape)
    y_pred = KMeans(n_clusters=N_CLUSTERS, random_state=180).fit_predict(X)
    return y_pred

def calcConfusionMat(doc, cluster, relDocs):
    relKeys = relDocs.keys()
    size = len(relKeys)
    
    sqsc = 0
    sqdc = 0
    dqsc = 0
    dqdc = 0 
    for i  in range(size):
        for j in range(i, size):
            try:
                iKey = relKeys[i]
                jKey = relKeys[j]
                
                ipos = doc[iKey]
                jpos = doc[jKey]
                
                if(relDocs[iKey] == relDocs[jKey]):                 # Same Query
                    if(cluster[ipos] == cluster[jpos]): sqsc = sqsc + 1 # Same Cluster
                    else: sqdc = sqdc + 1                               # Different Cluster 
                else:                                               # Different Query
                    if(cluster[ipos] == cluster[jpos]): dqsc = dqsc + 1 # Same Cluster
                    else: dqdc = dqdc + 1                               # Different Cluster
                    
            except KeyError:
                continue
    
    print ("\t\t same cluster \t different clusters")
    print ("same query      \t {} \t {}".format(sqsc, sqdc))
    print ("different query \t {} \t {}".format(dqsc, dqdc))


if __name__ == '__main__':
    docs = getAllDocs()
    print "\nFetching term vector..."
    ec = TermVectors(host=HOST, 
                   index_name=INDEX_NAME, 
                   doc_type=DOC_TYPE, 
                   field_name=FIELD_NAME, 
                   docs=docs.keys())
    vocab = ec.getVocab()
    print "\nGenerating topics..."
    model = runLDA(ec)
    
    print "\nClustering topics..."
    clusters = clusterDocs(model.doc_topic_)
    
    print "\nCalculating Results...\n"
    relDocs = getRelDocMap()
    calcConfusionMat(docs, clusters, relDocs)
    
    pass

