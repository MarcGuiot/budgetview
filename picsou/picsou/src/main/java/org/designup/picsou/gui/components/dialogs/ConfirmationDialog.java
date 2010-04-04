package org.designup.picsou.gui.components.dialogs;

import org.designup.picsou.utils.Lang;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class ConfirmationDialog {
  private PicsouDialog dialog;
  protected JEditorPane editorPane;
  protected AbstractAction cancel;
  protected AbstractAction ok;
  private SplitsBuilder builder;

  public ConfirmationDialog(String titleKey, String contentKey, Window owner, Directory directory, String... args) {
    builder = SplitsBuilder.init(directory)
      .setSource(getClass(), "/layout/utils/confirmationDialog.splits");

    dialog = PicsouDialog.create(owner, directory);

    builder.add("title", new JLabel(Lang.get(titleKey)));
    editorPane = new JEditorPane("text/html", Lang.get(contentKey, args));
    builder.add("hyperlinkHandler", new HyperlinkHandler(directory, dialog));
    builder.add("message", editorPane);

    dialog.addPanelWithButtons(builder.<JPanel>load(), createOkAction(), createCancelAction());
    dialog.pack();
  }

  public final void show() {
    dialog.showCentered();
    builder.dispose();
  }

  protected void postValidate() {
    // override this
  }

  private AbstractAction createOkAction() {
    ok = new AbstractAction(Lang.get("ok")) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
        postValidate();
      }
    };
    return ok;
  }

  protected AbstractAction createCancelAction() {
    cancel = new AbstractAction(Lang.get("cancel")) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    };
    return cancel;
  }
}
