package com.demo.usermanagement.commands;

import com.demo.usermanagement.entity.User;
import com.demo.usermanagement.repository.UserRepository;
import com.demo.usermanagement.util.InputReader;
import com.demo.usermanagement.util.ShellHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import java.util.Optional;

@ShellComponent
public class ApplicationCommand {

    @Autowired
    private ShellHelper shellHelper;

    @Autowired
    private InputReader inputReader;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @ShellMethod(value = "adding 2 numbers", key = "sum")
    public int add(int a, int b) {
        return a + b;
    }

    @ShellMethod("Create new user with supplied email")
    public void createUser(@ShellOption({"-E", "--email"}) String email) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            shellHelper.printError(String.format("User with email='%s' already exists --> ABORTING", email));
            return;
        }

        User user = new User();
        user.setEmail(email);

        do {
            String name = inputReader.prompt("Name");
            if (StringUtils.hasText(name)) {
                user.setName(name);
            } else {
                shellHelper.printWarning("User's name should not be empty!");
            }
        } while (user.getName() == null);

        do {
            String password = inputReader.prompt("Password", "secret", false);
            if (StringUtils.hasText(password)) {
                user.setPassword(passwordEncoder.encode(password));
            } else {
                shellHelper.printWarning("Password should not be empty!");
            }
        } while (user.getPassword() == null);

        shellHelper.printInfo("\nCreating new user:");
        userRepository.save(user);
        shellHelper.printSuccess("Created user with id=" + user.getId());
    }
}
