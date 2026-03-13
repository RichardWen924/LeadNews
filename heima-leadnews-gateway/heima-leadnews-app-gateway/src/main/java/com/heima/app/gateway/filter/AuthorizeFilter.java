package com.heima.app.gateway.filter;

import com.heima.app.gateway.util.AppJwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizeFilter implements Ordered, GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.获取request和response对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 放行OPTIONS请求，解决跨域预检问题
        if (request.getMethod().name().equalsIgnoreCase("OPTIONS")) {
            return chain.filter(exchange);
        }

        // 2.判断是否是白名单接口（放行登录和某些公开查询接口）
        String path = request.getURI().getPath();
        if (path.contains("/login") || path.contains("/load") || path.contains("/loadmore") || path.contains("/loadnew")) {
            // 如果带了token，还是尝试解析一下，把用户ID带过去
            String token = request.getHeaders().getFirst("token");
            if (StringUtils.isNotBlank(token)) {
                try {
                    Claims claimsBody = AppJwtUtil.getClaimsBody(token);
                    int result = AppJwtUtil.verifyToken(claimsBody);
                    if (result <= 0) {
                        Object userId = claimsBody.get("id");
                        ServerHttpRequest serverHttpRequest = request.mutate().headers(httpHeaders -> {
                            httpHeaders.add("userId", userId + "");
                        }).build();
                        return chain.filter(exchange.mutate().request(serverHttpRequest).build());
                    }
                } catch (Exception e) {
                    log.error("Token analysis failed in whitelist", e);
                }
            }
            return chain.filter(exchange);
        }

        // 3.获取token
        String token = request.getHeaders().getFirst("token");

        // 4.判断token是否存在
        if (StringUtils.isBlank(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 5.判断token是否有效
        try {
            Claims claimsBody = AppJwtUtil.getClaimsBody(token);
            // 是否是过期
            int result = AppJwtUtil.verifyToken(claimsBody);
            if (result == 1 || result == 2) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }

            Object userId = claimsBody.get("id");

            ServerHttpRequest serverHttpRequest = request.mutate().headers(httpHeaders -> {
                httpHeaders.add("userId", userId + "");
            }).build();
            // 重置header并传递给后续过滤器
            return chain.filter(exchange.mutate().request(serverHttpRequest).build());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
    }

    /**
     * 优先级设置 值越小 优先级越高
     * 
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
