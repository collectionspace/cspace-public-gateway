package org.collectionspace.publicbrowser.elasticsearch;

import java.io.IOException;

import org.junit.*;

// TODO: Flesh out these tests.
public class QueryModifierTest {
	private void testMultiSearchQuery(String query) throws IOException {
		String result = QueryModifier.modifyRequestContent(query, "application/x-ndjson");
		System.out.println("result: " + result);
	}

	@Test
	public void testModifiesMultiSearchQueries() throws IOException {
		testMultiSearchQuery(
			"{\"preference\":\"results\"}\n" +
			"{\"query\":{\"match_all\":{}},\"size\":10,\"from\":0}"
		);

		testMultiSearchQuery(
			"{\"preference\":\"results\"}\n" +
			"{\"query\":{\"bool\":{\"must\":[{\"bool\":{\"should\":[{\"multi_match\":{\"query\":\"2018.1.4\",\"fields\":[\"_all\"],\"type\":\"best_fields\",\"operator\":\"or\",\"fuzziness\":0}},{\"multi_match\":{\"query\":\"2018.1.4\",\"fields\":[\"_all\"],\"type\":\"phrase_prefix\",\"operator\":\"or\"}}],\"minimum_should_match\":\"1\"}}]}},\"size\":10,\"from\":0}"
		);
	}
}
