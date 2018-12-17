package org.collectionspace.publicbrowser.client;

import java.util.Map;

import feign.Feign;
import feign.QueryMap;
import feign.RequestLine;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;

public interface Elasticsearch {
	@RequestLine("GET /_count")
	CountResult count(@QueryMap Map<String, Object> queryMap);

	@RequestLine("GET /_search")
	SearchResult search(@QueryMap Map<String, Object> queryMap);

	static Elasticsearch connect(String url) {
		return Feign.builder()
			.encoder(new GsonEncoder())
			.decoder(new GsonDecoder())
			.target(Elasticsearch.class, url);
	}

	public static class Query {

	}

	public static class CountResult {
		private int count;

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}
	}

	public static class SearchResult {
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
