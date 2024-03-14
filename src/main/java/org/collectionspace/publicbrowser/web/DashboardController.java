package org.collectionspace.publicbrowser.web;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class DashboardController {
    @Autowired
    private Environment environment;
    private List<String> configurationPropertyPrefixes = Arrays.asList("es", "zuul");

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String dashboard(Model model) {
        model.addAttribute("config", getConfigurationProperties());

        return "dashboard";
    }

    private Map<String, String> getConfigurationProperties() {
        if (!(environment instanceof ConfigurableEnvironment)) {
            return new HashMap<>();
        }

        ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;

        return StreamSupport.stream(configurableEnvironment.getPropertySources().spliterator(), false)
            .flatMap(propertySource -> getConfigurationPropertyNames(propertySource))
            .distinct()
            .sorted()
            .collect(Collectors.toMap(
                Function.identity(),
                propertyName -> environment.getProperty(propertyName),
                (existing, replacement) -> existing,
                LinkedHashMap::new)
            );
    }

    private Stream<String> getConfigurationPropertyNames(PropertySource<?> propertySource) {
        if (!(propertySource instanceof EnumerablePropertySource)) {
            return Stream.of();
        }

        return Stream.of(((EnumerablePropertySource<?>) propertySource).getPropertyNames())
            .filter(propertyName -> isConfigurationProperty(propertyName))
            .filter(propertyName -> !propertyName.contains("password"));
    }

    private boolean isConfigurationProperty(String propertyName) {
        return configurationPropertyPrefixes.stream()
            .anyMatch(prefix -> propertyName.startsWith(prefix));
    }
}
