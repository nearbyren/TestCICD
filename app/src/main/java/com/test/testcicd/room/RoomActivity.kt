package com.test.testcicd.room

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.test.testcicd.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoomActivity : AppCompatActivity() {
    private var mUserFlowDao: UserFlowDao? = null


    var content: AppCompatTextView? = null
    val MIGRATION_1_10 = object : Migration(11, 12) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 执行从旧版本升级到新版本的 SQL 语句
            database.execSQL("ALTER TABLE user ADD COLUMN money TEXT")
        }
    }
    private val database by lazy {
        Room.databaseBuilder(this, SQLDatabase::class.java, "test_demo").fallbackToDestructiveMigration().addMigrations(MIGRATION_1_10).build()
    }

    var ageQeury: Int = 0
    var codeDel: Int = 0

    fun queryAll() {
        GlobalScope.launch {
            val list = mUserFlowDao!!.queryAllUser()
            withContext(Dispatchers.Main) {

                list.collect {
                    val sb = StringBuffer()
                    for (va in it) {
                        sb.append("${va.name}, ${va.age}, ${va.grender}, ${va.code} \n")
                    }
                    content?.text = sb.toString()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.room_activity)
        mUserFlowDao = database.userFlow()
        content = findViewById<AppCompatTextView>(R.id.content)
        findViewById<AppCompatButton>(R.id.add_one).setOnClickListener {
            GlobalScope.launch {
                val randValue = (Math.random() * 900).toInt() + 10
                val age = (Math.random() * 10).toInt() + 10
                ageQeury = age
                val sex = randValue % 2 == 0
                codeDel = randValue
                mUserFlowDao!!.insert(
                    UserEntity(
                        randValue, "$randValue", age, grender = sex, authorText = "33", aihao = "女", zhiye = "zhiye", money = "money", authorAlisa = ""
                    )
                )
                queryAll()
            }
        }
        findViewById<AppCompatButton>(R.id.query_all).setOnClickListener {
            queryAll()
        }
        findViewById<AppCompatButton>(R.id.query_one).setOnClickListener {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    if (ageQeury > 0) {
                        val list = mUserFlowDao!!.queryAgeUser(ageQeury)
                        withContext(Dispatchers.Main) {
                            if(list.size>0){
                                content?.text =
                                    "${list[0].name}, ${list[0].age}, ${list[0].grender}, ${list[0].code}"
                            }
                        }
                    }
                }
            }
        }
        findViewById<AppCompatButton>(R.id.del_one).setOnClickListener {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    if (codeDel > 0) {
                        mUserFlowDao!!.deletedUser(codeDel)
                        queryAll()
                    }
                }
            }
        }
        findViewById<AppCompatButton>(R.id.del_all).setOnClickListener {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    mUserFlowDao!!.deleteAll()
                    queryAll()
                }
            }
        }

    }
}