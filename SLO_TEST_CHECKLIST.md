# SAML SLO 快速測試檢查清單

## ✅ 配置檢查

### 1. application.yml 檢查
- [ ] `spring.security.saml2.relyingparty.registration.keycloak.singlelogout` 已配置
- [ ] `url: http://localhost:8080/logout/saml2/slo` 已設定
- [ ] `response-url: http://localhost:8080/logout/saml2/slo` 已設定
- [ ] `binding: POST` 已設定

### 2. SecurityConfig.java 檢查
- [ ] 導入了 `OpenSaml4LogoutRequestResolver`
- [ ] `.saml2Logout()` 已正確配置
- [ ] `.logoutRequest()` 和 `.logoutResponse()` 已配置
- [ ] `saml2LogoutRequestResolver` Bean 已定義

### 3. Keycloak 配置檢查
- [ ] `keycloak/realm-export.json` 中包含 `saml_single_logout_service_url_post`
- [ ] 值為 `http://localhost:8080/logout/saml2/slo`
- [ ] `frontchannelLogout: true` 已設定

## 🧪 功能測試步驟

### 步驟 1: 啟動服務
```bash
# 1. 啟動 Keycloak
cd d:\Program_Projects\Java_workspace\SpringSecurity-SAML-Test
docker-compose up -d

# 2. 等待 Keycloak 啟動 (約 30-60 秒)
timeout /t 45

# 3. 驗證 Keycloak 運行
curl http://localhost:8443/realms/saml-demo/.well-known/openid-configuration

# 4. 啟動 Spring Boot 應用
mvn clean spring-boot:run
```

### 步驟 2: 測試登入
- [ ] 訪問 http://localhost:8080
- [ ] 點擊【登入】按鈕
- [ ] 重導向到 Keycloak 登入頁面
- [ ] 輸入帳號：`testuser`
- [ ] 輸入密碼：`password123`
- [ ] 成功登入，跳轉到 `/user` 頁面
- [ ] 看到用戶資訊和 SAML 屬性

### 步驟 3: 測試 SAML SLO
- [ ] 在用戶頁面看到【登出 (SAML SLO)】按鈕
- [ ] 點擊登出按鈕
- [ ] **預期結果 (成功):**
  - [ ] 重導向到首頁 (http://localhost:8080/)
  - [ ] URL 包含 `?logout=success` 參數
  - [ ] Session 被清除
  - [ ] 嘗試訪問 `/user` 會重新要求登入
- [ ] **預期結果 (失敗):**
  - [ ] 跳轉到錯誤頁面
  - [ ] 顯示詳細錯誤資訊
  - [ ] 提供返回首頁連結

### 步驟 4: 驗證 IDP Session 清除
- [ ] 登出後，開新分頁訪問 http://localhost:8080
- [ ] 點擊登入
- [ ] **預期:** Keycloak 要求重新輸入帳號密碼（證明 IDP session 已清除）
- [ ] **異常:** 直接登入成功（表示 IDP session 未清除，SLO 未完全成功）

## 🔍 日誌檢查

### Spring Boot 應用日誌
查看是否有以下 DEBUG 訊息：

```
✅ 成功的 SLO 日誌關鍵字:
- "Creating SAML 2.0 LogoutRequest"
- "Sending LogoutRequest to IDP"
- "Processing LogoutResponse from IDP"
- "Invalidating session"
- "Redirecting to /?logout=success"

❌ 失敗的 SLO 日誌關鍵字:
- "Failed to process LogoutResponse"
- "Error validating SAML response"
- "Unable to resolve logout request"
- Exception stack trace
```

### Keycloak 日誌檢查
```bash
docker-compose logs -f keycloak | grep -i "logout"
```

查找：
- [ ] `Processing SAML LogoutRequest`
- [ ] `Sending LogoutResponse to SP`
- [ ] `Session invalidated`

## 🐛 問題排查

### 如果看到錯誤頁面：

#### 檢查 1: Keycloak 是否運行
```bash
docker ps | findstr keycloak
curl http://localhost:8443/realms/saml-demo
```

#### 檢查 2: SP Metadata 是否正確
訪問: http://localhost:8080/saml2/service-provider-metadata/keycloak

確認 XML 中包含：
```xml
<md:SingleLogoutService 
    Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" 
    Location="http://localhost:8080/logout/saml2/slo"/>
```

#### 檢查 3: Keycloak Client 配置
1. 訪問: http://localhost:8443/admin
2. 登入（使用 docker-compose.yml 中設定的 admin 帳號）
3. 選擇 `saml-demo` realm
4. Clients → 選擇 SP client
5. 檢查 Settings → Fine Grain SAML Endpoint Configuration
6. 確認 "Logout Service POST Binding URL" 正確

#### 檢查 4: 網路連線
```bash
# 從 Spring Boot 應用測試到 Keycloak 的連線
curl -v http://localhost:8443/realms/saml-demo/protocol/saml
```

## 📊 測試結果記錄

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

## 🎯 下一步

如果測試失敗：
1. 查看 Spring Boot 控制台日誌
2. 查看 Keycloak 日誌: `docker-compose logs keycloak`
3. 檢查 SP Metadata: http://localhost:8080/saml2/service-provider-metadata/keycloak
4. 參考 SAML_SLO_GUIDE.md 中的詳細排查步驟

如果測試成功：
1. ✅ SAML SLO 配置完成
2. ✅ 可以開始開發其他功能
3. ✅ 記得在生產環境使用 HTTPS
4. ✅ 配置適當的 session timeout

## 💡 提示

**快速驗證 SLO 是否工作:**
1. 登入應用
2. 開啟瀏覽器開發者工具 (F12) → Network 標籤
3. 點擊登出按鈕
4. 觀察網路請求:
   - 應該看到 POST `/logout`
   - 然後重導向到 Keycloak
   - 最後重導向回 `/?logout=success`
5. 如果卡在中間步驟，檢查失敗的請求細節
