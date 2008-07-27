package org.designup.picsou.gui.transactions.details;

import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.gui.utils.PicsouColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class TransactionSearch {
  private TransactionView transactionView;
  private JTextField textField;
  private Color backgroundColor;

  public TransactionSearch(TransactionView transactionView, Directory directory) {
    this.transactionView = transactionView;
    directory.get(ColorService.class).addListener(new ColorChangeListener() {
      public void colorsChanged(ColorLocator colorLocator) {
        backgroundColor = colorLocator.get(PicsouColors.TRANSACTION_SEARCH_FIELD);
        if (textField != null) {
          if (Strings.isNotEmpty(textField.getText())) {
            textField.setBackground(backgroundColor);
          }
        }
      }
    });
  }

  public JTextField getTextField() {
    if (textField == null) {
      createTextField();
    }
    return textField;
  }

  private void createTextField() {
    textField = new JTextField();
    textField.setOpaque(true);
    textField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) {
        updateSearch(textField);
      }

      public void removeUpdate(DocumentEvent e) {
        updateSearch(textField);
      }

      public void changedUpdate(DocumentEvent e) {
        updateSearch(textField);
      }
    });
  }

  private void updateSearch(JTextField textField) {
    String text = textField.getText();
    transactionView.setSearchFilter(text);
    textField.setBackground(Strings.isNullOrEmpty(text) ? Color.WHITE : backgroundColor);
  }

}
