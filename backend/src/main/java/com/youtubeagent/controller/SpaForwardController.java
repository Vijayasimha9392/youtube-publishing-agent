package com.youtubeagent.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardController {

    @RequestMapping(value = {
            "/settings",
            "/queue",
            "/library",
            "/published",
            "/scheduler",
            "/logs"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
