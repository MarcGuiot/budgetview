package org.globsframework.gui.utils;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

public abstract class AbstractDocumentListener implements DocumentListener {
  public void insertUpdate(DocumentEvent e) {
    documentChanged(e);
  }

  public void removeUpdate(DocumentEvent e) {
    documentChanged(e);
  }

  public void changedUpdate(DocumentEvent e) {
    documentChanged(e);
  }

  protected abstract void documentChanged(DocumentEvent e);
}
