package com.test.testcicd.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserEntity::class], version = 12, exportSchema = false)
abstract class SQLDatabase : RoomDatabase(){
    abstract fun userFlow():UserFlowDao //flow操作类
}
