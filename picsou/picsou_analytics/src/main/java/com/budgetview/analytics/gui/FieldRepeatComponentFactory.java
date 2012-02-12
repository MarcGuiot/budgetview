package com.budgetview.analytics.gui;

import com.budgetview.analytics.AnalyticsApp;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoChartConfig;
import org.designup.picsou.gui.components.charts.histo.line.HistoBarPainter;
import org.designup.picsou.gui.components.charts.histo.line.HistoLineColors;
import org.designup.picsou.gui.components.charts.histo.line.HistoLineDataset;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.util.List;

public class FieldRepeatComponentFactory implements RepeatComponentFactory<Field> {

  private List<Field> fields;
  private GlobRepository repository;
  private Directory directory;
  private SelectionService selectionService;
  private HistoLineColors chartColors;
  private IntegerField typeId;
  private GlobType type;

  public FieldRepeatComponentFactory(IntegerField typeId, List<Field> fields, GlobRepository repository, Directory directory) {
    this.fields = fields;
    this.repository = repository;
    this.directory = directory;
    this.selectionService = directory.get(SelectionService.class);
    this.chartColors = new HistoLineColors(
      "histo.line.positive",
      "histo.line.negative",
      "histo.fill.positive",
      "histo.fill.negative",
      directory
    );
    this.typeId = typeId;
    this.type = typeId.getGlobType();
  }

  public void registerComponents(RepeatCellBuilder cellBuilder, Field field) {
    cellBuilder.add("chartTitle", new JLabel(field.getName()));
    cellBuilder.add("chart", createPerfChart(field));
  }

  private HistoChart createPerfChart(final Field field) {
    HistoChartConfig chartConfig =
      new HistoChartConfig(true, field == fields.get(0),
                           false, true, true, true, false, false, false);
    final HistoChart chart = new HistoChart(chartConfig, directory);

    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        updateChart(field, chart);
      }
    }, type);

    updateChart(field, chart);

    return chart;
  }

  private void updateChart(Field field, HistoChart chart) {

    GlobList selection = selectionService.getSelection(type);

    HistoLineDataset dataset = new HistoLineDataset("histo.tooltip");
    for (Glob glob : getGlob().sort(new GlobFieldComparator(typeId))) {
      Integer weekId = glob.get(typeId);
      dataset.add(weekId,
                  getValue(glob, field),
                  Integer.toString(weekId % 100),
                  Integer.toString(weekId),
                  Integer.toString(weekId / 100),
                  true,
                  false,
                  selection.contains(glob));
    }
    chart.update(new HistoBarPainter(dataset, chartColors));
  }

  private GlobList getGlob() {
    return repository.getAll(type,
                             GlobMatchers.fieldGreaterOrEqual(typeId, AnalyticsApp.MIN_WEEK));
  }

  private Double getValue(Glob glob, Field field) {
    Object value = glob.getValue(field);
    if (value == null) {
      return 0.00;
    }
    if (field instanceof DoubleField) {
      return (Double)value;
    }
    else if (field instanceof IntegerField) {
      return ((Integer)value).doubleValue();
    }
    throw new InvalidParameter("Unexpected field type: " + field.getFullName());
  }
}