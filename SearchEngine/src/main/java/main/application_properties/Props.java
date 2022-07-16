package main.application_properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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

    private static Props inst;

    public static Props getInst() {
        return inst;
    }

    public static void init(Props props) {
        inst = props;
        for (SiteUrlName sun : inst.sites) {
            int fromIndex = 0;
            int pos = sun.url.indexOf("//");
            if (pos > 0) {
                fromIndex = pos + 2;
            }
            pos = sun.url.indexOf("/", fromIndex);
            if (pos > 0) {
                sun.url = sun.url.substring(0, pos);
            }
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
