package com.home.integration;

import com.example.jooq.tables.User
import io.github.cdimascio.dotenv.dotenv
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.sql.Connection
import java.sql.DriverManager

class DataBaseClient {
    fun proceedGetUsers() {
        executeQuery { dslContext ->
            val result = dslContext.select().from(User.USER).fetch()
            println(result)
            result.forEach {
                println("The element is $it")
            }
        }
    }

    fun proceedCreateUser() {
        executeQuery { dslContext ->
            val insertResult = dslContext.insertInto(User.USER,
                    User.USER.FIRSTNAME, User.USER.LASTNAME)
                    .values("John", "Smith")
                    .execute()
        }
    }

    fun executeQuery(function: (DSLContext) -> Unit) {
        getConnection().use {
            DSL.using(it, SQLDialect.POSTGRES).use(function)
        }
    }

    private fun getConnection(): Connection? {
        val dotenv = dotenv()

        val url = dotenv["DB_URL"]
        val password = dotenv["DB_PASSWORD"]
        val userName = dotenv["DB_USERNAME"]

        return DriverManager.getConnection(url, userName, password)
    }
}
