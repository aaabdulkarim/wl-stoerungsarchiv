package net.byleo.wls.data

import net.byleo.wls.models.*
import net.byleo.wls.models.Disturbances.startTime
import net.byleo.wls.util.DisturbanceFilter
import net.byleo.wls.util.OrderType
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter

private val LINE_ORDER: Array<Pair<Expression<*>, SortOrder>> = arrayOf(
    Lines.type to SortOrder.ASC,
    Lines.displayName.castTo<Int>(IntegerColumnType()) to SortOrder.ASC,
    Lines.displayName to SortOrder.ASC
)

class Dao {
     private suspend fun resultRowToDisturbance(row: ResultRow) = Disturbance(
        id = row[Disturbances.disturbanceId],
        title = row[Disturbances.title],
        type = row[Disturbances.type],
        startTime = row[Disturbances.startTime],
        endTime = row[Disturbances.endTime],
        lines = getLinesOfDisturbance(row[Disturbances.disturbanceId]),
        descriptions = getDescriptionsOfDisturbance(row[Disturbances.disturbanceId])
    )

    private fun resultRowToLine(row: ResultRow) = Line(
        id = row[Lines.lineId],
        type = row[Lines.type],
        displayName = row[Lines.displayName]
    )

    private fun resultRowToDescription(row: ResultRow) = Description(
        description = row[DisturbanceDescriptions.description],
        time = row[DisturbanceDescriptions.time]
    )

    private suspend fun getLinesOfDisturbance(id: String): List<Line> = Database.dbQuery {
        (Lines innerJoin DisturbancesLines).select {
            DisturbancesLines.disturbanceId eq EntityID(id, Disturbances)
        }.orderBy(*LINE_ORDER).map(::resultRowToLine)
    }

    private suspend fun getDescriptionsOfDisturbance(id: String): List<Description> = Database.dbQuery {
        DisturbanceDescriptions.select {
            DisturbanceDescriptions.disturbanceId eq EntityID(id, Disturbances)
        }.orderBy(DisturbanceDescriptions.time to SortOrder.ASC).map(::resultRowToDescription)
    }

    suspend fun insertDisturbance(disturbance: Disturbance) = Database.dbQuery {
        Disturbances.insertIgnore {
            it[disturbanceId] = disturbance.id
            it[title] = disturbance.title
            it[type] = disturbance.type
            it[startTime] = disturbance.startTime
            it[endTime] = disturbance.endTime
        }
        for(line in disturbance.lines) {
            getLineById(line.id) ?: Lines.insertIgnore {
                it[lineId] = line.id
                it[type] = line.type
                it[displayName] = line.displayName
            }
            DisturbancesLines.insertIgnore {
                it[DisturbancesLines.disturbanceId] = EntityID(disturbance.id, Disturbances)
                it[DisturbancesLines.lineId] = EntityID(line.id, Lines)
            }
        }
        for(description in disturbance.descriptions) {
            DisturbanceDescriptions.insertIgnore {
                it[DisturbanceDescriptions.description] = description.description
                it[time] = description.time
                it[DisturbanceDescriptions.disturbanceId] = EntityID(disturbance.id, Disturbances)
            }
        }
    }

    suspend fun getLines(): List<Line> = Database.dbQuery {
        Lines.selectAll().orderBy(*LINE_ORDER).map(::resultRowToLine)
    }

    // Supposed to evaluate Statistics
    suspend fun evaluateStatistics() {

        val startOfTrackingDate = LocalDateTime.of(2022, Month.JANUARY, 1, 0, 0)


        val disturbances = getDisturbances(
            DisturbanceFilter(
                lines = emptyList(),
                types = emptyList(),
                active = true,
                // From Start of tracking Disturbances
                from = startOfTrackingDate,
                // Vielleicht statt LocalDateTime eine Alternative benutzen
                // Eventuell kann die LocalDateTime ein fehlerhaftes Resultat liefern,
                // Falls der Rechner auf dem das Programm läuft falsch konfiguriert ist.
                to = LocalDateTime.now(),
                order = OrderType.START,
                desc = true
            )
        )

        val disturbanceMonthData = mutableListOf<DisturbanceMonthData>()

        disturbances.forEach { row ->
            val monthsOnly = disturbanceMonthData.map { it.month }

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
            val rowToString = row.startTime.toString()
            val monthString = LocalDateTime.parse(rowToString, DateTimeFormatter.ISO_DATE_TIME).format(formatter)

            // Jahr und Monat wird geparst
            val (year, month) = monthString.split("-").map { it.toInt() }

            // Individueller Key für jeden Monat eines bestimmten Jahres
            // z.B 202402 =  Jahr 2024 Februar
            val monthKey = year * 100 + month
            val monthKeyStr = monthKey.toString()

            val disturbanceIndex = monthsOnly.indexOf(monthKeyStr)

            if (disturbanceIndex == -1) {
                disturbanceMonthData.add(DisturbanceMonthData(0, monthKeyStr))
            } else {
                disturbanceMonthData[disturbanceIndex].amountDisturbances++;
            }
        }

        transaction {
                disturbanceMonthData.forEach {
                        data -> StatisticsTable.insert {
                    it[StatisticsTable.amountDisturbances] = data.amountDisturbances;
                    it[StatisticsTable.month] = data.month
                }
            }
        }



    }

