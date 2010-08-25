package org.globsframework.gui.splits.utils;

import org.globsframework.gui.utils.AbstractDocumentListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AutoDispose {
  public static void registerComboSelection(final JComboBox comboBox,
                                            final Disposable disposable) {
    comboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (comboBox.getSelectedIndex() > -1) {
          disposable.dispose();
          comboBox.removeActionListener(this);
        }
      }
    });
  }

  public static void registerTextEdition(final JTextField textField,
                                         final Disposable disposable) {
    final Document document = textField.getDocument();
    document.addDocumentListener(new AbstractDocumentListener() {
      protected void documentChanged(DocumentEvent e) {
        disposable.dispose();
        document.removeDocumentListener(this);
      }
    });
  }
}
