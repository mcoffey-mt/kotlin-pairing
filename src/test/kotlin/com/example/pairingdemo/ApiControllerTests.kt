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
    describe("GET /externalBook") {
        val externalBooksApi = WireMockServer(9000)
        listener(WireMockListener(externalBooksApi, ListenerMode.PER_SPEC))

        beforeTest { externalBooksApi.start() }

        afterTest { externalBooksApi.stop() }

        it("returns 200 with book") {
            val book = mapOf("id" to 1, "name" to "The Batman", "author" to "Bruce Wayne", "available" to true)

            externalBooksApi.stubFor(
                WireMock.get(WireMock.urlEqualTo("/books/1"))
                    .willReturn(WireMock.okJson(JSONObject(book).toString()))
            )

            val result = mockMvc.perform(get("/externalBook")).andReturn()

            result.response.status.shouldBe(200)
            JSONObject(result.response.contentAsString)["id"].shouldBe("1")
            JSONObject(result.response.contentAsString)["title"].shouldBe("The Batman")
            JSONObject(result.response.contentAsString)["author"].shouldBe("Bruce Wayne")
        }
    }
})