package org.designup.picsou.gui.accounts;

import org.designup.picsou.model.Account;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;

import javax.swing.*;
import java.awt.*;

public class AccountRenderer extends DefaultListCellRenderer {
  public Component getListCellRendererComponent(JList list, Object object, int i, boolean b, boolean b1) {
    return super.getListCellRendererComponent(list, getValueToDisplay((Glob)object), i, b, b1);
  }

  private String getValueToDisplay(Glob account) {
    if (account == null) {
      return "";
    }
    if (account.get(Account.ID).equals(Account.SUMMARY_ACCOUNT_ID)) {
      return Lang.get("account.summary.name");
    }

    return account.get(Account.NAME);
  }
}
