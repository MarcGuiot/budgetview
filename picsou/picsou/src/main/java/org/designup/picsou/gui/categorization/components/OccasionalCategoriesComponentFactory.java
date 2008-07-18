package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.description.CategoryComparator;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Series;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.KeyBuilder;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class OccasionalCategoriesComponentFactory extends AbstractSeriesComponentFactory {
  private String seriesName;
  private String name;
  private BudgetArea budgetArea;
  private PicsouDialog dialog;

  public OccasionalCategoriesComponentFactory(String seriesName, String name, BudgetArea budgetArea,
                                              JToggleButton invisibleToggle, GlobRepository repository,
                                              Directory directory, PicsouDialog dialog) {
    super(invisibleToggle, repository, directory, dialog);
    this.seriesName = seriesName;
    this.name = name;
    this.budgetArea = budgetArea;
    this.dialog = dialog;
  }

  public void registerComponents(RepeatCellBuilder cellBuilder, final Glob master) {
    final Key seriesKey = KeyBuilder.newKey(Series.TYPE, Series.OCCASIONAL_SERIES_ID);

    createUpdatableCategoryToggle(
      master, seriesKey, name, budgetArea, cellBuilder,
      seriesName + ":" + master.get(Category.NAME), dialog);

    GlobsPanelBuilder.addRepeat("occasionalSubcatRepeat",
                                Category.TYPE,
                                GlobMatchers.linkedTo(master, Category.MASTER),
                                new CategoryComparator(repository, directory),
                                repository,
                                cellBuilder,
                                new RepeatComponentFactory<Glob>() {
                                  public void registerComponents(RepeatCellBuilder subcatCellBuilder, Glob subcat) {
                                    final String toggleName =
                                      seriesName + ":" + master.get(Category.NAME) + ":" + subcat.get(Category.NAME);
                                    createUpdatableCategoryToggle(
                                      subcat, seriesKey, "occasionalSubcatToggle",
                                      budgetArea, subcatCellBuilder, toggleName,
                                      dialog);
                                  }
                                });

  }
}
