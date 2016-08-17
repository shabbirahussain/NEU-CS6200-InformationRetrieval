'''
Created on Aug 14, 2016

@author: shabbirhussain
'''
import nltk
import numpy as np
import lda
from HW8.ElasticClient import TermVectors

import sys
class DevNull(object): 
    def write(self, data): pass
#sys.stderr = DevNull()

nltk.data.path = ['/Users/shabbirhussain/Data/nltk_data']

N_TOP_WORDS = 8
N_ITER = 10
N_TOPICS = 200
MAX_DOCS = 100

HOST       = "elastichost:9200"
INDEX_NAME = "ap_dataset"
DOC_TYPE   = "document"
FIELD_NAME = "TEXT" 

BASE_PATH = '/Users/shabbirhussain/Data/IRData/topic_model/'
QDOC_PATH = BASE_PATH + 'doclist.txt'
QREL_PATH = BASE_PATH + 'qrels.txt'

def getRelDocSet():
    qDoc = set()
    
    # load qrel into map
    f = open(QREL_PATH, 'r')
    for line in f:
        tokens = line.split()
        if(int(tokens[3]) == 1):
            qDoc.add(tokens[2])
        
    return qDoc

def getRelDocMap():
    qMap = {}
    
    # load qrel into map
    f = open(QREL_PATH, 'r')
    for line in f:
        tokens = line.split()
        key = tokens[0]
        docs = qMap.get(key)
        if(int(tokens[3]) == 1):
            docs[tokens[2]] = 1
            qMap[key]       = docs
        #docs.setdefault(tokens[2], int(tokens[3]))
        #qMap.setdefault(key, docs)
    
    #print(qMap.get("60"))
    return qMap


def getAllDocList():
    docs = []
    
    # load qres file into map 
    f = open(QDOC_PATH, 'r')
    for i, line in enumerate(f):
        if(i>MAX_DOCS): break
        tokens = line.split()
        docs.append(tokens[1])
        
    return docs


def printTopics(termVector, relDocs):
    X     = termVector.getTermMatrix()
    vocab = termVector.getVocab()
    
    model = lda.LDA(n_topics=N_TOPICS, n_iter=N_ITER, random_state=1)
    model.fit(X)  # model.fit_transform(X) is also available
    topic_word = model.topic_word_  # model.components_ also works
    topicMap   = {} 
    for i, topic_dist in enumerate(topic_word):
        sort = np.argsort(topic_dist)
        topic_words = np.array(vocab)[sort][:-(N_TOP_WORDS+1):-1]
        topicMap[i] = ('Topic {}: {}'.format(i, ' '.join(topic_words)))
        
    #docs = termVector.getDocuments()
    doc_topic = model.doc_topic_
    doc_keys  = termVector.getDocuments()
    for i, topic_dist  in enumerate(doc_topic):
        for j, prob in enumerate(doc_topic[i]):
            print('"{}","{}",{}'.format(doc_keys[i], j, prob))
    print(topicMap)
    pass

if __name__ == '__main__':
    docsLst = getAllDocList()
    relDocs = getRelDocMap()
    ec = TermVectors(host=HOST, 
                   index_name=INDEX_NAME, 
                   doc_type=DOC_TYPE, 
                   field_name=FIELD_NAME, 
                   docs=docsLst)
    printTopics(ec, relDocs)
    pass

