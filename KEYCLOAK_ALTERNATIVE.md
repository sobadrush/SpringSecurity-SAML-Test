# 🔄 使用 Keycloak 作為 SAML IDP 的替代方案

由於 CAS Server 的 SAML IDP 功能在開源版本中需要額外配置，這裡提供使用 Keycloak 作為 SAML Identity Provider 的替代方案。

## 為什麼選擇 Keycloak？

- ✅ 完全開源且免費
- ✅ 內建完整的 SAML2 IDP 功能
- ✅ 易於配置和使用
- ✅ 優秀的管理介面
- ✅ 廣泛的社群支援

## 快速設定指南

### 步驟 1: 更新 docker-compose.yml

替換原有的 CAS Server 配置為 Keycloak：

```yaml
services:
  keycloak:
    image: quay.io/keycloak/keycloak:23.0
    container_name: keycloak-idp
    hostname: keycloak
    ports:
      - "8443:8080"
    environment:
      - KEYCLOAK_ADMIN=admin
      - KEYCLOAK_ADMIN_PASSWORD=admin
      - KC_HTTP_ENABLED=true
      - KC_HOSTNAME_STRICT=false
      - KC_HOSTNAME_STRICT_HTTPS=false
    command:
      - start-dev
    networks:
      - saml-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health/ready"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s

networks:
  saml-network:
    driver: bridge
```

### 步驟 2: 啟動 Keycloak

```bash
# 停止現有的 CAS Server
docker-compose down

# 啟動 Keycloak
docker-compose up -d

# 等待 Keycloak 啟動（約 30-60 秒）
docker logs -f keycloak-idp
```

### 步驟 3: 設定 Keycloak

1. **訪問 Keycloak 管理控制台**
   - URL: http://localhost:8443
   - 使用者: `admin`
   - 密碼: `admin`

2. **建立 Realm**
   - 點擊左上角的 Realm 下拉選單
   - 選擇 "Create Realm"
   - Name: `saml-demo`
   - 點擊 "Create"

3. **建立測試用戶**
   - 進入 "Users" 選單
   - 點擊 "Add user"
   - Username: `testuser`
   - Email: `testuser@example.com`
   - Email Verified: ON
   - 點擊 "Create"
   - 進入 "Credentials" 標籤
   - 設定密碼: `password123`
   - Temporary: OFF
   - 點擊 "Set Password"

4. **建立 SAML Client (Service Provider)**
   - 進入 "Clients" 選單
   - 點擊 "Create client"
   - Client type: `SAML`
   - Client ID: `http://localhost:8080/saml2/service-provider-metadata/keycloak`
   - 點擊 "Next"
   
   **General Settings**:
   - Name: `Spring Boot SAML SP`
   - 點擊 "Next"
   
   **Login settings**:
   - Root URL: `http://localhost:8080`
   - Valid redirect URIs: `http://localhost:8080/*`
   - Base URL: `http://localhost:8080`
   - 點擊 "Save"

5. **配置 SAML Client 設定**
   
   在剛建立的 Client 詳細頁面中：
   
   **Keys 標籤**:
   - Client signature required: OFF（開發環境簡化配置）
   
   **Advanced 標籤**:
   - Assertion Consumer Service POST Binding URL: `http://localhost:8080/login/saml2/sso/keycloak`
   - Logout Service POST Binding URL: `http://localhost:8080/logout/saml2/slo`

### 步驟 4: 更新 Spring Boot 配置

修改 `src/main/resources/application.yml`:

```yaml
spring:
  security:
    saml2:
      relyingparty:
        registration:
          keycloak:  # 改為 keycloak
            assertingparty:
              entity-id: http://localhost:8443/realms/saml-demo
              metadata-uri: http://localhost:8443/realms/saml-demo/protocol/saml/descriptor
              singlesignon:
                url: http://localhost:8443/realms/saml-demo/protocol/saml
                binding: POST
              singlelogout:
                url: http://localhost:8443/realms/saml-demo/protocol/saml
                binding: POST
            entity-id: http://localhost:8080/saml2/service-provider-metadata/keycloak
            acs:
              location: http://localhost:8080/login/saml2/sso/keycloak
              binding: POST
```

