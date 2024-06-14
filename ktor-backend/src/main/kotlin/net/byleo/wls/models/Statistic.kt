package net.byleo.wls.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

@Serializable
data class DisturbanceMonthData(

    var amountDisturbances: Int,

    // wird aus jahr + monat bestehen z.b
    // 202402; Jahr 2024 Februar
    val month: Int,
)

object StatisticsTable: Table() {
    val id = integer("id").autoIncrement()
    val amountDisturbances = integer("amount_disturbances")
    val month = integer("month")

    override val primaryKey = PrimaryKey(id, name = "PK_Statistics_ID")
}