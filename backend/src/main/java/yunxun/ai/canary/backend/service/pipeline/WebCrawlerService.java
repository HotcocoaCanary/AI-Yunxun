package yunxun.ai.canary.backend.service.pipeline.connectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import yunxun.ai.canary.backend.model.dto.crawler.CrawlResult;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebCrawlerService {

    private final RestClient restClient = RestClient.builder().build();
    private static final String ARXIV_ENDPOINT = "https://export.arxiv.org/api/query";

    public List<CrawlResult> crawlArxiv(String query, int maxResults) {
        String url = ARXIV_ENDPOINT +
                "?search_query=all:" + query +
                "&start=0&max_results=" + Math.min(maxResults, 50) +
                "&sortBy=submittedDate&sortOrder=descending";
        String response = restClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_ATOM_XML)
                .retrieve()
                .body(String.class);
        return parseArxivFeed(response);
    }

    public Optional<CrawlResult> crawlGenericUrl(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("AI-Yunxun-Agent/1.0")
                    .timeout(10000)
                    .get();
            String title = doc.title();
            String description = Optional.ofNullable(doc.selectFirst("meta[name=description]"))
                    .map(element -> element.attr("content"))
                    .orElseGet(() -> doc.body().text().substring(0, Math.min(400, doc.body().text().length())));
            Elements authorElements = doc.select("meta[name=author]");
            List<String> authors = new ArrayList<>();
            authorElements.forEach(element -> authors.add(element.attr("content")));
            return Optional.of(CrawlResult.builder()
                    .title(title)
                    .summary(description)
                    .authors(authors)
                    .source("generic")
                    .url(url)
                    .rawContent(doc.body().text())
                    .metadata(Map.of("fetchedAt", Instant.now().toString()))
                    .build());
        } catch (IOException e) {
            log.warn("crawlGenericUrl failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private List<CrawlResult> parseArxivFeed(String xml) {
        List<CrawlResult> results = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setExpandEntityReferences(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(new InputSource(new StringReader(xml)));
            NodeList entries = doc.getElementsByTagName("entry");
            for (int i = 0; i < entries.getLength(); i++) {
                Node entry = entries.item(i);
                NodeList children = entry.getChildNodes();
                String title = null;
                String summary = null;
                List<String> authors = new ArrayList<>();
                String link = null;
                Instant published = null;
                for (int j = 0; j < children.getLength(); j++) {
                    Node child = children.item(j);
                    switch (child.getNodeName()) {
                        case "title" -> title = child.getTextContent().trim();
                        case "summary" -> summary = child.getTextContent().trim();
                        case "author" -> {
                            NodeList authorChildren = child.getChildNodes();
                            for (int k = 0; k < authorChildren.getLength(); k++) {
                                Node aChild = authorChildren.item(k);
                                if ("name".equals(aChild.getNodeName())) {
                                    authors.add(aChild.getTextContent().trim());
                                }
                            }
                        }
                        case "link" -> {
                            Node href = child.getAttributes().getNamedItem("href");
                            if (href != null) {
                                link = href.getNodeValue();
                            }
                        }
                        case "published" -> {
                            String time = child.getTextContent();
                            published = Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(time));
                        }
                        default -> {
                        }
                    }
                }
                if (title != null) {
                    results.add(CrawlResult.builder()
                            .title(title)
                            .summary(summary)
                            .authors(authors)
                            .source("arxiv")
                            .url(link)
                            .publishedAt(published)
                            .rawContent(summary)
                            .metadata(Map.of("crawler", "arxiv"))
                            .build());
                }
            }
        }
        catch (ParserConfigurationException | IOException | SAXException e) {
            log.warn("parseArxivFeed failed: {}", e.getMessage());
        }
        return results;
    }
}
