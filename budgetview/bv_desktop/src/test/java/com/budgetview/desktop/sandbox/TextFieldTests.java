package com.budgetview.desktop.sandbox;

import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.event.ActionEvent;

public class TextFieldTests {
  public static void main(String[] args) throws Exception {

    System.out.println("TextFieldTests.main: " + Integer.MAX_VALUE);

    final JFormattedTextField textField = new JFormattedTextField(new MaskFormatter("#########"));
    textField.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        Integer value = (Integer)textField.getValue();
        System.out.println("TextFieldTests.actionPerformed: " + value);
      }
    });
    GuiUtils.show(textField);
  }
}
