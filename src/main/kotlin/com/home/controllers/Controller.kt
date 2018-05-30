package com.home.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.home.api
import com.home.domain.Repository
import com.home.dto.Aggregate
import com.home.dto.Data
import com.home.integration.RemoteDataClient
import spark.Request
import spark.Response
import spark.Spark
import java.io.IOException
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService

class Controller(val repository: Repository,
                 val objectMapper: ObjectMapper = api().objectMapper,
                 val remoteDataClient: RemoteDataClient = api().remoteDataClient,
                 val executorService: ExecutorService = api().executorService) {

    private fun tryParseId(request: Request): Long? = request.params("id").toLongOrNull()

    private fun readOrDelete(request: Request, response: Response, operation: (id: Long) -> Data?): Data? {
        response.type("application/json")

        val maybeId: Long? = tryParseId(request)
        val maybeData: Data? = maybeId?.let(operation)
        response.status(if (maybeData != null) 200 else 404)
        return maybeData
    }

    fun getData(request: Request, response: Response): Data? {
        return this.readOrDelete(
                request,
                response,
                { id: Long -> repository.read(id) }
        )
    }

    fun deleteData(request: Request, response: Response): Data? {
        return this.readOrDelete(
                request,
                response,
                { id: Long -> repository.delete(id) }
        )
    }


    @Throws(IOException::class)
    fun createData(request: Request, response: Response): Data {
        val body = request.body()
        val data = objectMapper.readValue(body, Data::class.java)

        repository.create(data)
        response.status(201)
        return data
    }

    @Throws(IOException::class)
    fun updateData(request: Request, response: Response): Data {
        val id = java.lang.Long.parseLong(request.params("id"))
        val body = request.body()
        val data = objectMapper.readValue(body, Data::class.java)

        if (id != data.id) {
            Spark.halt(400)
        }

        val old = repository.read(id)
        val status = if (old != null) 201 else 200

        repository.update(data)
        response.status(status)
        return data
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    fun getAggregate(request: Request, response: Response): Aggregate {

        val data1Future = executorService.submit<Data> { remoteDataClient.getRemoteData("1", 600) }
        val data2 = remoteDataClient.getRemoteData("2", 600)

        val results = setOf(data1Future.get(), data2!!)
        return Aggregate(results)
    }

}
