package com.home

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.home.integration.DataService
import com.home.integration.RemoteDataClient
import org.h2.jdbcx.JdbcDataSource
import org.jooq.DSLContext
import org.jooq.impl.DSL
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.sql.Connection
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

fun api() : Context = RunContext

/**
 * Think of this as something like Spring's context.
 */
interface Context {
    val executorService: ExecutorService
    val objectMapper: ObjectMapper
    val remoteDataClient: RemoteDataClient
    val jooqContext: DSLContext
}

/**
 * Note that this is an `object`, so we have a guarantee that this is a Singleton
 */
object RunContext : Context {
    override val executorService: ExecutorService = Executors.newFixedThreadPool(10)

    override val objectMapper: ObjectMapper = ObjectMapper()
            .registerModule(ParameterNamesModule())

    val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("http://localhost:4567/")
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build()

    val dataService: DataService = retrofit.create(DataService::class.java)

    override val remoteDataClient: RemoteDataClient = RemoteDataClient(dataService)

    val dbConnection: Connection
        get() {
            val jdbcDataSource: JdbcDataSource = JdbcDataSource()
            jdbcDataSource.setURL("jdbc:h2:mem:")
            jdbcDataSource.user = "sa"
            jdbcDataSource.password = "sa"

            return jdbcDataSource.connection
        }

    override val jooqContext: DSLContext = DSL.using(dbConnection)
}
