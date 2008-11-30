package org.designup.picsou.gui.time.tooltip;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.gui.time.TimeViewPanel;
import org.designup.picsou.gui.time.utils.TimeViewColors;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
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
    String month = Month.getFullLabel(monthId);
    Double position = panel.getPosition(monthId);
    if (!repository.contains(Transaction.TYPE) || (position == null)) {
      panel.setToolTipText(Lang.get("timeView.tooltip.month.noData", month));
      return;
    }

    Glob balanceStat = repository.find(Key.create(BalanceStat.TYPE, monthId));
    if (balanceStat == null) {
      return;
    }
    Double balance = balanceStat.get(BalanceStat.MONTH_BALANCE);
    double positionLimit = panel.getPositionThreshold(monthId);
    Color color = colors.getAmountColor(position - positionLimit);
    panel.setToolTipText(
      Lang.get("timeView.tooltip.month.standard",
               month,
               Formatting.toStringWithPlus(balance),
               Formatting.toString(position),
               Colors.toString(color)));
  }

  public void enterYear(int year) {
    panel.setToolTipText(Lang.get("timeView.tooltip.year", Integer.toString(year)));
  }

  public void leave() {
  }
}
