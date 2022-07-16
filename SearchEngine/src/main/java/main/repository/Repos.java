package main.repository;

import org.springframework.beans.factory.annotation.Autowired;

public class Repos {
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private FieldRepository fieldRepository;
    @Autowired
    private IndexRepository indexRepository;

    public static SiteRepository siteRepo;
    public static PageRepository pageRepo;
    public static LemmaRepository lemmaRepo;
    public static FieldRepository fieldRepo;
    public static IndexRepository indexRepo;

    public void init() {
        siteRepo = siteRepository;
        pageRepo = pageRepository;
        lemmaRepo = lemmaRepository;
        fieldRepo = fieldRepository;
        indexRepo = indexRepository;
    }
}
