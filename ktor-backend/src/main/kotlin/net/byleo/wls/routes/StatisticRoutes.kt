package net.byleo.wls.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.byleo.wls.data.dao
import net.byleo.wls.models.DisturbanceMonthData
import net.byleo.wls.util.DisturbanceFilter
import net.byleo.wls.util.MessageResponse
import net.byleo.wls.util.OrderType
import net.byleo.wls.util.StatisticResponse
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun Route.statisticRouting() {
    route("/statistics") {
        get {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            val lines = call.request.queryParameters["lines"]?.split(",") ?: emptyList()
            val types = call.request.queryParameters["types"]?.split(",")?.map {
                it.toIntOrNull() ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    MessageResponse("Invalid type code $it")
                )
            } ?: emptyList()
            val active = call.request.queryParameters["active"]?.toBooleanStrictOrNull() ?: false
            val from = call.request.queryParameters["from"]?.let {
                try { LocalDate.parse(it, formatter).atTime(LocalTime.MIN) } catch (e: DateTimeParseException) {
                    return@get call.respond(
                        HttpStatusCode.BadRequest,
                        MessageResponse("Invalid from date $it")
                    )
                }
            } ?: LocalDate.now().atTime(LocalTime.MIN)


            val to = call.request.queryParameters["to"]?.let {
                try { LocalDate.parse(it, formatter).atTime(LocalTime.MAX) } catch (e: DateTimeParseException) {
                    return@get call.respond(
                        HttpStatusCode.BadRequest,
                        MessageResponse("Invalid to date $it")
                    )
                }
            } ?: LocalDate.now().atTime(LocalTime.MAX)
            val order = call.request.queryParameters["order"]?.let { type ->
                OrderType.entries.firstOrNull { it.name.equals(type, ignoreCase = true) } ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    MessageResponse("Invalid order type $type")
                )
            } ?: OrderType.START
            val desc = call.request.queryParameters["desc"]?.toBooleanStrictOrNull() ?: true

            val stats =
                dao.getStatistic(
                    DisturbanceFilter(
                        lines = lines,
                        types = types,
                        active = active,
                        from = from,
                        to = to,
                        order = order,
                        desc = desc
                    )
                )
            var monthByYearStatistic: Map<String, List<DisturbanceMonthData>>? = null
            var yearStatistic: Map<String, Int>? = null
            // Falls die Filter Funktionen einen größeren Zeitraum anfragt
            if (to.year - from.year >= 2){
                val checkObject = StatisticMultipleYearsFormatter(stats)

                if(checkObject.checkMultipleYears()) {
                    monthByYearStatistic = checkObject.resultMonthByYearStatistic
                    yearStatistic = checkObject.resultYearStatistic

                }
            }
//            println(stats)
            call.respond(StatisticResponse(stats, monthByYearStatistic, yearStatistic))
        }

    }
}

class StatisticMultipleYearsFormatter() {

    // Map von String, Int Pairs: ("yearcode", amountDisturbances)
    lateinit var resultYearStatistic : Map<String, Int>

    // Liste an Störungs und Monaten per Jahr: ["yearcode": [{"monthcode", amountDisturbances}, ...]
    lateinit var resultMonthByYearStatistic : Map<String, List<DisturbanceMonthData>>

    // Liste von DisturbanceMonthData Objekten
    private lateinit var statisticsMap: List<DisturbanceMonthData>
    constructor(statisticsMap: List<DisturbanceMonthData>) : this() {
        this.statisticsMap = statisticsMap
    }

    fun checkMultipleYears(): Boolean {
        var differentYears = -1

        val years = mutableListOf<String>()
        val disturbances = mutableListOf<Int>()

        // Für resultMonthByYearStatistic
        val monthsByYear = mutableMapOf<String, MutableList<DisturbanceMonthData>>()

        statisticsMap.forEach { element ->
            val yearString = element.month.substring(0, 4)
            val amountDisturbance = element.amountDisturbances

            if (!years.contains(yearString)) {
                years.add(yearString)
                disturbances.add(amountDisturbance)

                // Für resultMonthByYearStatistic
                monthsByYear[yearString] = mutableListOf(element)

                differentYears++
            } else {
                disturbances[differentYears] += amountDisturbance
                monthsByYear[yearString]?.add(element)

            }

        }

        if (differentYears <= 1) return false

        val resultYears = years.mapIndexed { index, year ->
            year to disturbances[index]
        }.toMap()

        this.resultYearStatistic = resultYears
        this.resultMonthByYearStatistic = monthsByYear

        return true
    }
}


