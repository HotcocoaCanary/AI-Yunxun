package yunxun.ai.canary.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;
import yunxun.ai.canary.backend.model.dto.setting.DataImportTextRequest;
import yunxun.ai.canary.backend.model.dto.setting.DataImportUrlRequest;
import yunxun.ai.canary.backend.model.dto.setting.DataStatsDto;
import yunxun.ai.canary.backend.model.dto.setting.ImportJobDto;
import yunxun.ai.canary.backend.service.setting.DataStatsService;

@RestController
@RequestMapping("/api/setting")
@RequiredArgsConstructor
public class SettingController {

    private final DataStatsService dataStatsService;

    @GetMapping("/stats")
    public DataStatsDto stats() {
        return dataStatsService.getStats();
    }

    @PostMapping("/import/file")
    public ImportJobDto importFile(@RequestPart("file") MultipartFile file) {
        return dataStatsService.simulateImport("file");
    }

    @PostMapping("/import/text")
    public ImportJobDto importText(@RequestBody DataImportTextRequest request) {
        return dataStatsService.simulateImport("text");
    }

    @PostMapping("/import/url")
    public ImportJobDto importUrl(@RequestBody DataImportUrlRequest request) {
        return dataStatsService.simulateImport("url");
    }
}
