package com.dicoding.myunlimitedquotes.network

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// this data class works both for `Retrofit response` & `Room entity`
@Entity(tableName = "quote")
data class QuoteResponseItem(

	@PrimaryKey
	@field:SerializedName("id")
	val id: String,

	@field:SerializedName("author")
	val author: String? = null,

	@field:SerializedName("en")
	val en: String? = null
)
