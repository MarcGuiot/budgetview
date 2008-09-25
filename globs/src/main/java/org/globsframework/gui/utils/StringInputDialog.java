package org.globsframework.gui.utils;

import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class StringInputDialog {

  private String selectedName;
  private JLabel messageLabel = new JLabel();
  private JTextField textField = new JTextField();
  protected OkAction okAction;
  private JDialog dialog;

  public StringInputDialog(JDialog dialog, String initialValue, String title, String inputLabel,
                           String okLabel, String cancelLabel, Directory directory) {
    this.dialog = dialog;
    dialog.setTitle(title);

    okAction = new OkAction(okLabel);
    messageLabel.setVisible(false);

    SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.add("inputLabel", new JLabel(inputLabel));
    builder.add("input", textField);
    builder.add("label", messageLabel);
    builder.add("ok", okAction);
    builder.add("cancel", new CancelAction(cancelLabel));
    builder.setSource(
      "<splits>" +
      "  <panel  opaque='true'>" +
      "    <column marginLeft='20' marginRight='20' margin='5'>" +
      "      <row defaultMarginTop='5' defaultMarginBottom='5'>" +
      "        <label ref='inputLabel' marginRight='10'/>" +
      "        <textField ref='input' columns='15'/>" +
      "      </row>" +
      "      <label ref='label' foreground='red' marginTop='5' marginBottom='5'/>" +
      "      <row>" +
      "        <filler fill='horizontal'/>" +
      "        <button action='ok'/>" +
      "        <button action='cancel'/>" +
      "      </row>" +
      "    </column>" +
      "  </panel>" +
      "</splits>"
    );
    dialog.setContentPane((Container)builder.load());
    dialog.setSize(300, 150);
    installInputListener();
    if (initialValue != null) {
      textField.setText(initialValue);
      textField.selectAll();
    }
    textField.addActionListener(okAction);
    updateOkAction();
  }

  public String getSelectedName() {
    return selectedName;
  }

  protected abstract void validate(String name);

  private class OkAction extends AbstractAction {
    public OkAction(String label) {
      super(label);
    }

    public void actionPerformed(ActionEvent e) {
      selectedName = textField.getText().trim();
      try {
        validate(selectedName);
        dialog.setVisible(false);
      }
      catch (InvalidParameter exception) {
        okAction.setEnabled(false);
        displayMessage(exception.getMessage());

        StringInputDialog.this.dialog.invalidate();
        StringInputDialog.this.dialog.validate();

        StringInputDialog.this.dialog.repaint();
      }
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction(String label) {
      super(label);
    }

    public void actionPerformed(ActionEvent e) {
      selectedName = null;
      dialog.setVisible(false);
    }
  }

  private void updateOkAction() {
    okAction.setEnabled(textField.getText().length() > 0);
    displayMessage("");
  }

  private void installInputListener() {
    PlainDocument document = new PlainDocument();
    document.addDocumentListener(new AbstractDocumentListener() {
      protected void documentChanged(DocumentEvent e) {
        updateOkAction();
      }
    });
    textField.setDocument(document);
  }

  private void displayMessage(String message) {
    messageLabel.setText(message);
    messageLabel.setVisible(!Strings.isNullOrEmpty(message));
    dialog.pack();
    dialog.validate();
  }
}
