package com.jinjin.config;

import com.jinjin.interceptor.JwtTokenAdminInterceptor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类 - 注册web层相关组件
 * 
 * 重要说明：
 * 1. 实现 WebMvcConfigurer 接口（不是继承 WebMvcConfigurationSupport）
 * 2. WebMvcConfigurer 是Spring Boot 3.x推荐的方式，保留了Spring Boot的自动配置
 * 3. 如果继承 WebMvcConfigurationSupport，会禁用Spring Boot的自动配置，导致：
 *    - Swagger/Knife4j文档无法访问
 *    - Jackson的自动配置失效
 *    - 静态资源处理需要完全手动配置
 * 
 * 此配置类的职责：
 * - 注册JWT拦截器，保护管理端API
 * - 配置API文档（Swagger/Knife4j）
 * - 配置静态资源访问路径
 */
@Configuration
@Slf4j
public class WebMvcConfiguration implements WebMvcConfigurer {

    /**
     * JWT令牌管理端拦截器
     * 用于验证管理端API请求的JWT令牌
     */
    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    /**
     * 注册自定义拦截器
     * 
     * 功能：配置JWT令牌验证拦截器，保护管理端API的安全性
     * 
     * 拦截规则：
     * - 拦截路径：/admin/** (所有管理端API)
     * - 排除路径：/admin/employee/login (登录接口不需要验证)
     * 
     * 工作流程：
     * 1. 客户端请求管理端API
     * 2. 拦截器检查请求头中的JWT令牌
     * 3. 验证令牌的有效性（签名、过期时间等）
     * 4. 如果验证通过，放行请求；否则返回401未授权
     * 
     * @param registry 拦截器注册器，用于注册和配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")        // 拦截所有管理端请求
                .excludePathPatterns("/admin/employee/login");  // 登录接口例外，无需验证
    }

    /**
     * 配置Knife4j接口文档
     * 
     * Knife4j是增强版的Swagger UI，提供了更友好的API文档界面
     * 
     * 访问地址：http://localhost:8080/doc.html
     * 
     * 功能：
     * - 自动生成API接口文档
     * - 提供在线测试接口的功能
     * - 查看请求参数、响应结果的格式
     * 
     * @return OpenAPI配置对象，定义API文档的基本信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("JinJin外卖项目接口文档")
                        .version("2.0")
                        .description("JinJin外卖项目接口文档"));
    }

    /**
     * 设置静态资源映射
     * 
     * 作用：配置静态资源的访问路径，使得Knife4j的前端资源能够被正确访问
     * 
     * 映射规则：
     * 1. /doc.html -> classpath:/META-INF/resources/
     *    - 访问 http://localhost:8080/doc.html 时，返回Knife4j的主页面
     * 
     * 2. /webjars/** -> classpath:/META-INF/resources/webjars/
     *    - 访问 http://localhost:8080/webjars/xxx.js 时，返回对应的JavaScript库文件
     *    - webjars是将前端库（如jQuery、Bootstrap）打包成JAR的方式
     * 
     * 注意：
     * - 使用 WebMvcConfigurer 接口时，这些配置会叠加到Spring Boot的默认配置上
     * - 如果使用 WebMvcConfigurationSupport，则会完全覆盖默认配置，需要手动配置所有内容
     * 
     * @param registry 资源处理器注册器，用于配置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("设置静态资源映射...");
        // 配置Knife4j文档页面的访问路径
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        // 配置webjars资源（JavaScript、CSS等前端库）的访问路径
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
