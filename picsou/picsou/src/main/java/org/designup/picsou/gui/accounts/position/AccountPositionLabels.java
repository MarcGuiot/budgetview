package org.designup.picsou.gui.accounts.position;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.utils.AmountColors;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountPositionThreshold;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.ChangeSetMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public abstract class AccountPositionLabels {

  protected final GlobRepository repository;
  private Directory directory;
  private GlobLabelView accountPosition;
  private AmountColors amountColors;
  private Key accountKey;

  protected AccountPositionLabels(Key accountKey, GlobRepository repository, Directory directory) {
    this.accountKey = accountKey;
    this.repository = repository;
    this.directory = directory;
    this.amountColors = new AmountColors(directory);
  }

  protected abstract GlobType getType();

  protected abstract Double getEndOfMonthPosition(Key accountKey, GlobRepository repository, Integer monthId);

  protected abstract Color getLabelColor(Double position, AmountColors amountColors);

  public JLabel getEstimatedAccountPositionLabel(boolean updateColor) {
    accountPosition = GlobLabelView.init(Month.TYPE, repository, directory,
                                         new EstimatedPositionStringifier(accountKey, updateColor))
      .setUpdateMatcher(ChangeSetMatchers.changesForTypes(getType(), AccountPositionThreshold.TYPE))
      .setAutoHideIfEmpty(true);
    Glob account = repository.find(accountKey);
    if (account != null) {
      accountPosition.setName("estimatedAccountPosition." + account.get(Account.NAME));
    }
    return accountPosition.getComponent();
  }

  private class EstimatedPositionStringifier implements GlobListStringifier {

    private final Key accountKey;
    private boolean updateColor;

    public EstimatedPositionStringifier(Key accountKey, boolean updateColor) {
      this.accountKey = accountKey;
      this.updateColor = updateColor;
    }

    public String toString(GlobList months, GlobRepository repository) {
      if (!repository.contains(Transaction.TYPE)) {
        return "-";
      }

      months.sort(Month.ID);
      Glob lastMonth = months.getLast();
      Double position = null;
      if (lastMonth != null) {
        position = getEndOfMonthPosition(accountKey, repository, lastMonth.get(Month.ID));
      }
      if (updateColor) {
        accountPosition.getComponent().setForeground(getLabelColor(position, amountColors));
      }
      if (position == null) {
        return "-";
      }
      return Formatting.toString(position);
    }
  }

  public JLabel getEstimatedAccountPositionDateLabel() {
    GlobLabelView accountPosition =
      GlobLabelView.init(Month.TYPE, repository, directory,
                         new EstimatedPositionDateStringifier());
    Glob account = repository.find(accountKey);
    if (account != null) {
      accountPosition.setName("estimatedAccountPositionDate." + account.get(Account.NAME));
    }
    return accountPosition.getComponent();
  }

  private static class EstimatedPositionDateStringifier implements GlobListStringifier {
    public String toString(GlobList months, GlobRepository repository) {
      months.sort(Month.ID);
      Glob lastMonth = months.getLast();
      if (lastMonth == null) {
        return null;
      }
      Integer monthId = lastMonth.get(Month.ID);
      return Lang.get("accountView.total.date", Formatting.toString(Month.getLastDay(monthId)));
    }
  }
}
