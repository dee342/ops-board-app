package gov.nyc.dsny.smart.opsboard.configs;

import gov.nyc.dsny.smart.opsboard.utils.LogContext;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.support.SimpleThreadScope;

@Configuration
public class LogConfiguration {

    @Bean
    public static CustomScopeConfigurer scopeConfigurer() {
        CustomScopeConfigurer scopeConfigurer = new CustomScopeConfigurer();
        Map<String, Object> scopes = new HashMap<>();
        scopes.put("thread", new SimpleThreadScope());
        scopeConfigurer.setScopes(scopes);
        return scopeConfigurer;
    }

    @Bean
    @Scope(value = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public LogContext logContext() {
        return new LogContext();
    }

}
