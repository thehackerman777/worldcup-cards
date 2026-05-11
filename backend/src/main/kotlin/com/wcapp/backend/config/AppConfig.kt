package com.wcapp.backend.config

import com.wcapp.backend.security.UserPrincipal
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.web.config.EnableSpringDataWebSupport
import org.springframework.security.core.annotation.AuthenticationPrincipal

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
class AppConfig {

    @Bean
    fun authenticationPrincipalArgumentResolver(): Any {
        // Spring Boot auto-configures this when using @AuthenticationPrincipal
        return Object()
    }
}
