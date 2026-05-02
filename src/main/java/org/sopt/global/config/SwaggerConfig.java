package org.sopt.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SwaggerConfig implements WebMvcConfigurer {

  // 1. API 문서 기본 정보 설정 (제목, 버전 등)
  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("SOPT ASSIGNMENT API")
            .description("SOPT assignment API 명세서입니다.")
            .version("v1.0.0"));
  }

  // 2. 루트 경로(/) 접속 시 Swagger UI로 자동 리다이렉트
  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addRedirectViewController("/", "/swagger-ui/index.html");
  }
}
