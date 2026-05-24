package com.seungjig.shoppingmall.global;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DevViewController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
