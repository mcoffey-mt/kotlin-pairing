package com.example.pairingdemo

import com.example.pairingdemo.config.ExternalBooksApiProperties
import com.example.pairingdemo.models.Book
import com.example.pairingdemo.models.BookDTO
import org.apache.tomcat.util.json.JSONParser
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@SpringBootApplication
@EnableConfigurationProperties(ExternalBooksApiProperties::class)
class PairingDemoApplication

fun main(args: Array<String>) {
    runApplication<PairingDemoApplication>(*args)
}

@RestController
class ApiController(val externalBooksApi: ExternalBooksApiProperties) {
    var books = mutableListOf(
        Book(id = "1", title = "Clean Architecture", author = "Robert Martin"),
        Book(id = "2", title = "Pragmatic Programmer", author = "David Thomas")
    )

    @GetMapping("/books")
    fun index(): List<Book> = books

    @GetMapping("/books/{id}")
    fun bookById(@PathVariable id: String) = books.first { it.id == id }

    @PostMapping("/books")
    fun createBook(@RequestBody book: Book) = books.add(book)

    @GetMapping("/externalBooks/{id}")
    fun externalBook(@PathVariable id: String): Book {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${externalBooksApi.host}/books/$id"))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 404) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND, "External book with the ID: $id was not found."
            )
        }

        val externalBook = JSONParser(response.body()).parseObject();

        var bookDTO = BookDTO(
            externalBook["id"].toString(),
            externalBook["name"].toString(),
            externalBook["author"].toString(),
            externalBook["available"] as Boolean
        )

        return bookDTO.toBook();
    }
}
