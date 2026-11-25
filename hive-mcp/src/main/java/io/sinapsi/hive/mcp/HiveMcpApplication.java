package io.sinapsi.hive.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import io.sinapsi.hive.commons.HiveOperations;
import io.sinapsi.hive.mcp.security.OsvScannerProperties;
import io.sinapsi.hive.mcp.dependencies.infrastructure.MavenDependencyGraphProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({OsvScannerProperties.class, MavenDependencyGraphProperties.class})
public class HiveMcpApplication {
    @Bean
    HiveOperations hiveOperations() {
        return new HiveOperations();
    }

    @Bean
    ObjectMapper objectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }

    public static void main(String[] args) {
        SpringApplication.run(HiveMcpApplication.class, args);
    }
}
