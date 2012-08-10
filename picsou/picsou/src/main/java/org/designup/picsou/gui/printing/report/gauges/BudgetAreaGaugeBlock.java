package org.designup.picsou.gui.printing.report.gauges;

import org.designup.picsou.gui.budget.BudgetAreaHeader;
import org.designup.picsou.gui.budget.BudgetAreaHeaderUpdater;
import org.designup.picsou.gui.components.TextDisplay;
import org.designup.picsou.gui.components.charts.Gauge;
import org.designup.picsou.gui.printing.PrintStyle;
import org.designup.picsou.gui.printing.report.utils.PageBlock;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.SortedSet;

import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class BudgetAreaGaugeBlock implements PageBlock {

  private SortedSet<Integer> selectedMonths;
  private BudgetGaugeContext context;
  private BudgetArea budgetArea;

  private Gauge gauge;
  private String actualAmount;
  private String plannedAmount;

  public BudgetAreaGaugeBlock(BudgetArea budgetArea, SortedSet<Integer> selectedMonths, BudgetGaugeContext context, GlobRepository repository, Directory directory) {
    this.selectedMonths = selectedMonths;
    this.context = context;
    this.budgetArea = budgetArea;
    init(repository, directory);
  }

  private void init(GlobRepository repository, Directory parentDirectory) {
    Directory localDirectory = new DefaultDirectory(parentDirectory);
    SelectionService selectionService = new SelectionService();
    localDirectory.add(selectionService);

    JLabel actualLabel = new JLabel();
    JLabel plannedLabel = new JLabel();
    gauge = new Gauge();
    BudgetAreaHeaderUpdater headerUpdater =
      new BudgetAreaHeaderUpdater(TextDisplay.create(actualLabel), TextDisplay.create(plannedLabel),
                                  gauge, repository, localDirectory);
    BudgetAreaHeader.init(budgetArea, headerUpdater, repository, localDirectory);

    selectionService.select(repository.getAll(Month.TYPE, fieldIn(Month.ID, selectedMonths)),
                            Month.TYPE);

    actualAmount = actualLabel.getText();
    plannedAmount = plannedLabel.getText();
  }

  public int getNeededHeight() {
    return getHeight() + SeriesGaugeBlock.HEIGHT;
  }

  public int getHeight() {
    return 20;
  }

  public String getLabel() {
    return budgetArea.getLabel();
  }

  public String getActualAmount() {
    return actualAmount;
  }

  public String getPlannedAmount() {
    return plannedAmount;
  }

  public Gauge getGauge() {
    return gauge;
  }

  public void print(Dimension area, Graphics2D g2, PrintStyle style) {

    BudgetGaugeBlockMetrics metrics = context.getMetrics(area, g2,
                                                         style.getSectionTitleFont(),
                                                         style.getTextFont(false));

    g2.setColor(style.getSectionBackgroundColor());
    g2.fillRect(0, 0, area.width, area.height);

    g2.setColor(style.getSectionTextColor());
    g2.setFont(style.getTextFont(false));
    g2.drawString(getActualAmount(), metrics.actualAmountX(getActualAmount()), metrics.labelY);
    g2.drawString(getPlannedAmount(), metrics.plannedAmountX(getPlannedAmount()), metrics.labelY);

    g2.setClip(0, 0, metrics.labelWidth, area.height);
    g2.setFont(style.getTextFont(false));
    g2.drawString(getLabel(), metrics.labelX, metrics.labelY);

    g2.setClip(0, 0, area.width, area.height);
    Gauge gauge = getGauge();
    style.setColors(gauge);
    gauge.setBackground(style.getSectionBackgroundColor());
    gauge.setBounds(0, 0, metrics.gaugeWidth, metrics.gaugeHeight);
    gauge.setSize(metrics.gaugeWidth, metrics.gaugeHeight);
    g2.translate(metrics.gaugeX, metrics.gaugeY);
    gauge.printAll(g2);
  }
}
