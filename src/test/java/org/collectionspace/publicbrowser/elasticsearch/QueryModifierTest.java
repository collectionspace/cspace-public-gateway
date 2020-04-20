package org.collectionspace.publicbrowser.elasticsearch;

import java.io.IOException;

import org.junit.*;
import org.springframework.mock.env.MockEnvironment;

// TODO: Flesh out these tests.
public class QueryModifierTest {
	private static final String TENANT_ID = "core";
	private static final String PROXY_ID = TENANT_ID + "-es";

	private QueryModifier queryModifier;

	public QueryModifierTest() {
		queryModifier = new QueryModifier(PROXY_ID);

		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("zuul.routes." + PROXY_ID + ".allowedPublishToValues", "all");
		environment.setProperty("zuul.routes." + PROXY_ID + ".allowedRecordTypes", "CollectionObject");
		environment.setProperty("zuul.routes." + PROXY_ID + ".recordTypes.CollectionObject.publishToField", "collectionobjects_common:publishToList");

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