### 步驟 5: 測試流程

1. **啟動 Spring Boot 應用**
   ```bash
   mvn spring-boot:run
   ```

2. **訪問應用**
   - 開啟瀏覽器訪問: http://localhost:8080
   - 點擊「開始使用 SAML 登入」
   
3. **Keycloak 登入**
   - 自動重導向到 Keycloak 登入頁面
   - 輸入測試帳號:
     - Username: `testuser`
     - Password: `password123`

4. **驗證成功**
   - 登入成功後返回應用程式
   - 顯示用戶資訊和 SAML 屬性

## 優勢對比

| 功能 | CAS Server | Keycloak |
|------|-----------|----------|
| 開源 | ✅ | ✅ |
| SAML IDP 開箱即用 | ❌ 需要額外配置 | ✅ |
| 管理介面 | 基本 | ⭐ 優秀 |
| 文檔 | 一般 | ⭐ 詳細 |
| 社群支援 | 一般 | ⭐ 活躍 |
| 設定複雜度 | 高 | 低 |
| OAuth2/OIDC 支援 | 有限 | ⭐ 完整 |

## 其他 SAML IDP 選項

### 1. SimpleSAMLphp
- PHP 實作的 SAML 解決方案
- 輕量級
- 適合簡單測試

### 2. Azure AD
- 企業級解決方案
- 需要 Microsoft 帳號
- 適合生產環境

### 3. Okta
- SaaS 解決方案
- 免費開發者帳號
- 功能完整

### 4. OneLogin
- SaaS 解決方案
- 提供開發者方案
- 易於整合

## 完整的 Docker Compose 範例（Keycloak）

```yaml
services:
  keycloak:
    image: quay.io/keycloak/keycloak:23.0
    container_name: keycloak-idp
    hostname: keycloak
    ports:
      - "8443:8080"
    environment:
      - KEYCLOAK_ADMIN=admin
      - KEYCLOAK_ADMIN_PASSWORD=admin
      - KC_HTTP_ENABLED=true
      - KC_HOSTNAME_STRICT=false
      - KC_HOSTNAME_STRICT_HTTPS=false
      - KC_HEALTH_ENABLED=true
    command:
      - start-dev
      - --import-realm
    volumes:
      - ./keycloak/realm-export.json:/opt/keycloak/data/import/realm-export.json:ro
    networks:
      - saml-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health/ready"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s

networks:
  saml-network:
    driver: bridge
```

## 自動化 Realm 匯入

可以準備一個 `keycloak/realm-export.json` 檔案，包含預先配置的 Realm、User 和 Client，實現一鍵部署。

```json
{
  "realm": "saml-demo",
  "enabled": true,
  "users": [
    {
      "username": "testuser",
      "enabled": true,
      "email": "testuser@example.com",
      "emailVerified": true,
      "credentials": [
        {
          "type": "password",
          "value": "password123",
          "temporary": false
        }
      ]
    }
  ],
  "clients": [
    {
      "clientId": "http://localhost:8080/saml2/service-provider-metadata/keycloak",
      "name": "Spring Boot SAML SP",
      "protocol": "saml",
      "enabled": true,
      "baseUrl": "http://localhost:8080",
      "redirectUris": ["http://localhost:8080/*"],
      "attributes": {
        "saml.client.signature": "false",
        "saml.assertion.signature": "true",
        "saml.encrypt": "false"
      }
    }
  ]
}
```

## 總結

使用 Keycloak 作為 SAML IDP 可以：
- 🚀 快速開始測試
- 📝 簡化配置流程
- 🔧 提供完整的管理功能
- 📚 獲得更好的文檔支援
- 🌐 支援多種認證協定（SAML、OAuth2、OIDC）

**建議**: 對於開發和測試環境，強烈推薦使用 Keycloak 替代 CAS Server。

---

**參考資源**:
- [Keycloak 官方文檔](https://www.keycloak.org/documentation)
- [Keycloak SAML 指南](https://www.keycloak.org/docs/latest/server_admin/#_saml)
- [Spring Security SAML2 文檔](https://docs.spring.io/spring-security/reference/servlet/saml2/index.html)
