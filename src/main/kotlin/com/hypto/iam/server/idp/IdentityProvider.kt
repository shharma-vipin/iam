package com.hypto.iam.server.idp

/**
 * Identity provider interface to manage users
 */
interface IdentityProvider {
    enum class IdentitySource { AWS_COGNITO }
    suspend fun createIdentityGroup(
        name: String,
        configuration: Configuration = Configuration(PasswordPolicy())
    ): IdentityGroup
    suspend fun deleteIdentityGroup(identityGroup: IdentityGroup)
    suspend fun createUser(
        context: RequestContext,
        identityGroup: IdentityGroup,
        userCredentials: UserCredentials
    ): User

    /**
     * Gets user details for the given username. If no user available with the username this will throw Exception
     */
    suspend fun getUser(identityGroup: IdentityGroup, userName: String): User
    suspend fun updateUser(
        identityGroup: IdentityGroup,
        userName: String,
        email: String,
        phone: String,
        status: com.hypto.iam.server.models.User.Status?
    ): User
    suspend fun listUsers(identityGroup: IdentityGroup, pageToken: String?, limit: Int?): Pair<List<User>, NextToken?>
    suspend fun deleteUser(identityGroup: IdentityGroup, userName: String)
    suspend fun getIdentitySource(): IdentitySource
    suspend fun authenticate(identityGroup: IdentityGroup, userName: String, password: String): User
}

typealias NextToken = String
class UnsupportedCredentialsException(message: String) : Exception(message)
class UserNotFoundException(message: String) : Exception(message)
class UserAlreadyExistException(message: String) : Exception(message)

data class IdentityGroup(
    val id: String,
    val name: String,
    val identitySource: IdentityProvider.IdentitySource,
    val metadata: Map<String, String> = mapOf()
)
data class User(
    val username: String,
    val phoneNumber: String,
    val email: String,
    val loginAccess: Boolean,
    val isEnabled: Boolean,
    val createdBy: String,
    val createdAt: String
)
abstract class UserCredentials {
    abstract val userName: String
}
data class PasswordCredentials(
    override val userName: String,
    val email: String,
    val phoneNumber: String,
    val password: String
) : UserCredentials()
data class AccessTokenCredentials(
    override val userName: String,
    val email: String,
    val phoneNumber: String,
    val accessToken: String
) : UserCredentials()
data class RequestContext(val organizationId: String, val requestedPrincipal: String)
