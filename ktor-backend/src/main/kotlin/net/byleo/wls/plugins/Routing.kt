package net.byleo.wls.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.byleo.wls.routes.disturbanceRouting
import net.byleo.wls.routes.lineRouting
import net.byleo.wls.routes.statisticRouting

fun Application.configureRouting() {
    routing {
        disturbanceRouting()
        lineRouting()
        statisticRouting()
    }
}
