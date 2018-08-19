package org.collectionspace.publicbrowser.filter;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.ZuulFilter;

import javax.servlet.http.HttpServletRequest;

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
	public boolean shouldFilter() {
		HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
		String servletPath = request.getServletPath();
		String[] servletPathParts = servletPath.split("/", 3);
		String root = servletPathParts[1];

		return root.startsWith("cspace-services");
	}

	@Override
	public Object run() {
		RequestContext context = RequestContext.getCurrentContext();
		String proxyId = (String) context.get(FilterConstants.PROXY_KEY);
		HttpServletRequest request = context.getRequest();

		log.info(String.format("%s to %s at %s", request.getMethod(), proxyId, request.getRequestURL().toString()));

		String username = environment.getProperty("zuul.routes." + proxyId + ".username");
		String password = environment.getProperty("zuul.routes." + proxyId + ".password");

		try {
			context.setRequest(new CollectionSpaceRequestWrapper(request, username, password));
		} catch (Exception e) {
			context.setThrowable((e));
		}

		return null;
	}
}
