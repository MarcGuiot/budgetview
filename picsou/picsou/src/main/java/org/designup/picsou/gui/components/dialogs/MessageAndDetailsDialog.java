package org.designup.picsou.gui.components.dialogs;

import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MessageAndDetailsDialog {
  private PicsouDialog dialog;
  private JButton copyButton;
  private SplitsBuilder builder;

  public MessageAndDetailsDialog(String titleKey,
                                 String messageKey,
                                 final String details,
                                 Window owner,
                                 Directory directory,
                                 String... messageArgs) {
    builder = SplitsBuilder.init(directory)
      .setSource(getClass(), "/layout/utils/messageAndDetailsDialog.splits");

    builder.add("title", new JLabel(Lang.get(titleKey)));
    builder.add("message", new JEditorPane("text/html", Lang.get(messageKey, messageArgs)));

    JTextArea textArea = new JTextArea();
    textArea.setText(details);
    textArea.setCaretPosition(0);
    textArea.setEditable(false);
    builder.add("details", textArea);

    copyButton = new JButton(new AbstractAction(Lang.get("exception.copy")) {
      public void actionPerformed(ActionEvent e) {
        GuiUtils.copyTextToClipboard(details);
      }
    });
    builder.add("copy", copyButton);

    dialog = PicsouDialog.create(owner, true, directory);
    dialog.addPanelWithButton(builder.<JPanel>load(), new CloseDialogAction(dialog));
    dialog.pack();
  }

  public final void show() {
    dialog.showCentered();
    builder.dispose();
  }
}
