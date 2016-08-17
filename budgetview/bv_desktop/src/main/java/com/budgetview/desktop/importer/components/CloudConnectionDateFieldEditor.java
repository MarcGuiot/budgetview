package com.budgetview.desktop.importer.components;

import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CloudConnectionDateFieldEditor extends CloudConnectionFieldEditor {
  private GlobTextEditor editor;

  public CloudConnectionDateFieldEditor(Glob budgeaField, Glob budgeaConnectionValue, GlobRepository repository, Directory directory) {
    super(budgeaField, budgeaConnectionValue, repository, directory);

    Format format = new SimpleDateFormat("MM/dd/yyyy");
    JFormattedTextField textField = new JFormattedTextField(format);
    try {
      MaskFormatter maskFormatter = new MaskFormatter("##/##/####");
      maskFormatter.setPlaceholderCharacter('_');
    }
    catch (ParseException e) {
      e.printStackTrace();
    }
    textField.addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) {
        if (textField.getFocusLostBehavior() == JFormattedTextField.PERSIST)
          textField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
      }

      public void focusLost(FocusEvent e) {
        try {
          Date date = (Date) format.parseObject(textField.getText());
          textField.setValue(format.format(date));
        }
        catch (ParseException pe) {
          textField.setFocusLostBehavior(JFormattedTextField.PERSIST);
          textField.setText("");
          textField.setValue(null);
        }
      }
    });
  }

  public JTextField getEditor() {
    return editor.getComponent();
  }

  public void dispose() {
    super.dispose();
    editor.dispose();
    editor = null;
  }
}
