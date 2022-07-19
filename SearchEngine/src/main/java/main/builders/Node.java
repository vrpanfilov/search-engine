package main.builders;

import main.application_properties.Props;
import main.model.Page;
import main.model.Site;

import lombok.Data;
import main.repository.Repos;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

@Data
public class Node {
    public static final int OK = 200;
    public static final int JS_UNKNOWN = -600;
    public static final int JS_HTTP_ERROR_FETCHING_URL_500 = 600;
    public static final int JS_UNAUTHORIZED = 601;
    public static final int JS_HTTP_ERROR_FETCHING_URL_502 = 602;
    public static final int JS_FORBIDDEN = 603;
    public static final int JS_NOT_FOUND = 604;
    public static final int JS_UNSUPPORTED_MEDIA_TYPE = 605;
    public static final int JS_UNEXPECTED_END_OF_ZLIB = 606;
    public static final int JS_CONNECTION_REFUSED = 607;
    public static final int JS_CONNECTION_TIMED_OUT = 608;
    public static final int JS_READ_TIMED_OUT = 609;
    private Site site;
    private String pagePath;
    private Set<String> viewedNodes;
    private ConcurrentLinkedQueue<String> lastNodes;
    private CopyOnWriteArraySet<String> forbiddenNodes;
    private int addedPageId;


    public Node(Site site, String pagePath) {
        this.site = site;
        this.pagePath = pagePath;
        if (site.getSiteBuilder() != null) {
            viewedNodes = site.getSiteBuilder().getViewedPages();
            lastNodes = site.getSiteBuilder().getLastNodes();
            forbiddenNodes = site.getSiteBuilder().getForbiddenNodes();
        } else {
            viewedNodes = new HashSet<>();
            lastNodes = new ConcurrentLinkedQueue<>();
            forbiddenNodes = new CopyOnWriteArraySet<>();
        }
    }

    private String getUrl() {
        return site.getUrl() + pagePath;
    }

    public Document processAndRetunPageDoc() {
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            e = e;
        }

        Page page = new Page();

        page.setSite(site);
        page.setPath(pagePath);
        page.setCode(OK);
        page.setContent("");

        Document doc = null;
        Connection connection = null;
        try {
            Connection.Response response = Jsoup.connect(getUrl())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.114 Safari/537.36")
//                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; " +
//                            "rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
//                    .referrer("http://www.google.com")
                    .execute();
            int statusCode = response.statusCode();
            if (statusCode != OK) {
                page.setCode(statusCode);
                return null;
            }
            doc = Jsoup.parse(response.body());
            page.setContent(doc.outerHtml());
        } catch (Exception e) {
            String message = e.toString();

            int errorCode;
            if (message.contains("UnsupportedMimeTypeException")) {
                errorCode = 404;    // Ссылка на pdf, jpg, png документы
            } else if (message.contains("Status=401")) {
                errorCode = 404;    // На несуществующий домен
            } else if (message.contains("Status=403")) {
                errorCode = 404;    // Нет доступа, 403 Forbidden
            } else if (message.contains("Status=404")) {
                errorCode = 404;    // // Ссылка на pdf-документ, несущ. страница, проигрыватель
            } else if (message.contains("Status=500")) {
                errorCode = 404;    // Страница авторизации
            } else if (message.contains("ConnectException: Connection refused")) {
                errorCode = 500;    // ERR_CONNECTION_REFUSED, не удаётся открыть страницу
            } else {
                errorCode = -1;
            }
            page.setCode(errorCode);
        }


        synchronized (Page.class) {
            Repos.pageRepo.saveAndFlush(page);
        }
        addedPageId = page.getId();

        synchronized (Site.class) {
            site.getPages().add(page);
        }

        page.setContent(null);
        page.setPath(null);

        System.out.println(pagePath);

        return doc;
    }

    public List<Node> getChildren(Document doc) {
        List<Node> children = new ArrayList<>();

        if (doc == null) {
            return null;
        }
        Element body = doc.body();
        if (body == null) {
            return null;
        }

        viewTagsA(body, children);
        viewAttributesOnclick(body, children);
        while (lastNodes.size() > Props.getInst().getRepeatedPageCount()) {
            lastNodes.poll();
        }

        return children;
    }

    private void viewTagsA(Element body, List<Node> children) {
        Elements links = body.getElementsByTag("a");
        for (Element link : links) {
            String href = link.attr("href");
            if (href.startsWith("http")) {
                if (!href.startsWith(site.getUrl())) {
                    continue;
                }
                href = href.substring(site.getUrl().length(), href.length());
            }
            if (!href.startsWith("/") || href.startsWith("#")) {
                continue;
            }
            if (href.endsWith("/")) {
                href = href.substring(0, href.length() - 1);
            }
            if (href.equals("")) {
                continue;
            }
            if (href.contains("/?")) {
                continue;
            }
            if (href.startsWith("//")) {
                continue;
            }
            permissabilitySupport(href, children);
        }
    }

    private void viewAttributesOnclick(Element body, List<Node> children) {
        Elements elements = body.getElementsByAttribute("onclick");
        for (Element elem : elements) {
            String attrContent = elem.attr("onclick").intern();
            if (attrContent.startsWith("window.location") ||
                    attrContent.startsWith(" location.href")) {
                int begin = attrContent.indexOf("'") + 1;
                int end = attrContent.indexOf("'", begin) - 1;
                attrContent = attrContent.substring(begin, end);

                permissabilitySupport(attrContent, children);
            }
        }
    }

    private static Integer viewedPagesMonitor = 0;

    private void permissabilitySupport(String content, List<Node> children) {
        boolean added;
        synchronized (viewedPagesMonitor) {
            added = viewedNodes.add(content);
        }
        if (isForbidden(content)) {
            return;
        }
        if (added) {
            Node node = new Node(site, content);
            children.add(node);
            lastNodes.add(content);
        }
    }

    private boolean isForbidden(String node) {
        int pos = node.indexOf("/", 1);
        if (pos > 0) {
            pos = node.indexOf("/", pos + 1);
            if (pos > 0) {
                node = node.substring(0, pos);
            }
        }
        if (lastNodes.size() < Props.getInst().getRepeatedPageCount())
            return false;
        for (String s : lastNodes) {
            if (!s.startsWith(node)) {
                return false;
            }
        }
        forbiddenNodes.add(node);
        return true;
    }
}
