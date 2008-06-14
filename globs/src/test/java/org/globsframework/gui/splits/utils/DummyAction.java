package org.globsframework.gui.splits.utils;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DummyAction extends AbstractAction {
  private boolean clicked = false;

  public DummyAction() {
    super("dummyAction");
  }

  public void actionPerformed(ActionEvent e) {
    clicked = true;
  }

  public boolean wasClicked() {
    return clicked;
  }
}
