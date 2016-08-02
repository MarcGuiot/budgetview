package com.budgetview.desktop.components.dialogs;

import com.budgetview.desktop.Application;
import com.budgetview.desktop.components.dialogs.MessageDialog;
import com.budgetview.desktop.components.dialogs.MessageType;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.io.IOException;

public class MessageDialogDemo {
  public static void main(String[] args) throws IOException {

    Directory directory = Application.createDirectory();

    JFrame frame = new JFrame();
    GuiUtils.showCentered(frame);
    MessageDialog.show("delete.user.fail.title", MessageType.ERROR, frame, directory, "delete.user.fail.content");
  }
}
