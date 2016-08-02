package com.budgetview.desktop.importer;

public interface MessageHandler {

  void showFileErrorMessage(String message);

  void showFileErrorMessage(String message, String details);
}
