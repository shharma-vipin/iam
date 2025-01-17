package com.hypto.iam.server.apis

import com.google.gson.Gson
import com.hypto.iam.server.handleRequest
import com.hypto.iam.server.helpers.AbstractContainerBaseTest
import com.hypto.iam.server.helpers.DataSetupHelper
import com.hypto.iam.server.models.AdminUser
import com.hypto.iam.server.models.CreateOrganizationRequest
import com.hypto.iam.server.models.CreateOrganizationResponse
import com.hypto.iam.server.models.Organization
import com.hypto.iam.server.utils.IdGenerator
import io.ktor.application.Application
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.server.testing.contentType
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlin.test.assertFalse
import kotlin.text.Charsets.UTF_8
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class OrganizationApiKtTest : AbstractContainerBaseTest() {
    private val gson = Gson()

    @Test
    fun `create organization with valid root credentials`() {

        withTestApplication(Application::handleRequest) {
            val orgName = "test-org" + IdGenerator.randomId()
            val userName = "test-user" + IdGenerator.randomId()
            val testEmail = "test-user-email" + IdGenerator.randomId() + "@hypto.in"
            val testPhone = "+919626012778"
            val testPassword = "testPassword@Hash1"
            lateinit var orgId: String
            val requestBody = CreateOrganizationRequest(
                orgName,
                AdminUser(userName, testPassword, testEmail, testPhone)
            )
            with(
                handleRequest(HttpMethod.Post, "/organizations") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader("X-Api-Key", rootToken)
                    setBody(gson.toJson(requestBody))
                }
            ) {
                val responseBody = gson.fromJson(response.content, CreateOrganizationResponse::class.java)
                assertEquals(HttpStatusCode.Created, response.status())
                assertEquals(ContentType.Application.Json.withCharset(UTF_8), response.contentType())

                orgId = responseBody.organization!!.id
                assertEquals(requestBody.name, responseBody.organization!!.name)
                assertEquals(10, responseBody.organization!!.id.length)
            }

            DataSetupHelper.deleteOrganization(orgId, this)
        }
    }

    @Test
    fun `create organization with invalid root credentials`() {
        withTestApplication(Application::handleRequest) {
            val orgName = "test-org" + IdGenerator.randomId()
            val userName = "test-user" + IdGenerator.randomId()
            val testEmail = "test-user-email" + IdGenerator.randomId() + "@hypto.in"
            val testPhone = "+919626012778"
            val testPassword = "testPassword@Hash1"
            with(
                handleRequest(HttpMethod.Post, "/organizations") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader("X-Api-Key", "bad creds")
                    setBody(
                        gson.toJson(
                            CreateOrganizationRequest(
                                orgName,
                                AdminUser(userName, testPassword, testEmail, testPhone)
                            )
                        )
                    )
                }
            ) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
                assertFalse(response.headers.contains(HttpHeaders.ContentType))
                assertEquals(null, response.content)
            }
        }
    }

    @Test
    fun `get organization with invalid credentials`() {
        withTestApplication(Application::handleRequest) {
            val orgName = "test-org" + IdGenerator.randomId()
            val userName = "test-user" + IdGenerator.randomId()
            val testEmail = "test-user-email" + IdGenerator.randomId() + "@hypto.in"
            val testPhone = "+919626012778"
            val testPassword = "testPassword@Hash1"
            val createOrganizationCall = handleRequest(HttpMethod.Post, "/organizations") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader("X-Api-Key", rootToken)
                setBody(
                    gson.toJson(
                        CreateOrganizationRequest(
                            orgName,
                            AdminUser(userName, testPassword, testEmail, testPhone)
                        )
                    )
                )
            }
            val createdOrganization =
                gson.fromJson(createOrganizationCall.response.content, CreateOrganizationResponse::class.java)

            with(
                handleRequest(HttpMethod.Get, "/organizations/${createdOrganization.organization!!.id}") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader(HttpHeaders.Authorization, "Bearer test-bearer-token")
                }
            ) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
                assertFalse(response.headers.contains(HttpHeaders.ContentType))
                assertEquals(null, response.content)
            }

            DataSetupHelper.deleteOrganization(createdOrganization.organization!!.id, this)
        }
    }

    @Test
    fun `get organization success`() {
        withTestApplication(Application::handleRequest) {
            val orgName = "test-org" + IdGenerator.randomId()
            val userName = "test-user" + IdGenerator.randomId()
            val testEmail = "test-user-email" + IdGenerator.randomId() + "@hypto.in"
            val testPhone = "+919626012778"
            val testPassword = "testPassword@Hash1"
            val createOrganizationCall = handleRequest(HttpMethod.Post, "/organizations") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader("X-Api-Key", rootToken)
                setBody(
                    gson.toJson(
                        CreateOrganizationRequest(
                            orgName,
                            AdminUser(userName, testPassword, testEmail, testPhone)
                        )
                    )
                )
            }
            val createdOrganization =
                gson.fromJson(createOrganizationCall.response.content, CreateOrganizationResponse::class.java)

            with(
                handleRequest(HttpMethod.Get, "/organizations/${createdOrganization.organization!!.id}") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader(HttpHeaders.Authorization, "Bearer ${createdOrganization.adminUserCredential!!.secret}")
                }

            ) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(ContentType.Application.Json.withCharset(UTF_8), response.contentType())

                val fetchedOrganization = gson.fromJson(response.content, Organization::class.java)
                assertEquals(createdOrganization.organization, fetchedOrganization)
            }

            DataSetupHelper.deleteOrganization(createdOrganization.organization!!.id, this)
        }
    }

    @Test
    fun `get organization not found`() {
        withTestApplication(Application::handleRequest) {
            val orgName = "test-org" + IdGenerator.randomId()
            val userName = "test-user" + IdGenerator.randomId()
            val testEmail = "test-user-email" + IdGenerator.randomId() + "@hypto.in"
            val testPhone = "+919626012778"
            val testPassword = "testPassword@Hash1"
            // Create organization
            val createOrganizationCall = handleRequest(HttpMethod.Post, "/organizations") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader("X-Api-Key", rootToken)
                setBody(
                    gson.toJson(
                        CreateOrganizationRequest(
                            orgName,
                            AdminUser(userName, testPassword, testEmail, testPhone)
                        )
                    )
                )
            }
            val createdOrganization =
                gson.fromJson(createOrganizationCall.response.content, CreateOrganizationResponse::class.java)

            with(
                handleRequest(HttpMethod.Get, "/organizations/inValidOrganizationId") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader(
                        HttpHeaders.Authorization,
                        "Bearer ${createdOrganization.adminUserCredential!!.secret}"
                    )
                }

            ) {
                // These assertions
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }

            DataSetupHelper.deleteOrganization(createdOrganization.organization!!.id, this)
        }
    }
}
