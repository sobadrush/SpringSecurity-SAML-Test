package com.example.saml.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首頁控制器
 * 
 * 處理應用程式的主要頁面導航
 */
@Controller
public class HomeController {

    /**
     * 首頁 - 未認證時可訪問
     * 
     * @return 首頁視圖
     */
    @GetMapping({"/", "/home"})
    public String home() {
        return "home";
    }

    /**
     * 用戶頁面 - 需要認證
     * 顯示已認證用戶的資訊和 SAML 斷言屬性
     * 
     * @param principal SAML2 認證主體
     * @param model Spring MVC Model
     * @return 用戶頁面視圖
     */
    @GetMapping("/user")
    public String user(@AuthenticationPrincipal Saml2AuthenticatedPrincipal principal, Model model) {
        if (principal != null) {
            // 將用戶名稱加入模型
            model.addAttribute("username", principal.getName());
            
            // 將所有 SAML 屬性加入模型
            model.addAttribute("attributes", principal.getAttributes());
            
            // 將第一個 SAML 斷言加入模型（如果存在）
            if (!principal.getRelyingPartyRegistrationId().isEmpty()) {
                model.addAttribute("registrationId", principal.getRelyingPartyRegistrationId());
            }
        }
        
        return "user";
    }

    /**
     * 登入錯誤頁面
     * 
     * @param model Spring MVC Model
     * @return 錯誤頁面視圖
     */
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("error", true);
        return "login-error";
    }

    /**
     * 一般錯誤頁面
     * 
     * @return 錯誤頁面視圖
     */
    @GetMapping("/error")
    public String error() {
        return "error";
    }
}
