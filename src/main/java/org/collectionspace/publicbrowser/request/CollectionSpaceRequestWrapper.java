package org.collectionspace.publicbrowser.request;

import com.netflix.zuul.http.HttpServletRequestWrapper;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

public class CollectionSpaceRequestWrapper extends HttpServletRequestWrapper {
	private static Logger log = LoggerFactory.getLogger(CollectionSpaceRequestWrapper.class);

	private String username;
	private String password;

	public CollectionSpaceRequestWrapper(HttpServletRequest request, String username, String password) {
		super(request);

		this.username = username;
		this.password = password;
	}

  @Override
  public String getHeader(String name) {
		if (name.equals(HttpHeaders.AUTHORIZATION)){
			return getAuthHeader();
		}

		return super.getHeader(name);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		if (name.equals(HttpHeaders.AUTHORIZATION)){
			List<String> values = new ArrayList<String>();

			values.add(getAuthHeader());

			return Collections.enumeration(values);
		}

		return super.getHeaders(name);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		List<String> names = Collections.list(super.getHeaderNames());

		names.add(HttpHeaders.AUTHORIZATION);

		return Collections.enumeration(names);
	}

	private String getAuthHeader() {
		String encoding = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

		return "Basic " + encoding;
	}
}
