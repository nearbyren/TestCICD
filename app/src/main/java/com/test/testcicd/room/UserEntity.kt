package com.test.testcicd.room

import android.os.Build.UNKNOWN
import androidx.room.*

@Entity(tableName = "user")//表名默认为UserEntity类名，使用tableName可重命名
class UserEntity(
    @PrimaryKey //主键
    var code: Int?,
    @ColumnInfo(name = "user_name")//列名默认为字段名name，可使用该注解重命名
    var name: String?,
    var age: Int?,
    var grender: Boolean,
//    @Embedded
//    var authorDetail: AuthorDetail?, // 嵌入了AuthorDetail对象
    var authorText: String?,
    var aihao: String?,
    var zhiye: String?,
    var money: String?,
    @Ignore
    var authorAlisa: String = "",//忽略字段
) {

    constructor() : this(null, "", 0, false, "authorText","aihao", "zhiye","money")
}

