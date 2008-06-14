package org.globsframework.gui.splits.components;

import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class FontDemo {
  public static void main(String[] args) {

    JButton button = new JButton("Hello world");
    button.setFont(new Font("Arial", Font.BOLD, 24));
    GuiUtils.show(button);
  }
}
;