package org.collectionspace.publicbrowser.filter;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.ZuulFilter;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.collectionspace.publicbrowser.client.Elasticsearch;
import org.collectionspace.publicbrowser.request.CollectionSpaceRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CollectionSpaceQueryFilter extends ZuulFilter {
	private static Logger log = LoggerFactory.getLogger(CollectionSpaceQueryFilter.class);
	private Elasticsearch es;

	@Autowired
	private Environment environment;

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
		String root = servletPathParts[1];

		return root.equals("cspace-services");
	}

	@Override
	public Object run() {
		RequestContext context = RequestContext.getCurrentContext();
		String proxyId = (String) context.get(FilterConstants.PROXY_KEY);
		HttpServletRequest request = context.getRequest();

		String servletPath = request.getServletPath();
		String[] servletPathParts = servletPath.split("/", 5);
		String serviceName = servletPathParts.length > 2 ? servletPathParts[2] : "";

		log.info(String.format("%s to %s at %s", request.getMethod(), proxyId, request.getRequestURL().toString()));

		boolean isBlocked = true;

		if (serviceName.equals("media")) {
			String mediaCsid = servletPathParts.length > 3 ? servletPathParts[3] : "";

			if (!mediaCsid.equals("") && isMediaPublished(mediaCsid)) {
				String username = environment.getProperty("zuul.routes." + proxyId + ".username");
				String password = environment.getProperty("zuul.routes." + proxyId + ".password");

				try {
					context.setRequest(new CollectionSpaceRequestWrapper(request, username, password));
				} catch (Exception e) {
					context.setThrowable((e));
				}

				isBlocked = false;
			}
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

	private boolean isMediaPublished(String mediaCsid) {
		log.info(String.format("Checking for published media with csid %s", mediaCsid));

		if (this.es == null) {
			String esBaseUrl = environment.getProperty("zuul.routes.es.url");
			String esIndex = environment.getProperty("es.index");
			String esUrl = esBaseUrl + "/" + esIndex;

			log.info(String.format("Connecting to ES at %s", esUrl));

			this.es = Elasticsearch.connect(esUrl);
		}

		Map<String, Object> query= new HashMap<String, Object>();

		String mediaPublishedQuery = environment.getProperty("es.mediaPublishedQuery");

		if (mediaPublishedQuery != null && mediaPublishedQuery.length() > 0) {
			mediaPublishedQuery = "AND (" + mediaPublishedQuery + ")";
		} 

		String q = String.format("((ecm\\:name:%s) AND (ecm\\:primaryType:Media) AND (NOT(ecm\\:currentLifeCycleState:deleted)) %s)", mediaCsid.replace("-", "\\-"), mediaPublishedQuery);

		log.info(String.format("Query: %s", q));

		query.put("q", q);
		query.put("_source", "false");
		query.put("size", "0");

		Elasticsearch.Result result = this.es.search(query);
		int count = result.getHits().getTotal();

		if (count < 1) {
			log.warn(String.format("No published media found for csid %s", mediaCsid));

			return false;
		}

		return true;
	}
}
