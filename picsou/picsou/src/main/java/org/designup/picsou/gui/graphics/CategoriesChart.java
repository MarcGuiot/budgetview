package org.designup.picsou.gui.graphics;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.gui.TransactionSelection;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.MasterCategory;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.color.ColorUpdater;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.RectangleInsets;

import java.awt.*;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriesChart extends View implements GlobSelectionListener, ChangeSetListener {
  private TransactionSelection transactionSelection;
  private DefaultPieDataset dataset = new DefaultPieDataset();
  protected GlobStringifier categoryStringifier;
  protected PiePlot plot;

  public CategoriesChart(GlobRepository repository, Directory directory, TransactionSelection transactionSelection) {
    super(repository, directory);
    this.transactionSelection = transactionSelection;
    this.transactionSelection.addListener(this);
    this.categoryStringifier = descriptionService.getStringifier(Category.TYPE);
    repository.addChangeListener(this);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    ChartPanel chartPanel = new ChartPanel(createChart());
    chartPanel.setOpaque(false);
    chartPanel.setRangeZoomable(false);
    chartPanel.setDomainZoomable(false);
    builder.add("categoriesChart", chartPanel);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(MonthStat.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
    update();
  }

  public void selectionUpdated(GlobSelection selection) {
    update();
  }

  private void update() {
    Map<Integer, Double> categoryIdToExpenses = new HashMap<Integer, Double>();
    GlobList monthStats = transactionSelection.getMonthStatsForAllMasterCategories();
    for (Glob monthStat : monthStats) {
      Integer categoryId = monthStat.get(MonthStat.CATEGORY);
      if (MasterCategory.ALL.getId().equals(categoryId)) {
        continue;
      }
      Double expenses = monthStat.get(MonthStat.EXPENSES);
      Double expensesForCategory = categoryIdToExpenses.get(categoryId);
      if (expensesForCategory == null) {
        expensesForCategory = 0.0;
      }
      categoryIdToExpenses.put(categoryId, expensesForCategory + expenses);
    }

    dataset.clear();
    for (MasterCategory master : MasterCategory.values()) {
      if (master.equals(MasterCategory.ALL)) {
        continue;
      }
      Integer categoryId = master.getId();
      dataset.setValue(categoryId,
                       categoryIdToExpenses.get(categoryId));
      if (transactionSelection.isCategorySelected(categoryId)) {
        plot.setExplodePercent(categoryId, 0.30);
      }
      else {
        plot.setExplodePercent(categoryId, 0);
      }
    }
  }

  private JFreeChart createChart() {
    JFreeChart chart = ChartFactory.createPieChart(
      null,
      dataset,
      false,
      false,
      false
    );

    plot = (PiePlot)chart.getPlot();
    for (final MasterCategory master : MasterCategory.values()) {
      if (!master.equals(MasterCategory.ALL)) {
        colorService.install("category.pie.section." + master.getName(), new ColorUpdater() {
          public void updateColor(Color color) {
            plot.setSectionPaint(master.getId(), color);
          }
        });
      }
    }

    plot.setNoDataMessage("No data available");
    plot.setInteriorGap(0);

    plot.setLabelGenerator(new PieSectionLabelGenerator() {
      public AttributedString generateAttributedSectionLabel(PieDataset dataset, Comparable key) {
        return new AttributedString(generateSectionLabel(dataset, key));
      }

      public String generateSectionLabel(PieDataset dataset, Comparable categoryId) {
        Glob category = repository.get(Key.create(Category.TYPE, categoryId));
        return categoryStringifier.toString(category, repository);
      }
    });

    plot.setIgnoreNullValues(true);
    plot.setIgnoreZeroValues(true);

    plot.setLegendLabelToolTipGenerator(new StandardPieSectionLabelGenerator("{2}%"));

    plot.setOutlinePaint(null);
    plot.setBackgroundPaint(null);
    plot.setLabelBackgroundPaint(null);
    plot.setLabelOutlinePaint(null);
    plot.setLabelShadowPaint(null);
    plot.setMinimumArcAngleToDraw(2.0);

    colorService.install("category.pie.link", new ColorUpdater() {
      public void updateColor(Color color) {
        if (color == null) {
          color = Color.BLACK;
        }
        plot.setLabelLinkPaint(color);
      }
    });
    colorService.install("category.pie.label", new ColorUpdater() {
      public void updateColor(Color color) {
        if (color == null) {
          color = Color.BLACK;
        }
        plot.setLabelPaint(color);
      }
    });

    chart.setBackgroundPaint(null);
    chart.setPadding(new RectangleInsets(0, 0, 0, 0));
    return chart;
  }
}
