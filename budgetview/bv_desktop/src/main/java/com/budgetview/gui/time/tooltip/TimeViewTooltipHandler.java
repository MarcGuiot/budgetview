package com.budgetview.gui.time.tooltip;

import com.budgetview.gui.time.TimeViewPanel;
import com.budgetview.gui.time.utils.TimeViewColors;
import com.budgetview.model.Transaction;
import com.budgetview.shared.utils.AmountFormat;
import com.budgetview.gui.model.AccountStat;
import com.budgetview.model.Account;
import com.budgetview.model.Month;
import com.budgetview.utils.Lang;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import java.awt.*;

public class TimeViewTooltipHandler implements TimeViewMouseHandler {

  private TimeViewPanel panel;
  private GlobRepository repository;
  private TimeViewColors colors;

  public TimeViewTooltipHandler(TimeViewPanel panel, GlobRepository repository, TimeViewColors colors) {
    this.panel = panel;
    this.repository = repository;
    this.colors = colors;
  }

  public void enterMonth(int monthId) {
    String month = Month.getFullLabel(monthId, true);
    Double minPosition = panel.getMinPosition(monthId);
    if (!repository.contains(Transaction.TYPE) || (minPosition == null)) {
      panel.setToolTipText(Lang.get("timeView.tooltip.month.noData", month));
      return;
    }

    Glob budgetStat = repository.find(Key.create(AccountStat.MONTH, monthId,
                                                 AccountStat.ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID));
    if (budgetStat == null) {
      return;
    }
    Glob target = repository.findLinkTarget(budgetStat, AccountStat.MIN_ACCOUNT);
    String accountName = target != null ? target.get(Account.NAME) : "";

    if (budgetStat.get(AccountStat.ACCOUNT_COUNT) == 1) {
      panel.setToolTipText(
        Lang.get("timeView.tooltip.month.standard.one.account",
                 month,
                 AmountFormat.toStandardValueString(budgetStat.get(AccountStat.SUMMARY_POSITION_AT_MIN)),
                 Colors.toString(colors.getAmountTextColor(minPosition, Color.BLACK))));
    }
    else {
      panel.setToolTipText(
        Lang.get("timeView.tooltip.month.standard",
                 month,
                 AmountFormat.toStandardValueString(minPosition),
                 Colors.toString(colors.getAmountTextColor(minPosition, Color.BLACK)),
                 accountName,
                 AmountFormat.toStandardValueString(budgetStat.get(AccountStat.SUMMARY_POSITION_AT_MIN))
        ));
    }
  }

  public void enterYear(int year) {
    panel.setToolTipText(Lang.get("timeView.tooltip.year", Integer.toString(year)));
  }

  public void leave() {
  }
}
