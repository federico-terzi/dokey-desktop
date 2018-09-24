package system.applications.MS

import json.JSONArray
import json.JSONObject
import json.JSONTokener
import system.storage.StorageManager
import java.io.File

const val LNK_CACHE_FILENAME = "lnkcache.json"

class LinkCacheManager(val storageManager: StorageManager) {
    private val cacheFile = File(storageManager.cacheDir, LNK_CACHE_FILENAME)

    // Association between LNK file path and Target file path
    val linkCache = mutableMapOf<String, String>()

    fun load() {
        if (!cacheFile.isFile) {
            return
        }

        cacheFile.inputStream().use {
            val tokener = JSONTokener(it)
            val entries = JSONArray(tokener)
            for (entry in entries) {
                entry as JSONObject
                val lnkPath = entry.getString("lnk")
                val targetPath = entry.getString("target")
                linkCache[lnkPath] = targetPath
            }
        }

    }

    fun persist() {
        // Convert all the entries to JSON
        val entries = JSONArray()
        linkCache.forEach { key, value ->
            val json = JSONObject()
            json.put("lnk", key)
            json.put("target", value)
            entries.put(json)
        }

        // Write the entres to file
        cacheFile.printWriter().use {
            it.write(entries.toString())
        }
    }
}