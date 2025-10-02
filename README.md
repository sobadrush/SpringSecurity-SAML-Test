# 🔐 Spring Security SAML2 認證示範專案

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Required-blue.svg)](https://www.docker.com/)
[![Keycloak](https://img.shields.io/badge/Keycloak-23.0-blue.svg)](https://www.keycloak.org/)

> **完整的企業級 SAML2 單一登入（SSO）解決方案**  
> 使用 Spring Security SAML2 + Keycloak，支援 Single Sign-On (SSO) 和 Single Logout (SLO)

---

## 🚀 五分鐘快速啟動

```bash
# 1. 啟動 Keycloak (Identity Provider)
docker-compose up -d

# 2. 啟動 Spring Boot 應用 (Service Provider)
mvn spring-boot:run

# 3. 開啟瀏覽器訪問
http://localhost:8080

# 4. 使用測試帳號登入
使用者: testuser
密碼: password123
```

✅ **自動配置完成** - Realm、測試用戶和 SAML Client 已預先設定！  
📖 **需要詳細步驟？** 請參閱 [**快速啟動指南** →](docs/QUICKSTART.md)

---

## 📖 文檔導航

本專案提供完整的文檔，請根據您的需求選擇：

### 🎯 快速入門
- **[快速啟動指南](docs/QUICKSTART.md)** - 從零到運行的完整步驟（含測試與排查）
- **[Keycloak 設定指南](docs/KEYCLOAK_SETUP.md)** - Keycloak IDP 的詳細配置說明

### 🔧 進階功能
- **[SAML Single Logout (SLO) 指南](docs/SAML_SLO.md)** - 完整的 SLO 配置、測試和排查

### 📚 技術文檔
- [專案簡介](#專案簡介) - 了解專案功能和架構
- [系統架構](#技術架構) - 技術棧說明
- [重要端點](#重要端點) - API 和服務端點總覽

---

## 📋 專案簡介

本專案是一個**完整可運行**的 Spring Security SAML2 認證示範，展示企業級單一登入（SSO）的實作。

### ✨ 核心功能

- ✅ **SAML2 單一登入 (SSO)** - 一次登入，多應用存取
- ✅ **SAML2 單一登出 (SLO)** - 統一登出，清除所有 session
- ✅ **Keycloak 作為 IDP** - 開源、功能完整的 Identity Provider
- ✅ **用戶屬性傳遞** - 自動取得並顯示 SAML 屬性
- ✅ **Metadata 自動生成** - SP 和 IDP Metadata 完整支援
- ✅ **Docker 一鍵部署** - 簡化開發環境設定
- ✅ **完整錯誤處理** - 友善的錯誤頁面和日誌

---

## 🏗️ 技術架構

### Service Provider (SP) - Spring Boot 應用

| 技術 | 版本 | 用途 |
|------|------|------|
| **Java** | 17+ | 開發語言 |
| **Spring Boot** | 3.2.0 | 應用框架 |
| **Spring Security** | 6.x | 安全框架 |
| **Spring Security SAML2** | - | SAML2 Service Provider |
| **Thymeleaf** | - | 前端模板引擎 |
| **Maven** | 3.6+ | 構建工具 |

### Identity Provider (IDP) - Keycloak

| 技術 | 版本 | 用途 |
|------|------|------|
| **Keycloak** | 23.0 | 開源 IAM/SSO 解決方案 |
| **Docker** | Latest | 容器化部署 |
| **Docker Compose** | Latest | 服務編排 |

### 為什麼選擇 Keycloak？

- ✅ **完全開源免費** - Apache 2.0 授權
- ✅ **內建 SAML2 支援** - 無需額外插件
- ✅ **企業級功能** - 支援 SSO, SLO, 多租戶
- ✅ **易於管理** - Web 管理介面
- ✅ **活躍社群** - 豐富的文檔和範例

📖 **詳細說明**: [Keycloak 設定指南](docs/KEYCLOAK_SETUP.md)

---

## 📁 專案結構

```
SpringSecurity-SAML-Test/
├── docs/                          📚 完整文檔
│   ├── QUICKSTART.md             - 快速啟動指南
│   ├── KEYCLOAK_SETUP.md         - Keycloak 設定
│   └── SAML_SLO.md               - SAML SLO 指南
├── src/main/
│   ├── java/com/example/saml/
│   │   ├── SamlApplication.java     - 主應用程式
│   │   ├── config/
│   │   │   └── SecurityConfig.java  - Spring Security 配置
│   │   └── controller/
│   │       └── HomeController.java  - 控制器
│   └── resources/
│       ├── application.yml          - 應用配置（含 SAML 設定）
│       ├── saml/                    - SAML 憑證目錄（不在版本控制中）
│       └── templates/               - Thymeleaf 模板
│           ├── home.html           - 首頁
│           ├── user.html           - 用戶資訊頁面
│           ├── error.html          - 錯誤頁面
│           └── login-error.html    - 登入錯誤頁
├── keycloak/
│   └── realm-export.json            - Keycloak Realm 配置（自動匯入）
├── docker-compose.yml               - Docker Compose 配置
├── pom.xml                          - Maven 專案配置
└── README.md                        - 本文件（專案入口）
```

---

## 🎯 系統需求

### 必要環境

| 軟體 | 版本要求 | 檢查指令 |
|------|---------|---------|
| **Java JDK** | 17+ | `java -version` |
| **Maven** | 3.6+ | `mvn -version` |
| **Docker** | Latest | `docker --version` |
| **Docker Compose** | Latest | `docker-compose --version` |

### 硬體需求

- **記憶體**: 至少 2GB 可用 RAM
- **磁碟空間**: 至少 1GB 可用空間
- **網路**: 需要網路連線下載 Docker 映像

---

## 🚀 安裝與啟動

### 步驟 1: 啟動 Keycloak

```bash
docker-compose up -d
```

⏱️ **等待時間**: 首次啟動約 60-90 秒  
✅ **自動配置**: Realm、用戶和 Client 會自動匯入

### 步驟 2: 啟動 Spring Boot

```bash
mvn spring-boot:run
```

🌐 **訪問**: http://localhost:8080

📖 **需要詳細步驟？** → [完整啟動指南](docs/QUICKSTART.md)

---

## 🧪 測試與驗證

### 預配置的測試帳號

| 用戶名 | 密碼 | 角色 | Email | 部門 |
|--------|------|------|-------|------|
| **testuser** | password123 | user | testuser@example.com | Engineering |
| **admin** | admin123 | admin, user | admin@example.com | Administration |

### 快速測試流程

1. **訪問首頁**: http://localhost:8080
2. **點擊登入** → 重導向到 Keycloak
3. **輸入帳號**: testuser / password123
4. **查看結果** → 顯示用戶資訊和 SAML 屬性
5. **測試登出** → 執行 SAML Single Logout

📊 **完整測試檢查清單** → [快速啟動指南 - 測試章節](docs/QUICKSTART.md#完整測試流程)

---

## � 重要端點

### Spring Boot (Service Provider)

| 端點 | URL |
|------|-----|
| 🏠 **首頁** | http://localhost:8080/ |
| 👤 **用戶頁面** | http://localhost:8080/user |
| 📄 **SP Metadata** | http://localhost:8080/saml2/service-provider-metadata/keycloak |
| 🔐 **SSO 端點** | http://localhost:8080/login/saml2/sso/keycloak |
| 🚪 **SLO 端點** | http://localhost:8080/logout/saml2/slo |

### Keycloak (Identity Provider)

| 端點 | URL | 帳號密碼 |
|------|-----|---------|
| 🎛️ **管理控制台** | http://localhost:8443/admin | admin / admin |
| 🌐 **Realm 資訊** | http://localhost:8443/realms/saml-demo | - |
| 📄 **IDP Metadata** | http://localhost:8443/realms/saml-demo/protocol/saml/descriptor | - |

---

## ⚙️ 核心配置

### Spring Boot 配置 (`application.yml`)

```yaml
spring:
  security:
    saml2:
      relyingparty:
        registration:
          keycloak:
            # IDP Metadata URL（自動載入 IDP 配置）
            assertingparty:
              metadata-uri: http://localhost:8443/realms/saml-demo/protocol/saml/descriptor
            
            # SP Entity ID
            entity-id: http://localhost:8080/saml2/service-provider-metadata/keycloak
            
            # ACS - 接收 SAML 回應的端點
            acs:
              location: http://localhost:8080/login/saml2/sso/keycloak
            
            # SLO - 登出端點（支援 SAML Single Logout）
            singlelogout:
              url: http://localhost:8080/logout/saml2/slo
```

### Keycloak 配置

✅ **自動配置** - 使用 Realm 匯入功能：
- 📄 **配置檔案**: `keycloak/realm-export.json`
- 👥 **包含內容**: Realm、測試用戶、SAML Client、屬性映射
- 🚀 **啟動即用**: Docker Compose 啟動時自動匯入

🔧 **進階配置** → [Keycloak 設定指南](docs/KEYCLOAK_SETUP.md)

---

## � 常見問題與排除

### ❓ Keycloak 無法啟動

```bash
# 檢查容器狀態
docker ps -a

# 查看錯誤日誌
docker logs keycloak-idp

# 完全重置
docker-compose down -v
docker-compose up -d
```

### ❓ SAML 認證失敗

**快速檢查清單：**
- [ ] Keycloak 是否正常運行？ → `curl http://localhost:8443/realms/saml-demo`
- [ ] 測試帳號是否正確？ → testuser / password123
- [ ] Spring Boot 是否已啟動？ → 檢查控制台日誌
- [ ] 網路連線是否正常？

### ❓ SAML SLO 登出失敗

詳細排查步驟請參考：[SAML SLO 故障排除指南](docs/SAML_SLO.md#問題排查)

### 📖 更多問題？

請參閱完整的故障排除指南：
- [快速啟動指南 - 故障排除](docs/QUICKSTART.md#故障排除)
- [Keycloak 設定 - 疑難排解](docs/KEYCLOAK_SETUP.md#疑難排解)

---

## 🔄 SAML 流程說明

### Single Sign-On (SSO) 流程

```
1. 用戶訪問 /user (受保護資源)
   ↓
2. Spring Security 檢測到未認證
   ↓
3. 重導向到 Keycloak 登入頁面
   ↓
4. 用戶輸入帳號密碼
   ↓
5. Keycloak 驗證身份並產生 SAML 斷言
   ↓
6. 瀏覽器 POST SAML 回應到 SP 的 ACS 端點
   ↓
7. Spring Security 驗證 SAML 簽章和斷言
   ↓
8. 建立 Session，重導向到用戶頁面
```

### Single Logout (SLO) 流程

```
1. 用戶點擊登出按鈕
   ↓
2. SP 發送 LogoutRequest 到 IDP
   ↓
3. IDP 清除 session 並發送 LogoutResponse
   ↓
4. SP 接收 LogoutResponse 並清除本地 session
   ↓
5. 重導向到首頁，顯示登出成功訊息
```

📖 **詳細流程圖** → [SAML SLO 指南 - 流程說明](docs/SAML_SLO.md#slo-流程說明)

---

## 📚 參考資源

### 官方文檔
- [Spring Security SAML2](https://docs.spring.io/spring-security/reference/servlet/saml2/index.html)
- [Keycloak 官方網站](https://www.keycloak.org/)
- [Keycloak SAML 配置](https://www.keycloak.org/docs/latest/server_admin/#_saml)
- [SAML 2.0 規範](http://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html)

### 專案文檔
- 📖 [快速啟動指南](docs/QUICKSTART.md)
- 🔧 [Keycloak 設定](docs/KEYCLOAK_SETUP.md)
- 🚪 [SAML SLO 指南](docs/SAML_SLO.md)

---

## � 安全性說明

### SAML 憑證管理

- ⚠️ **不在版本控制中**: `src/main/resources/saml/` 目錄下的私鑰和憑證已加入 `.gitignore`
- 🔑 **開發環境**: 使用自簽憑證（僅供測試）
- 🏢 **生產環境**: 必須使用 CA 簽發的正式憑證
- 🔐 **金鑰管理**: 建議使用金鑰管理系統（如 AWS KMS、Azure Key Vault）

### HTTP vs HTTPS

- ⚠️ **目前配置**: Keycloak 使用 HTTP 和 dev 模式
- 🏢 **生產環境要求**: 必須啟用 HTTPS
- 📖 **配置指南** → [Keycloak 設定 - HTTPS 配置](docs/KEYCLOAK_SETUP.md#進階設定)

---

## ⚠️ 注意事項

1. 本專案僅供**學習和開發測試**使用
2. 測試帳號密碼為明文，不適用於生產環境
3. 生產環境必須啟用 HTTPS 和憑證驗證
4. Keycloak 管理員密碼應使用強密碼並透過環境變數設定
5. 定期更新依賴套件以修補安全漏洞

---

## 📝 授權與貢獻

- **授權**: 本專案僅供學習測試使用
- **貢獻**: 歡迎提交 Issue 和 Pull Request
- **開發日期**: 2025年10月

---

## 💬 支援與幫助

### 遇到問題？

1. 📖 查看 [快速啟動指南](docs/QUICKSTART.md) 的故障排除章節
2. 🔍 檢查應用程式日誌（終端機輸出）
3. 🐋 檢查 Keycloak 日誌：`docker logs -f keycloak-idp`
4. 🔧 參考 [Keycloak 設定指南](docs/KEYCLOAK_SETUP.md) 的疑難排解

### 需要進階功能？

- 🚪 **SAML Single Logout** → [SLO 完整指南](docs/SAML_SLO.md)
- 🔐 **Keycloak 深入配置** → [Keycloak 設定](docs/KEYCLOAK_SETUP.md)
- 🎯 **自訂屬性映射** → [Keycloak 設定 - 手動配置](docs/KEYCLOAK_SETUP.md#手動配置步驟)

---

## 🎉 專案特色

- ✅ **開箱即用** - Docker Compose 一鍵啟動
- ✅ **自動配置** - Realm、用戶、Client 預先設定
- ✅ **完整文檔** - 從快速啟動到進階配置
- ✅ **企業級** - 支援 SSO 和 SLO
- ✅ **易於理解** - 清晰的程式碼結構和註解

---

**享受 SAML2 認證開發！** 🚀 | Made with ❤️
