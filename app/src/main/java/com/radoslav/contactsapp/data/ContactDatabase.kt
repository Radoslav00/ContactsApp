package com.radoslav.contactsapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.radoslav.contactsapp.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Contact::class], version = 1)
abstract class ContactDatabase : RoomDatabase(){

    abstract fun contactDao(): ContactDao

    class Callback @Inject constructor(
        private val database: Provider<ContactDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback(){
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().contactDao()

            applicationScope.launch {
                dao.insert(Contact("Radoslav", "Lambrev", "Myself", "089275654", "rad@gmail"))
                dao.insert(Contact("Todor", "Lambrev", "Brother", "089835546", "tod@gmail", favourite = true))
                dao.insert(Contact("Stefka", "Lambrev", "Mother", "0888984656", "stef@gmail"))
                dao.insert(Contact("Angel", "Popov", "Friend", "088898466435", "angel@gmail"))
                dao.insert(Contact("Petar", "Petrov", "Friend", "08889846653", "petar@gmail", favourite = true))

            }
        }
    }
}