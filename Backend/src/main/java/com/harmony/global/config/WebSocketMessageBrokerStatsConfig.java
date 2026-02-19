package com.harmony.global.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;

@Configuration
@RequiredArgsConstructor
public class WebSocketMessageBrokerStatsConfig {
  private final WebSocketMessageBrokerStats stats;

  @PostConstruct
  public void init() {
    stats.setLoggingPeriod(30*1000); // 밀리초 단위. 30초
  }
}
