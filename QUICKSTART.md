# 🚀 快速啟動指南

## 系統需求

- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- 至少 2GB 可用記憶體

## 一鍵啟動

### 1. 生成 SAML 金鑰和憑證

**⚠️ 首次使用必須執行此步驟**：

```bash
# 在專案根目錄執行
./generate-saml-keys.sh
```

這會生成開發用的 SAML 私鑰和憑證。這些檔案不包含在版本控制中以確保安全。

### 2. 啟動 Keycloak IDP

```bash
# 在專案根目錄執行
docker-compose up -d

# 查看啟動狀態
docker logs -f keycloak-idp

# 等待看到類似以下訊息，表示啟動完成：
# "Added user 'admin' to realm 'master'"
# "Imported realm 'saml-demo' from file"
```

**首次啟動約需 60-90 秒**（需要下載映像和啟動 Keycloak）

### 3. 驗證 Keycloak 運行

訪問 Keycloak 管理控制台：
- URL: http://localhost:8443
- 管理員帳號: `admin`
- 管理員密碼: `admin`

驗證 SAML IDP Metadata：
- URL: http://localhost:8443/realms/saml-demo/protocol/saml/descriptor
- 應該看到 XML 格式的 SAML metadata

### 4. 啟動 Spring Boot 應用

```bash
# 方式 1: 使用 Maven直接執行
mvn spring-boot:run

# 方式 2: 編譯後執行
mvn clean package
java -jar target/spring-security-saml-test-1.0.0-SNAPSHOT.jar
```

應用程式將在 **http://localhost:8080** 啟動

### 5. 開始測試

1. **訪問首頁**: http://localhost:8080

2. **點擊「開始使用 SAML 登入」**
   - 自動重導向到 Keycloak 登入頁面

3. **使用測試帳號登入**:
   - 使用者: `testuser`
   - 密碼: `password123`
   
   或
   
   - 使用者: `admin`
   - 密碼: `admin123`

4. **查看認證結果**
   - 登入成功後自動返回應用程式
   - 顯示用戶資訊和 SAML 屬性

5. **測試登出**
   - 點擊「登出」按鈕
   - 返回首頁

## 預期的完整流程

```
使用者訪問 http://localhost:8080
  ↓
點擊「開始使用 SAML 登入」
  ↓
重導向到 Keycloak 登入頁面
  ↓
輸入 testuser / password123
  ↓
Keycloak 驗證身份並產生 SAML 斷言
  ↓
自動返回 Spring Boot 應用（http://localhost:8080/user）
  ↓
顯示用戶資訊和 SAML 屬性
  ↓
點擊「登出」
  ↓
返回首頁
```

## 重要端點

### Keycloak (IDP)
- 管理控制台: http://localhost:8443
- SAML Metadata: http://localhost:8443/realms/saml-demo/protocol/saml/descriptor
- Realm: `saml-demo`

### Spring Boot 應用 (SP)
- 首頁: http://localhost:8080
- 用戶頁面: http://localhost:8080/user（需要認證）
- SP Metadata: http://localhost:8080/saml2/service-provider-metadata/keycloak

## 故障排除

### 問題 1: Keycloak 無法啟動

```bash
# 檢查容器狀態
docker ps -a

# 查看日誌
docker logs keycloak-idp

# 完全重置
docker-compose down -v
docker-compose up -d
```

### 問題 2: "Cannot connect to Keycloak"

**原因**: Keycloak 尚未完全啟動

**解決**:
```bash
# 檢查 Keycloak 健康狀態
curl http://localhost:8443/health/ready

# 如果返回 200 OK，表示已就緒
```

### 問題 3: SAML 認證失敗

**檢查清單**:
1. ✅ Keycloak 是否正常運行
2. ✅ Realm `saml-demo` 是否已匯入
3. ✅ 測試用戶是否已創建
4. ✅ Client 是否正確配置

**驗證步驟**:
```bash
# 1. 檢查 Realm
curl http://localhost:8443/realms/saml-demo

# 2. 檢查 Metadata
curl http://localhost:8443/realms/saml-demo/protocol/saml/descriptor

# 3. 登入 Keycloak 管理控制台確認配置
```

### 問題 4: Port 已被佔用

```bash
# 檢查 8080 和 8443 port
lsof -i :8080
lsof -i :8443

# 修改 port（如果需要）
# 在 docker-compose.yml 中修改 ports 映射
# 在 application.yml 中修改 server.port
```

## 清理與重置

### 停止所有服務

```bash
# 停止 Spring Boot（按 Ctrl+C）

# 停止 Keycloak
docker-compose down

# 完全清理（包含 volumes 和資料）
docker-compose down -v
rm -rf keycloak-data/  # 如果有建立持久化資料
```

### 重新開始

```bash
# 清理編譯產物
mvn clean

# 重新啟動
docker-compose up -d
mvn spring-boot:run
```

## 成功指標

✅ **Keycloak 就緒**:
- 容器狀態: `healthy`
- 管理控制台可訪問
- Metadata 端點返回 XML

✅ **Spring Boot 啟動成功**:
- 沒有錯誤訊息
- 首頁可訪問
- SP Metadata 可生成

✅ **SAML 流程正常**:
- 未認證時訪問 /user 會重導向
- 輸入正確帳密後能成功登入
- 能看到用戶資訊和屬性
- 登出功能正常

## 進階配置

### 查看完整的 SAML 斷言

在 application.yml 中已啟用 DEBUG 日誌：
```yaml
logging:
  level:
    org.springframework.security.saml2: DEBUG
```

查看應用程式日誌可以看到完整的 SAML 交互過程。

### 新增更多測試用戶

1. 登入 Keycloak 管理控制台
2. 選擇 `saml-demo` Realm
3. 進入 Users → Add user
4. 設定用戶名、Email、屬性
5. 在 Credentials 標籤設定密碼

### 修改 SAML 屬性

修改 `keycloak/realm-export.json` 中的 `protocolMappers` 配置，然後重新匯入 Realm。

## 測試帳號資訊

| 使用者名稱 | 密碼 | Email | 角色 | 部門 |
|-----------|------|-------|------|------|
| testuser | password123 | testuser@example.com | user | Engineering |
| admin | admin123 | admin@example.com | admin, user | Administration |

## 下一步

- 📖 閱讀完整的 [README.md](README.md)
- 🔧 查看 [KEYCLOAK_ALTERNATIVE.md](KEYCLOAK_ALTERNATIVE.md) 了解更多配置選項
- 🧪 參考 [TESTING.md](TESTING.md) 進行詳細測試
- 📝 查看 [progress.md](progress.md) 了解開發歷程

---

**祝您測試順利！** 🎉

如有問題，請查看日誌：
- Keycloak: `docker logs -f keycloak-idp`
- Spring Boot: 查看終端機輸出
