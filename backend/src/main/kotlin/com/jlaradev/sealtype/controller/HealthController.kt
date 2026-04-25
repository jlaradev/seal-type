package com.jlaradev.sealtype.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.sql.DataSource

@RestController
class HealthController(private val dataSource: DataSource) {

    @GetMapping("/api/health")
    fun checkHealth() = try {

        dataSource.connection.use { connection ->

            val metaData = connection.metaData

            mapOf(
                "status" to "UP",
                "server_time" to LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "database" to mapOf(
                    "name" to connection.catalog,
                    "product" to metaData.databaseProductName,
                    "version" to metaData.databaseProductVersion
                ),
                "message" to "Conexión exitosa"
            )

        }

    } catch (e: Exception) {

        mapOf(
            "status" to "DOWN",
            "error" to (e.message ?: "Error de conexion")
        )

    }

}