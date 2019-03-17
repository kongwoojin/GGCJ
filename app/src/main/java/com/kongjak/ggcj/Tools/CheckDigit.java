package com.kongjak.ggcj.Tools;

public class CheckDigit {
    public static String check(String num) {
        String output;
        if (num.length() == 1)
            output = "0" + num;
        else
            output = num;
        return output;
    }
}
