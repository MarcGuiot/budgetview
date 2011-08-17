package org.designup.picsou.gui.components;

import org.globsframework.utils.Strings;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BorderlessTextField {
  private JTextField textField;
  private Border defaultBorder;
  private boolean rollover;

  public static void install(JTextField textField) {
    new BorderlessTextField(textField);
  }

  public BorderlessTextField(JTextField textField) {
    this.textField = textField;
    defaultBorder = textField.getBorder();
    registerListeners(textField);
    update();
  }

  private void registerListeners(JTextField textField) {
    textField.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent mouseEvent) {
        rollover = true;
        update();
      }

      public void mouseExited(MouseEvent mouseEvent) {
        rollover = false;
        update();
      }
    });
    textField.addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent focusEvent) {
        update();
      }

      public void focusLost(FocusEvent focusEvent) {
        update();
      }
    });
    textField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent documentEvent) {
        update();
      }

      public void removeUpdate(DocumentEvent documentEvent) {
        update();
      }

      public void changedUpdate(DocumentEvent documentEvent) {
        update();
      }
    });
  }

  private void update() {
    boolean hasText = Strings.isNotEmpty(textField.getText());
    boolean focus = textField.hasFocus();
    boolean hideBorder = hasText && !rollover && !focus;
    textField.setBorder(hideBorder ? null : defaultBorder);
  }
}
