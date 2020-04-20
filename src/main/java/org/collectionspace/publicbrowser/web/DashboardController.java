package org.collectionspace.publicbrowser.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class DashboardController {
    @Autowired
    private Environment environment;
    private String properties[] = new String[] {
        "zuul.routes.core-cspace-services.url",
        "zuul.routes.core-cspace-services.username",
        "zuul.routes.core-es.url",
        "zuul.routes.core-es.allowedPublishToValues",
        "zuul.routes.core-es.allowedRecordTypes",
        "zuul.routes.core-es.index",
        "zuul.routes.core-es.recordTypes.CollectionObject.publishToField",
        "zuul.routes.core-es.recordTypes.Media.publishToField",
    };

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String dashboard(Model model) {
        Map<String, String> config = new LinkedHashMap<>();
        for(String property : properties) {
            config.put(property, environment.getProperty(property));
        }

        model.addAttribute("config", config);
        return "dashboard";
    }
}
