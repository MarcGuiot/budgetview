package org.designup.picsou.gui.accounts.utils;

import org.designup.picsou.gui.description.stringifiers.AccountStringifier;
import org.globsframework.model.Glob;

import javax.swing.*;
import java.awt.*;

public class AccountRenderer extends DefaultListCellRenderer {
  AccountStringifier stringifier = new AccountStringifier();
  public Component getListCellRendererComponent(JList list, Object object, int i, boolean b, boolean b1) {
    return super.getListCellRendererComponent(list, getValueToDisplay((Glob)object), i, b, b1);
  }

  private String getValueToDisplay(Glob account) {
    return stringifier.toString(account, null);
  }
}
