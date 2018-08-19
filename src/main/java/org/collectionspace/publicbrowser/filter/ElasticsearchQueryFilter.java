package org.collectionspace.publicbrowser.filter;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.ZuulFilter;

import javax.servlet.http.HttpServletRequest;

import org.collectionspace.publicbrowser.request.ElasticsearchRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

public class ElasticsearchQueryFilter extends ZuulFilter {
	private static Logger log = LoggerFactory.getLogger(ElasticsearchQueryFilter.class);

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
		String[] servletPathParts = servletPath.split("/", 3);
		String root = servletPathParts[1];

		return (root.equals("es") && request.getMethod().equalsIgnoreCase("POST"));
	}

	@Override
	public Object run() {
		RequestContext context = RequestContext.getCurrentContext();
		HttpServletRequest request = context.getRequest();

		log.info(String.format("%s to %s", request.getMethod(), request.getRequestURL().toString()));

		try {
			context.setRequest(new ElasticsearchRequestWrapper(request));
		} catch (Exception e) {
			context.setThrowable((e));
		}

		return null;
	}
}
