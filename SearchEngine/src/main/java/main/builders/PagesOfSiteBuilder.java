package main.builders;

import main.application_properties.Props;
import main.model.Site;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class PagesOfSiteBuilder extends RecursiveAction {
    private final Node node;
    final static int forPagesThreadNumber = Props.getInst().getForPagesThreadNumber();

    public PagesOfSiteBuilder(Node node) {
        this.node = node;
    }

    @Override
    protected void compute() {
        List<PagesOfSiteBuilder> builders = new ArrayList<>();

        if (node.getSite().getPages().size() >=
                Props.getInst().getMaxPagesInSite()) {
            return;
        }

        Document doc = node.processAndRetunPageDoc();

        List<Node> children = node.getChildren(doc);

        if (children == null || children.size() == 0) {
            return;
        }
        for (Node child : children) {
            PagesOfSiteBuilder builder = new PagesOfSiteBuilder(child);
            builder.fork();
            builders.add(builder);
        }
        builders.forEach(ForkJoinTask::join);
    }

    public static void build(Site site) {
        Boolean isFirstStagePresent = Props.getInst().getIsFirstStagePresent();
        if (isFirstStagePresent == null || isFirstStagePresent) {
            Node node = new Node(site, "/");
            PagesOfSiteBuilder builder = new PagesOfSiteBuilder(node);
            new ForkJoinPool(forPagesThreadNumber).invoke(builder);
        }
    }
}
