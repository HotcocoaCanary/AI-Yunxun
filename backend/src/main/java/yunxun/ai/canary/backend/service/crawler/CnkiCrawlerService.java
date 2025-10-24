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
import java.util.ArrayList;
import java.util.List;

/**
 * 知网爬虫服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CnkiCrawlerService {
    
    private static final String CNKI_BASE_URL = "https://kns.cnki.net";
    private static final String CNKI_SEARCH_URL = "https://kns.cnki.net/kns8/defaultresult/index?dbprefix=SCDB&kw=%s";
    
    public List<Paper> crawlPapers(String query, int maxPapers) {
        List<Paper> papers = new ArrayList<>();
        
        try {
            String searchUrl = String.format(CNKI_SEARCH_URL, query.replace(" ", "%20"));
            log.info("开始爬取知网: {}", searchUrl);
            
            Document doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
            
            Elements paperElements = doc.select("tr.result-table-item");
            int count = 0;
            
            for (Element element : paperElements) {
                if (count >= maxPapers) break;
                
                try {
                    Paper paper = parseCnkiPaper(element);
                    if (paper != null) {
                        papers.add(paper);
                        count++;
                    }
                } catch (Exception e) {
                    log.warn("解析知网论文失败: {}", e.getMessage());
                }
            }
            
            log.info("成功爬取 {} 篇知网论文", papers.size());
            
        } catch (IOException e) {
            log.error("爬取知网失败", e);
        }
        
        return papers;
    }
    
    private Paper parseCnkiPaper(Element element) {
        try {
            Paper paper = new Paper();
            
            // 标题
            Element titleElement = element.selectFirst("td.name a");
            if (titleElement == null) return null;
            
            paper.setTitle(titleElement.text());
            String paperUrl = titleElement.attr("href");
            
            // 作者
            Element authorElement = element.selectFirst("td.author a");
            if (authorElement != null) {
                String authorText = authorElement.text();
                List<String> authors = new ArrayList<>();
                String[] authorArray = authorText.split(";");
                for (String author : authorArray) {
                    authors.add(author.trim());
                }
                paper.setAuthors(authors);
            }
            
            // 来源（期刊/会议）
            Element sourceElement = element.selectFirst("td.source a");
            if (sourceElement != null) {
                paper.setVenue(sourceElement.text());
            }
            
            // 关键词
            Element keywordElement = element.selectFirst("td.keyword a");
            if (keywordElement != null) {
                String keywordText = keywordElement.text();
                List<String> keywords = new ArrayList<>();
                String[] keywordArray = keywordText.split(";");
                for (String keyword : keywordArray) {
                    keywords.add(keyword.trim());
                }
                paper.setKeywords(keywords);
            }
            
            // 摘要（需要进入详情页获取）
            try {
                String detailUrl = CNKI_BASE_URL + paperUrl;
                Document detailDoc = Jsoup.connect(detailUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .timeout(5000)
                        .get();
                
                Element abstractElement = detailDoc.selectFirst("div.abstract");
                if (abstractElement != null) {
                    paper.setAbstractText(abstractElement.text());
                }
                
                // 获取DOI
                Element doiElement = detailDoc.selectFirst("div.doi");
                if (doiElement != null) {
                    String doiText = doiElement.text();
                    if (doiText.contains("DOI:")) {
                        paper.setDoi(doiText.replace("DOI:", "").trim());
                    }
                }
                
            } catch (Exception e) {
                log.warn("获取论文详情失败: {}", e.getMessage());
            }
            
            // 基本信息
            paper.setSource("cnki");
            paper.setCrawledAt(LocalDateTime.now());
            paper.setLanguage("zh");
            
            return paper;
            
        } catch (Exception e) {
            log.error("解析知网论文失败", e);
            return null;
        }
    }
}
