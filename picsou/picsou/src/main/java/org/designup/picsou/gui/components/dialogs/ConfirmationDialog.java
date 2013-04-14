package org.designup.picsou.gui.components.dialogs;

import org.designup.picsou.utils.Lang;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.utils.Ref;
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

  public static boolean confirmed(String titleKey, String content, Window parent, Directory directory) {
    final Ref<Boolean> result = new Ref<Boolean>(false);
    ConfirmationDialog dialog = new ConfirmationDialog(titleKey, content, parent, directory) {
      protected void processOk() {
        result.set(true);
      }
    };
    dialog.show();
    return result.get();
  }

  public enum Mode {
    STANDARD("/layout/utils/confirmationDialog.splits"),
    EXPANDED("/layout/utils/confirmationDialogExpanded.splits");

    final String sourceFile;

    Mode(String sourceFile) {
      this.sourceFile = sourceFile;
    }
  }

  public ConfirmationDialog(String titleKey, String content, Directory directory) {
    this(titleKey, content, directory.get(JFrame.class), directory, Mode.STANDARD);
  }

  public ConfirmationDialog(String titleKey, String content, Window owner, Directory directory) {
    this(titleKey, content, owner, directory, Mode.STANDARD);
  }

  public ConfirmationDialog(String titleKey, String content,
                            Window owner, Directory directory,
                            Mode mode) {
    builder = SplitsBuilder.init(directory)
      .setSource(getClass(), mode.sourceFile);

    dialog = PicsouDialog.create(owner, directory);

    builder.add("title", new JLabel(Lang.get(titleKey)));
    editorPane = new JEditorPane("text/html", content);
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

  protected String getCancelButtonText() {
    return Lang.get("cancel");
  }

  protected void processCustomLink(String href) {
  }

  protected void processOk() {
    // override this
  }

  protected void processCancel() {
    // override this
  }

  private AbstractAction createOkAction() {
    ok = new AbstractAction(getOkButtonText()) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
        builder.dispose();
        processOk();
      }
    };
    return ok;
  }


  protected AbstractAction createCancelAction() {
    cancel = new AbstractAction(getCancelButtonText()) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
        builder.dispose();
        processCancel();
      }
    };
    return cancel;
  }
}
