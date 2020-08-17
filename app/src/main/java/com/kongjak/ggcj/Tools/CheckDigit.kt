package com.kongjak.ggcj.Tools

object CheckDigit {
    @JvmStatic
    fun check(num: String): String {
        return if (num.length == 1) "0$num" else num
    }
}