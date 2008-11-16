package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesToCategory;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class MultiCategoriesSeriesComponentFactory extends AbstractSeriesComponentFactory {
  private BudgetArea budgetArea;

  public MultiCategoriesSeriesComponentFactory(BudgetArea budgetArea,
                                               JRadioButton invisibleSelector,
                                               SeriesEditionDialog seriesEditionDialog,
                                               GlobRepository repository,
                                               Directory directory) {
    super(invisibleSelector, seriesEditionDialog, repository, directory);
    this.budgetArea = budgetArea;
  }

  public void registerComponents(RepeatCellBuilder cellBuilder, final Glob series) {
    final GlobLabelView globLabelView = GlobLabelView.init(Series.TYPE, repository, directory, seriesStringifier);
    JLabel seriesLabel = globLabelView.forceSelection(series).getComponent();
    String label = seriesStringifier.toString(new GlobList(series), repository);
    cellBuilder.add("seriesName", seriesLabel);

    JButton editSeriesButton = new JButton(new EditSeriesAction(series.getKey()));
    editSeriesButton.setName("editSeries:" + label);
    cellBuilder.add("editSeries", editSeriesButton);

    GlobsPanelBuilder.addRepeat("categoryRepeat", SeriesToCategory.TYPE,
                                GlobMatchers.fieldEquals(SeriesToCategory.SERIES, series.get(Series.ID)),
                                new GlobFieldComparator(SeriesToCategory.ID), repository, cellBuilder,
                                new CategoriesComponentFactory(label, "categorySelector", budgetArea));

    cellBuilder.addDisposeListener(globLabelView);
  }

  private class CategoriesComponentFactory implements RepeatComponentFactory<Glob> {
    private String seriesName;
    private String name;
    private BudgetArea budgetArea;

    public CategoriesComponentFactory(String seriesName, String name, BudgetArea budgetArea) {
      this.seriesName = seriesName;
      this.name = name;
      this.budgetArea = budgetArea;
    }

    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob seriesToCategory) {
      Glob category = repository.findLinkTarget(seriesToCategory, SeriesToCategory.CATEGORY);
      final Key seriesKey = seriesToCategory.getTargetKey(SeriesToCategory.SERIES);
      String name = category.get(Category.INNER_NAME);
      if (name == null) {
        name = category.get(Category.NAME);
      }
      createUpdatableCategorySelector(category, seriesKey, this.name, budgetArea, cellBuilder,
                                      seriesName + ":" + name);
    }
  }
}
