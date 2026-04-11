package com.cvinsight.ui.controllers;

import com.cvinsight.service.UserService;
import com.cvinsight.ui.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML private TextField     usernameField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label         errorLabel;

    private final UserService userService = new UserService();

    @FXML
    private void handleRegister() {
        String username        = usernameField.getText().trim();
        String email           = emailField.getText().trim();
        String password        = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            showError("Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            confirmPasswordField.clear();
            return;
        }

        try {
            userService.register(username, email, password);
            SceneManager.switchTo("login.fxml");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (RuntimeException e) {
            showError("Registration failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleGoToLogin() {
        SceneManager.switchTo("login.fxml");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
