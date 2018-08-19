package org.collectionspace.publicbrowser.filter;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.ZuulFilter;

import javax.servlet.http.HttpServletRequest;

import org.collectionspace.publicbrowser.request.CollectionSpaceRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionSpaceQueryFilter extends ZuulFilter {
	private static Logger log = LoggerFactory.getLogger(CollectionSpaceQueryFilter.class);

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return 1;
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
		HttpServletRequest request = context.getRequest();

		log.info(String.format("%s to %s", request.getMethod(), request.getRequestURL().toString()));

		try {
			context.setRequest(new CollectionSpaceRequestWrapper(request));
		} catch (Exception e) {
			context.setThrowable((e));
		}

		return null;
	}
}
