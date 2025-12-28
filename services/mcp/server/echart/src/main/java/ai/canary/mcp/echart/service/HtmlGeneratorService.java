package ai.canary.mcp.echart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HtmlGeneratorService {

    private final ObjectMapper objectMapper;

    public String generateHtml(String echartConfigJson) throws Exception {
        // Validate JSON format
        try {
            objectMapper.readTree(echartConfigJson);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ECharts configuration JSON: " + e.getMessage());
        }

        // Generate HTML template
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"utf-8\">\n");
        html.append("    <title>ECharts Chart</title>\n");
        html.append("    <script src=\"https://cdn.jsdelivr.net/npm/echarts@5.5.0/dist/echarts.min.js\"></script>\n");
        html.append("    <style>\n");
        html.append("        body {\n");
        html.append("            margin: 0;\n");
        html.append("            padding: 20px;\n");
        html.append("            font-family: Arial, sans-serif;\n");
        html.append("        }\n");
        html.append("        #chart {\n");
        html.append("            width: 100%;\n");
        html.append("            height: 600px;\n");
        html.append("            min-width: 800px;\n");
        html.append("        }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div id=\"chart\"></div>\n");
        html.append("    <script>\n");
        html.append("        var chart = echarts.init(document.getElementById('chart'));\n");
        html.append("        var option = ");
        html.append(echartConfigJson);
        html.append(";\n");
        html.append("        chart.setOption(option);\n");
        html.append("        window.addEventListener('resize', function() {\n");
        html.append("            chart.resize();\n");
        html.append("        });\n");
        html.append("    </script>\n");
        html.append("</body>\n");
        html.append("</html>\n");

        return html.toString();
    }
}

