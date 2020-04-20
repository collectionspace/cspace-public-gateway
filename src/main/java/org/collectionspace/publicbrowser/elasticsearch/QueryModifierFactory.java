package org.collectionspace.publicbrowser.elasticsearch;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class QueryModifierFactory {
  private static Logger log = LoggerFactory.getLogger(QueryModifierFactory.class);

  private Map<String, QueryModifier> queryModifiers = new HashMap<>();

  @Autowired
  private ApplicationContext applicationContext;

  public QueryModifier createQueryModifier(String proxyId) {
    QueryModifier queryModifier = queryModifiers.get(proxyId);

    if (queryModifier == null) {
      queryModifier = applicationContext.getBean(QueryModifier.class, proxyId);

      queryModifiers.put(proxyId, queryModifier);
    }

    return queryModifier;
  }
}
