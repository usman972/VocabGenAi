package com.rameez.hel.utils

import android.content.Context
import android.net.Uri
import com.rameez.hel.model.WIPModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object ImportExportUtil {

    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    // Export to JSON
    suspend fun exportToJSON(context: Context, items: List<WIPModel>, uri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    val jsonString = json.encodeToString(items)
                    writer.write(jsonString)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Import from JSON
    suspend fun importFromJSON(context: Context, uri: Uri): List<WIPModel>? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val jsonString = reader.readText()
                    json.decodeFromString<List<WIPModel>>(jsonString)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Export to CSV
    suspend fun exportToCSV(context: Context, items: List<WIPModel>, uri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    // Header
                    writer.write("id,wip,meaning,sampleSentence,category,customTag,readCount,displayCount,createdAt,updatedAt,uploadedAt,encounteredCount,viewedCount,encounteredLastUpdatedAt,viewedLastUpdatedAt,firstEncounteredAt,firstViewedAt\n")
                    
                    // Data rows
                    items.forEach { item ->
                        val row = listOf(
                            item.id ?: "",
                            escapeCSV(item.wip ?: ""),
                            escapeCSV(item.meaning ?: ""),
                            escapeCSV(item.sampleSentence ?: ""),
                            escapeCSV(item.category ?: ""),
                            escapeCSV(item.customTag?.joinToString(";") ?: ""),
                            item.readCount?.toString() ?: "",
                            item.displayCount?.toString() ?: "",
                            item.createdAt?.toString() ?: "",
                            item.updatedAt?.toString() ?: "",
                            item.uploadedAt?.toString() ?: "",
                            item.encounteredCount?.toString() ?: "0",
                            item.viewedCount?.toString() ?: "0",
                            item.encounteredLastUpdatedAt?.toString() ?: "",
                            item.viewedLastUpdatedAt?.toString() ?: "",
                            item.firstEncounteredAt?.toString() ?: "",
                            item.firstViewedAt?.toString() ?: ""
                        ).joinToString(",")
                        writer.write("$row\n")
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Import from CSV
    suspend fun importFromCSV(context: Context, uri: Uri): List<WIPModel>? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val items = mutableListOf<WIPModel>()
                    
                    // Skip header
                    reader.readLine()
                    
                    // Read data rows
                    reader.forEachLine { line ->
                        val values = parseCSVLine(line)
                        if (values.size >= 17) {
                            val item = WIPModel(
                                id = values[0].ifEmpty { null },
                                wip = values[1].ifEmpty { null },
                                meaning = values[2].ifEmpty { null },
                                sampleSentence = values[3].ifEmpty { null },
                                category = values[4].ifEmpty { null },
                                customTag = values[5].split(";").filter { it.isNotEmpty() },
                                readCount = values[6].toFloatOrNull(),
                                displayCount = values[7].toFloatOrNull(),
                                createdAt = values[8].toLongOrNull(),
                                updatedAt = values[9].toLongOrNull(),
                                uploadedAt = values[10].toLongOrNull(),
                                encounteredCount = values[11].toIntOrNull() ?: 0,
                                viewedCount = values[12].toIntOrNull() ?: 0,
                                encounteredLastUpdatedAt = values[13].toLongOrNull(),
                                viewedLastUpdatedAt = values[14].toLongOrNull(),
                                firstEncounteredAt = values[15].toLongOrNull(),
                                firstViewedAt = values[16].toLongOrNull()
                            )
                            items.add(item)
                        }
                    }
                    
                    items
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun escapeCSV(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun parseCSVLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        
        var i = 0
        while (i < line.length) {
            val char = line[i]
            
            when {
                char == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                    i++
                }
                char == '"' -> {
                    inQuotes = !inQuotes
                }
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> {
                    current.append(char)
                }
            }
            i++
        }
        
        result.add(current.toString())
        return result
    }
}
