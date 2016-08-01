package com.budgetview.gui.startup.components;

import javax.swing.*;
import java.util.Arrays;

public abstract class Passwords {
  private JPasswordField passwordField;
  private JPasswordField confirmPasswordField;

  public Passwords(JPasswordField passwordField, JPasswordField confirmPasswordField) {
    this.passwordField = passwordField;
    this.confirmPasswordField = confirmPasswordField;
  }

  public boolean passwordAccepted() {
    char[] pwd = passwordField.getPassword();
    if (pwd.length == 0) {
      displayErrorMessage("login.password.required");
      return false;
    }
    if (pwd.length < 4) {
      displayErrorMessage("login.password.too.short");
      return false;
    }
    char[] confirm = confirmPasswordField.getPassword();
    if (confirm.length == 0) {
      displayErrorMessage("login.confirm.required");
      return false;
    }
    if (!Arrays.equals(pwd, confirm)) {
      displayErrorMessage("login.confirm.error");
      return false;
    }
    clearMessage();
    return true;
  }

  public abstract void displayErrorMessage(String key);
  
  public abstract void clearMessage();

}
