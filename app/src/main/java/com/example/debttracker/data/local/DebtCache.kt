package com.example.debttracker.data.local

import com.example.debttracker.data.remote.DebtResponse
import com.example.debttracker.data.remote.PaymentResponse
import com.google.gson.Gson
import java.io.File

/**
 * Копия последних данных с сервера для просмотра без связи. Лежит в закрытой папке
 * приложения и очищается при входе, выходе и удалении аккаунта. Сервер остаётся
 * единственным источником правды: кэш только читается, когда сервер недоступен.
 */
class DebtCache(private val file: File) {

    data class Snapshot(
        val savedAt: Long = 0L,
        val debts: List<DebtResponse> = emptyList(),
        /** null — платежи при последней синхронизации загрузить не удалось. */
        val payments: Map<Long, List<PaymentResponse>>? = null
    )

    private val gson = Gson()
    private var cached: Snapshot? = null
    private var loaded = false

    @Synchronized
    fun load(): Snapshot? {
        if (!loaded) {
            cached = read()
            loaded = true
        }
        return cached
    }

    @Synchronized
    fun save(debts: List<DebtResponse>, payments: List<PaymentResponse>?, savedAt: Long) {
        write(Snapshot(savedAt, debts, payments?.groupBy { it.debtId }))
    }

    @Synchronized
    fun clear() {
        cached = null
        loaded = true
        file.delete()
        tmpFile().delete()
    }

    private fun read(): Snapshot? {
        if (!file.exists()) return null
        return try {
            gson.fromJson(file.readText(Charsets.UTF_8), Snapshot::class.java)
                ?.takeIf { it.savedAt > 0L }
        } catch (_: Exception) {
            null
        }
    }

    private fun write(snapshot: Snapshot) {
        cached = snapshot
        loaded = true
        try {
            val tmp = tmpFile()
            tmp.writeText(gson.toJson(snapshot), Charsets.UTF_8)
            if (!tmp.renameTo(file)) {
                file.delete()
                tmp.renameTo(file)
            }
        } catch (_: Exception) {
            // Кэш необязателен: при сбое записи приложение просто работает без него.
        }
    }

    private fun tmpFile() = File(file.parentFile, file.name + ".tmp")
}
