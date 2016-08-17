'''
Created on Aug 14, 2016

@author: shabbirhussain
'''
import nltk
import numpy as np
import lda
from HW8.ElasticClient import TermVectors
from __builtin__ import str

import sys
class DevNull(object): 
    def write(self, data): pass
sys.stderr = DevNull()

nltk.data.path = ['/Users/shabbirhussain/Data/nltk_data']

N_TOP_WORDS = 20
N_ITER = 1000
N_TOPICS = 20
MAX_DOC_PER_Q = 10

HOST       = "elastichost:9200"
INDEX_NAME = "ap_dataset"
DOC_TYPE   = "document"
FIELD_NAME = "TEXT" 

BASE_PATH = '/Users/shabbirhussain/Data/IRData/topic_model/'
QRES_PATH = BASE_PATH + 'output1000_OkapiBM25Controller.txt'
QREL_PATH = BASE_PATH + 'qrels.txt'
QDSC_PATH = BASE_PATH + "query_desc.txt"

def getQueryDocList():
    qMap = {}
    
    # load qres file into map 
    f = open(QRES_PATH, 'r')
    for line in f:
        tokens = line.split()
        key = tokens[0]
        docs = qMap.get(key, {})
        if int(tokens[3]) <= 1000:
            docs.setdefault(tokens[2],0)
        qMap.setdefault(key, docs)
    
    # load qrel into map
    f = open(QREL_PATH, 'r')
    for line in f:
        tokens = line.split()
        key = tokens[0]
        docs = qMap.get(key)
        if(docs == None): continue
        
        docs[tokens[2]] = int(tokens[3])
        qMap[key]       = docs
        #docs.setdefault(tokens[2], int(tokens[3]))
        #qMap.setdefault(key, docs)
    
    #print(qMap.get("60"))
    return qMap

def getQueryDscMap():
    qMap = {}
    
    # load qrel into map
    f = open(QDSC_PATH, 'r')
    for line in f:
        tokens = line.split(".")
        key = tokens[0]
        qMap.setdefault(key, ''.join(tokens[1:]).strip(' \t\n\r'))
    return qMap
    


def printTopics(termVector, docs):
    #X = lda.datasets.load_reuters()
    #vocab = lda.datasets.load_reuters_vocab()
    #titles = lda.datasets.load_reuters_titles()
    
    X     = termVector.getTermMatrix()
    vocab = termVector.getVocab()
    
    model = lda.LDA(n_topics=N_TOPICS, n_iter=N_ITER, random_state=1)
    model.fit(X)  # model.fit_transform(X) is also available
    topic_word = model.topic_word_  # model.components_ also works
    
    for i, topic_dist in enumerate(topic_word):
        sort = np.argsort(topic_dist)
        topic_prob  = topic_dist[sort][:-(N_TOP_WORDS+1):-1]
        topic_words = np.array(vocab)[sort][:-(N_TOP_WORDS+1):-1]
        print(',Topic {}:'.format(i)),
        for j, word in enumerate(topic_words):
            print(' {}({})'.format(word, topic_prob[j])),
    
    print(",Top Topic")
    
    #docs = termVector.getDocuments()
    doc_topic = model.doc_topic_
    doc_keys  = termVector.getDocuments()

    cnt = 1
    for i, topic_dist  in enumerate(doc_topic):
        if(cnt > MAX_DOC_PER_Q): break           # max documents printed
        if(docs.get(doc_keys[i]) == 0): continue # irrelevant document skip
        cnt = cnt + 1
        print("{}".format(doc_keys[i])),
        for prob in doc_topic[i]:
            print(",{}".format(prob)),
        print(",{}".format(doc_topic[i].argmax()))
    pass

if __name__ == '__main__':
    docsMap = getQueryDocList()
    qDecMap = getQueryDscMap()
    
    for key, docs in docsMap.items():
        print('Q{}:{} "{}"'.format(key, str(len(docs)), qDecMap.get(key))),
        ec = TermVectors(host=HOST, 
                   index_name=INDEX_NAME, 
                   doc_type=DOC_TYPE, 
                   field_name=FIELD_NAME, 
                   docs=docs.keys())
        printTopics(ec, docs)
        print("")
        #break
    pass

