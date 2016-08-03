package com.ir.homework.hw7.dataloader.io;


import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.ir.homework.hw7.dataloader.models.MInputData;
import com.ir.homework.hw7.dataloader.parsers.Parser;

public final class MInputDataDAO {
	Parser parser;
	
	
	/**
	 * Gets the model out of 
	 * @param parser
	 * @param clusterName
	 * @param host
	 * @param port
	 * @param indices
	 * @param types
	 * @param limit
	 * @throws UnknownHostException
	 */
	public static MInputData getModel(Parser parser, String clusterName, String host, Integer port, String indices, String types, Integer limit) throws UnknownHostException {
		Builder settings = Settings.settingsBuilder()
				.put("client.transport.ignore_cluster_name", false)
		        .put("node.client", true)
		        .put("client.transport.sniff", true)
				.put("cluster.name", clusterName);

		TransportClient client = TransportClient.builder().settings(settings.build()).build()
				.addTransportAddress(new InetSocketTransportAddress(
									InetAddress.getByName(host), port));
		
		MInputData mInputData = new MInputData(client, parser, indices, types, limit);
		return mInputData;
	}
	
}
