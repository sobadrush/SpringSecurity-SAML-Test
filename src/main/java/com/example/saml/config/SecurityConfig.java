package com.example.saml.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Spring Security SAML2 配置
 * 
 * 此配置類別負責設定 SAML2 Service Provider 的安全設定
 * 包含：
 * 1. SAML2 登入配置
 * 2. SAML2 登出配置
 * 3. 請求授權規則
 * 4. Metadata 端點配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置 Security Filter Chain
     * 
     * @param http HttpSecurity 物件
     * @param relyingPartyRegistrationRepository SAML2 Relying Party 註冊倉庫
     * @return SecurityFilterChain
     * @throws Exception 配置異常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RelyingPartyRegistrationRepository relyingPartyRegistrationRepository) throws Exception {
        
        http
            // 配置請求授權規則
            .authorizeHttpRequests(authorize -> authorize
                // 公開端點：首頁、錯誤頁面、靜態資源
                .requestMatchers("/", "/home", "/error", "/css/**", "/js/**", "/images/**").permitAll()
                // SAML2 相關端點需要認證
                .requestMatchers("/saml2/**").permitAll()
                // 其他所有請求都需要認證
                .anyRequest().authenticated()
            )
            // 配置 SAML2 登入
            .saml2Login(saml2 -> saml2
                // 登入成功後重導向到 /user 頁面
                .defaultSuccessUrl("/user", true)
                // 登入失敗處理
                .failureUrl("/login?error=true")
            )
            // 配置 SAML2 登出
            .saml2Logout(withDefaults())
            // 配置一般登出
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            // 配置 SAML2 Metadata
            .saml2Metadata(withDefaults());

        return http.build();
    }
}
