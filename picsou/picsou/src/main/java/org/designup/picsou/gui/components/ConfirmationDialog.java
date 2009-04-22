package org.designup.picsou.gui.components;

import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class ConfirmationDialog {
  private PicsouDialog dialog;
  protected JEditorPane editorPane;
  protected AbstractAction cancel;

  public ConfirmationDialog(String titleKey, String contentKey, Window owner, Directory directory, String... args) {
    SplitsBuilder builder = SplitsBuilder.init(directory)
      .setSource(getClass(), "/layout/confirmationDialog.splits");

    builder.add("title", new JLabel(Lang.get(titleKey)));
    editorPane = new JEditorPane("text/html", Lang.get(contentKey, args));
    builder.add("message", editorPane);

    dialog = PicsouDialog.create(owner, directory);
    dialog.addPanelWithButtons(builder.<JPanel>load(), createOkAction(), createCancelAction());
    dialog.pack();
  }

  public final void show() {
    GuiUtils.showCentered(dialog);
  }

  protected void preValidate() {
    // override this
  }

  protected void postValidate() {
    // override this
  }

  private AbstractAction createOkAction() {
    return new AbstractAction(Lang.get("ok")) {
      public void actionPerformed(ActionEvent e) {
        preValidate();
        dialog.setVisible(false);
        postValidate();
      }
    };
  }

  private AbstractAction createCancelAction() {
    cancel = new AbstractAction(Lang.get("cancel")) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    };
    return cancel;
  }
}
