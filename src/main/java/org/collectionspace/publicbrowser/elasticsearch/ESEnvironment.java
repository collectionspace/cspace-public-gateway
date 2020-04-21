package org.collectionspace.publicbrowser.elasticsearch;

import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ESEnvironment {
	private static Logger log = LoggerFactory.getLogger(QueryModifier.class);

	@Autowired
	private Environment environment;

  public String getProperty(String proxyId, String key) {
    String proxyKey = getProxyKey(proxyId, key);

    if (environment.containsProperty(proxyKey)) {
      return environment.getProperty(proxyKey);
    }
    return environment.getProperty(getDefaultKey(key));
  }

  public <T> T getProperty(String proxyId, String key, Class<T> targetType) {
    String proxyKey = getProxyKey(proxyId, key);

    if (environment.containsProperty(proxyKey)) {
      return environment.getProperty(proxyKey, targetType);
    }

    return environment.getProperty(getDefaultKey(key), targetType);
  }

  private String getProxyKey(String proxyId, String key) {
    return "zuul.routes." + proxyId + "." + key;
  }

  private String getDefaultKey(String key) {
    return "es." + key;
  }

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
}
