package com.example.debttracker

import com.example.debttracker.data.repository.Repository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class RepositoryTest {

    @Test
    fun parseErrorMessage_extractsDetailFromHttpException() {
        val body = """{"detail":"Invalid credentials"}"""
        val response = Response.error<Any>(401, body.toResponseBody("application/json".toMediaType()))
        assertEquals("Invalid credentials", Repository.parseErrorMessage(HttpException(response)))
    }

    @Test
    fun parseErrorMessage_returnsNetworkMessageForIoException() {
        assertEquals("Нет соединения с сервером", Repository.parseErrorMessage(IOException("boom")))
    }

    @Test
    fun parseErrorMessage_fallsBackToCodeWhenNoDetail() {
        val response = Response.error<Any>(500, "".toResponseBody("application/json".toMediaType()))
        assertEquals("Ошибка сервера (500)", Repository.parseErrorMessage(HttpException(response)))
    }
}
