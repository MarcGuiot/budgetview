package org.designup.picsou.gui.transactions.details;

import org.designup.picsou.gui.components.FilterSet;
import org.designup.picsou.gui.utils.PicsouColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public abstract class TransactionSearch {
  private FilterSet filterSet;
  private JTextField textField;
  private Color backgroundColor;
  private ColorService colorService;
  private ColorChangeListener listener;

  public TransactionSearch(FilterSet filterSet, Directory directory) {
    this.filterSet = filterSet;
    this.colorService = directory.get(ColorService.class);
    this.listener = new ColorChangeListener() {
      public void colorsChanged(ColorLocator colorLocator) {
        backgroundColor = colorLocator.get(PicsouColors.TRANSACTION_SEARCH_FIELD);
        if (textField != null) {
          if (Strings.isNotEmpty(textField.getText())) {
            textField.setBackground(backgroundColor);
          }
        }
      }
    };
    this.colorService.addListener(listener);
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
    if (Strings.isNullOrEmpty(text)) {
      filterSet.remove("search");
      textField.setBackground(Color.WHITE);
    }
    else {
      filterSet.set("search", createMatcher(text));
      textField.setBackground(backgroundColor);
    }
  }

  protected abstract GlobMatcher createMatcher(String searchFilter);

  protected void finalize() throws Throwable {
    super.finalize();
    colorService.removeListener(listener);
  }
}
