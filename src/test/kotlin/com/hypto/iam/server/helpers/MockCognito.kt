package com.hypto.iam.server.helpers

import io.mockk.coEvery
import io.mockk.mockk
import java.time.Instant
import org.koin.test.KoinTest
import org.koin.test.mock.declareMock
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.AddCustomAttributesRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDisableUserRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminRespondToAuthChallengeRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolClientRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolClientResponse
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolResponse
import software.amazon.awssdk.services.cognitoidentityprovider.model.DeleteUserPoolRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.DeleteUserPoolResponse
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolClientType
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolType
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType

fun KoinTest.mockCognitoClient() {
    declareMock<CognitoIdentityProviderClient> {
        coEvery { this@declareMock.createUserPool(any<CreateUserPoolRequest>()) } coAnswers {
            val result = CreateUserPoolResponse.builder()
                .userPool(UserPoolType.builder().id("").name("").build())
                .build()
            result
        }
        coEvery { this@declareMock.createUserPoolClient(any<CreateUserPoolClientRequest>()) } coAnswers {
            CreateUserPoolClientResponse.builder()
                .userPoolClient(UserPoolClientType.builder().clientId("").build())
                .build()
        }
        coEvery { this@declareMock.deleteUserPool(any<DeleteUserPoolRequest>()) } returns DeleteUserPoolResponse
            .builder().build()
        coEvery { this@declareMock.adminGetUser(any<AdminGetUserRequest>()) } coAnswers {
            AdminGetUserResponse.builder()
                .enabled(true)
                .userAttributes(listOf())
                .username("")
                .userCreateDate(Instant.now())
                .build()
        }
        coEvery { this@declareMock.adminDisableUser(any<AdminDisableUserRequest>()) } returns mockk()
        coEvery { this@declareMock.adminCreateUser(any<AdminCreateUserRequest>()) } coAnswers {
            AdminCreateUserResponse.builder()
                .user(
                    UserType.builder().attributes(listOf())
                    .username("")
                    .userCreateDate(Instant.now())
                    .build())
                .build()
        }
        coEvery { this@declareMock.adminInitiateAuth(any<AdminInitiateAuthRequest>()) } coAnswers {
            AdminInitiateAuthResponse.builder()
                .session("").build()
        }
        coEvery { this@declareMock.adminRespondToAuthChallenge(
            any<AdminRespondToAuthChallengeRequest>()) } returns mockk()
        coEvery { this@declareMock.listUsers(any<ListUsersRequest>()) } returns mockk()
        coEvery { this@declareMock.adminDeleteUser(any<AdminDeleteUserRequest>()) } returns mockk()
        coEvery { this@declareMock.addCustomAttributes(any<AddCustomAttributesRequest>()) } returns mockk()
    }
}