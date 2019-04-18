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
        "es.allowedPublishToValues",
        "es.allowedRecordTypes",
        "es.index",
        "es.recordTypes.CollectionObject.publishToField",
        "zuul.routes.cspace-services.url",
        "zuul.routes.cspace-services.username",
        "zuul.routes.es.url",
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
