# 🔐 SAML Single Logout (SLO) 完整指南

> **完整的 SAML SLO 配置、測試和排查指南**

## 📋 目錄

- [概述](#概述)
- [SLO 配置說明](#slo-配置說明)
- [測試檢查清單](#測試檢查清單)
- [SLO 流程說明](#slo-流程說明)
- [問題排查](#問題排查)
- [安全性考量](#安全性考量)

---

## 概述

SAML Single Logout (SLO) 允許用戶在登出 Service Provider (SP) 時，同時登出 Identity Provider (IDP)，確保所有相關的 session 都被清除。

### SLO 的優勢

- ✅ **統一登出** - 一次登出，清除所有 session
- ✅ **安全性提升** - 防止 session 劫持
- ✅ **更好的用戶體驗** - 無需多次登出
- ✅ **符合合規要求** - 滿足企業安全政策

---

## SLO 配置說明

### 1. application.yml - SP SLO 端點配置

在 `src/main/resources/application.yml` 中添加 SP 的 SLO 配置：

```yaml
spring:
  security:
    saml2:
      relyingparty:
        registration:
          keycloak:
            # ... 其他配置 ...
            singlelogout:
              # SP 接收 IDP LogoutRequest 的端點
              url: http://localhost:8080/logout/saml2/slo
              # SP 接收 IDP LogoutResponse 的端點
              response-url: http://localhost:8080/logout/saml2/slo
              # 使用 POST binding
              binding: POST
```

**配置說明：**
- `url` - SP 接收 IDP 發送的 LogoutRequest 的端點
- `response-url` - SP 接收 IDP 發送的 LogoutResponse 的端點
- `binding` - 使用 HTTP POST binding（推薦）

### 2. SecurityConfig.java - SAML2 Logout 配置

```java
@Bean
public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        RelyingPartyRegistrationRepository relyingPartyRegistrationRepository) throws Exception {
    
    http
        // 配置請求授權規則
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/", "/home", "/error").permitAll()
            .requestMatchers("/saml2/**", "/logout/saml2/slo").permitAll()
            .anyRequest().authenticated()
        )
        // 配置 SAML2 登入
        .saml2Login(saml2 -> saml2
            .defaultSuccessUrl("/user", true)
            .failureUrl("/login?error=true")
        )
        // 配置 SAML2 登出（Single Logout）
        .saml2Logout(saml2Logout -> saml2Logout
            .logoutRequest(logoutRequest -> logoutRequest
                .logoutRequestResolver(saml2LogoutRequestResolver(relyingPartyRegistrationRepository))
            )
            .logoutResponse(logoutResponse -> logoutResponse
                .logoutUrl("/logout/saml2/slo")
            )
        )
        // 配置一般登出
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/?logout=success")
            .permitAll()
            .deleteCookies("JSESSIONID")
            .invalidateHttpSession(true)
            .clearAuthentication(true)
        )
        // 配置 SAML2 Metadata
        .saml2Metadata(withDefaults());

    return http.build();
}

@Bean
public Saml2LogoutRequestResolver saml2LogoutRequestResolver(
        RelyingPartyRegistrationRepository relyingPartyRegistrationRepository) {
    return new OpenSaml4LogoutRequestResolver(relyingPartyRegistrationRepository);
}
```

**重點功能：**
- ✅ 完整的 SAML SLO 流程支援
- ✅ 自動清除 Session 和 Cookie
- ✅ 錯誤處理和重導向
- ✅ 與 IDP 的 SLO 端點整合

### 3. Keycloak IDP 配置

在 `keycloak/realm-export.json` 中已包含以下配置：

```json
{
  "clients": [
    {
      "clientId": "http://localhost:8080/saml2/service-provider-metadata/keycloak",
      "frontchannelLogout": true,
      "attributes": {
        "saml_single_logout_service_url_post": "http://localhost:8080/logout/saml2/slo",
        ...
      }
    }
  ]
}
```

**配置說明：**
- `frontchannelLogout: true` - 啟用前端通道登出
- `saml_single_logout_service_url_post` - SP 的 SLO 端點 URL

---

## 測試檢查清單

### ✅ 配置檢查

#### 1. application.yml 檢查
- [ ] `spring.security.saml2.relyingparty.registration.keycloak.singlelogout` 已配置
- [ ] `url: http://localhost:8080/logout/saml2/slo` 已設定
- [ ] `response-url: http://localhost:8080/logout/saml2/slo` 已設定
- [ ] `binding: POST` 已設定

#### 2. SecurityConfig.java 檢查
- [ ] 導入了 `OpenSaml4LogoutRequestResolver`
- [ ] `.saml2Logout()` 已正確配置
- [ ] `.logoutRequest()` 和 `.logoutResponse()` 已配置
- [ ] `saml2LogoutRequestResolver` Bean 已定義

#### 3. Keycloak 配置檢查
- [ ] `keycloak/realm-export.json` 中包含 `saml_single_logout_service_url_post`
- [ ] 值為 `http://localhost:8080/logout/saml2/slo`
- [ ] `frontchannelLogout: true` 已設定

### 🧪 功能測試步驟

#### 步驟 1: 啟動服務

**Windows:**
```cmd
# 1. 啟動 Keycloak
cd d:\Program_Projects\Java_workspace\SpringSecurity-SAML-Test
docker-compose up -d

# 2. 等待 Keycloak 啟動 (約 30-60 秒)
timeout /t 45

# 3. 驗證 Keycloak 運行
curl http://localhost:8443/realms/saml-demo

# 4. 啟動 Spring Boot 應用
mvn clean spring-boot:run
```

**Linux/Mac:**
```bash
# 1. 啟動 Keycloak
docker-compose up -d

# 2. 等待 Keycloak 啟動
sleep 45

# 3. 驗證 Keycloak 運行
curl http://localhost:8443/realms/saml-demo

# 4. 啟動 Spring Boot 應用
mvn clean spring-boot:run
```

#### 步驟 2: 測試登入
- [ ] 訪問 http://localhost:8080
- [ ] 點擊【登入】按鈕
- [ ] 重導向到 Keycloak 登入頁面
- [ ] 輸入帳號：`testuser`
- [ ] 輸入密碼：`password123`
- [ ] 成功登入，跳轉到 `/user` 頁面
- [ ] 看到用戶資訊和 SAML 屬性

#### 步驟 3: 測試 SAML SLO
- [ ] 在用戶頁面看到【登出 (SAML SLO)】按鈕
- [ ] 點擊登出按鈕
- [ ] **預期結果 (成功):**
  - [ ] 重導向到首頁 (http://localhost:8080/)
  - [ ] URL 包含 `?logout=success` 參數
  - [ ] 看到綠色的「登出成功」訊息
  - [ ] Session 被清除
  - [ ] 嘗試訪問 `/user` 會重新要求登入
- [ ] **預期結果 (失敗):**
  - [ ] 跳轉到錯誤頁面
  - [ ] 顯示詳細錯誤資訊
  - [ ] 提供返回首頁連結

#### 步驟 4: 驗證 IDP Session 清除
- [ ] 登出後，開新分頁訪問 http://localhost:8080
- [ ] 點擊登入
- [ ] **預期:** Keycloak 要求重新輸入帳號密碼（證明 IDP session 已清除）
- [ ] **異常:** 直接登入成功（表示 IDP session 未清除，SLO 未完全成功）

### 🔍 日誌檢查

#### Spring Boot 應用日誌

查看是否有以下 DEBUG 訊息：

**✅ 成功的 SLO 日誌關鍵字:**
```
- "Creating SAML 2.0 LogoutRequest"
- "Sending LogoutRequest to IDP"
- "Processing LogoutResponse from IDP"
- "Invalidating session"
- "Redirecting to /?logout=success"
```

**❌ 失敗的 SLO 日誌關鍵字:**
```
- "Failed to process LogoutResponse"
- "Error validating SAML response"
- "Unable to resolve logout request"
- Exception stack trace
```

#### Keycloak 日誌檢查

```bash
# 查看 Keycloak 登出相關日誌
docker-compose logs -f keycloak | grep -i "logout"
```

查找：
- [ ] `Processing SAML LogoutRequest`
- [ ] `Sending LogoutResponse to SP`
- [ ] `Session invalidated`

### 💡 快速驗證方法

**使用瀏覽器開發者工具:**

1. 登入應用
2. 開啟瀏覽器開發者工具 (F12) → Network 標籤
3. 點擊登出按鈕
4. 觀察網路請求順序:
   - 應該看到 `POST /logout`
   - 然後重導向到 Keycloak
   - 最後重導向回 `/?logout=success`
5. 如果卡在中間步驟，檢查失敗的請求細節

---

## SLO 流程說明

### 完整的 SAML SLO 流程圖

```
┌─────────┐                    ┌─────────┐                    ┌─────────┐
│ Browser │                    │   SP    │                    │   IDP   │
└────┬────┘                    └────┬────┘                    └────┬────┘
     │                              │                              │
     │  1. POST /logout             │                              │
     ├─────────────────────────────>│                              │
     │                              │                              │
     │                              │  2. SAML LogoutRequest       │
     │                              ├─────────────────────────────>│
     │                              │                              │
     │                              │  3. 處理登出 & 清除 Session  │
     │                              │                              │
     │  4. Redirect to IDP Logout   │                              │
     │<─────────────────────────────┤                              │
     │                              │                              │
     │  5. SAML LogoutResponse                                     │
     │<────────────────────────────────────────────────────────────┤
     │                              │                              │
     │  6. POST to /logout/saml2/slo│                              │
     ├─────────────────────────────>│                              │
     │                              │                              │
     │  7. Clear local session      │                              │
     │                              │                              │
     │  8. Redirect to home (/?logout=success)                     │
     │<─────────────────────────────┤                              │
     │                              │                              │
```

### 流程步驟說明

1. **用戶觸發登出** - 點擊登出按鈕，發送 POST 請求到 `/logout`
2. **SP 發送 LogoutRequest** - SP 建立 SAML LogoutRequest 並發送到 IDP
3. **IDP 處理登出** - IDP 驗證請求，清除自己的 session
4. **重導向** - IDP 重導向用戶到 IDP 登出頁面
5. **IDP 發送 LogoutResponse** - IDP 建立 SAML LogoutResponse
6. **SP 接收回應** - SP 在 `/logout/saml2/slo` 端點接收 LogoutResponse
7. **SP 清除 Session** - SP 驗證回應，清除本地 session 和 cookies
8. **完成登出** - 重導向到首頁，顯示成功訊息

---

## 問題排查

### 問題 1: 登出後顯示錯誤頁面

**可能原因:**
- Keycloak 未正確配置 SLO 端點
- 網路連線問題
- SAML 簽章驗證失敗
- IDP 無回應

**解決方案:**

#### A. 檢查 Keycloak 是否運行
```bash
# Windows
docker ps | findstr keycloak

# Linux/Mac
docker ps | grep keycloak

# 測試連線
curl http://localhost:8443/realms/saml-demo
```

#### B. 檢查 SP Metadata 是否正確
訪問: http://localhost:8080/saml2/service-provider-metadata/keycloak

確認 XML 中包含：
```xml
<md:SingleLogoutService 
    Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" 
    Location="http://localhost:8080/logout/saml2/slo"/>
```

#### C. 檢查 Keycloak Client 配置
1. 訪問: http://localhost:8443/admin
2. 登入（使用 docker-compose.yml 中設定的 admin 帳號）
3. 選擇 `saml-demo` realm
4. Clients → 選擇 SP client
5. 檢查 Settings → Fine Grain SAML Endpoint Configuration
6. 確認 "Logout Service POST Binding URL" 正確

#### D. 檢查日誌
```bash
# 查看 Spring Boot 日誌（控制台）
# 查看 Keycloak 日誌
docker-compose logs keycloak

# 實時監控 Keycloak 日誌
docker-compose logs -f keycloak
```

### 問題 2: 登出後 IDP Session 未清除

**症狀:** 再次訪問應用時，直接登入成功，無需輸入帳號密碼

**解決方案:**

1. 確認 Keycloak Client 設定中的 `frontchannelLogout` 為 `true`
2. 檢查 `saml_single_logout_service_url_post` 是否正確配置
3. 確認 Keycloak 確實收到並處理了 LogoutRequest

### 問題 3: CSRF Token 錯誤

**解決方案:**

確保登出表單使用 POST 方法，並且 Thymeleaf 自動包含 CSRF token：

```html
<form th:action="@{/logout}" method="post">
    <button type="submit">登出</button>
</form>
```

### 問題 4: 網路連線問題

**檢查連線:**
```bash
# 從 Spring Boot 應用測試到 Keycloak 的連線
curl -v http://localhost:8443/realms/saml-demo/protocol/saml

# 檢查 Docker 網路
docker network ls
docker network inspect springsecu rity-saml-test_saml-network
```

---

## 安全性考量

### 1. 簽章驗證

確保 SAML 訊息的完整性：

```yaml
spring:
  security:
    saml2:
      relyingparty:
        registration:
          keycloak:
            assertingparty:
              singlesignon:
                sign-request: true  # 簽署 LogoutRequest
              verification:
                credentials:
                  - certificate-location: classpath:saml/idp-cert.crt
```

### 2. Session 管理

Spring Security 會自動：
- ✅ 清除 HTTP Session
- ✅ 刪除 JSESSIONID Cookie
- ✅ 清除 SecurityContext
- ✅ 使所有相關的 Authentication 失效

### 3. 超時設定

在 application.yml 中配置適當的 session 超時：

```yaml
server:
  servlet:
    session:
      timeout: 30m  # Session 超時時間
      cookie:
        secure: true  # 生產環境使用 HTTPS
        http-only: true
        same-site: strict
```

### 4. HTTPS 配置（生產環境必須）

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
```

⚠️ **重要:** 生產環境必須使用 HTTPS，否則 SAML 訊息可能被截取。

---

## 最佳實踐

### 1. 監控 SLO 成功率

記錄並監控 SLO 的成功和失敗事件：

```java
@Slf4j
public class SloMonitor {
    
    @EventListener
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        log.info("SLO Success: user={}, timestamp={}", 
            event.getAuthentication().getName(),
            Instant.now());
        // 發送監控指標
    }
    
    @EventListener
    public void onLogoutFailure(LogoutFailureEvent event) {
        log.error("SLO Failed: user={}, error={}", 
            event.getAuthentication().getName(),
            event.getException().getMessage());
        // 觸發告警
    }
}
```

### 2. 提供備用登出方式

如果 SAML SLO 失敗，提供本地登出選項：

```html
<form th:action="@{/logout}" method="post">
    <button type="submit" class="btn btn-primary">
        完整登出 (SAML SLO)
    </button>
</form>

<form th:action="@{/local-logout}" method="post">
    <button type="submit" class="btn btn-secondary">
        僅登出本應用
    </button>
</form>
```

### 3. 詳細的日誌記錄

在 application.yml 中啟用詳細日誌：

```yaml
logging:
  level:
    root: INFO
    org.springframework.security: DEBUG
    org.springframework.security.saml2: TRACE
    org.opensaml: DEBUG
```

### 4. 定期測試

- 📅 每週測試一次 SLO 流程
- 📊 監控 SLO 成功率
- 🔍 定期檢查日誌，及早發現問題
- 🧪 在 staging 環境充分測試後再部署到生產環境

---

## 測試結果記錄表

### 測試日期: _______________

| 測試項目 | 結果 | 備註 |
|---------|------|------|
| Keycloak 啟動 | ⬜ Pass / ⬜ Fail | |
| Spring Boot 啟動 | ⬜ Pass / ⬜ Fail | |
| SAML 登入 | ⬜ Pass / ⬜ Fail | |
| 用戶資訊顯示 | ⬜ Pass / ⬜ Fail | |
| SAML SLO 觸發 | ⬜ Pass / ⬜ Fail | |
| SP Session 清除 | ⬜ Pass / ⬜ Fail | |
| IDP Session 清除 | ⬜ Pass / ⬜ Fail | |
| 錯誤處理 | ⬜ Pass / ⬜ Fail | |

### 測試結論:
```
□ 全部通過 - SAML SLO 功能正常
□ 部分通過 - 需要進一步調整
□ 失敗 - 需要檢查配置和日誌

問題描述:
_________________________________________________________________
_________________________________________________________________
_________________________________________________________________
```

---

## 相關資源

### 官方文檔
- [Spring Security SAML2 Logout](https://docs.spring.io/spring-security/reference/servlet/saml2/logout.html)
- [Keycloak SAML 配置](https://www.keycloak.org/docs/latest/server_admin/#_saml)
- [SAML 2.0 規範](http://docs.oasis-open.org/security/saml/v2.0/)

### 專案文檔
- [快速開始指南](QUICKSTART.md)
- [Keycloak 配置說明](KEYCLOAK_SETUP.md)
- [主要 README](../README.md)

---

## 總結

完成 SAML Single Logout 配置後，系統將支援：

- ✅ 完整的 SAML SLO 流程
- ✅ 同步清除 SP 和 IDP 的 session
- ✅ 良好的錯誤處理和用戶反饋
- ✅ 詳細的日誌記錄
- ✅ 安全的 session 管理
- ✅ 符合企業級安全要求

如有任何問題，請：
1. 查看本文檔的問題排查章節
2. 檢查應用程式和 Keycloak 日誌
3. 驗證 SP Metadata 配置
4. 確認網路連線正常

**祝測試順利！** 🎉
