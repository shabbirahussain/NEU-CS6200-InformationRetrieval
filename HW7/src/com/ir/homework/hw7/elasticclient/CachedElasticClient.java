package com.ir.homework.hw7.elasticclient;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;

import com.ir.homework.hw2.queryprocessing.QueryProcessor;


public class CachedElasticClient extends BaseElasticClient{

	public CachedElasticClient(String indices, String types, Integer limit, String field) {
		super(indices, types, limit, field);
		// TODO Auto-generated constructor stub
	}
	
}
