package com.gy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author gy
 */
@RestController
@RequestMapping("index")
public class IndexController {

    @GetMapping
    public String index() {
        return "hello world";
    }

}
