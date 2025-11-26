package com.example.RazorpayAppliction.controller;

//package com.example.razorpaydemo.controller;

import com.example.RazorpayAppliction.config.RazorpayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @Autowired
    private RazorpayConfig razorpayConfig;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("razorpayKeyId", razorpayConfig.getKeyId());
        return "index";
    }

    @GetMapping("/success")
    public String success() {
        return "success";
    }

    @GetMapping("/failure")
    public String failure() {
        return "failure";
    }
}
