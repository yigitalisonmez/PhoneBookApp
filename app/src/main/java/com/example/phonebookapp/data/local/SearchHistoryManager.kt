package com.example.phonebookapp.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
    
    private val gson = Gson()
    private val maxHistorySize = 10
    
    fun addSearchQuery(query: String) {
        if (query.isBlank()) return
        
        val currentHistory = getSearchHistory().toMutableList()
        
        // Eğer query zaten varsa, önce kaldır
        currentHistory.remove(query)
        
        // Yeni query'yi başa ekle
        currentHistory.add(0, query)
        
        // Maksimum boyutu aşarsa, son elemanları kaldır
        if (currentHistory.size > maxHistorySize) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        
        // SharedPreferences'a kaydet
        val json = gson.toJson(currentHistory)
        sharedPreferences.edit()
            .putString("search_queries", json)
            .apply()
    }
    
    fun getSearchHistory(): List<String> {
        val json = sharedPreferences.getString("search_queries", null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    fun removeSearchQuery(query: String) {
        val currentHistory = getSearchHistory().toMutableList()
        currentHistory.remove(query)
        
        val json = gson.toJson(currentHistory)
        sharedPreferences.edit()
            .putString("search_queries", json)
            .apply()
    }
    
    fun clearSearchHistory() {
        sharedPreferences.edit()
            .remove("search_queries")
            .apply()
    }
}
