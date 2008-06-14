package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.utils.Html;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.model.Account;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.color.ColorSource;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AccountView extends View implements ChangeSetListener {
  private JLabel infoLabel = new JLabel();
  private Color accountNameColor;
  private Color positiveAmountColor;
  private Color negativeAmountColor;
  private GlobStringifier balanceStringifier;

  public AccountView(GlobRepository globRepository, Directory directory) {
    super(globRepository, directory);
    balanceStringifier = descriptionService.getStringifier(Account.BALANCE);
    repository.addChangeListener(this);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("accountView", infoLabel);
  }

  public void colorsChanged(ColorSource colorSource) {
    accountNameColor = colorSource.get(PicsouColors.ACCOUNT_NAME);
    positiveAmountColor = colorSource.get("balance.positive");
    negativeAmountColor = colorSource.get("balance.negative");
    update();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Account.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
    update();
  }

  private void update() {
    if (infoLabel != null) {
      infoLabel.setText(getText());
    }
  }

  private String getText() {
    GlobList accounts = repository.getAll(Account.TYPE).sort(new AccountComparator());
    if (accounts.isEmpty()) {
      return "";
    }
    StringBuilder builder = new StringBuilder();
    builder.append("<html>");
    int count = 0;
    for (Glob account : accounts) {
      if (account.get(Account.ID).equals(Account.SUMMARY_ACCOUNT_ID)) {
        continue;
      }
      append(account, count++, builder);
    }

    builder.append("</html>");
    return builder.toString();
  }

  private void append(Glob account, int count, StringBuilder builder) {

    builder.append("<font size='3' color='");
    builder.append(Colors.toString(accountNameColor));
    builder.append("'>");
    if (count > 0) {
      builder.append("&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;");
    }
    builder.append(getName(account));
    builder.append("</font>");

    builder.append(" <font size='3' color='");
    Double balance = account.get(Account.BALANCE);
    if (balance != null) {
      boolean positive = balance > 0;
      builder.append(Colors.toString(positive ? positiveAmountColor : negativeAmountColor));
      builder.append("'><b>");
      if (positive) {
        builder.append("+");
      }
      builder.append(balanceStringifier.toString(account, repository));
      builder.append(Html.EURO_SYMBOL);
      builder.append("</b></font>");
    }
    else {
      builder.append("?");
    }
  }

  private String getName(Glob account) {
    String name = account.get(Account.NAME);
    if (Strings.isNullOrEmpty(name)) {
      return account.get(Account.NUMBER);
    }
    return name;
  }

  public void selectFirst() {
    repository.get(Key.create(Account.TYPE, Account.SUMMARY_ACCOUNT_ID));
  }
}
