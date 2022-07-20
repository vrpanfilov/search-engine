package main.builders;

import lombok.Data;
import main.application_properties.Props;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
//@Component
public class DbBuilder implements CommandLineRunner {
    @Autowired
    public Props props;

    private SiteBuilder sitesBuilder;

    @Override
    public void run(String... args) throws Exception {
        Props.init();

        List<Props.SiteUrlName> siteUrlNames = props.getSites();
        for (var siteUrlName : siteUrlNames) {
            SiteBuilder.buildSite(siteUrlName.getUrl());
        }
    }

}
