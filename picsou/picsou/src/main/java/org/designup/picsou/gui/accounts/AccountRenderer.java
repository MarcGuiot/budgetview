package org.designup.picsou.gui.accounts;

import org.crossbowlabs.globs.model.Glob;
import org.designup.picsou.model.Account;

import javax.swing.*;
import java.awt.*;

public class AccountRenderer extends DefaultListCellRenderer {

  public Component getListCellRendererComponent(JList list, Object object, int i, boolean b, boolean b1) {
    Glob account = (Glob) object;
    String name = (account != null ? account.get(Account.NAME) : "");
    return super.getListCellRendererComponent(list, name, i, b, b1);
  }
}
