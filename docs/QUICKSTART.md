# 🚀 快速啟動指南

> **從零到運行的完整步驟，包含測試與排查**

## 📋 目錄

- [系統需求](#系統需求)
- [快速啟動步驟](#快速啟動步驟)
- [完整測試流程](#完整測試流程)
- [驗證檢查清單](#驗證檢查清單)
- [故障排除](#故障排除)
- [測試帳號資訊](#測試帳號資訊)

---

## 系統需求

### 必要環境
- **Java 17+** - JDK 開發環境
- **Maven 3.6+** - 專案構建工具
- **Docker & Docker Compose** - 容器化部署
- **至少 2GB 可用記憶體** - Keycloak 需求

### 驗證環境

```bash
# 檢查 Java 版本
java -version

# 檢查 Maven 版本
mvn -version

# 檢查 Docker 版本
docker --version
docker-compose --version
```

---

## 快速啟動步驟

### 步驟 1: 啟動 Keycloak IDP

```bash
# 在專案根目錄執行
cd d:\Program_Projects\Java_workspace\SpringSecurity-SAML-Test
docker-compose up -d

# 查看啟動狀態
docker logs -f keycloak-idp
```

**等待啟動完成的標誌：**
```
✅ "Added user 'admin' to realm 'master'"
✅ "Imported realm 'saml-demo' from file"
✅ "Keycloak ... started"
```

⏱️ **首次啟動約需 60-90 秒**（需要下載映像和初始化）

### 步驟 2: 驗證 Keycloak 運行

```bash
# 測試 Keycloak 健康狀態
curl http://localhost:8443/realms/saml-demo

# 如果返回 JSON，表示 Keycloak 已就緒
```

**Web 驗證：**
- **Keycloak 首頁**: http://localhost:8443
- **SAML IDP Metadata**: http://localhost:8443/realms/saml-demo/protocol/saml/descriptor

**管理控制台（可選）：**
- URL: http://localhost:8443/admin
- 帳號: `admin`
- 密碼: `admin`

### 步驟 3: 啟動 Spring Boot 應用

```bash
# 方式 1: 使用 Maven 直接執行（推薦開發時使用）
mvn spring-boot:run

# 方式 2: 編譯後執行（推薦生產環境）
mvn clean package
java -jar target/spring-security-saml-test-1.0.0-SNAPSHOT.jar
```

**啟動成功標誌：**
```
✅ "Started SamlApplication in X seconds"
✅ "Tomcat started on port(s): 8080"
```

應用程式將在 **http://localhost:8080** 啟動

---

## 完整測試流程

### 測試 1: 訪問首頁

```bash
# 方式 1: 使用瀏覽器
打開 http://localhost:8080

# 方式 2: 使用命令列
curl http://localhost:8080
```

**預期結果：**
- ✅ 看到歡迎頁面
- ✅ 顯示系統功能說明
- ✅ 有測試帳號資訊
- ✅ 顯示【開始使用 SAML 登入】按鈕

### 測試 2: 觸發 SAML 登入

**方式 1: 透過首頁**
1. 點擊【開始使用 SAML 登入】按鈕
2. 自動重導向到 Keycloak 登入頁面

**方式 2: 直接訪問受保護資源**
1. 直接訪問 http://localhost:8080/user
2. 自動重導向到 Keycloak 登入頁面

### 測試 3: 使用測試帳號登入

在 Keycloak 登入頁面輸入：

**選項 1: 一般用戶**
- 使用者: `testuser`
- 密碼: `password123`

**選項 2: 管理員**
- 使用者: `admin`
- 密碼: `admin123`

### 測試 4: 查看認證結果

**預期結果：**
1. ✅ 自動返回應用程式（http://localhost:8080/user）
2. ✅ 顯示「SAML 認證成功！」訊息
3. ✅ 顯示用戶名稱
4. ✅ 顯示 SAML 屬性列表（username, email, firstName, lastName, 等）
5. ✅ 顯示【返回首頁】和【登出】按鈕

**SAML 屬性範例：**
```
username: testuser
email: testuser@example.com
firstName: Test
lastName: User
department: Engineering
employeeNumber: 12345
roles: user
```

### 測試 5: 測試登出功能

1. 在用戶頁面點擊【登出 (SAML SLO)】按鈕
2. 觸發 SAML Single Logout 流程

**預期結果（成功）：**
- ✅ 重導向到首頁 (http://localhost:8080/)
- ✅ URL 包含 `?logout=success` 參數
- ✅ 顯示綠色的「登出成功」訊息
- ✅ Session 被清除

**驗證登出：**
```bash
# 嘗試再次訪問用戶頁面
打開 http://localhost:8080/user

# 預期：自動重導向到 Keycloak 登入頁面（需重新登入）
```

### 測試 6: 驗證 IDP Session 清除

1. 登出後，開新瀏覽器分頁
2. 訪問 http://localhost:8080
3. 點擊【登入】

**預期結果：**
- ✅ Keycloak 要求重新輸入帳號密碼
- ✅ 證明 IDP session 已被清除

**異常情況：**
- ❌ 直接登入成功（表示 IDP session 未清除）
- 👉 請參考 [SAML SLO 指南](SAML_SLO.md)

---

## 驗證檢查清單

### ✅ Keycloak (IDP) 檢查

- [ ] **容器運行狀態**
  ```bash
  docker ps | findstr keycloak  # Windows
  docker ps | grep keycloak     # Linux/Mac
  ```

- [ ] **Keycloak 首頁可訪問**
  - http://localhost:8443

- [ ] **SAML IDP Metadata 可訪問**
  - http://localhost:8443/realms/saml-demo/protocol/saml/descriptor
  - 應該返回 XML 格式的 metadata

- [ ] **Realm 已匯入**
  - 登入管理控制台確認 `saml-demo` realm 存在

- [ ] **測試用戶已創建**
  - 在 Keycloak Admin → Users 中確認 `testuser` 和 `admin` 存在

### ✅ Spring Boot 應用 (SP) 檢查

- [ ] **應用程式正常啟動**
  - 控制台沒有紅色 ERROR 訊息

- [ ] **首頁可以訪問**
  - http://localhost:8080/

- [ ] **SP Metadata 可以生成**
  - http://localhost:8080/saml2/service-provider-metadata/keycloak
  - 應該返回 XML 格式的 metadata
  - 應該包含 `<md:SingleLogoutService>` 標籤

- [ ] **日誌級別正確**
  - 應該看到 DEBUG 級別的 SAML 相關日誌

### ✅ SAML 流程檢查

- [ ] **未認證時的重導向**
  - 訪問 /user 會自動重導向到 Keycloak

- [ ] **登入流程**
  - 輸入正確帳密後能成功登入
  - 沒有 SAML 錯誤訊息

- [ ] **認證後的資料**
  - 能看到用戶名稱
  - 能看到 SAML 屬性列表
  - 屬性值正確

- [ ] **登出流程**
  - SAML SLO 正常執行
  - SP Session 被清除
  - IDP Session 被清除

---

## 故障排除

### 問題 1: Keycloak 無法啟動

**症狀：**
- 容器啟動後立即停止
- Docker logs 顯示錯誤

**檢查步驟：**
```bash
# 1. 查看容器狀態
docker ps -a

# 2. 查看詳細日誌
docker logs keycloak-idp

# 3. 檢查 port 是否被佔用
netstat -ano | findstr :8443  # Windows
lsof -i :8443                  # Linux/Mac
```

**解決方案：**
```bash
# 方案 1: 完全重置
docker-compose down -v
docker-compose up -d

# 方案 2: 釋放 port（如果被佔用）
# 找到佔用 port 的進程並終止

# 方案 3: 修改 port
# 編輯 docker-compose.yml，將 8443 改為其他 port
```

### 問題 2: "Cannot connect to Keycloak"

**症狀：**
- Spring Boot 啟動時報連線錯誤
- 無法取得 IDP Metadata

**原因：** Keycloak 尚未完全啟動

**解決方案：**
```bash
# 1. 等待 Keycloak 完全啟動
docker logs -f keycloak-idp

# 2. 檢查健康狀態
curl http://localhost:8443/realms/saml-demo

# 3. 如果返回 JSON，表示已就緒，重新啟動 Spring Boot
```

### 問題 3: SAML 認證失敗

**症狀：**
- 登入後返回錯誤頁面
- 日誌顯示 SAML validation 錯誤

**檢查清單：**
1. ✅ Keycloak 是否正常運行
2. ✅ Realm `saml-demo` 是否已匯入
3. ✅ 測試用戶是否已創建
4. ✅ Client 是否正確配置
5. ✅ SP Entity ID 是否匹配

**驗證步驟：**
```bash
# 1. 檢查 Realm
curl http://localhost:8443/realms/saml-demo

# 2. 檢查 IDP Metadata
curl http://localhost:8443/realms/saml-demo/protocol/saml/descriptor

# 3. 檢查 SP Metadata
curl http://localhost:8080/saml2/service-provider-metadata/keycloak

# 4. 查看詳細日誌
# 在 Spring Boot 控制台查看 DEBUG 日誌
```

**常見錯誤：**
- `Invalid Assertion` - 檢查時間同步
- `Unknown Principal` - 檢查用戶是否存在
- `Signature Validation Failed` - 檢查證書配置

### 問題 4: 無法看到 SAML 屬性

**症狀：**
- 登入成功但屬性列表為空
- 只顯示 username

**解決方案：**
1. 登入 Keycloak Admin Console
2. 選擇 `saml-demo` realm
3. 進入 Clients → 選擇 SP client
4. 檢查 "Client Scopes" 和 "Mappers"
5. 確認屬性映射正確配置

**重新匯入 Realm：**
```bash
# 1. 停止 Keycloak
docker-compose down

# 2. 確認 realm-export.json 配置正確

# 3. 重新啟動
docker-compose up -d
```

### 問題 5: Port 已被佔用

**症狀：**
- Docker 或 Spring Boot 無法啟動
- 錯誤訊息：`Address already in use`

**檢查 Port 佔用：**
```bash
# Windows
netstat -ano | findstr :8080
netstat -ano | findstr :8443

# Linux/Mac
lsof -i :8080
lsof -i :8443
```

**解決方案：**
- **方案 1**: 終止佔用 port 的進程
- **方案 2**: 修改應用程式使用的 port

```yaml
# 修改 application.yml
server:
  port: 8090  # 改為其他 port

# 修改 docker-compose.yml
ports:
  - "8444:8080"  # 改為其他 port
```

### 問題 6: SAML SLO 失敗

**症狀：**
- 點擊登出後顯示錯誤頁面
- IDP session 未被清除

**解決方案：**
請參考詳細的 **[SAML SLO 故障排除指南](SAML_SLO.md#問題排查)**

---

## 清理與重置

### 停止所有服務

```bash
# 1. 停止 Spring Boot
# 在運行的終端機按 Ctrl+C

# 2. 停止 Keycloak
docker-compose down

# 3. 完全清理（包含 volumes 和資料）
docker-compose down -v
```

### 重新開始

```bash
# 1. 清理編譯產物
mvn clean

# 2. 重新啟動 Keycloak
docker-compose up -d

# 3. 等待 Keycloak 啟動完成
timeout /t 45  # Windows
sleep 45       # Linux/Mac

# 4. 重新啟動 Spring Boot
mvn spring-boot:run
```

---

## 測試帳號資訊

### 預配置的測試帳號

| 使用者名稱 | 密碼 | 角色 | Email | 部門 | 員工編號 |
|-----------|------|------|-------|------|---------|
| **testuser** | password123 | user | testuser@example.com | Engineering | 12345 |
| **admin** | admin123 | admin, user | admin@example.com | Administration | 00001 |

### Keycloak 管理員帳號

| 用途 | 帳號 | 密碼 |
|------|------|------|
| Keycloak Admin Console | admin | admin |

---

## 重要端點總覽

### Keycloak (IDP)
| 端點 | URL | 說明 |
|------|-----|------|
| 管理控制台 | http://localhost:8443/admin | 管理 Realm、用戶、Client |
| SAML Metadata | http://localhost:8443/realms/saml-demo/protocol/saml/descriptor | IDP 的 SAML 配置資訊 |
| Realm 資訊 | http://localhost:8443/realms/saml-demo | Realm 基本資訊（JSON） |

### Spring Boot 應用 (SP)
| 端點 | URL | 說明 |
|------|-----|------|
| 首頁 | http://localhost:8080 | 公開訪問 |
| 用戶頁面 | http://localhost:8080/user | 需要認證 |
| SP Metadata | http://localhost:8080/saml2/service-provider-metadata/keycloak | SP 的 SAML 配置資訊 |
| 登出端點 | http://localhost:8080/logout | POST 請求觸發 SAML SLO |

---

## 成功指標

### ✅ 完整的測試流程應該包括：

1. ✅ Keycloak 正常啟動並可訪問
2. ✅ Spring Boot 應用正常啟動
3. ✅ Metadata 端點可以正常訪問（IDP 和 SP）
4. ✅ 未認證時訪問 /user 會重導向到 Keycloak
5. ✅ 輸入正確帳密後能成功登入
6. ✅ 登入後能看到用戶名稱和 SAML 屬性
7. ✅ SAML SLO 登出功能正常運作
8. ✅ 登出後需要重新登入

### 📊 預期的完整流程

```
1. 使用者訪問 http://localhost:8080
   ↓
2. 點擊【開始使用 SAML 登入】
   ↓
3. 重導向到 Keycloak 登入頁面
   ↓
4. 輸入 testuser / password123
   ↓
5. Keycloak 驗證身份並產生 SAML 斷言
   ↓
6. 自動返回 Spring Boot 應用
   ↓
7. 顯示用戶資訊和 SAML 屬性（/user）
   ↓
8. 點擊【登出 (SAML SLO)】
   ↓
9. 執行 SAML Single Logout
   ↓
10. 返回首頁，顯示登出成功訊息
```

---

## Debug 模式

### 查看完整的 SAML 交互過程

在 `application.yml` 中已啟用 DEBUG 日誌：

```yaml
logging:
  level:
    root: INFO
    org.springframework.security: DEBUG
    org.springframework.security.saml2: DEBUG
```

### 關鍵日誌關鍵字

**登入流程：**
- `Saml2WebSsoAuthenticationFilter` - SSO 認證過濾器
- `OpenSaml4AuthenticationProvider` - SAML 斷言驗證
- `Saml2Authentication` - 認證成功

**登出流程：**
- `OpenSaml4LogoutRequestResolver` - 建立 LogoutRequest
- `Saml2LogoutRequestFilter` - 發送 LogoutRequest
- `Saml2LogoutResponseFilter` - 處理 LogoutResponse

---

## 下一步

完成快速啟動後，您可以：

1. 📖 **深入了解 SAML SLO** - 參考 [SAML_SLO.md](SAML_SLO.md)
2. 🔧 **配置 Keycloak** - 參考 [KEYCLOAK_SETUP.md](KEYCLOAK_SETUP.md)
3. 🏠 **回到主文檔** - 參考 [README.md](../README.md)
4. 🆕 **新增測試用戶** - 在 Keycloak Admin Console 中操作
5. 🔐 **自訂 SAML 屬性** - 修改 Keycloak Client Mappers

---

**祝測試順利！** 🎉

如有任何問題，請參考本文檔的故障排除章節，或查看詳細的 SAML SLO 指南。
