package com.test.testcicd.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserFlowDao {

    @Query("select * from user")//结合flow监听数据变化，避免重复更新ui数据
    fun queryAllUser(): Flow<List<UserEntity>>

    //key键重复的替换
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: UserEntity)

    @Query("delete from user")//删除所有数据
    fun deleteAll()

    @Query("select * from user WHERE age = :age")//根据条件查询
    fun queryAgeUser(age: Int): List<UserEntity>

    @Query("delete from user WHERE code = :code")
    fun deletedUser(code: Int)
}
