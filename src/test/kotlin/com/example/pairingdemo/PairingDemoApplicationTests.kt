package com.example.pairingdemo

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.wiremock.ListenerMode
import io.kotest.extensions.wiremock.WireMockListener
import io.kotest.matchers.shouldBe
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

@SpringBootTest
@AutoConfigureMockMvc
class PairingDemoApplicationTests(@Autowired val mockMvc: MockMvc) : DescribeSpec({
    describe("GET /externalBooks/{id}") {
        val externalBooksApi = WireMockServer(9000)
        val id = 1
        val book = mapOf("id" to id, "name" to "The Batman", "author" to "Bruce Wayne", "available" to true)

        beforeTest {
            listener(WireMockListener(externalBooksApi, ListenerMode.PER_SPEC))
            externalBooksApi.start()
        }

        afterTest { externalBooksApi.stop() }

        describe("when an external book is successfully retrieved") {
            beforeTest {
                externalBooksApi.stubFor(
                    WireMock.get(WireMock.urlEqualTo("/books/$id"))
                        .willReturn(WireMock.okJson(JSONObject(book).toString()))
                )
            }

            it("responds with 200 status") {
                val result = mockMvc.perform(MockMvcRequestBuilders.get("/externalBooks/$id")).andReturn()

                result.response.status.shouldBe(200)
            }

            it("returns a book with the matching ID") {
                val result = mockMvc.perform(MockMvcRequestBuilders.get("/externalBooks/$id")).andReturn()

                JSONObject(result.response.contentAsString)["id"].shouldBe("$id")
                JSONObject(result.response.contentAsString)["title"].shouldBe("The Batman")
                JSONObject(result.response.contentAsString)["author"].shouldBe("Bruce Wayne")
            }
        }

        describe("when an external book is not found") {
            beforeTest {
                externalBooksApi.stubFor(
                    WireMock.get(WireMock.urlEqualTo("/books/$id"))
                        .willReturn(WireMock.notFound())
                )
            }

            it("responds with 404 status") {
                val result = mockMvc.perform(MockMvcRequestBuilders.get("/externalBooks/$id")).andReturn()

                result.response.status.shouldBe(404)
            }
        }
    }
})
