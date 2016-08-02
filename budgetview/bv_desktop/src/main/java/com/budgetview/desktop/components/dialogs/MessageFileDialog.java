package com.budgetview.desktop.components.dialogs;

import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class MessageFileDialog {
  private GlobRepository repository;
  private Directory directory;

  public MessageFileDialog(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public void show(String titleKey, String messageKey, File file) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/utils/messageFileDialog.splits", repository, directory);

    builder.add("title", new JLabel(Lang.get(titleKey)));

    builder.add("message", new JEditorPane("text/html", Lang.get(messageKey)));

    JTextArea fileArea = new JTextArea();
    fileArea.setEditable(false);
    builder.add("filePath", fileArea);
    if (file == null) {
      fileArea.setVisible(false);
    }
    else {
      fileArea.setText(file.getAbsolutePath());
    }

    final PicsouDialog dialog = PicsouDialog.create(this, directory.get(JFrame.class), directory);
    AbstractAction action = new AbstractAction(Lang.get("ok")) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    };

    dialog.addPanelWithButton(builder.<JPanel>load(), action);
    dialog.pack();
    dialog.showCentered();
    builder.dispose();
  }
}
