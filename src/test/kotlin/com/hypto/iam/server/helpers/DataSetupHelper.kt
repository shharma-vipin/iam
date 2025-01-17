package com.hypto.iam.server.helpers

import com.google.gson.Gson
import com.hypto.iam.server.configs.AppConfig
import com.hypto.iam.server.db.Tables.CREDENTIALS
import com.hypto.iam.server.db.Tables.USERS
import com.hypto.iam.server.db.repositories.CredentialsRepo
import com.hypto.iam.server.db.repositories.OrganizationRepo
import com.hypto.iam.server.db.repositories.UserRepo
import com.hypto.iam.server.db.tables.pojos.Users
import com.hypto.iam.server.models.Action
import com.hypto.iam.server.models.AdminUser
import com.hypto.iam.server.models.CreateActionRequest
import com.hypto.iam.server.models.CreateOrganizationRequest
import com.hypto.iam.server.models.CreateOrganizationResponse
import com.hypto.iam.server.models.CreateResourceRequest
import com.hypto.iam.server.models.Credential
import com.hypto.iam.server.models.Resource
import com.hypto.iam.server.utils.ActionHrn
import com.hypto.iam.server.utils.IdGenerator
import com.hypto.iam.server.utils.ResourceHrn
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import org.koin.test.inject
import org.koin.test.junit5.AutoCloseKoinTest

object DataSetupHelper : AutoCloseKoinTest() {
    private val gson = Gson()
    private val appConfig: AppConfig.Config by inject()
    private val rootToken = appConfig.app.secretKey

    private val organizationRepo: OrganizationRepo by inject()
    private val userRepo: UserRepo by inject()
    private val credentialRepo: CredentialsRepo by inject()

    fun createOrganization(
        engine: TestApplicationEngine
    ): Pair<CreateOrganizationResponse, AdminUser> {
        with(engine) {
            // Create organization
            val orgName = "test-org" + IdGenerator.randomId()
            val userName = "test-user" + IdGenerator.randomId()
            val testEmail = "test-email" + IdGenerator.randomId() + "@hypto.in"
            val testPhone = "+919626012778"
            val testPassword = "testPassword@Hash1"

            val adminUser = AdminUser(
                username = userName,
                passwordHash = testPassword,
                email = testEmail,
                phone = testPhone
            )

            val createOrganizationCall = handleRequest(HttpMethod.Post, "/organizations") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader("X-Api-Key", rootToken)
                setBody(
                    gson.toJson(
                        CreateOrganizationRequest(
                            orgName,
                            adminUser
                        )
                    )
                )
            }

            val createdOrganizationResponse = gson
                .fromJson(createOrganizationCall.response.content, CreateOrganizationResponse::class.java)

            return Pair(createdOrganizationResponse, adminUser)
        }
    }

    fun createResource(
        orgId: String,
        userCredential: Credential,
        engine: TestApplicationEngine,
        resourceName: String? = null
    ): Resource {
        with(engine) {
            val name = resourceName ?: ("test-resource" + IdGenerator.randomId())

            val createResourceCall = handleRequest(HttpMethod.Post, "/organizations/$orgId/resources") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.Authorization, "Bearer ${userCredential.secret}")
                setBody(gson.toJson(CreateResourceRequest(name = name)))
            }

            return gson
                .fromJson(createResourceCall.response.content, Resource::class.java)
        }
    }

    fun createAction(
        orgId: String,
        resource: Resource? = null,
        userCredential: Credential,
        engine: TestApplicationEngine
    ): Pair<Action, Resource> {
        with(engine) {
            val createdResource = resource ?: createResource(orgId, userCredential, engine)
            val actionName = "test-action" + IdGenerator.randomId()

            val createActionCall =
                handleRequest(HttpMethod.Post, "/organizations/$orgId/resources/${createdResource.name}/actions") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    addHeader(HttpHeaders.Authorization, "Bearer ${userCredential.secret}")
                    setBody(gson.toJson(CreateActionRequest(name = actionName)))
                }

            val createdAction = gson
                .fromJson(createActionCall.response.content, Action::class.java)

            return Pair(createdAction, createdResource)
        }
    }

    fun createResourceActionHrn(
        orgId: String,
        accountId: String?,
        resourceName: String,
        actionName: String,
        resourceInstance: String? = null
    ): Pair<String, String> {
        val resourceHrn = ResourceHrn(orgId, accountId, resourceName, resourceInstance).toString()
        val actionHrn = ActionHrn(orgId, accountId, resourceName, actionName).toString()
        return Pair(resourceHrn, actionHrn)
    }

    // This function is used for cleaning up all the data created during the test for the organization
    fun deleteOrganization(orgId: String, engine: TestApplicationEngine) {
        with(engine) {
            val organization = organizationRepo.findById(orgId)
            organization?.let {
                userRepo.fetch(USERS.ORGANIZATION_ID, orgId).filterNotNull().forEach { user ->
                    cleanupCredentials(user, it.adminUserHrn)
                    organizationRepo.deleteById(orgId)
                }
            }
        }
    }

    private fun cleanupCredentials(user: Users, adminUserHrn: String) {
        credentialRepo.fetch(CREDENTIALS.USER_HRN, user.hrn).forEach { credentials ->
            credentialRepo.delete(credentials)
        }
        // if no users created, directly delete credentials for admin user
        credentialRepo.deleteByUserHRN(adminUserHrn)
    }
}
