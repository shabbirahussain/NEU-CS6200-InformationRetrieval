'''
Created on Aug 14, 2016

@author: shabbirhussain
'''
import pickle
import nltk
import time
import lda
from HW8.ElasticClient import TermVectors
from sklearn.cluster import KMeans

import sys
class DevNull(object): 
    def write(self, data): pass
#sys.stderr = DevNull()

nltk.data.path = ['/Users/shabbirhussain/Data/nltk_data']

N_TOP_WORDS = 2
N_ITER = 10
N_TOPICS = 200
MAX_DOCS = 100000
N_CLUSTERS = 75

HOST       = "elastichost:9200"
INDEX_NAME = "ap_dataset1"
DOC_TYPE   = "document"
FIELD_NAME = "TEXT" 

BASE_PATH = '/Users/shabbirhussain/Data/IRData/topic_model/'
QDOC_PATH = BASE_PATH + 'doclist.txt'
QREL_PATH = BASE_PATH + 'qrels.txt'
QRES_PATH = BASE_PATH + 'query_desc.txt'
OBJ_STORE_TV = BASE_PATH + "cache/ElasticClient.pyt"

def getRelDocMap():
    qDoc = {}
    
    qLst = set()
    f = open(QRES_PATH, 'r')
    for line in f:
        tokens = line.split(".")
        qLst.add(tokens[0])
    
    
    # load qrel into map
    f = open(QREL_PATH, 'r')
    for line in f:
        tokens = line.split()
        if(int(tokens[3]) == 1):
            qry = tokens[0]
            if(qry in qLst):
                queries = qDoc.get(tokens[2], set())
                queries.add(qry)
                qDoc[tokens[2]] = queries
        
    return qDoc

def getAllDocs():
    docs = {}
    
    # load qres file into map 
    f = open(QDOC_PATH, 'r')
    for i, line in enumerate(f):
        if(i>=MAX_DOCS): break
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
    print "Topic Matrix dim = {}".format(X.shape)
    y_pred = KMeans(n_clusters=N_CLUSTERS, random_state=100).fit_predict(X)
    return y_pred

def calcConfusionMat(doc, cluster, relDocs):
    relKeys = relDocs.keys()
    size = len(relKeys)
    
    sqsc = 0
    sqdc = 0
    dqsc = 0
    dqdc = 0 
    for i  in range(size):
        for j in range(i+1, size):
            try:
                iKey = relKeys[i]
                jKey = relKeys[j]
                
                ipos = doc[iKey]
                jpos = doc[jKey]
                
                for qi in relDocs[iKey]:
                    for qj in relDocs[jKey]:
                        if(qi == qj):                 # Same Query
                            if(cluster[ipos] == cluster[jpos]): sqsc = sqsc + 1 # Same Cluster
                            else: sqdc = sqdc + 1                               # Different Cluster 
                        else:                                               # Different Query
                            if(cluster[ipos] == cluster[jpos]): dqsc = dqsc + 1 # Same Cluster
                            else: dqdc = dqdc + 1                               # Different Cluster
            except  KeyError: continue
    
    print ("\n                \t same cluster \t different clusters")
    print ("-----------------------------------------------------------")
    print ("same query      \t {:12d} \t {:18d}".format(sqsc, sqdc))
    print ("different query \t {:12d} \t {:18d}".format(dqsc, dqdc))
    print ("\n*** Accuracy = {: 2.2%}".format(float(sqsc+dqdc)/float(sqsc+dqdc+sqdc+dqsc)))
    
    pass

import datetime
def log(start, msg):
    end = time.time()
    datetime.datetime.today()
    print "\n[{}][Took:{: 5.2f}s]{}".format(str(datetime.datetime.now())
                                       , end-start
                                       , msg)
    return end


def getTermVectors(filePath):
    try:
        with open(filePath,'rb') as f:
            ec = pickle.load(f)
    except Exception: 
        log(time.time(), "Fetching term vector...")
        ec = TermVectors(host=HOST, 
                   index_name=INDEX_NAME, 
                   doc_type=DOC_TYPE, 
                   field_name=FIELD_NAME, 
                   docs=docKeys)
        #with open(filePath,'wb') as f:
        #    pickle.dump(ec, f)
    return ec
    
    
    
if __name__ == '__main__':
    start = time.time()
    log(start, "Initializing...")
    docs = getAllDocs()
    docKeys = docs.keys()
    
    log(start, "Loading term vector...")
    ec = getTermVectors(OBJ_STORE_TV)
    
    vocab = ec.getVocab()
    log(start, "Generating topics...") 
    model = runLDA(ec)
    
    log(start, "Clustering topics...")
    clusters = clusterDocs(model.doc_topic_)
    
    log(start, "Calculating Results...")
    relDocs = getRelDocMap()
    calcConfusionMat(docs, clusters, relDocs)
    
    log(start, "Done.")
    
    pass

