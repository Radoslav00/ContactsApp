package com.radoslav.contactsapp.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat

@Entity(tableName = "contact_table")
@Parcelize
data class Contact (
    val first_name: String,
    val last_name: String,
    val category: String,
    val phone_number: String,
    val email_address: String,
    val favourite: Boolean = false,
    val created: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0
        ) : Parcelable{
    val createdDateFormatted: String
        get() = DateFormat.getDateTimeInstance().format(created)

}