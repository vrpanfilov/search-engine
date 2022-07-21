package main.apiResponses;

import lombok.Data;
import main.application_properties.Props;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResponse extends Response {
    private int count;
    private List<PageData> pageDataList = new ArrayList<>();

    public void createSample() {
        count = 2;
        PageData data = new PageData();
        data.setSite("https://et-cetera.ru");
        data.setSiteName(Props.SiteUrlName.getNameByUrl(data.getSite()));
        data.setUri("/performance");
        data.setTitle("Афиша");
        data.setSnippet("Это <b>афиша</b> на сентябрь");
        data.setRelevance(0.93326f);
        pageDataList.add(data);

        data = new PageData();
        data.setSite("https://nikoartgallery.com");
        data.setSiteName(Props.SiteUrlName.getNameByUrl(data.getSite()));
        data.setUri("/contacts");
        data.setTitle("Контактная информация");
        data.setSnippet("<b>Посещение</b> по предварительной договоренности");
        data.setRelevance(0.912345f);
        pageDataList.add(data);
    }

    @Data
    public class PageData {
        private String site;
        private String siteName;
        private String uri;
        private String title;
        private String snippet;
        private float relevance;
    }
}
