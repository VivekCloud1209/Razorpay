package com.example.RazorpayAppliction.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class SSLRedirectController {

    @Value("${server.port:8443}")
    private int httpsPort;

    @GetMapping("/redirect")
    public String redirectToHttps(HttpServletRequest request) {
        if (!request.isSecure()) {
            String httpsUrl = "https://" + request.getServerName() + ":" + httpsPort + request.getRequestURI();
            return "redirect:" + httpsUrl;
        }
        return "index";
    }
}