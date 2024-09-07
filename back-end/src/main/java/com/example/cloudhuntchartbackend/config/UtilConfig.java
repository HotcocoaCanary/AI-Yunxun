package com.example.cloudhuntchartbackend.config;

import com.example.cloudhuntchartbackend.utils.EntityToCypherQuery;
import com.example.cloudhuntchartbackend.utils.NormalizedData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description:
 * @Author: Canary
 * @Date: 2024/9/7 下午9:43
 */

@Configuration
public class UtilConfig {

    @Bean
    public EntityToCypherQuery myUtils() {
        return new EntityToCypherQuery();
    }

    @Bean
    public NormalizedData normalizedData() {
        return new NormalizedData();
    }
}
