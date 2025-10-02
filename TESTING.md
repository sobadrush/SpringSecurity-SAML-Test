# 🧪 測試指南

## 快速測試步驟

### 1. 啟動 CAS Server (IDP)

```bash
# 啟動 Docker Compose
docker-compose up -d

# 等待約 30-60 秒讓 CAS Server 完全啟動
docker logs -f cas-server

# 當看到類似 "Started CasWebApplication" 的訊息時，表示啟動完成
```

驗證 CAS Server 是否正常運行：
- 訪問: http://localhost:8443/cas/login
- 應該能看到 CAS 登入頁面

### 2. 啟動 Spring Boot 應用 (SP)

```bash
# 方式 1: 使用 Maven
mvn spring-boot:run

# 方式 2: 編譯後執行
mvn clean package
java -jar target/spring-security-saml-test-1.0.0-SNAPSHOT.jar
```

### 3. 測試 SAML 認證流程

1. **訪問首頁**
   - URL: http://localhost:8080/
   - 應該看到歡迎頁面

2. **觸發 SAML 登入**
   - 點擊「開始使用 SAML 登入」
   - 或直接訪問: http://localhost:8080/user
   
3. **CAS 登入**
   - 自動重導向到 CAS 登入頁面: http://localhost:8443/cas/login
   - 輸入測試帳號:
     - 使用者: `casuser`
     - 密碼: `Mellon`
   
4. **查看認證結果**
   - 登入成功後自動返回應用程式
   - 顯示用戶資訊頁面（/user）
   - 可以看到 SAML 屬性資訊

5. **測試登出**
   - 點擊「登出」按鈕
   - 應該返回首頁

## 驗證檢查清單

### ✅ CAS Server 檢查

- [ ] CAS Server 容器正常運行
  ```bash
  docker ps | grep cas-server
  ```

- [ ] CAS 登入頁面可以訪問
  - http://localhost:8443/cas/login

- [ ] SAML IDP Metadata 可以訪問
  - http://localhost:8443/cas/idp/metadata
  - 應該返回 XML 格式的 metadata

### ✅ Spring Boot 應用檢查

- [ ] 應用程式正常啟動（沒有錯誤）

- [ ] 首頁可以訪問
  - http://localhost:8080/

- [ ] SP Metadata 可以生成
  - http://localhost:8080/saml2/service-provider-metadata/cas
  - 應該返回 XML 格式的 metadata

### ✅ SAML 流程檢查

- [ ] 訪問 /user 會自動重導向到 CAS 登入頁

- [ ] 輸入正確帳號密碼後能成功登入

- [ ] 登入後能看到用戶資訊和 SAML 屬性

- [ ] 登出功能正常

## 常見問題排除

### 問題 1: CAS Server 無法啟動

**症狀**: 容器啟動後立即停止

**檢查**:
```bash
docker logs cas-server
```

**可能原因**:
- Port 8443 被佔用
- Docker 記憶體不足
- 配置檔案錯誤

**解決方案**:
```bash
# 檢查 port 是否被佔用
lsof -i :8443

# 完全重置
docker-compose down -v
docker-compose up -d
```

### 問題 2: SAML Metadata 404

**症狀**: 訪問 metadata 端點返回 404

**可能原因**:
- CAS Server SAML IDP 功能未啟用
- URL 路徑錯誤

**檢查**:
```bash
# 查看 CAS 日誌
docker logs cas-server | grep -i saml
```

### 問題 3: 登入後返回錯誤

**症狀**: CAS 登入成功，但返回 SP 時出現錯誤

**檢查清單**:
1. 查看 Spring Boot 日誌
2. 檢查 SAML 斷言是否正確
3. 確認 SP Entity ID 是否匹配

**調整日誌級別**:
在 application.yml 中已設定 DEBUG 級別，查看詳細日誌。

### 問題 4: 無法訪問用戶屬性

**症狀**: 登入成功但屬性為空

**檢查**:
1. CAS Server 的服務註冊配置
2. 屬性釋放策略（Attribute Release Policy）

**修改**: `cas-server/services/SpringBoot-SAML-SP-1000.json`

## 測試用帳號

| 使用者 | 密碼 | 用途 |
|--------|------|------|
| casuser | Mellon | 預設測試帳號 |
| testuser | password123 | 一般用戶測試 |
| admin | admin123 | 管理員測試 |

## Debug 模式

應用程式已啟用 DEBUG 日誌，可以看到詳細的 SAML 交互過程：

```
org.springframework.security: DEBUG
org.springframework.security.saml2: DEBUG
```

關鍵日誌關鍵字：
- `SAML` - SAML 相關操作
- `Authentication` - 認證流程
- `Assertion` - SAML 斷言處理
- `Metadata` - Metadata 生成和解析

## 測試成功標準

✅ 完整的測試流程應該包括：

1. ✅ CAS Server 正常啟動並可訪問
2. ✅ Spring Boot 應用正常啟動
3. ✅ Metadata 端點可以正常訪問（IDP 和 SP）
4. ✅ 未認證時訪問 /user 會重導向到 CAS
5. ✅ 輸入正確帳密後能成功登入
6. ✅ 登入後能看到用戶名稱
7. ✅ 能看到 SAML 屬性列表
8. ✅ 登出功能正常運作

## 清理與重置

### 停止所有服務

```bash
# 停止 Spring Boot
# 按 Ctrl+C 停止

# 停止 Docker
docker-compose down

# 完全清理（包含 volumes）
docker-compose down -v
```

### 重新開始

```bash
# 清理編譯產物
mvn clean

# 重新啟動所有服務
docker-compose up -d
mvn spring-boot:run
```

---

**祝測試順利！** 🎉
