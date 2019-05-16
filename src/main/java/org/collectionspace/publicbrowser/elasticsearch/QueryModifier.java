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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class QueryModifier {
	private static Logger log = LoggerFactory.getLogger(QueryModifier.class);

	private ObjectMapper mapper;
	private ObjectReader reader;
	private JsonNode defaultFilter;

	@Autowired
	private Environment environment;

	public QueryModifier() {
		mapper = new ObjectMapper();
		reader = mapper.readerFor(JsonNode.class);
	}

	private JsonNode createDefaultFilter() {
		// TODO: Filter fields returned

		JsonNode lifecycleStateFilterNode = mapper.createObjectNode()
			.set("bool", mapper.createObjectNode()
				.set("mustNot", mapper.createObjectNode()
					.set("term", mapper.createObjectNode()
						.put("ecm:currentLifeCycleState", "deleted")
					)
				)
			);

		JsonNode recordTypesFilterNode = createRecordTypesFilterNode();

		if (recordTypesFilterNode == null) {
			return lifecycleStateFilterNode;
		}

		JsonNode filterNode = mapper.createObjectNode()
			.set("bool", mapper.createObjectNode()
				.set("must", mapper.createArrayNode()
					.add(lifecycleStateFilterNode)
					.add(recordTypesFilterNode)
				)
			);

		return filterNode;
	}

	private JsonNode createRecordTypesFilterNode() {
		String[] allowedRecordTypes = environment.getProperty("es.allowedRecordTypes", String[].class);

		if (allowedRecordTypes == null || allowedRecordTypes.length == 0) {
			return null;
		}

		if (allowedRecordTypes.length == 1) {
			return createRecordTypeFilterNode(allowedRecordTypes[0]);
		}

		ArrayNode filtersNode = mapper.createArrayNode();

		for (String allowedRecordType : allowedRecordTypes) {
			filtersNode.add(createRecordTypeFilterNode(allowedRecordType));
		}

		return mapper.createObjectNode()
			.set("bool", mapper.createObjectNode()
				.set("should", filtersNode)
			);
	}

	private JsonNode createRecordTypeFilterNode(String recordType) {
		String publishToField = environment.getProperty("es.recordTypes." + recordType + ".publishToField");

		return mapper.createObjectNode()
			.set("bool", mapper.createObjectNode()
				.set("must", mapper.createArrayNode()
					.add(mapper.createObjectNode()
						.set("term", mapper.createObjectNode()
							.put("ecm:primaryType", recordType)
						)
					)
					.add(mapper.createObjectNode()
						.set("terms", mapper.createObjectNode()
							.set(publishToField, createPublishToValuesNode())
						)
					)
				)
			);
	}

	private ArrayNode createPublishToValuesNode() {
		String[] publishToValues = environment.getProperty("es.allowedPublishToValues", String[].class);

		if (publishToValues == null) {
			return null;
		}

		ArrayNode arrayNode = mapper.createArrayNode();

		for (String publishToValue : publishToValues) {
			arrayNode.add(publishToValue);
		}

		return arrayNode;
	}

	// private static JsonNode defaultSourceFields = mapper.createObjectNode()
	// 	.set("bool", mapper.createObjectNode()
	// 		.set("excludes", mapper.createArrayNode()
	// 			.add("ecm:*")
	// 			.add("dc:*")
	// 			.add("common:*")
	// 		)
	// 	);

	private JsonNode getFilteredQuery(JsonNode query) {
		// Transform a geo_bounding_box query to a filter (to support es 1.7).

		// JsonNode geoBoundingBox = query.path("bool").path("must").path(1).path("geo_bounding_box");
		// JsonNode geoBoundingBoxQuery = null;

		// if (!geoBoundingBox.isMissingNode()) {
		// 	ArrayNode mustNode = (ArrayNode) query.get("bool").get("must");

		// 	geoBoundingBoxQuery = mustNode.path(1);

		// 	mustNode.remove(1);
		// }

		// Ugh. https://github.com/FasterXML/jackson-databind/issues/212

		ObjectNode boolNode = mapper.createObjectNode();
		JsonNode filter = getDefaultFilter(); // .deepCopy();

		// if (geoBoundingBoxQuery != null) {
		// 	((ArrayNode) filter.get("bool").get("must")).add(geoBoundingBoxQuery);
		// }

		boolNode.set("must", query);
		boolNode.set("filter", filter);

		return mapper.createObjectNode()
			.set("bool", boolNode);
	}

	public JsonNode modifyQuery(ObjectNode query) {
		query.set("query", getFilteredQuery(query.get("query")));

		return query;
	}

	public String modifyRequestContent(String content, String contentType) throws IOException {
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

			String modifiedContent = stringWriter.toString();

			log.info(String.format("Modified content: %s", stringWriter.toString()));

			return modifiedContent;
		}

		throw new IOException(String.format("Unknown content type %s", contentType));
	}

	private JsonNode getDefaultFilter() {
		if (defaultFilter == null) {
			defaultFilter = createDefaultFilter();
		}

		return defaultFilter;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
}
