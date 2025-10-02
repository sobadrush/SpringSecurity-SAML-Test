# SAML Single Logout (SLO) 設定指南

## 📋 概述

本指南說明如何配置和測試 SAML Single Logout (SLO) 功能。SLO 允許用戶在登出 Service Provider (SP) 時，同時登出 Identity Provider (IDP)。

## 🔧 已完成的配置

### 1. **application.yml** - SP SLO 端點配置

```yaml
singlelogout:
  # Single Logout Service - SP 接收登出請求的端點
  url: http://localhost:8080/logout/saml2/slo
  response-url: http://localhost:8080/logout/saml2/slo
  binding: POST
```

**說明：**
- `url`: SP 接收 IDP 發送的 LogoutRequest 的端點
- `response-url`: SP 接收 IDP 發送的 LogoutResponse 的端點
- `binding`: 使用 POST binding

### 2. **SecurityConfig.java** - 增強的 SAML2 Logout 配置

```java
.saml2Logout(saml2Logout -> saml2Logout
    .logoutRequest(logoutRequest -> logoutRequest
        .logoutRequestResolver(saml2LogoutRequestResolver(relyingPartyRegistrationRepository))
    )
    .logoutResponse(logoutResponse -> logoutResponse
        .logoutUrl("/logout/saml2/slo")
    )
)
```

**特點：**
- ✅ 支援完整的 SAML SLO 流程
- ✅ 自動清除 Session 和 Cookie
- ✅ 錯誤處理和重導向
- ✅ 與 IDP 的 SLO 端點整合

### 3. **Keycloak IDP 配置**

在 `keycloak/realm-export.json` 中已配置：

```json
"saml_single_logout_service_url_post": "http://localhost:8080/logout/saml2/slo"
```

這告訴 Keycloak 在處理 SLO 時，應該向 SP 的這個端點發送 LogoutResponse。

## 🚀 測試 SLO 功能

### 步驟 1: 確保 Keycloak 正在運行

```bash
docker-compose up -d
```

檢查 Keycloak 狀態：
```bash
curl http://localhost:8443/realms/saml-demo
```

### 步驟 2: 啟動 Spring Boot 應用

```bash
mvn clean spring-boot:run
```

### 步驟 3: 測試 SLO 流程

1. **登入應用**
   - 訪問 http://localhost:8080
   - 點擊【登入】
   - 使用測試帳號：`testuser` / `password123`
   - 成功後會跳轉到用戶頁面

2. **執行登出**
   - 在用戶頁面點擊【登出 (SAML SLO)】按鈕
   - 系統會觸發 SAML SLO 流程

3. **驗證結果**
   
   **成功情況：**
   - 自動重導向到首頁
   - URL 顯示 `?logout=success`
   - Session 被清除
   - IDP (Keycloak) 的 session 也被清除

   **失敗情況：**
   - 跳轉到錯誤頁面
   - 顯示詳細的錯誤資訊
   - 提供返回首頁的連結

## 🔍 SLO 流程說明

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

## 🐛 常見問題排查

### 問題 1: 登出後顯示錯誤頁面

**可能原因：**
- Keycloak 未正確配置 SLO 端點
- 網路連線問題
- SAML 簽章驗證失敗

**解決方案：**
1. 檢查 Keycloak 日誌：
   ```bash
   docker-compose logs keycloak
   ```

2. 檢查 Spring Boot 應用日誌（已開啟 DEBUG）：
   ```bash
   # 查看 application.yml 中的日誌級別
   logging:
     level:
       org.springframework.security.saml2: DEBUG
   ```

3. 驗證 Keycloak Client 配置：
   - 登入 Keycloak Admin Console: http://localhost:8443/admin
   - 選擇 `saml-demo` realm
   - 進入 Clients → 選擇 SP client
   - 檢查 "Fine Grain SAML Endpoint Configuration"
   - 確認 "Logout Service POST Binding URL" 為: `http://localhost:8080/logout/saml2/slo`

### 問題 2: 登出後 IDP session 未清除

**解決方案：**
1. 確認 Keycloak Client 設定中的 `frontchannelLogout` 為 `true`
2. 檢查 `saml_single_logout_service_url_post` 是否正確配置

### 問題 3: CSRF Token 錯誤

**解決方案：**
- 確保登出表單使用 POST 方法
- 確保 Thymeleaf 自動包含 CSRF token：
  ```html
  <form th:action="@{/logout}" method="post">
      <button type="submit">登出</button>
  </form>
  ```

## 📊 日誌分析

### 成功的 SLO 日誌範例：

```
DEBUG o.s.s.saml2.provider.service.web.authentication.logout.OpenSaml4LogoutRequestResolver 
  - Creating SAML 2.0 LogoutRequest for [keycloak]

DEBUG o.s.s.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestFilter 
  - Sending LogoutRequest to IDP

DEBUG o.s.s.saml2.provider.service.web.authentication.logout.Saml2LogoutResponseFilter 
  - Processing LogoutResponse from IDP

INFO  o.s.s.web.authentication.logout.SecurityContextLogoutHandler 
  - Invalidating session: XXXXXXXXXXXXXX

DEBUG o.s.s.web.authentication.logout.SimpleUrlLogoutSuccessHandler 
  - Redirecting to /?logout=success
```

## 🔐 安全性考量

### 1. **簽章驗證**

確保 SAML 訊息的完整性：
```yaml
assertingparty:
  singlesignon:
    sign-request: true  # 簽署 LogoutRequest
  verification:
    credentials:
      - certificate-location: classpath:saml/idp-cert.crt
```

### 2. **Session 管理**

Spring Security 會自動：
- 清除 HTTP Session
- 刪除 JSESSIONID Cookie
- 清除 SecurityContext

### 3. **超時設定**

在 application.yml 中配置：
```yaml
server:
  servlet:
    session:
      timeout: 30m  # Session 超時時間
```

## 📝 最佳實踐

1. **永遠使用 HTTPS**（生產環境）
   ```yaml
   server:
     ssl:
       enabled: true
       key-store: classpath:keystore.p12
       key-store-password: changeit
   ```

2. **監控 SLO 成功率**
   - 記錄 SLO 成功/失敗事件
   - 設定告警機制

3. **提供備用登出方式**
   - 如果 SAML SLO 失敗，提供本地登出選項
   - 在 UI 上明確說明差異

4. **定期測試**
   - 驗證 SLO 流程
   - 檢查 IDP 和 SP 的 session 是否都被清除

## 🔗 相關資源

- [Spring Security SAML2 官方文檔](https://docs.spring.io/spring-security/reference/servlet/saml2/logout.html)
- [Keycloak SAML 配置指南](https://www.keycloak.org/docs/latest/server_admin/#_saml)
- [SAML 2.0 規範](http://docs.oasis-open.org/security/saml/v2.0/)

## 🎯 總結

SAML Single Logout 配置完成後，系統將支援：
- ✅ 完整的 SAML SLO 流程
- ✅ 同步清除 SP 和 IDP 的 session
- ✅ 良好的錯誤處理
- ✅ 詳細的日誌記錄

如有問題，請參考上述的排查步驟或查看應用日誌。
