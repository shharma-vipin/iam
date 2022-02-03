/**
* Hypto IAM
* APIs for Hypto IAM Service.
*
* The version of the OpenAPI document: 1.0.0
* Contact: engineering@hypto.in
*
* NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
* https://openapi-generator.tech
* Do not edit the class manually.
*/
package org.hypto.iam.apis

import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import org.hypto.iam.Paths
import io.ktor.locations.*
import io.ktor.routing.*
import org.hypto.iam.infrastructure.ApiPrincipal
import org.hypto.iam.models.ErrorResponse
import org.hypto.iam.models.Model401Response

@KtorExperimentalLocationsAPI
fun Route.TokenApi() {
    val gson = Gson()
    val empty = mutableMapOf<String, Any?>()

    authenticate("refresh_token") {
    post<Paths.getToken> {
        val principal = call.authentication.principal<UserIdPrincipal>()!!
        
        call.respond(HttpStatusCode.NotImplemented)
    }
    }

}