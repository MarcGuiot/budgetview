package com.budgetview.desktop.printing.budget.gauges;

import com.budgetview.desktop.components.charts.Gauge;
import com.budgetview.desktop.description.Formatting;
import com.budgetview.desktop.model.PeriodSeriesStat;
import com.budgetview.desktop.printing.PrintStyle;
import com.budgetview.desktop.printing.utils.PageBlock;
import com.budgetview.shared.model.BudgetArea;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

import java.awt.*;

public class SeriesGaugeBlock implements PageBlock {

  public static final int HEIGHT = 20;

  private Glob periodStat;
  private BudgetGaugeContext context;
  private int sectionIndex;
  private GlobRepository repository;
  private BudgetArea budgetArea;

  public SeriesGaugeBlock(Glob periodStat, BudgetGaugeContext budgetGaugeContext, int sectionIndex, GlobRepository repository) {
    this.periodStat = periodStat;
    this.context = budgetGaugeContext;
    this.sectionIndex = sectionIndex;
    this.repository = repository;
    this.budgetArea = PeriodSeriesStat.getBudgetArea(periodStat, repository);
  }

  public int getNeededHeight() {
    return getHeight();
  }

  public int getHeight() {
    return HEIGHT;
  }

  public String getLabel() {
    return PeriodSeriesStat.getName(periodStat, repository);
  }

  public String getActualAmount() {
    return Formatting.toString(periodStat.get(PeriodSeriesStat.AMOUNT), budgetArea);
  }

  public String getPlannedAmount() {
    return Formatting.toString(periodStat.get(PeriodSeriesStat.PLANNED_AMOUNT), budgetArea);
  }

  public Gauge getGauge(PrintStyle style) {
    Gauge gauge = context.getGauge(style);
    gauge.getModel().setValues(periodStat.get(PeriodSeriesStat.AMOUNT, 0),
                               periodStat.get(PeriodSeriesStat.PLANNED_AMOUNT, 0));
    return gauge;
  }

  public void print(Dimension area, Graphics2D g2, PrintStyle style) {

    if (sectionIndex % 2 == 1) {
      g2.setColor(style.getTableRowColor());
      g2.fillRect(0, 0, area.width, area.height);
    }

    BudgetGaugeBlockMetrics metrics = context.getMetrics(area, g2, style.getTextFont(false), style.getTextFont(false));

    g2.setColor(style.getTextColor());
    g2.setFont(style.getTextFont(false));
    g2.drawString(getActualAmount(), metrics.actualAmountX(getActualAmount()), metrics.labelY);
    g2.drawString(getPlannedAmount(), metrics.plannedAmountX(getPlannedAmount()), metrics.labelY);

    g2.setClip(0, 0, metrics.labelWidth, area.height);
    g2.drawString(getLabel(), metrics.labelX(PeriodSeriesStat.isSeriesInGroup(periodStat, repository)), metrics.labelY);

    g2.setClip(0, 0, area.width, area.height);
    Gauge gauge = getGauge(style);
    gauge.setBounds(0, 0, metrics.gaugeWidth, metrics.gaugeHeight);
    gauge.setSize(metrics.gaugeWidth, metrics.gaugeHeight);
    g2.translate(metrics.gaugeX, metrics.gaugeY);
    gauge.printAll(g2);
  }

}
