package org.designup.picsou.gui.components.dialogs;

import org.designup.picsou.utils.Lang;
import org.designup.picsou.gui.components.CloseAction;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class MessageDialog {
  private PicsouDialog dialog;
  private SplitsBuilder builder;

  public MessageDialog(String titleKey, String contentKey,
                       Window owner, Directory directory,
                       String... args) {
    builder = SplitsBuilder.init(directory)
      .setSource(getClass(), "/layout/messageDialog.splits");

    builder.add("title", new JLabel(Lang.get(titleKey)));
    builder.add("message", new JEditorPane("text/html", Lang.get(contentKey, args)));

    dialog = PicsouDialog.create(owner, directory);
    dialog.addPanelWithButton(builder.<JPanel>load(), new CloseAction(dialog));
    dialog.pack();
  }

  public final void show() {
    dialog.showCentered();
    builder.dispose();
  }
}
