package org.designup.picsou.gui.components.dialogs;

import org.designup.picsou.gui.components.CloseAction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class MessageDialog {
  private PicsouDialog dialog;
  private SplitsBuilder builder;

  public static void showWithButtonMessage(String titleKey, String buttonKey, Window owner, Directory directory,
                                           String contentKey, String... contentArgs) {
    MessageDialog dialog = new MessageDialog(titleKey, buttonKey, owner, directory, contentKey, contentArgs);
    dialog.show();
  }

  public static void show(String titleKey, Window owner, Directory directory, String contentKey, String... args) {
    MessageDialog dialog = new MessageDialog(titleKey, "close", owner, directory, contentKey, args);
    dialog.show();
  }

  private MessageDialog(String titleKey, String buttonKey,
                        Window owner, Directory directory,
                        String contentKey, String... contentArgs) {
    builder = SplitsBuilder.init(directory)
      .setSource(getClass(), "/layout/utils/messageDialog.splits");

    builder.add("title", new JLabel(Lang.get(titleKey)));
    builder.add("message", new JEditorPane("text/html", Lang.get(contentKey, contentArgs)));

    dialog = PicsouDialog.create(owner, directory);
    dialog.addPanelWithButton(builder.<JPanel>load(), new CloseAction(buttonKey, dialog));
    dialog.pack();
  }

  public final void show() {
    dialog.showCentered();
    builder.dispose();
  }
}
