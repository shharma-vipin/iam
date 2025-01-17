package com.hypto.iam.server.db.repositories

import com.hypto.iam.server.db.tables.AuditEntries.AUDIT_ENTRIES
import com.hypto.iam.server.db.tables.pojos.AuditEntries
import com.hypto.iam.server.db.tables.records.AuditEntriesRecord
import com.hypto.iam.server.service.DatabaseFactory.getConfiguration
import com.hypto.iam.server.utils.Hrn
import com.hypto.iam.server.utils.HrnFactory
import com.hypto.iam.server.utils.ResourceHrn
import java.time.LocalDateTime
import java.util.UUID
import mu.KotlinLogging
import org.jooq.Result
import org.jooq.impl.DAOImpl
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object AuditEntriesRepo : KoinComponent, DAOImpl<AuditEntriesRecord, AuditEntries, UUID>(
    AUDIT_ENTRIES,
    AuditEntries::class.java,
    getConfiguration()
) {
    private val logger = KotlinLogging.logger { }
    private val hrnFactory by inject<HrnFactory>()

    override fun getId(action: AuditEntries): UUID {
        return action.id
    }

    fun fetchByPrincipalAndTime(
        principalOrg: String,
        principal: Hrn,
        eventTimeStart: LocalDateTime,
        eventTimeEnd: LocalDateTime
    ): Result<AuditEntriesRecord> {
        return ctx()
            .selectFrom(table)
            .where(AUDIT_ENTRIES.PRINCIPAL_ORGANIZATION.eq(principalOrg))
            .and(AUDIT_ENTRIES.PRINCIPAL.eq(principal.toString()))
            .and(AUDIT_ENTRIES.EVENT_TIME.ge(eventTimeStart))
            .and(AUDIT_ENTRIES.EVENT_TIME.ge(eventTimeEnd))
            .fetch()
    }

    fun fetchByResourceAndTime(
        resource: Hrn,
        eventTimeStart: LocalDateTime,
        eventTimeEnd: LocalDateTime,
        operations: List<Hrn>?
    ): Result<AuditEntriesRecord> {
        var query = ctx()
            .selectFrom(table)
            .where(AUDIT_ENTRIES.RESOURCE.eq(resource.toString()))
            .and(AUDIT_ENTRIES.EVENT_TIME.ge(eventTimeStart))
            .and(AUDIT_ENTRIES.EVENT_TIME.ge(eventTimeEnd))
        if (operations != null) {
            query = query.and(AUDIT_ENTRIES.OPERATION.`in`(operations.map { it.toString() }))
        }

        return query.fetch()
    }

    fun batchInsert(auditEntries: List<AuditEntries>): Boolean {
        val batchBindStep = ctx().batch(
            ctx()
                .insertInto(
                    AUDIT_ENTRIES,
                    AUDIT_ENTRIES.REQUEST_ID,
                    AUDIT_ENTRIES.EVENT_TIME,
                    AUDIT_ENTRIES.PRINCIPAL,
                    AUDIT_ENTRIES.PRINCIPAL_ORGANIZATION,
                    AUDIT_ENTRIES.RESOURCE,
                    AUDIT_ENTRIES.OPERATION
                    // No meta arguments at the moment
                )
                .values(null as String?, null, null, null, null, null)
        )
        auditEntries.forEach {
            val principalHrn: ResourceHrn = hrnFactory.getHrn(it.principal) as ResourceHrn
            batchBindStep.bind(
                it.requestId, it.eventTime, it.principal, principalHrn.organization, it.resource, it.operation
            )
        }
        val result = batchBindStep.execute()
        var returnValue = true

        result.forEachIndexed { index, it ->
            if (it != 1) {
                returnValue = false
                logger.error { "Insert failed: ${auditEntries[index]}" }
            }
        }
        return returnValue
    }
}
