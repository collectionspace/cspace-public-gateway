package org.collectionspace.publicbrowser;

import org.collectionspace.publicbrowser.filter.CollectionSpaceQueryFilter;
import org.collectionspace.publicbrowser.filter.ElasticsearchQueryFilter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@EnableZuulProxy
@SpringBootApplication
public class Gateway {
	public static void main(String[] args) {
		SpringApplication.run(Gateway.class, args);
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
	public FilterRegistrationBean corsFilter() {
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
