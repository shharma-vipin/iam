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
package com.hypto.iam.server.models

import java.io.Serializable
/**
 *
 * @param id
 * @param status
 * @param secret
 * @param validUntil
 */
data class Credential(
    val id: kotlin.String,
    val status: Credential.Status,
    val secret: kotlin.String,
    val validUntil: kotlin.String? = null
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 123
    }
    /**
    *
    * Values: active,inactive
    */
    enum class Status(val value: kotlin.String) {
        active("active"),
        inactive("inactive");
    }
}
