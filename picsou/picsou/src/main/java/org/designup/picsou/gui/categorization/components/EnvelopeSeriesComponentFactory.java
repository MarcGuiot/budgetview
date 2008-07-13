package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.SeriesToCategory;
import org.designup.picsou.model.Category;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class EnvelopeSeriesComponentFactory extends AbstractSeriesComponentFactory {

  public EnvelopeSeriesComponentFactory(JToggleButton invisibleToggle,
                                        GlobRepository repository,
                                        Directory directory) {
    super(invisibleToggle, repository, directory);
  }

  public void registerComponents(RepeatCellBuilder cellBuilder, final Glob series) {
    cellBuilder.add("envelopeSeriesName",
                    new JLabel(seriesStringifier.toString(series, repository)));

    cellBuilder.addRepeat("envelopeCategoryRepeat",
                          repository.findLinkedTo(series, SeriesToCategory.SERIES).sort(SeriesToCategory.ID),
                          new EnvelopeCategoriesComponentFactory(seriesStringifier.toString(series, repository),
                                                                 "envelopeCategoryToggle",
                                                                 BudgetArea.EXPENSES_ENVELOPE)
    );
  }

  private class EnvelopeCategoriesComponentFactory implements RepeatComponentFactory<Glob> {
    private String seriesName;
    private String name;
    private BudgetArea budgetArea;

    public EnvelopeCategoriesComponentFactory(String seriesName, String name, BudgetArea budgetArea) {
      this.seriesName = seriesName;
      this.name = name;
      this.budgetArea = budgetArea;
    }

    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob seriesToCategory) {
      Glob category = repository.findLinkTarget(seriesToCategory, SeriesToCategory.CATEGORY);
      final Key seriesKey = seriesToCategory.getTargetKey(SeriesToCategory.SERIES);

      createUpdatableCategoryToggle(category, seriesKey, name, budgetArea, cellBuilder,
                                    seriesName + ":" + category.get(Category.NAME));      
    }
  }
}
