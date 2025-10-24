package yunxun.ai.canary.backend.service.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.model.entity.Paper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * arXiv爬虫服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ArxivCrawlerService {
    
    private static final String ARXIV_BASE_URL = "https://arxiv.org";
    private static final String ARXIV_SEARCH_URL = "https://arxiv.org/search/?query=%s&searchtype=all&source=header";
    private static final Pattern ARXIV_ID_PATTERN = Pattern.compile("arXiv:([0-9.]+)");
    
    public List<Paper> crawlPapers(String query, int maxPapers) {
        List<Paper> papers = new ArrayList<>();
        
        try {
            String searchUrl = String.format(ARXIV_SEARCH_URL, query.replace(" ", "+"));
            log.info("开始爬取arXiv: {}", searchUrl);
            
            Document doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
            
            Elements paperElements = doc.select("li.arxiv-result");
            int count = 0;
            
            for (Element element : paperElements) {
                if (count >= maxPapers) break;
                
                try {
                    Paper paper = parseArxivPaper(element);
                    if (paper != null) {
                        papers.add(paper);
                        count++;
                    }
                } catch (Exception e) {
                    log.warn("解析论文失败: {}", e.getMessage());
                }
            }
            
            log.info("成功爬取 {} 篇arXiv论文", papers.size());
            
        } catch (IOException e) {
            log.error("爬取arXiv失败", e);
        }
        
        return papers;
    }
    
    private Paper parseArxivPaper(Element element) {
        try {
            Paper paper = new Paper();
            
            // 标题
            Element titleElement = element.selectFirst("p.title a");
            if (titleElement == null) return null;
            
            paper.setTitle(titleElement.text());
            String paperUrl = titleElement.attr("href");
            
            // arXiv ID
            Matcher matcher = ARXIV_ID_PATTERN.matcher(paperUrl);
            if (matcher.find()) {
                paper.setArxivId(matcher.group(1));
            }
            
            // 作者
            Elements authorElements = element.select("p.authors a");
            List<String> authors = new ArrayList<>();
            for (Element authorElement : authorElements) {
                authors.add(authorElement.text());
            }
            paper.setAuthors(authors);
            
            // 摘要
            Element abstractElement = element.selectFirst("span.abstract-full");
            if (abstractElement != null) {
                paper.setAbstractText(abstractElement.text());
            }
            
            // 分类
            Elements categoryElements = element.select("span.primary-subject");
            List<String> categories = new ArrayList<>();
            for (Element categoryElement : categoryElements) {
                categories.add(categoryElement.text());
            }
            paper.setCategories(categories);
            
            // 发布日期
            Element dateElement = element.selectFirst("p.is-size-7");
            if (dateElement != null) {
                String dateText = dateElement.text();
                try {
                    // 解析日期格式 "Submitted on 15 Jan 2024"
                    if (dateText.contains("Submitted on")) {
                        String dateStr = dateText.replace("Submitted on ", "").trim();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy");
                        paper.setPublishedDate(LocalDateTime.parse(dateStr, formatter));
                    }
                } catch (Exception e) {
                    log.warn("解析日期失败: {}", dateText);
                }
            }
            
            // 基本信息
            paper.setSource("arxiv");
            paper.setCrawledAt(LocalDateTime.now());
            paper.setLanguage("en");
            paper.setPdfUrl(ARXIV_BASE_URL + paperUrl.replace("/abs/", "/pdf/") + ".pdf");
            
            return paper;
            
        } catch (Exception e) {
            log.error("解析arXiv论文失败", e);
            return null;
        }
    }
}
