package main.application_properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "")
@Data
public class Props {
    private Integer forSitesThreadNumber;
    private Integer forPagesThreadNumber;
    private Integer forIndexesThreadNumber;
    private Integer repeatedPageCount;
    private Integer maxPagesInSite;
    private Boolean isFirstStagePresent;
    private List<SiteUrlName> sites;

    private static Boolean inited = false;

    private static Props inst;

    public Props() {
        inst = this;
    }

    public static Props getInst() {
        synchronized (inited) {
            if (!inst.inited) {
                init();
                inited = true;
            }
        }
        return inst;
    }

    public static void init() {
        for (SiteUrlName sun : inst.sites) {
            URL url = null;
            try {
                url = new URL(sun.getUrl());
            } catch (MalformedURLException e) {
                continue;
            }
            sun.setUrl(url.getProtocol() + "://" + url.getHost());
        }
    }

    @Data
    public static class SiteUrlName {
        private String url;
        private String name;

        public static String getNameByUrl(String url) {
            SiteUrlName sun = Props.inst.sites.stream()
                    .filter(siteUrlName -> siteUrlName.getUrl().equals(url))
                    .findFirst().orElse(null);
            return sun != null ? sun.getName() : "";
        }

        public static String getUrlByName(String name) {
            SiteUrlName sun = Props.inst.sites.stream()
                    .filter(siteUrlName -> siteUrlName.getName().equals(name))
                    .findFirst().orElse(null);
            return sun != null ? sun.getUrl() : "";
        }
    }

}
