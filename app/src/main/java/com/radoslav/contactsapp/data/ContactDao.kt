

package com.radoslav.contactsapp.data

import androidx.room.*

import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    fun getContacts(query: String, sortOrder: SortOrder, showFavourite: Boolean): Flow<List<Contact>> =
        when(sortOrder){
            SortOrder.BY_DATE -> getContactsSortedByDateCreated(query, showFavourite)
            SortOrder.BY_NAME -> getContactsSortedByName(query, showFavourite)
        }

    @Query("SELECT * FROM contact_table WHERE (favourite= :showFavourite or  favourite = 1) AND first_name LIKE '%' || :searchQuery || '%' ORDER BY favourite DESC, first_name")
    fun getContactsSortedByName(searchQuery: String, showFavourite: Boolean = false): Flow<List<Contact>>

    @Query("SELECT * FROM contact_table WHERE (favourite= :showFavourite or favourite = 1) AND first_name LIKE '%' || :searchQuery || '%' ORDER BY favourite DESC, created")
    fun getContactsSortedByDateCreated(searchQuery: String, showFavourite: Boolean = false): Flow<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: Contact)

    @Update
    suspend fun update(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact)

}