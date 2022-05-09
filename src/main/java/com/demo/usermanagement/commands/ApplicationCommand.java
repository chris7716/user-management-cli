package com.demo.usermanagement.commands;

import com.demo.usermanagement.entity.User;
import com.demo.usermanagement.repository.UserRepository;
import com.demo.usermanagement.util.InputReader;
import com.demo.usermanagement.util.ShellHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;
import org.springframework.util.StringUtils;

import java.util.List;
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

    Logger logger = LoggerFactory.getLogger(ApplicationCommand.class);

    @ShellMethod("Create new user with supplied email")
    public void createUser(@ShellOption({"-E", "--email"}) String email) {
        try {
            logger.trace("Start creating the user for email: " + email);
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
            logger.info("User created successfully.");
        } catch (Exception e) {
            logger.error("Error while creating an user. Message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @ShellMethod("List all user details after authenticating")
    public void listUsers()  {
        try {
            shellHelper.printInfo("Enter your credentials to list users!");
            String email;
            String password;

            do {
                email = inputReader.prompt("Email");
                if (!StringUtils.hasText(email)) {
                    shellHelper.printWarning("User's email should not be empty!");
                }
            } while (email == null);

            logger.trace("Start logging user in. Email: " + email);

            do {
                password = inputReader.prompt("Password", "secret", false);
                if (!StringUtils.hasText(password)) {
                    shellHelper.printWarning("User's password should not be empty!");
                }
            } while (email == null);

            Optional<User> user = userRepository.findByEmail(email);
            if (!user.isPresent()) {
                shellHelper.printError(String.format("User with email='%s' not found --> ABORTING", email));
                return;
            }
            if (!passwordEncoder.matches(password, user.get().getPassword())) {
                logger.error("User logging failed.");
                shellHelper.printError(String.format("Invalid password --> ABORTING"));
                return;
            }

            List<User> users = userRepository.findAll();
            if (users.isEmpty()) {
                shellHelper.printWarning(String.format("No users found!"));
            }

            String[][] data = new String[users.size() + 1][3];
            String[] headers = {"Id", "Name", "Email"};
            data[0] = headers;
            for (int i = 0; i < users.size(); i++) {
                String[] userData = {users.get(i).getId().toString(), users.get(i).getName(), users.get(i).getEmail()};
                data[i + 1] = userData;
            }

            TableModel model = new ArrayTableModel(data);
            TableBuilder tableBuilder = new TableBuilder(model);

            tableBuilder.addFullBorder(BorderStyle.oldschool);
            shellHelper.print(tableBuilder.build().render(80));
        } catch (Exception e) {
            logger.error("Error while fetching users. Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
