package com.arshwaseem.oe_calc.configuration;

import com.arshwaseem.oe_calc.filter.JwtAuthenticationFilter;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class GatewayConfig {

    @Bean
    public WebClient authServiceWebclient(
            WebClient.Builder builder,
            AuthServiceProperties authServiceProperties
    ) {
        return  builder
                .baseUrl(authServiceProperties.getUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter((request, next) -> {
                    return next.exchange(request).contextWrite(ctx -> ctx);
                })
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                                .option(ChannelOption.SO_KEEPALIVE, true)
                                .option(EpollChannelOption.TCP_KEEPIDLE, 300)
                                .option(EpollChannelOption.TCP_KEEPINTVL, 60)
                                .option(EpollChannelOption.TCP_KEEPCNT, 8)
                                .responseTimeout(Duration.ofSeconds(10))
                                .doOnConnected(conn ->
                                        conn.addHandlerLast(new ReadTimeoutHandler(10))
                                                .addHandlerLast(new WriteTimeoutHandler(10)))
                )).build();
    }

    @Bean(name = "jwtAuthenticationFilterRegistration")
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilter( JwtAuthenticationFilter jwtAuthenticationFilter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtAuthenticationFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);

        return registrationBean;
    }

}
