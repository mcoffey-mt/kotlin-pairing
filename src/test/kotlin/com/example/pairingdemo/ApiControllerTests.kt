package com.example.pairingdemo

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.wiremock.ListenerMode
import io.kotest.extensions.wiremock.WireMockListener
import io.kotest.matchers.shouldBe
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@WebMvcTest
@TestConfiguration
class ApiControllerTests(@Autowired val mockMvc: MockMvc) : DescribeSpec({
    describe("GET /externalBooks/{id}") {
        val externalBooksApi = WireMockServer(9000)
        listener(WireMockListener(externalBooksApi, ListenerMode.PER_SPEC))
        val id = 1
        val book = mapOf("id" to 1, "name" to "The Batman", "author" to "Bruce Wayne", "available" to true)

        beforeTest {
            externalBooksApi.start()

            externalBooksApi.stubFor(
                WireMock.get(WireMock.urlEqualTo("/books/$id"))
                    .willReturn(WireMock.okJson(JSONObject(book).toString()))
            )
        }

        afterTest { externalBooksApi.stop() }

        it("responds with 200 status") {
            val result = mockMvc.perform(get("/externalBooks/$id")).andReturn()

            result.response.status.shouldBe(200)
        }

        it("returns a book with the matching ID") {
            val result = mockMvc.perform(get("/externalBooks/$id")).andReturn()

            JSONObject(result.response.contentAsString)["id"].shouldBe("$id")
            JSONObject(result.response.contentAsString)["title"].shouldBe("The Batman")
            JSONObject(result.response.contentAsString)["author"].shouldBe("Bruce Wayne")
        }
    }
})