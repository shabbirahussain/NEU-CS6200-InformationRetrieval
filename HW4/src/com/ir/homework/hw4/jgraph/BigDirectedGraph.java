package com.ir.homework.hw4.jgraph;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.graph.DefaultDirectedGraph;

/**
 * 
 * @author shabbirhussain
 *
 * @param <V> is the vertex node to be stored
 * @param <T> is the numeric translation type which maps vertices to number
 * @param <E> is the Graph Edge
 */
public class BigDirectedGraph<V,E> extends DefaultDirectedGraph<Integer,E> {
	private static final long serialVersionUID = 1L;
	
	private Map<V, Integer> idEncodeMap;
	private Map<Integer, V> idDecodeMap;
	private Integer idSeq;

	/**
	 * Creates a big directed graph
	 * @param edge
	 */
	public BigDirectedGraph(Class<? extends E> edge) {
		super(edge);
		idEncodeMap = new HashMap<V, Integer>();
		idDecodeMap = new HashMap<Integer, V>();
		idSeq = 0;
	}
	
	/**
	 * This method finalizes the graph creation marking nodes as final. 
	 * No new Nodes and Edges can be added after this call.
	 */
	public BigDirectedGraph<V,E> buildGraph(){
		this.idEncodeMap = null;
		return this;
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
	public V decodeVertex(Integer v){
		return idDecodeMap.get(v);
	}
	
    //---------------------------------------------------------------------------------------
	
	/**
	 * Translates vertex to a numeric id
	 * @param v is the vertex to translate to
	 * @return Numeric id of the vertex
	 */
	private Integer encodeVertex(V v){
		Integer vertexId = idEncodeMap.get(v);
		if(vertexId == null){
			vertexId = idSeq++;
			idEncodeMap.put(v, vertexId);
			idDecodeMap.put(vertexId, v);
		}
		return vertexId;
	}
}
