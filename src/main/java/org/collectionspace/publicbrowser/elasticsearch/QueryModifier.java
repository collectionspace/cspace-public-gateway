package org.collectionspace.publicbrowser.elasticsearch;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

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
					.set("not", mapper.createObjectNode()
						.set("filter", mapper.createObjectNode()
							.set("term", mapper.createObjectNode()
								.put("ecm:currentLifeCycleState", "deleted")
							)
						)
					)
				)
				.add(mapper.createObjectNode()
					.set("bool", mapper.createObjectNode()
						.set("should", mapper.createArrayNode()
							.add(mapper.createObjectNode()
								.set("term", mapper.createObjectNode()
									.put("ecm:primaryType", "Materialitem")
								)
							)
							.add(mapper.createObjectNode()
								.set("term", mapper.createObjectNode()
									.put("ecm:primaryType", "CollectionObject")
								)
							)
						)
					)
				)

				// .add(mapper.createObjectNode()
				// 	.set("term", mapper.createObjectNode()
				// 		.put("collectionobjects_common:publishToList", "urn:cspace:publicart.collectionspace.org:vocabularies:name(publishto):item:name(paa)'Public Art Archive'")
				// 	)
				// )
			)
		);

	// private static JsonNode defaultSourceFields = mapper.createObjectNode()
	// 	.set("bool", mapper.createObjectNode()
	// 		.set("excludes", mapper.createArrayNode()
	// 			.add("ecm:*")
	// 			.add("dc:*")
	// 			.add("common:*")
	// 		)
	// 	);

	private static JsonNode getFilteredQuery(JsonNode query) {
		// Transform a geo_bounding_box query to a filter (to support es 1.7).

		// JsonNode geoBoundingBox = query.path("bool").path("must").path(1).path("geo_bounding_box");
		// JsonNode geoBoundingBoxQuery = null;

		// if (!geoBoundingBox.isMissingNode()) {
		// 	ArrayNode mustNode = (ArrayNode) query.get("bool").get("must");

		// 	geoBoundingBoxQuery = mustNode.path(1);

		// 	mustNode.remove(1);
		// }

		// Ugh. https://github.com/FasterXML/jackson-databind/issues/212

		ObjectNode filteredNode = mapper.createObjectNode();
		JsonNode filter = defaultFilter; // .deepCopy();

		// if (geoBoundingBoxQuery != null) {
		// 	((ArrayNode) filter.get("bool").get("must")).add(geoBoundingBoxQuery);
		// }

		filteredNode.set("query", query);
		filteredNode.set("filter", filter);

		return mapper.createObjectNode()
			.set("filtered", filteredNode);
	}

	public static JsonNode modifyQuery(ObjectNode query) {
		query.set("query", getFilteredQuery(query.get("query")));

		return query;
	}

	public static String modifyRequestContent(String content, String contentType) throws IOException {
		log.info(String.format("Request of type %s: %s", contentType, content));

		if (contentType.equals("application/json")) {
			JsonNode query = reader.readValue(content);
			JsonNode modifiedQuery = modifyQuery((ObjectNode) query);

			return mapper.writeValueAsString(modifiedQuery);
		}

		if (contentType.equals("application/x-ndjson")) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);

			MappingIterator<JsonNode> iterator = reader.readValues(content);

			while (iterator.hasNext()) {
				JsonNode header = iterator.next();
				JsonNode query = iterator.next();
				JsonNode modifiedQuery = modifyQuery((ObjectNode) query);

				printWriter.println(mapper.writeValueAsString(header));
				printWriter.println(mapper.writeValueAsString(modifiedQuery));
			}

			printWriter.close();

			return stringWriter.toString();
		}

		throw new IOException(String.format("Unknown content type %s", contentType));
	}
}
