package com.dicoding.myunlimitedquotes.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// this to save new page info requested by server
@Entity(tableName = "remote_keys")
data class RemoteKeys (
    @PrimaryKey val id: String,
    val prevKey: Int?,
    val nextKey: Int?
)