package com.example.pairingdemo.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "api.external-books")
data class ExternalBooksApiProperties(
    val host: String,
)
