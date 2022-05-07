package com.demo.usermanagement.commands;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class ApplicationCommand {

    @ShellMethod(value = "adding 2 numbers", key = "sum")
    public int add(int a, int b) {
        return a + b;
    }
}
