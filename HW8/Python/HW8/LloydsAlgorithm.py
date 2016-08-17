'''
Created on Aug 15, 2016

@author: shabbirhussain
'''
import numpy as np
import random

class LoydsAlgorithm(object):
    '''
    classdocs
    '''
    def cluster_points(self, X, mu):
        clusters  = {}
        for x in X:
            bestmukey = min([(i[0], np.linalg.norm(x-mu[i[0]])) \
                        for i in enumerate(mu)], key=lambda t:t[1])[0]
            try:
                clusters[bestmukey].append(x)
            except KeyError:
                clusters[bestmukey] = [x]
        return clusters
     
    def reevaluate_centers(self, mu, clusters):
        newmu = []
        keys = sorted(clusters.keys())
        for k in keys:
            newmu.append(np.mean(clusters[k], axis = 0))
        return newmu
     
    def has_converged(self, mu, oldmu):
        return (set([tuple(a) for a in mu]) == set([tuple(a) for a in oldmu]))
     
    def find_centers(self, X, K):
        # Initialize to K random centers
        oldmu = random.sample(X, K)
        mu = random.sample(X, K)
        while not self.has_converged(mu, oldmu):
            oldmu = mu
            # Assign all points in X to clusters
            clusters = self.cluster_points(X, mu)
            # Reevaluate centers
            mu = self.reevaluate_centers(oldmu, clusters)
        return(mu, clusters)

    def __init__(self):
        '''
        Constructor
        '''


def init_board(N):
    X = np.array([(random.uniform(-1, 1), random.uniform(-1, 1)) for i in range(N)])
    return X

la = LoydsAlgorithm()
print(la.find_centers(init_board(100), 3))