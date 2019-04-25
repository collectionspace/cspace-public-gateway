package org.collectionspace.publicbrowser;

import org.apache.catalina.connector.Connector;
import org.collectionspace.publicbrowser.filter.CollectionSpaceQueryFilter;
import org.collectionspace.publicbrowser.filter.ElasticsearchQueryFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@EnableZuulProxy
@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class Gateway {
	private static Logger log = LoggerFactory.getLogger(Gateway.class);

	@Value("${server.ajp.port:}")
	Integer ajpPort;

	public static void main(String[] args) {
		SpringApplication.run(Gateway.class, args);
	}

	@Bean
	public EmbeddedServletContainerFactory servletContainer() {
			TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();

			if (ajpPort != null) {
				log.info(String.format("Adding Tomcat AJP connector on port %d", ajpPort));

				Connector ajpConnector = new Connector("AJP/1.3");

				ajpConnector.setPort(ajpPort);
				ajpConnector.setSecure(false);
				ajpConnector.setAllowTrace(false);
				ajpConnector.setScheme("http");

				tomcat.addAdditionalTomcatConnectors(ajpConnector);
			}

			return tomcat;
	}

	@Bean
	public ElasticsearchQueryFilter elasticsearchQueryFilter() {
		return new ElasticsearchQueryFilter();
	}

	@Bean
	public CollectionSpaceQueryFilter collectionSpaceQueryFilter() {
		return new CollectionSpaceQueryFilter();
	}

	@Bean
	public FilterRegistrationBean corsFilterRegistrationBean() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();

		config.setAllowCredentials(true);
		config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");

		source.registerCorsConfiguration("/**", config);

		FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));

		bean.setOrder(0);

		return bean;
	}
}
