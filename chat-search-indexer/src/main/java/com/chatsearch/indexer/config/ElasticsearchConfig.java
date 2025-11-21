package com.chatsearch.indexer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * ElasticSearch 클라이언트 설정
 *
 * <p>대용량 검색을 위한 최적화:
 * <ul>
 *   <li>커넥션 풀 설정</li>
 *   <li>타임아웃 설정</li>
 *   <li>재시도 정책</li>
 * </ul>
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.chatsearch.domain.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris:localhost:9200}")
    private String elasticsearchUri;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Override
    public ClientConfiguration clientConfiguration() {
        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder =
            ClientConfiguration.builder()
                .connectedTo(elasticsearchUri)
                .withConnectTimeout(5000)  // 연결 타임아웃: 5초
                .withSocketTimeout(60000); // 소켓 타임아웃: 60초

        // 인증 정보가 있을 경우 설정
        if (!username.isEmpty() && !password.isEmpty()) {
            builder.withBasicAuth(username, password);
        }

        return builder.build();
    }
}
