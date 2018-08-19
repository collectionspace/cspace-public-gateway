package org.collectionspace.publicbrowser.elasticsearch;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryModifier {
	private static Logger log = LoggerFactory.getLogger(QueryModifier.class);
	private static ObjectMapper mapper = new ObjectMapper();
	private static ObjectReader reader = mapper.readerFor(JsonNode.class);

	// TODO: Make configurable
	// TODO: Filter fields returned
	private static JsonNode defaultFilter = mapper.createObjectNode()
		.set("bool", mapper.createObjectNode()
			.set("must", mapper.createArrayNode()
				.add(mapper.createObjectNode()
					.set("term", mapper.createObjectNode()
						.put("ecm:primaryType", "CollectionObjectTenant5000")
					)
				)
				.add(mapper.createObjectNode()
					.set("term", mapper.createObjectNode()
						.put("ecm:currentLifeCycleState", "project")
					)
				)
				.add(mapper.createObjectNode()
					.set("term", mapper.createObjectNode()
						.put("collectionobjects_common:publishToList", "urn:cspace:publicart.collectionspace.org:vocabularies:name(publishto):item:name(paa)'Public Art Archive'")
					)
				)
			)
		);

	public static JsonNode modifyQuery(JsonNode query) {
		JsonNode queryValue = query.get("query");

		// Transform a geo_bounding_box query to a filter (to support es 1.7).

		JsonNode geoBoundingBox = queryValue.path("bool").path("must").path(1).path("geo_bounding_box");
		JsonNode geoBoundingBoxQuery = null;

		if (!geoBoundingBox.isMissingNode()) {
			ArrayNode mustNode = (ArrayNode) queryValue.get("bool").get("must");

			geoBoundingBoxQuery = mustNode.path(1);

			mustNode.remove(1);
		}

		// Ugh. https://github.com/FasterXML/jackson-databind/issues/212

		ObjectNode filteredNode = mapper.createObjectNode();
		JsonNode filter = defaultFilter.deepCopy();

		if (geoBoundingBoxQuery != null) {
			((ArrayNode) filter.get("bool").get("must")).add(geoBoundingBoxQuery);
		}

		filteredNode.set("query", queryValue);
		filteredNode.set("filter", filter);

		return ((ObjectNode) query)
			.set("query", mapper.createObjectNode()
				.set("filtered", filteredNode)
			);
	}

	public static String modifyRequestContent(String content, String contentType) throws IOException {
		log.info(String.format("Request of type %s: %s", contentType, content));

		if (contentType.equals("application/json")) {
			JsonNode query = reader.readValue(content);
			JsonNode modifiedQuery = modifyQuery(query);

			return mapper.writeValueAsString(modifiedQuery);
		}

		if (contentType.equals("application/x-ndjson")) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);

			MappingIterator<JsonNode> iterator = reader.readValues(content);

			while (iterator.hasNext()) {
				JsonNode header = iterator.next();
				JsonNode query = iterator.next();
				JsonNode modifiedQuery = modifyQuery(query);

				printWriter.println(mapper.writeValueAsString(header));
				printWriter.println(mapper.writeValueAsString(modifiedQuery));
			}

			printWriter.close();

			return stringWriter.toString();
		}

		throw new IOException(String.format("Unknown content type %s", contentType));
	}
}
