package org.designup.picsou.gui.importer.components;

import org.designup.picsou.gui.components.dialogs.CloseDialogAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SynchroErrorDialog {
  private final PicsouDialog dialog;
  private SplitsBuilder builder;

  public SynchroErrorDialog(final String details, PicsouDialog owner, Directory directory) {
    builder = SplitsBuilder.init(directory)
      .setSource(getClass(), "/layout/importexport/components/synchroErrorDialog.splits");

    builder.add("details", GuiUtils.createReadOnlyTextArea(details));
    builder.add("copy", new AbstractAction(Lang.get("exception.copy")) {
      public void actionPerformed(ActionEvent e) {
        GuiUtils.copyTextToClipboard(details);
      }
    });
    final CardHandler cards = builder.addCardHandler("cards");
    cards.show("intro");
    builder.add("link", new AbstractAction(Lang.get("synchroError.link")) {
      public void actionPerformed(ActionEvent e) {
        cards.show("send");
      }
    });

    dialog = PicsouDialog.create(owner, true, directory);
    dialog.addPanelWithButton(builder.<JPanel>load(), new CloseDialogAction(dialog));
    dialog.pack();
  }

  public final void show() {
    dialog.showCentered();
    builder.dispose();
  }
}
