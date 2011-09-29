package org.designup.picsou.gui.importer;

public interface MessageHandler {

  void showFileErrorMessage(String message);

  void showFileErrorMessage(String message, Exception e);
}
