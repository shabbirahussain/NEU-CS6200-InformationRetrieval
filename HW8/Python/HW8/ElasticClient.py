'''
Created on Aug 15, 2016

@author: shabbirhussain
'''
import elasticsearch
import time
from scipy.sparse import csr_matrix

class TermVectors(object):
    '''
    classdocs
    '''


    def __init__(self, host, index_name, doc_type, field_name, docs):
        '''
        Constructor
            index_name is the name of index to hit
            type_name is the document type to hit
            field_name is the name of field to search
            docs is the list of document ids to fetch
        '''
        self.index_name = index_name
        self.doc_type   = doc_type 
        self.field_name = field_name
        self.es = elasticsearch.Elasticsearch([host])
        #print(self.es.termvectors())
        
        indptr = [0]
        indices = []
        data = []
        vocabulary = {}
        
        size  = len(docs)
        start = time.clock()
        for i, d in enumerate(docs):
            if((time.clock() - start) > 10):
                start = time.clock()
                print ("\t{: 6.2%} done ({:d})".format((float(i)/size), i))
            
            terms = self.get_tv_dict(id=d)
            for term, value in terms.items():
                index = vocabulary.setdefault(term, len(vocabulary))
                indices.append(index)
                data.append(value)
            indptr.append(len(indices))
        self.termMatrix = csr_matrix((data, indices, indptr), dtype=int)
        self.vocabulary = sorted(vocabulary, key=vocabulary.get)
        self.documents  = docs
        
        #print(vocabulary)
    
    def get_tv_dict(self, id):
        '''
        Fetches the term vectors for the given id
            id is the document id to search for
        '''
        #print(id)
        tv_json = self.es.termvectors(index=self.index_name, 
                                     doc_type=self.doc_type,
                                     id=id)
        tv_dict = {}
        try:
            tv_dict = dict([ (k, v['term_freq'])  
                        for k,v in tv_json \
                        .get('term_vectors')\
                        .get(self.field_name)\
                        .get('terms')\
                        .iteritems()])
        except: AttributeError
            
        return tv_dict
            
    
    def getTermMatrix(self): return self.termMatrix
    def getVocab(self): return self.vocabulary
    def getDocuments(self): return self.documents
    
    
