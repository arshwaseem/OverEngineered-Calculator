package com.arshwaseem.oe_calc.configuration;

import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class WebClientConfiguration {

    private final UserServiceProperties userServiceProperties;

    @Bean
    public WebClient userServiceWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(EpollChannelOption.TCP_KEEPIDLE, 300)
                .option(EpollChannelOption.TCP_KEEPINTVL, 60)
                .option(EpollChannelOption.TCP_KEEPCNT, 8)
                .responseTimeout(Duration.ofSeconds(5))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5))
                                .addHandlerLast(new WriteTimeoutHandler(5)));

        return WebClient.builder()
                .baseUrl(userServiceProperties.getUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

}