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

  public static void showWithButtonMessage(String titleKey, String contentKey,
                                                                   Window owner, Directory directory,
                                                                   String key,
                                                                   String... args) {
    MessageDialog dialog = new MessageDialog(titleKey, contentKey, owner, directory, key, args);
    dialog.show();
  }

  public static void show(String titleKey, String contentKey,
                                           Window owner, Directory directory,
                                           String... args) {
    MessageDialog dialog = new MessageDialog(titleKey, contentKey, owner, directory, "close", args);
    dialog.show();
  }

  private MessageDialog(String titleKey, String contentKey,
                        Window owner, Directory directory,
                        String key,
                        String... args) {
    builder = SplitsBuilder.init(directory)
      .setSource(getClass(), "/layout/utils/messageDialog.splits");

    builder.add("title", new JLabel(Lang.get(titleKey)));
    builder.add("message", new JEditorPane("text/html", Lang.get(contentKey, args)));

    dialog = PicsouDialog.create(owner, directory);
    dialog.addPanelWithButton(builder.<JPanel>load(), new CloseAction(key, dialog));
    dialog.pack();
  }

  public final void show() {
    dialog.showCentered(true);
    builder.dispose();
  }
}
