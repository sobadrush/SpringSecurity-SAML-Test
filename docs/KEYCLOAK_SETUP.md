# 🔐 Keycloak 設定指南

> **Keycloak 作為 SAML Identity Provider 的完整配置說明**

## 📋 目錄

- [為什麼選擇 Keycloak](#為什麼選擇-keycloak)
- [自動配置（推薦）](#自動配置推薦)
- [手動配置步驟](#手動配置步驟)
- [進階設定](#進階設定)
- [疑難排解](#疑難排解)

---

## 為什麼選擇 Keycloak？

### 優勢

- ✅ **完全開源且免費** - Apache 2.0 授權
- ✅ **內建完整的 SAML2 IDP 功能** - 無需額外插件
- ✅ **易於配置和使用** - 直覺的Web管理介面
- ✅ **優秀的管理介面** - 簡化用戶和Client管理
- ✅ **廣泛的社群支援** - 豐富的文檔和範例
- ✅ **企業級功能** - 支援 SSO, SLO, 多租戶等

### 功能特點

- 🔒 **多協定支援** - SAML 2.0, OpenID Connect, OAuth 2.0
- 👥 **用戶聯合** - LDAP, Active Directory 整合
- 🎨 **可自訂介面** - 支援主題客製化
- 📊 **審計日誌** - 完整的操作記錄
- 🌐 **多語言支援** - 包含繁體中文

---

## 自動配置（推薦）

本專案已包含完整的 Keycloak 自動配置，使用 Realm 匯入功能。

### 已自動配置的內容

✅ **Realm 設定**
- Realm Name: `saml-demo`
- 已啟用 SAML 支援

✅ **預配置的測試用戶**

| 用戶名 | 密碼 | 角色 | Email | 部門 |
|--------|------|------|-------|------|
| testuser | password123 | user | testuser@example.com | Engineering |
| admin | admin123 | admin, user | admin@example.com | Administration |

✅ **SAML Client（SP）設定**
- Client ID: `http://localhost:8080/saml2/service-provider-metadata/keycloak`
- SSO URL: `http://localhost:8080/login/saml2/sso/keycloak`
- SLO URL: `http://localhost:8080/logout/saml2/slo`
- 已配置屬性映射（username, email, firstName, lastName, department, employeeNumber, roles）

###啟動步驟

```bash
# 1. 啟動 Keycloak（會自動匯入配置）
docker-compose up -d

# 2. 等待啟動完成（約 60 秒）
docker logs -f keycloak-idp

# 3. 驗證 Keycloak
curl http://localhost:8443/realms/saml-demo

# 4. 啟動 Spring Boot 應用
mvn spring-boot:run
```

### 驗證自動配置

訪問 Keycloak 管理控制台：
- **URL**: http://localhost:8443/admin
- **帳號**: admin
- **密碼**: admin

檢查項目：
- ✅ `saml-demo` Realm 存在
- ✅ 測試用戶已創建
- ✅ SAML Client 已配置

---

## 手動配置步驟

如果需要從零開始配置或自訂設定，請按照以下步驟操作。

### 步驟 1: 啟動 Keycloak

```yaml
# docker-compose.yml
services:
  keycloak:
    image: quay.io/keycloak/keycloak:23.0
    container_name: keycloak-idp
    ports:
      - "8443:8080"
    environment:
      - KEYCLOAK_ADMIN=admin
      - KEYCLOAK_ADMIN_PASSWORD=admin
      - KC_HTTP_ENABLED=true
      - KC_HOSTNAME_STRICT=false
    command:
      - start-dev
```

```bash
docker-compose up -d
```

### 步驟 2: 登入管理控制台

1. 訪問 http://localhost:8443/admin
2. 使用帳號：`admin` / 密碼：`admin`

### 步驟 3: 建立 Realm

1. 點擊左上角的 Realm 下拉選單
2. 選擇 **"Create Realm"**
3. 設定：
   - **Realm name**: `saml-demo`
   - **Enabled**: ON
4. 點擊 **"Create"**

### 步驟 4: 建立測試用戶

1. 在 `saml-demo` Realm 中，進入 **"Users"** 選單
2. 點擊 **"Add user"**
3. 填寫資訊：
   ```
   Username: testuser
   Email: testuser@example.com
   First name: Test
   Last name: User
   Email verified: ON
   ```
4. 點擊 **"Create"**
5. 進入 **"Credentials"** 標籤：
   - 點擊 **"Set password"**
   - Password: `password123`
   - Temporary: **OFF**
   - 點擊 **"Save"**

6. 進入 **"Attributes"** 標籤，添加自訂屬性：
   ```
   department: Engineering
   employeeNumber: 12345
   ```

7. 進入 **"Role mapping"** 標籤：
   - 點擊 **"Assign role"**
   - 選擇 `user`
   - 點擊 **"Assign"**

### 步驟 5: 建立 SAML Client (Service Provider)

1. 進入 **"Clients"** 選單
2. 點擊 **"Create client"**

**Step 1: General Settings**
```
Client type: SAML
Client ID: http://localhost:8080/saml2/service-provider-metadata/keycloak
```
點擊 **"Next"**

**Step 2: SAML capabilities**
```
Name: Spring Boot SAML SP
Description: Spring Boot SAML Service Provider
```
點擊 **"Next"**

**Step 3: Login settings**
```
Root URL: http://localhost:8080
Valid redirect URIs: http://localhost:8080/*
Base URL: http://localhost:8080
Master SAML Processing URL: (留空)
```
點擊 **"Save"**

### 步驟 6: 配置 SAML Client 詳細設定

在剛建立的 Client 詳細頁面中：

#### Settings 標籤

**Basic settings:**
- Client ID: `http://localhost:8080/saml2/service-provider-metadata/keycloak`
- Name: `Spring Boot SAML SP`
- Enabled: ON

**SAML capabilities:**
- Name ID format: `username`
- Force POST binding: OFF
- Force name ID format: OFF
- Force artifact binding: OFF
- Include AuthnStatement: ON
- Include OneTimeUse Condition: OFF
- Sign documents: ON
- Sign assertions: ON
- Signature algorithm: RSA_SHA256
- SAML signature key name: KEY_ID
- Canonicalization method: EXCLUSIVE
- Front channel logout: ON

**Login settings:**
- Root URL: `http://localhost:8080`
- Valid redirect URIs: `http://localhost:8080/*`

#### Keys 標籤

**Client signature required:**
- OFF（開發環境，生產環境建議 ON）

#### Advanced 標籤

**Fine Grain SAML Endpoint Configuration:**
```
Assertion Consumer Service POST Binding URL:
http://localhost:8080/login/saml2/sso/keycloak

Logout Service POST Binding URL:
http://localhost:8080/logout/saml2/slo
```

### 步驟 7: 配置 SAML 屬性映射

在 Client 詳細頁面，進入 **"Client scopes"** 標籤：

1. 點擊 Client 的 dedicated scope（通常命名為 `<client-id>-dedicated`）
2. 進入 **"Mappers"** 標籤
3. 點擊 **"Add mapper"** → **"By configuration"**

**添加以下 Mappers:**

#### Username Mapper
```
Name: username
Mapper Type: User Property
Property: username
Friendly Name: Username
SAML Attribute Name: username
SAML Attribute NameFormat: Basic
```

#### Email Mapper
```
Name: email
Mapper Type: User Property
Property: email
Friendly Name: Email
SAML Attribute Name: email
SAML Attribute NameFormat: Basic
```

#### First Name Mapper
```
Name: firstName
Mapper Type: User Property
Property: firstName
Friendly Name: First Name
SAML Attribute Name: firstName
SAML Attribute NameFormat: Basic
```

#### Last Name Mapper
```
Name: lastName
Mapper Type: User Property
Property: lastName
Friendly Name: Last Name
SAML Attribute Name: lastName
SAML Attribute NameFormat: Basic
```

#### Department Mapper
```
Name: department
Mapper Type: User Attribute
User Attribute: department
Friendly Name: Department
SAML Attribute Name: department
SAML Attribute NameFormat: Basic
```

#### Employee Number Mapper
```
Name: employeeNumber
Mapper Type: User Attribute
User Attribute: employeeNumber
Friendly Name: Employee Number
SAML Attribute Name: employeeNumber
SAML Attribute NameFormat: Basic
```

#### Role List Mapper
```
Name: role list
Mapper Type: Role list
Role attribute name: roles
Friendly Name: Roles
SAML Attribute NameFormat: Basic
Single Role Attribute: OFF
```

---

## 進階設定

### 1. 匯出 Realm 配置

```bash
# 進入 Keycloak 容器
docker exec -it keycloak-idp bash

# 匯出 Realm
/opt/keycloak/bin/kc.sh export --realm saml-demo --file /tmp/realm-export.json

# 複製到主機
docker cp keycloak-idp:/tmp/realm-export.json ./keycloak/realm-export.json
```

### 2. 匯入 Realm 配置

專案已包含 `keycloak/realm-export.json`，Docker Compose 會自動匯入。

手動匯入方式：
1. 登入 Keycloak 管理控制台
2. 點擊左上角 Realm 下拉選單
3. 選擇 **"Create Realm"**
4. 點擊 **"Browse"** 選擇 `realm-export.json`
5. 點擊 **"Create"**

### 3. 啟用 HTTPS（生產環境）

```yaml
# docker-compose.yml
services:
  keycloak:
    environment:
      - KC_HTTPS_CERTIFICATE_FILE=/opt/keycloak/conf/server.crt
      - KC_HTTPS_CERTIFICATE_KEY_FILE=/opt/keycloak/conf/server.key
    volumes:
      - ./certs/server.crt:/opt/keycloak/conf/server.crt
      - ./certs/server.key:/opt/keycloak/conf/server.key
```

### 4. 持久化資料

```yaml
# docker-compose.yml
services:
  keycloak:
    volumes:
      - keycloak-data:/opt/keycloak/data

volumes:
  keycloak-data:
```

### 5. 自訂主題

```yaml
# docker-compose.yml
services:
  keycloak:
    volumes:
      - ./keycloak/themes:/opt/keycloak/themes
```

---

## 疑難排解

### 問題 1: Keycloak 啟動失敗

**檢查：**
```bash
docker logs keycloak-idp
```

**常見原因：**
- Port 8443 被佔用
- 記憶體不足
- Realm 匯入檔案格式錯誤

**解決：**
```bash
# 完全重置
docker-compose down -v
docker-compose up -d
```

### 問題 2: 無法匯入 Realm

**檢查：**
- `keycloak/realm-export.json` 檔案是否存在
- JSON 格式是否正確
- Docker volume 掛載是否正確

**手動匯入：**
```bash
# 複製檔案到容器
docker cp keycloak/realm-export.json keycloak-idp:/tmp/

# 進入容器匯入
docker exec -it keycloak-idp /opt/keycloak/bin/kc.sh import --file /tmp/realm-export.json
```

### 問題 3: SAML 認證失敗

**檢查清單：**
- ✅ Realm `saml-demo` 存在
- ✅ Client 配置正確
- ✅ 用戶已創建並啟用
- ✅ SP Metadata URL 正確
- ✅ 時間同步

**驗證 IDP Metadata：**
```bash
curl http://localhost:8443/realms/saml-demo/protocol/saml/descriptor
```

### 問題 4: 屬性未傳遞

**檢查：**
1. Client Scopes → Mappers 是否正確配置
2. 用戶是否有對應的屬性值
3. Spring Boot 日誌中是否收到屬性

**解決：**
- 重新配置 Attribute Mappers
- 確認 User Attributes 已設定
- 檢查 SAML 斷言內容（DEBUG 日誌）

---

## 重要端點

### Keycloak IDP

| 端點 | URL |
|------|-----|
| 管理控制台 | http://localhost:8443/admin |
| Realm 資訊 | http://localhost:8443/realms/saml-demo |
| SAML Metadata | http://localhost:8443/realms/saml-demo/protocol/saml/descriptor |
| SAML SSO | http://localhost:8443/realms/saml-demo/protocol/saml |

### Spring Boot SP

| 端點 | URL |
|------|-----|
| SP Metadata | http://localhost:8080/saml2/service-provider-metadata/keycloak |
| ACS (Assertion Consumer) | http://localhost:8080/login/saml2/sso/keycloak |
| SLO (Single Logout) | http://localhost:8080/logout/saml2/slo |

---

## 相關資源

### 官方文檔
- [Keycloak 官方網站](https://www.keycloak.org/)
- [Keycloak SAML 文檔](https://www.keycloak.org/docs/latest/server_admin/#_saml)
- [Keycloak 下載](https://www.keycloak.org/downloads)

### 專案文檔
- [快速啟動指南](QUICKSTART.md)
- [SAML SLO 設定](SAML_SLO.md)
- [主要 README](../README.md)

---

**配置完成後，請參考 [快速啟動指南](QUICKSTART.md) 進行測試！** 🚀
