package com.budgetview.desktop.importer.components;

import com.budgetview.desktop.components.dialogs.CloseDialogAction;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.splits.utils.OnLoadListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SynchroErrorDialog {
  private final PicsouDialog dialog;
  private SplitsBuilder builder;

  public static void show(final String details, Mode mode, Window owner, Directory directory) {
    SynchroErrorDialog dialog = new SynchroErrorDialog(details, mode, owner, directory);
    dialog.show();
  }

  public static enum Mode {
    LOGIN("synchro.login.failed.title", "login"),
    OTHER("synchroError.title", "other");

    private String titleKey;
    private String card;

    Mode(String titleKey, String card) {
      this.titleKey = titleKey;
      this.card = card;
    }

    String getTitle() {
      return Lang.get(titleKey);
    }
    
    String getCard() {
      return card;
    }

  }

  private SynchroErrorDialog(final String details, final Mode mode, Window owner, Directory directory) {
    builder = SplitsBuilder.init(directory)
      .setSource(getClass(), "/layout/importexport/components/synchroErrorDialog.splits");

    builder.add("title", new JLabel(mode.getTitle()));

    builder.add("details", GuiUtils.createReadOnlyTextArea(details));
    builder.add("copy", new AbstractAction(Lang.get("exception.copy")) {
      public void actionPerformed(ActionEvent e) {
        GuiUtils.copyTextToClipboard(details);
      }
    });

    final CardHandler cards = builder.addCardHandler("cards");
    builder.add("link", new AbstractAction(Lang.get("synchroError.link")) {
      public void actionPerformed(ActionEvent e) {
        cards.show("send");
      }
    });

    builder.addOnLoadListener(new OnLoadListener() {
      public void processLoad() {
        cards.show(mode.getCard());
      }
    });

    dialog = PicsouDialog.create(this, owner, true, directory);
    dialog.addPanelWithButton(builder.<JPanel>load(), new CloseDialogAction(dialog));
    dialog.pack();
  }

  private void show() {
    dialog.showCentered();
    builder.dispose();
  }
}
