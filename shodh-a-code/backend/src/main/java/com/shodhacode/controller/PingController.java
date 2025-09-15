package com.shodhacode.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple ping endpoint used to verify that controllers are scanned and mapped.
 */
@RestController
public class PingController {
    @GetMapping("/api/ping")
    public String ping() {
        return "pong";
    }
}
