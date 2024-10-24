package com.demo.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Description:
 * @Date: 2024/10/22 15:23
 */
@RestController
@RequestMapping("/cors")
public class CorsController {

    @GetMapping("/getInfo")
    @CrossOrigin(origins = "http://127.0.0.1:5500", maxAge = 3600, methods = RequestMethod.GET, allowedHeaders = "Content-Type")
    public Map<String, Object> getCorsConfig(HttpServletResponse response) {
        System.out.println("收到请求了");
        return Map.of(
                "allowOrigins", "*",
                "allowMethods", "GET, POST, PUT, DELETE, OPTIONS",
                "allowHeaders", "Content-Type, Authorization",
                "allowCredentials", "true",
                "maxAge", "3600"
        );
    }

    @GetMapping("/jsonp")
    public String getJsonp(String callback) {
        System.out.println("收到请求了aaaa");
//        return "alert(666)";  // 仿佛服务器在调用客户端
        return callback + "('hello world')";
    }

}
