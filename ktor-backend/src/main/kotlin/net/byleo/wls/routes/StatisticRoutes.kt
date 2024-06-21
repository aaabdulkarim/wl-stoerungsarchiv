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
import java.time.LocalDateTime
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




            call.respond(StatisticResponse(stats))
        }

    }
}

//fun checkMultipleYears(statisticsMap: List<DisturbanceMonthData>): Map<String, List<Int>>? {
//    val statisticsYearly = mutableListOf<Map<String, Int>>()
//    var differentYears = -1
//
//    val years = mutableListOf<String>()
//    val disturbances = mutableListOf<Int>()
//
//    statisticsMap.forEach { element ->
//        val yearString = element.month.substring(0, 4)
//        val amountDisturbance = element.amountDisturbances
//
//        if (!years.contains(yearString)) {
//            years.add(yearString)
//            disturbances.add(amountDisturbance)
//            differentYears++
//        } else {
//            disturbances[differentYears] += amountDisturbance
//        }
//    }
//
//    if (differentYears <= 1) return null
//
//    val resultYears = years.mapIndexed { index, year ->
//        year to disturbances[index]
//    }.toMap()
//
//    return mapOf("years" to resultYears.keys.toList(), "amountDisturbances" to resultYears.values.toList())
//}