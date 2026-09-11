package com.gamevault.shared.security;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/csrf")
public class CsrfController {

    @GetMapping
    public CsrfToken obterToken(
            CsrfToken csrfToken
    ) {
        return csrfToken;
    }
}
