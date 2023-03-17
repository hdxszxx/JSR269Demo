package org.example.processor;


import org.example.processor.jsr269.annotations.ToBeTested;

public class Main {

    @ToBeTested(owner = "JSR 269")
    public void test() {
        System.out.println("abc");
    }

    public static void main(String[] args) {
        final Main bean = new Main();
        bean.test();
    }
}