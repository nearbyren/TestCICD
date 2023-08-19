package com.test.testcicd

class TestLet {

    var a: String? = "some a "
    var b: String? = null

    fun testlet() {
        a?.let {
            b?.let {
                println("b ? ...")
            }
        } ?: kotlin.run {
            println("a ? ...")
        }
    }

    fun testlet2() {
        a?.let {
            b?.let {
                println("b ? ...")
            } ?: kotlin.run {
                println("b ? ... null")
            }
        } ?: kotlin.run {
            println("a ? ... null")
        }
    }

    fun testapply() {
        a?.apply {
            b?.apply {
                println("b ? ...")
            }
        } ?: kotlin.run {
            println("a ? ... null")
        }
    }

    fun testapply2() {
        a?.apply {
            b?.apply {
                println("b ? ...")
            } ?: kotlin.run {
                println("b ? ... null")
            }
        } ?: kotlin.run {
            println("a ? ... null")
        }
    }
}