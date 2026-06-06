package com.cvinsight.ui.controllers;

import com.cvinsight.service.SessionManager;
import com.cvinsight.service.UserService;
import com.cvinsight.model.User;
import com.cvinsight.ui.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Optional;

public class LoginController {

    @FXML private TextField    emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label        errorLabel;

    private final UserService userService = new UserService();

    @FXML
    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isBlank() || password.isBlank()) {
            showError("Please fill in all fields.");
            return;
        }

        try {
            Optional<User> result = userService.login(email, password);
            if (result.isPresent()) {
                SessionManager.getInstance().login(result.get());
                SceneManager.switchTo("dashboard.fxml");
            } else {
                showError("Incorrect email or password.");
                passwordField.clear();
            }
        } catch (RuntimeException e) {
            showError("Login failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleGoToRegister() {
        SceneManager.switchTo("register.fxml");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
