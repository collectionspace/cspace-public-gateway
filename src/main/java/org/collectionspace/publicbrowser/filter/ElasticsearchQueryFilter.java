package org.collectionspace.publicbrowser.filter;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.ZuulFilter;

import javax.servlet.http.HttpServletRequest;

import org.collectionspace.publicbrowser.elasticsearch.QueryModifier;
import org.collectionspace.publicbrowser.elasticsearch.QueryModifierFactory;
import org.collectionspace.publicbrowser.request.ElasticsearchRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

public class ElasticsearchQueryFilter extends ZuulFilter {
	private static Logger log = LoggerFactory.getLogger(ElasticsearchQueryFilter.class);

	@Autowired
	private QueryModifierFactory queryModifierFactory;

	@Override
	public String filterType() {
		return FilterConstants.PRE_TYPE;
	}

	@Override
	public int filterOrder() {
		return FilterConstants.PRE_DECORATION_FILTER_ORDER + 1;
	}

	@Override
	public boolean shouldFilter() {
		HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
		String servletPath = request.getServletPath();
		String[] servletPathParts = servletPath.split("/", 4);
		String api = servletPathParts[2];

		return api.equals("es");
	}

	@Override
	public Object run() {
		RequestContext context = RequestContext.getCurrentContext();
		String proxyId = (String) context.get(FilterConstants.PROXY_KEY);
		HttpServletRequest request = context.getRequest();

		String servletPath = request.getServletPath();
		String[] servletPathParts = servletPath.split("/");
		String apiName = servletPathParts[servletPathParts.length - 1];

		log.info(String.format("%s to %s", request.getMethod(), request.getRequestURL().toString()));

		boolean isBlocked = true;

		// Allow access to search, multi search, and count APIs.
		// Only allow POST requests for now. To support GET, the query string query would need to be
		// modified. Currently, only JSON request bodies are modified.

		if (
			request.getMethod().equalsIgnoreCase("POST") &&
			(
				apiName.equals("_search") ||
				apiName.equals("_msearch") ||
				apiName.equals("_count")
			)
		) {
			isBlocked = false;

			QueryModifier queryModifier = queryModifierFactory.createQueryModifier(proxyId);

			try {
				context.setRequest(new ElasticsearchRequestWrapper(proxyId, request, queryModifier));
			} catch (Exception e) {
				context.setThrowable((e));
			}
		}

		if (
			request.getMethod().equalsIgnoreCase("GET") && apiName.equals("_count")
		) {
			isBlocked = false;
			try {
				context.setRequest(request);
			} catch (Exception e) {
				context.setThrowable((e));
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
}
