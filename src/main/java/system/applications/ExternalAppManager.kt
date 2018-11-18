package system.applications

import json.JSONArray
import json.JSONTokener
import system.storage.StorageManager
import java.io.File

const val EXTERNAL_CACHE_FILENAME = "externals.json"

class ExternalAppManager(val storageManager: StorageManager) {
    private val cacheFile = File(storageManager.cacheDir, EXTERNAL_CACHE_FILENAME)

    val externalAppIds = mutableSetOf<String>()

    fun load() {
        if (!cacheFile.isFile) {
            return
        }

        cacheFile.inputStream().use {
            val tokener = JSONTokener(it)
            val entries = JSONArray(tokener)
            for (entry in entries) {
                entry as String
                externalAppIds.add(entry)
            }
        }

    }

    @Synchronized
    fun add(app: String) {
        externalAppIds.add(app)
    }

    @Synchronized
    fun persist() {
        // Convert all the entries to JSON
        val entries = JSONArray()
        externalAppIds.forEach { app ->
            entries.put(app)
        }

        // Write the entres to file
        cacheFile.printWriter().use {
            it.write(entries.toString())
        }
    }
}