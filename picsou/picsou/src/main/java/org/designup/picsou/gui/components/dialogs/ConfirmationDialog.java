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

  public enum Mode {
    STANDARD("/layout/utils/confirmationDialog.splits"),
    EXPANDED("/layout/utils/confirmationDialogExpanded.splits");

    final String sourceFile;

    Mode(String sourceFile) {
      this.sourceFile = sourceFile;
    }
  }

  public ConfirmationDialog(String titleKey, String contentKey, Window owner, Directory directory,
                            String... args) {
    this(titleKey, contentKey, owner, directory, Mode.STANDARD, args);
  }

  public ConfirmationDialog(String titleKey, String contentKey,
                            Window owner, Directory directory,
                            Mode mode, String... args) {
    builder = SplitsBuilder.init(directory)
      .setSource(getClass(), mode.sourceFile);

    dialog = PicsouDialog.create(owner, directory);

    builder.add("title", new JLabel(Lang.get(titleKey)));
    editorPane = new JEditorPane("text/html", Lang.get(contentKey, args));
    builder.add("hyperlinkHandler", new HyperlinkHandler(directory, dialog) {
      protected void processCustomLink(String href) {
        ConfirmationDialog.this.processCustomLink(href);
      }
    });
    builder.add("message", editorPane);

    dialog.addPanelWithButtons(builder.<JPanel>load(), createOkAction(), createCancelAction());
    dialog.pack();
  }

  public final void show() {
    dialog.showCentered();
  }

  public final void dispose() {
    dialog.setVisible(false);
    builder.dispose();
  }

  protected String getOkButtonText() {
    return Lang.get("ok");
  }

  protected void processCustomLink(String href) {
  }

  protected void postValidate() {
    // override this
  }

  private AbstractAction createOkAction() {
    ok = new AbstractAction(getOkButtonText()) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
        builder.dispose();
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
