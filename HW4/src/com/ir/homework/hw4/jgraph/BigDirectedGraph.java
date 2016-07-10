package com.ir.homework.hw4.jgraph;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.graph.DefaultDirectedGraph;

public class BigDirectedGraph<V,E> extends DefaultDirectedGraph<Long,E> {
	private static final long serialVersionUID = 1L;
	
	private Map<V, Long> idEncodeMap;
	private Map<Long, V> idDecodeMap;
	private Long idSeq;

	/**
	 * Creates a big directed graph
	 * @param edge
	 */
	public BigDirectedGraph(Class<? extends E> edge) {
		super(edge);
		idEncodeMap = new HashMap<V, Long>();
		idDecodeMap = new HashMap<Long, V>();
		idSeq = 0L;
	}
	
	/**
	 * Adds vertex to the current graph
	 * @param v is the vertex to add
	 * @return True iff vertex is successfully added
	 */
	
	public boolean encodeAndAddVertex(V v){
		return super.addVertex(this.encodeVertex(v));
	}
	
	/**
	 * Adds an edge to the graph 
	 * @param sourceVertex is the vertex from which edge is started
	 * @param targetVertex is the vertex to which edge is directed
	 * @return Newly created Edge
	 */
	public E encodeAndAddEdge(V sourceVertex, V targetVertex){
		return super.addEdge(this.encodeVertex(sourceVertex), this.encodeVertex(targetVertex));
	}
	

	/**
	 * Translates long vertex to a vertex id
	 * @param v is the vertex to translate to
	 * @return Numeric id of the vertex
	 */
	public V decodeVertex(Long v){
		return idDecodeMap.get(v);
	}
	
    //---------------------------------------------------------------------------------------
	
	/**
	 * Translates vertex to a numeric id
	 * @param v is the vertex to translate to
	 * @return Numeric id of the vertex
	 */
	private Long encodeVertex(V v){
		Long vertexId = idEncodeMap.get(v);
		if(vertexId == null){
			vertexId = idSeq++;
			idEncodeMap.put(v, vertexId);
			idDecodeMap.put(vertexId, v);
		}
		return vertexId;
	}
}