    // Supposed to get them from the Table
    suspend fun getStatistic(
        filter: DisturbanceFilter = DisturbanceFilter()
    ) = Database.dbQuery {
        val disturbances = getDisturbances(filter)

        val disturbanceMonthData = mutableListOf<DisturbanceMonthData>()

        disturbances.forEach { row ->
            val monthsOnly = disturbanceMonthData.map { it.month }

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
            val rowToString = row.startTime.toString()
            val monthString = LocalDateTime.parse(rowToString, DateTimeFormatter.ISO_DATE_TIME).format(formatter)

            // Jahr und Monat wird geparst
            val (year, month) = monthString.split("-").map { it.toInt() }

            // Individueller Key für jeden Monat eines bestimmten Jahres
            // z.B 202402 =  Jahr 2024 Februar
            val monthKey = year * 100 + month
            val monthKeyStr = monthKey.toString()

            val disturbanceIndex = monthsOnly.indexOf(monthKeyStr)

            if (disturbanceIndex == -1) {
                disturbanceMonthData.add(DisturbanceMonthData(0, monthKeyStr))
            } else {
                disturbanceMonthData[disturbanceIndex].amountDisturbances++;
            }
        }

        disturbanceMonthData

    }

    suspend fun getLineById(id: String): Line? = Database.dbQuery {
        Lines.select { Lines.lineId eq id }.singleOrNull()?.let(::resultRowToLine)
    }

    private suspend fun getDisturbanceIdsOfLines(lines: List<String>): List<String> = Database.dbQuery {
        val lineIds = lines.map { EntityID(it, Disturbances) }
        DisturbancesLines.select {
            DisturbancesLines.lineId inList lineIds
        }.groupBy(DisturbancesLines.disturbanceId).map { it[DisturbancesLines.disturbanceId].value }
    }

    suspend fun getDisturbances(
        filter: DisturbanceFilter = DisturbanceFilter()
    ) = Database.dbQuery {
        val query = Disturbances.select {
            Disturbances.endTime.isNull() or
                    ((Disturbances.startTime greaterEq filter.from) and (Disturbances.endTime lessEq filter.to))
        }
        if(filter.lines.isNotEmpty()) {
            val disturbanceIds = getDisturbanceIdsOfLines(filter.lines)
            query.andWhere { Disturbances.disturbanceId inList disturbanceIds }
        }
        if(filter.types.isNotEmpty())
            query.andWhere { Disturbances.type inList filter.types }
        filter.active?.let { active ->
            if(active)
                query.andWhere { if(active) Disturbances.endTime.isNull() else Disturbances.endTime.isNotNull() }
            else
                query.andWhere { Disturbances.endTime.isNotNull() }
        }
        query.orderBy(
            Disturbances.endTime.isNull() to SortOrder.DESC,
            when(filter.order) {
                OrderType.START -> Disturbances.startTime
                OrderType.END -> Disturbances.endTime
                OrderType.TYPE -> Disturbances.type
            } to if(filter.desc) SortOrder.DESC else SortOrder.ASC
        )
        val disturbanceIds = query.map {
            it[Disturbances.disturbanceId]
        }

        val lines = disturbanceIds.associateBy({it}, { mutableListOf<Line>()})
        ((Disturbances innerJoin DisturbancesLines) innerJoin Lines).select {
            Disturbances.disturbanceId inList disturbanceIds
        }.orderBy(*LINE_ORDER).forEach {
            lines[it[Disturbances.disturbanceId]]?.add(
                resultRowToLine(it)
            )
        }

        val descriptions = disturbanceIds.associateBy({it}, { mutableListOf<Description>()})
        (Disturbances innerJoin DisturbanceDescriptions).select {
            Disturbances.disturbanceId inList disturbanceIds
        }.orderBy(DisturbanceDescriptions.time to SortOrder.ASC).forEach {
            descriptions[it[Disturbances.disturbanceId]]?.add(
                resultRowToDescription(it)
            )
        }

        query.map { row ->
            Disturbance(
                id = row[Disturbances.disturbanceId],
                title = row[Disturbances.title],
                type = row[Disturbances.type],
                startTime = row[Disturbances.startTime],
                endTime = row[Disturbances.endTime],
                lines = lines[row[Disturbances.disturbanceId]] ?: emptyList(),
                descriptions = descriptions[row[Disturbances.disturbanceId]] ?: emptyList()
            )
        }
    }

    suspend fun getDisturbanceById(id: String): Disturbance? = Database.dbQuery {
        Disturbances.select { Disturbances.disturbanceId eq id }.singleOrNull()?.let { resultRowToDisturbance(it) }
    }

    suspend fun insertDescription(
        description: Description,
        disturbanceId: String
    ) = Database.dbQuery {
        DisturbanceDescriptions.insert {
            it[DisturbanceDescriptions.disturbanceId] = disturbanceId
            it[DisturbanceDescriptions.description] = description.description
            it[DisturbanceDescriptions.time] = description.time
        }
    }

    suspend fun closeDisturbancesExcept(exceptDisturbances: List<String>) = Database.dbQuery {
        val currentTime = LocalDateTime.now()
        Disturbances.update({
            Disturbances.endTime.isNull() and (Disturbances.disturbanceId notInList exceptDisturbances)
        }) {
            it[Disturbances.endTime] = currentTime
        }
    }
}

val dao = Dao()