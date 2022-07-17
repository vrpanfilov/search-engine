package main.controller;

import main.application_properties.Props;
import main.builders.SiteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class IndexingController {
    @Autowired
    public Props props;

    @GetMapping("/api/startIndexing")
    public ResponseEntity startEntity() {
        List<Props.SiteUrlName> siteUrlNames = props.getSites();
        for (var siteUrlName : siteUrlNames) {
            boolean isIndexing = SiteBuilder.buildSite(siteUrlName.getUrl());
        }

        return null;
    }
}
