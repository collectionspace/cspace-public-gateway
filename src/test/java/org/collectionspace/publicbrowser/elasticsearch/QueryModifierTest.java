package org.collectionspace.publicbrowser.elasticsearch;

import java.io.IOException;

import org.junit.*;
import org.springframework.mock.env.MockEnvironment;

// TODO: Flesh out these tests.
public class QueryModifierTest {
	private QueryModifier queryModifier;

	public QueryModifierTest() {
		queryModifier = new QueryModifier();
		
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("es.allowedPublishToValues", "all");
		environment.setProperty("es.allowedRecordTypes", "CollectionObject");
		environment.setProperty("es.recordTypes.CollectionObject.publishToField", "collectionobjects_common:publishToList");

		queryModifier.setEnvironment(environment);
	}

	private void testMultiSearchQuery(String query) throws IOException {
		String result = queryModifier.modifyRequestContent(query, "application/x-ndjson");
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
