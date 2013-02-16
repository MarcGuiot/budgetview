package org.designup.picsou.gui.components;

import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;

public class TextFieldLimitDemo {
  public static void main(String[] args) {
    JTextField textField = new JTextField();
    TextFieldLimit.install(textField, 10);
    GuiUtils.show(textField);
  }
}
