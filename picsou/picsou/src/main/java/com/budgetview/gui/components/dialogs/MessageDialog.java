package com.budgetview.gui.components.dialogs;

import com.budgetview.gui.utils.Gui;
import com.budgetview.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.*;

public class MessageDialog {
  private final JEditorPane editorPane;
  private PicsouDialog dialog;
  private SplitsBuilder builder;

  public static final Icon SUCCESS_ICON;
  public static final Icon INFO_ICON;
  public static final Icon ERROR_ICON;

  static {
    SUCCESS_ICON = Gui.IMAGE_LOCATOR.get("status_completed.png");
    INFO_ICON = Gui.IMAGE_LOCATOR.get("status_info.png");
    ERROR_ICON = Gui.IMAGE_LOCATOR.get("status_error.png");
  }

  public static void showMessage(String titleKey, MessageType type, Window owner, Directory directory, String message) {
    MessageDialog dialog = new MessageDialog(titleKey, type, "close", owner, directory, message);
    dialog.show();
  }

  public static void show(String titleKey, MessageType type, Window owner, Directory directory, String contentKey, String... args) {
    MessageDialog dialog = new MessageDialog(titleKey, type, "close", owner, directory, Lang.get(contentKey, args));
    dialog.show();
  }

  public static MessageDialog create(String titleKey, MessageType type, Window owner, Directory directory, String contentKey, String... args) {
    return new MessageDialog(titleKey, type, "close", owner, directory, Lang.get(contentKey, args));
  }

  public static void show(String titleKey, MessageType type, Directory directory, String contentKey, String... args) {
    MessageDialog dialog = new MessageDialog(titleKey, type, "close", directory.get(JFrame.class), directory, Lang.get(contentKey, args));
    dialog.show();
  }

  public static void showWithButtonMessage(String titleKey, String buttonKey, MessageType type, Window owner, Directory directory,
                                           String contentKey, String... contentArgs) {
    MessageDialog dialog = new MessageDialog(titleKey, type, buttonKey, owner, directory, Lang.get(contentKey, contentArgs));
    dialog.show();
  }

  private MessageDialog(String titleKey, MessageType type, String buttonKey,
                        Window owner, Directory directory,
                        String message) {
    builder = SplitsBuilder.init(directory)
      .setSource(getClass(), "/layout/utils/messageDialog.splits");

    builder.add("title", new JLabel(Lang.get(titleKey)));
    this.editorPane = new JEditorPane("text/html", message);
    builder.add("messageField", this.editorPane);

    builder.add("icon", new JLabel(getIcon(type)));

    dialog = PicsouDialog.create(this, owner, directory);
    dialog.addPanelWithButton(builder.<JPanel>load(), new CloseDialogAction(buttonKey, dialog));
    dialog.pack();
  }

  public void changeMessage(final String msg){
    editorPane.setText(msg);
  }

  private Icon getIcon(MessageType type) {
    switch (type) {
      case SUCCESS:
        return SUCCESS_ICON;
      case INFO:
        return INFO_ICON;
      case ERROR:
        return ERROR_ICON;
    }
    throw new InvalidParameter("Unexpected type: " + type);
  }

  public final void show() {
    dialog.showCentered();
    builder.dispose();
  }
}
