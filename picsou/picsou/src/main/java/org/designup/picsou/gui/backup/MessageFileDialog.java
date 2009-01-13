package org.designup.picsou.gui.backup;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class MessageFileDialog {
  private GlobRepository repository;
  private Directory directory;

  public MessageFileDialog(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  void show(String messageKey, String file) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/messageDialog.splits", repository, directory);

    builder.add("message", new JLabel(Lang.get(messageKey)));
    JTextArea jTextArea = new JTextArea();
    jTextArea.setEditable(false);
    builder.add("file", jTextArea);
    if (file == null) {
      jTextArea.setVisible(false);
    }
    else {
      jTextArea.setText(file);
    }

    final PicsouDialog dialog = PicsouDialog.create(directory.get(JFrame.class), directory);
    AbstractAction action = new AbstractAction(Lang.get("ok")) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    };
    dialog.addPanelWithButtons(builder.<JPanel>load(), action);
    dialog.pack();
    dialog.showCentered();
  }
}
