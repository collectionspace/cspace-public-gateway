package org.collectionspace.publicbrowser.request;

import com.netflix.zuul.http.HttpServletRequestWrapper;
import com.netflix.zuul.http.ServletInputStreamWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.collectionspace.publicbrowser.elasticsearch.QueryModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

public class ElasticsearchRequestWrapper extends HttpServletRequestWrapper {
	private static Logger log = LoggerFactory.getLogger(ElasticsearchRequestWrapper.class);

	private byte[] content;

	public ElasticsearchRequestWrapper(String proxyId, HttpServletRequest request, QueryModifier queryModifier) throws IOException {
		super(request);

		String contentType = request.getContentType();
		String content = StreamUtils.copyToString(request.getInputStream(), Charset.forName(("utf-8")));

		this.content = (content == null)
			? new byte[0]
			: queryModifier.modifyRequestContent(content, contentType).getBytes();
	}

	@Override
	public byte[] getContentData() {
		return content;
	}

	@Override
	public int getContentLength() {
		return content.length;
	}

	@Override
	public long getContentLengthLong() {
		return content.length;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return new ServletInputStreamWrapper(content);
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content)));
	}
}
