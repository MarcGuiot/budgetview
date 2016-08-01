package com.budgetview.gui.components.dialogs;

import com.budgetview.gui.PicsouApplication;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.io.IOException;

public class MessageDialogDemo {
  public static void main(String[] args) throws IOException {

    Directory directory = PicsouApplication.createDirectory();

    JFrame frame = new JFrame();
    GuiUtils.showCentered(frame);
    MessageDialog.show("delete.user.fail.title", MessageType.ERROR, frame, directory, "delete.user.fail.content");
  }
}
