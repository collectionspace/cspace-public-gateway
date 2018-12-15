package org.collectionspace.publicbrowser.client;

import java.util.Map;

import feign.Feign;
import feign.QueryMap;
import feign.RequestLine;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;

public interface Elasticsearch {
	@RequestLine("GET /_search")
	Result search(@QueryMap Map<String, Object> queryMap);

	static Elasticsearch connect(String url) {
		return Feign.builder()
			.encoder(new GsonEncoder())
			.decoder(new GsonDecoder())
			.target(Elasticsearch.class, url);
	}

	public static class Query {

	}

	public static class Result {
		private Hits hits;

		public Hits getHits() {
			return hits;
		}

		public void setHits(Hits hits) {
			this.hits = hits;
		}

		public class Hits {
			private int total;

			public int getTotal() {
				return total;
			}

			public void setTotal(int total) {
				this.total = total;
			}
		}
	}
}
