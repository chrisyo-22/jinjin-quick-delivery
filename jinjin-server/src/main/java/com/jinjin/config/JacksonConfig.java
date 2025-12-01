package com.jinjin.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson配置类 - 自定义JSON序列化和反序列化行为
 * 
 * 作用：
 * 1. 统一配置Java 8+时间类型(LocalDateTime, LocalDate, LocalTime)的JSON格式
 * 2. 避免日期被序列化为数组格式 [2025, 11, 6, 18, 51, 35]
 * 3. 确保日期按照指定格式(如 "2025-11-06 18:51")进行序列化和反序列化
 * 
 * Spring Boot 3.x最佳实践：
 * - 使用 Jackson2ObjectMapperBuilderCustomizer 而不是直接创建 ObjectMapper bean
 * - 这样可以保持Spring Boot的自动配置，避免与Swagger/Knife4j等框架冲突
 * - 配置会自动应用到所有的ObjectMapper实例（包括HTTP消息转换器）
 */
@Configuration
@Slf4j
public class JacksonConfig {

    /**
     * 日期格式：yyyy-MM-dd
     * 用于 LocalDate 类型的序列化和反序列化
     * 示例：2025-11-06
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    
    /**
     * 日期时间格式：yyyy-MM-dd HH:mm (不包含秒)
     * 用于 LocalDateTime 类型的序列化和反序列化
     * 示例：2025-11-06 18:51
     * 
     * 注意：这里不包含秒数，是为了保持与旧系统的兼容性
     */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    
    /**
     * 时间格式：HH:mm:ss
     * 用于 LocalTime 类型的序列化和反序列化
     * 示例：18:51:35
     */
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    /**
     * 配置Jackson的ObjectMapper构建器
     * 
     * 此方法返回一个定制器(Customizer)，Spring Boot会在创建ObjectMapper时自动应用
     * 
     * @return Jackson2ObjectMapperBuilderCustomizer 定制器实例
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        log.info("Configuring Jackson for LocalDateTime serialization with format: {}", DEFAULT_DATE_TIME_FORMAT);

        return builder -> {
            // 步骤1: 创建日期时间格式化器
            // 这些格式化器将用于将字符串转换为日期对象，以及将日期对象转换为字符串
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT);

            // 步骤2: 创建自定义模块，注册序列化器和反序列化器
            // SimpleModule 允许我们为特定的Java类型注册自定义的处理逻辑
            SimpleModule simpleModule = new SimpleModule()
                    // 反序列化器：将JSON字符串转换为Java对象
                    // 例如："2025-11-06 18:51" -> LocalDateTime对象
                    .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter))
                    .addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter))
                    .addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter))
                    
                    // 序列化器：将Java对象转换为JSON字符串
                    // 例如：LocalDateTime对象 -> "2025-11-06 18:51"
                    .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter))
                    .addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter))
                    .addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter));

            // 步骤3: 应用配置到builder
            builder
                    // 注册我们的自定义模块
                    .modules(simpleModule)
                    
                    // 禁用以下功能：
                    .featuresToDisable(
                            // FAIL_ON_UNKNOWN_PROPERTIES: 当JSON中包含Java对象没有的属性时，不抛出异常
                            // 这样可以提高系统的兼容性和健壮性
                            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                            
                            // WRITE_DATES_AS_TIMESTAMPS: 不将日期写为时间戳(数字)或数组格式
                            // 禁用后，日期会按照我们指定的格式序列化为字符串
                            // 如果不禁用，LocalDateTime会被序列化为 [2025,11,6,18,51,35] 这样的数组
                            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
                    );
        };
    }
}
