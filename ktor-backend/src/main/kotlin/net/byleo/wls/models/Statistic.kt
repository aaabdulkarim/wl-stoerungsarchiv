package net.byleo.wls.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class DisturbanceMonthData(

    var amountDisturbances: Int,

    // wird aus jahr + monat bestehen z.b
    // 202402; Jahr 2024 Februar
    // Eventuell zu einem String machen
    val month: String,
)

object StatisticsTable: Table() {
    val id = integer("id").autoIncrement()
    val amountDisturbances = integer("amount_disturbances")
    val month = text("month")

    override val primaryKey = PrimaryKey(id, name = "PK_Statistics_ID")
}