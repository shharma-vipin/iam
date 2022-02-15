package com.hypto.iam.server.db.repositories

import com.hypto.iam.server.db.tables.pojos.Credentials
import com.hypto.iam.server.db.tables.records.CredentialsRecord
import java.util.Optional
import java.util.UUID
import org.jooq.impl.DAOImpl

object CredentialsRepo : DAOImpl<CredentialsRecord, Credentials, UUID>(
    com.hypto.iam.server.db.tables.Credentials.CREDENTIALS,
    Credentials::class.java,
    com.hypto.iam.server.service.DatabaseFactory.getConfiguration()
) {
    override fun getId(credentials: Credentials): UUID {
        return credentials.id
    }

    /**
     * Fetch a unique record that has `id = value`
     */
    fun fetchOneById(value: UUID): Credentials? {
        return fetchOne(com.hypto.iam.server.db.tables.Credentials.CREDENTIALS.ID, value)
    }

    /**
     * Fetch a unique record that has `id = value`
     */
    fun fetchOptionalById(value: UUID): Optional<Credentials> {
        return fetchOptional(com.hypto.iam.server.db.tables.Credentials.CREDENTIALS.ID, value)
    }

    /**
     * Fetch records that have `user_hrn = value`
     */
    fun fetchByUserId(value: String): List<Credentials> {
        return fetch(com.hypto.iam.server.db.tables.Credentials.CREDENTIALS.USER_HRN, value)
    }

    fun fetchByRefreshToken(refreshToken: String): Credentials? {
        return ctx().selectFrom(table).where(
            com.hypto.iam.server.db.tables.Credentials.CREDENTIALS.REFRESH_TOKEN.eq(refreshToken)
        ).fetchOne(mapper())
    }
}