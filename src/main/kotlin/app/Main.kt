package app

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.util.UUID
import kotlinx.serialization.Serializable
import org.postgresql.ds.PGSimpleDataSource

private val dataSource = PGSimpleDataSource().apply {
    databaseName = System.getenv("DATABASE_NAME")
    user = System.getenv("DATABASE_USER")
    password = System.getenv("DATABASE_PASSWORD")
    portNumbers = intArrayOf(System.getenv("DATABASE_PORT").toInt())
    serverNames = arrayOf(System.getenv("DATABASE_HOST"))
}

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()
        }

        install(StatusPages) {
            exception { call: ApplicationCall, cause: Exception ->
                call.respond(HttpStatusCode.BadRequest, cause.message ?: "No error message provided")
            }
        }

        routing {
            get("/health-check") {
                call.respond(HttpStatusCode.OK)
            }
            post("/wallet") {
                val id = createWallet()
                call.respond(id)
            }
            get("/wallet/{id}/balance") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val wallet = getWallet(id)
                call.respond(wallet)
            }
            post("/wallet/{id}/credit") {
                val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val body = call.receive<RequestBody>()
                credit(id, body.amount)
                call.respond(HttpStatusCode.OK)
            }
            post("/wallet/{id}/debit") {
                val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val body = call.receive<RequestBody>()
                debit(id, body.amount)
                call.respond(HttpStatusCode.OK)
            }
        }
    }.start(wait = true)
}

@Serializable
data class RequestBody(val amount: Int)

@Serializable
data class Wallet(val id: String, val balance: Int)

data class WalletNotFoundException(val walletId: String) : Exception("Wallet could not be found. walletId[$walletId]")
data class NotEnoughFundsException(val walletId: String, val balance: Int, val debitAmount: Int) :
    Exception("Not enough funds. walletId[$walletId] balance[$balance] amount[$debitAmount]")

data class InvalidAmountException(val walletId: String, val amount: Int) :
    Exception("Invalid amount, may not be negative. walletId[$walletId] amount[$amount]")

fun createWallet(): String {
    val id = UUID.randomUUID().toString()
    dataSource.connection.use { connection ->
        connection.prepareStatement(
            """
                INSERT INTO wallets (id, balance)
                VALUES (?, ?)
        """.trimIndent()
        ).use { statement ->
            statement.setString(1, id)
            statement.setInt(2, 0)
            statement.executeUpdate()
        }
    }
    return id
}

fun getWallet(id: String): Wallet {
    dataSource.connection.use { connection ->
        connection.prepareStatement(
            """
                SELECT balance
                FROM wallets
                WHERE id = ?
        """.trimIndent()
        ).use { statement ->
            statement.setString(1, id)
            statement.executeQuery().use { result ->
                if (result.next()) {
                    val balance = result.getInt("balance")
                    return Wallet(id, balance)
                } else {
                    throw WalletNotFoundException(id)
                }
            }
        }
    }
}

fun credit(id: String, amount: Int) {
    if (amount < 0) throw InvalidAmountException(id, amount)
    val wallet = getWallet(id)
    dataSource.connection.use { connection ->
        connection.prepareStatement(
            """
                UPDATE wallets
                SET balance = ?
                WHERE id = ?
        """.trimIndent()
        ).use { statement ->
            statement.setInt(1, wallet.balance + amount)
            statement.setString(2, id)
            statement.executeUpdate()
        }
    }
}

fun debit(id: String, amount: Int) {
    if (amount < 0) throw InvalidAmountException(id, amount)
    val wallet = getWallet(id)
    if (wallet.balance < amount) throw NotEnoughFundsException(id, wallet.balance, amount)
    dataSource.connection.use { connection ->
        connection.prepareStatement(
            """
                UPDATE wallets
                SET balance = ?
                WHERE id = ?
        """.trimIndent()
        ).use { statement ->
            statement.setInt(1, wallet.balance - amount)
            statement.setString(2, id)
            statement.executeUpdate()
        }
    }
}
