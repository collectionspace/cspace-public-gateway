package org.collectionspace.publicbrowser.filter;

import com.netflix.util.Pair;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.ZuulFilter;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.collectionspace.publicbrowser.client.Elasticsearch;
import org.collectionspace.publicbrowser.elasticsearch.ESEnvironment;
import org.collectionspace.publicbrowser.request.CollectionSpaceRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.core.env.Environment;

public class CollectionSpaceQueryFilter extends ZuulFilter {
	private static Logger log = LoggerFactory.getLogger(CollectionSpaceQueryFilter.class);

	private Elasticsearch es;
	private String mediaPublishedQuery;

	@Autowired
	private Environment environment;

	@Autowired
	private ESEnvironment esEnvironment;

	@Override
	public String filterType() {
		return FilterConstants.PRE_TYPE;
	}

	@Override
	public int filterOrder() {
		return FilterConstants.PRE_DECORATION_FILTER_ORDER + 1;
	}

	@Override
	public boolean isStaticFilter() {
		return false;
	}

	@Override
	public boolean shouldFilter() {
		HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
		String servletPath = request.getServletPath();
		String[] servletPathParts = servletPath.split("/", 4);
		String api = servletPathParts[2];

		return api.equals("cspace-services");
	}

	@Override
	public Object run() {
		RequestContext context = RequestContext.getCurrentContext();
		String proxyId = (String) context.get(FilterConstants.PROXY_KEY);
		HttpServletRequest request = context.getRequest();

		String servletPath = request.getServletPath();
		String[] servletPathParts = servletPath.split("/", 5);
		String serviceName = servletPathParts.length > 3 ? servletPathParts[3] : "";

		log.info(String.format("%s to %s at %s", request.getMethod(), proxyId, request.getRequestURL().toString()));

		boolean isBlocked = true;

		if (serviceName.equals("media")) {
			String mediaCsid = servletPathParts.length > 4 ? servletPathParts[4] : "";

			if (!mediaCsid.equals("") && isMediaPublished(proxyId, mediaCsid)) {
				setupRequest(context, proxyId, request);

				context.getZuulResponseHeaders().add(new Pair("Cache-Control", "max-age=2419200"));

				isBlocked = false;
			}
		}

		if (serviceName.equals("systeminfo")) {
			setupRequest(context, proxyId, request);
			isBlocked = false;
		}

		if (isBlocked) {
			log.info("Blocking request");

			try {
				context.getResponse().sendError(404);
			} catch (Exception e) {
				context.setThrowable((e));
			}

			context.setSendZuulResponse(false);
		}

		return null;
	}

	private boolean isMediaPublished(String proxyId, String mediaCsid) {
		log.info(String.format("Checking for published media with csid %s", mediaCsid));

		if (this.es == null) {
			String esProxyId = getEsProxyId(proxyId);
			String esUrl = environment.getProperty("zuul.routes." + esProxyId + ".url");

			log.info(String.format("Connecting to ES at %s", esUrl));

			this.es = Elasticsearch.connect(esUrl);
		}

		Map<String, Object> query= new HashMap<String, Object>();

		String mediaPublishedQuery = getMediaPublishedQuery(proxyId);

		if (mediaPublishedQuery != null && mediaPublishedQuery.length() > 0) {
			mediaPublishedQuery = "AND (" + mediaPublishedQuery + ")";
		}

		String q = String.format("((ecm\\:name:%s) AND (ecm\\:primaryType:Media) AND (NOT(ecm\\:currentLifeCycleState:deleted)) %s)", mediaCsid.replace("-", "\\-"), mediaPublishedQuery);

		log.debug(String.format("Query: %s", q));

		query.put("q", q);

		Elasticsearch.CountResult result = this.es.count(query);
		int count = result.getCount();

		if (count < 1) {
			log.warn(String.format("No published media found for csid %s", mediaCsid));

			return false;
		}

		return true;
	}

	private String getMediaPublishedQuery(String proxyId) {
		if (mediaPublishedQuery == null) {
			String esProxyId = getEsProxyId(proxyId);

			String publishToField = esEnvironment.getProperty(esProxyId, "recordTypes.Media.publishToField");
			String[] publishToValues = esEnvironment.getProperty(esProxyId, "allowedPublishToValues", String[].class);

			if (publishToField != null && publishToValues != null) {
				publishToField = publishToField.replace(":", "\\:");

				String values = "(" + StringUtils.join(publishToValues, " OR ") + ")";

				mediaPublishedQuery = publishToField + ":" + values;
			}
		}

		return mediaPublishedQuery;
	}

	private void setupRequest(RequestContext context, String proxyId, HttpServletRequest request) {
		String username = environment.getProperty("zuul.routes." + proxyId + ".username");
		String password = environment.getProperty("zuul.routes." + proxyId + ".password");

		try {
			context.setRequest(new CollectionSpaceRequestWrapper(request, username, password));
		} catch (Exception e) {
			context.setThrowable((e));
		}
	}

	private String getEsProxyId(String proxyId) {
		String tenantId = proxyId.substring(0, proxyId.indexOf("-"));
		String esProxyId = tenantId + "-es";

		return esProxyId;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public ESEnvironment getEsEnvironment() {
		return esEnvironment;
	}

	public void setEsEnvironment(ESEnvironment esEnvironment) {
		this.esEnvironment = esEnvironment;
	}
}
