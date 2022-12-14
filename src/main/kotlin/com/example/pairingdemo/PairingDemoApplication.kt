package com.example.pairingdemo

import com.example.pairingdemo.models.Book
import com.example.pairingdemo.models.BookDTO
import org.apache.tomcat.util.json.JSONParser
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.reflect.typeOf

@SpringBootApplication
class PairingDemoApplication

fun main(args: Array<String>) {
    runApplication<PairingDemoApplication>(*args)
}

@RestController
class ApiController {
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

    @GetMapping("/externalBook")
    fun something(): Book {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://simple-books-api.glitch.me/books/1"))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val externalBook = JSONParser(response.body()).parseObject();

        var bookDTO = BookDTO(externalBook["id"].toString(), externalBook["name"].toString(), externalBook["author"].toString(), externalBook["available"] as Boolean)

        return bookDTO.toBook();
    }
}
