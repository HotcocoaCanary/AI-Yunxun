package yunxun.ai.canary.backend.setting.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import yunxun.ai.canary.backend.setting.model.dto.ConfigEntryDto;
import yunxun.ai.canary.backend.setting.model.dto.ConfigUpsertRequest;
import yunxun.ai.canary.backend.setting.model.entity.ConfigEntryEntity;
import yunxun.ai.canary.backend.setting.repository.mysql.ConfigEntryMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final ConfigEntryMapper configEntryMapper;

    public Map<String, ConfigEntryDto> getByScope(String scope) {
        List<ConfigEntryEntity> entries = configEntryMapper.selectList(
                new LambdaQueryWrapper<ConfigEntryEntity>().eq(ConfigEntryEntity::getScope, scope));
        return entries.stream().collect(Collectors.toMap(
                ConfigEntryEntity::getConfigKey,
                e -> ConfigEntryDto.builder()
                        .key(e.getConfigKey())
                        .value(e.getConfigValue())
                        .encrypted(e.getEncrypted())
                        .build()));
    }

    public void upsert(String scope, ConfigUpsertRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.getValues())) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<String, String> entry : request.getValues().entrySet()) {
            ConfigEntryEntity existing = configEntryMapper.selectOne(new LambdaQueryWrapper<ConfigEntryEntity>()
                    .eq(ConfigEntryEntity::getScope, scope)
                    .eq(ConfigEntryEntity::getConfigKey, entry.getKey()));
            if (existing == null) {
                ConfigEntryEntity entity = ConfigEntryEntity.builder()
                        .scope(scope)
                        .configKey(entry.getKey())
                        .configValue(entry.getValue())
                        .encrypted(Boolean.TRUE.equals(request.getEncrypted()))
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                configEntryMapper.insert(entity);
            } else {
                existing.setConfigValue(entry.getValue());
                existing.setEncrypted(Boolean.TRUE.equals(request.getEncrypted()));
                existing.setUpdatedAt(now);
                configEntryMapper.updateById(existing);
            }
        }
    }
}
