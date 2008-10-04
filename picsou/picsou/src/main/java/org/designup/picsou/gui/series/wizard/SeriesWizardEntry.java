package org.designup.picsou.gui.series.wizard;

import org.designup.picsou.gui.utils.PicsouMatchers;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.exceptions.ItemNotFound;

public class SeriesWizardEntry {
  private BudgetArea budgetArea;
  private String name;
  private ProfileType profileType;
  private MasterCategory master;
  private String subCategory;
  private boolean addSubCategories;
  private boolean selected;
  private Integer categoryId;

  public SeriesWizardEntry(BudgetArea budgetArea, ProfileType profileType, String key,
                           MasterCategory master, String subCategory, boolean addSubCategories,
                           GlobRepository repository) {
    this.budgetArea = budgetArea;
    this.profileType = profileType;
    this.name = Lang.get("seriesWizard." + budgetArea.getName() + "." + key);
    this.master = master;
    this.subCategory = subCategory;
    this.categoryId = getCategoryId(repository);
    this.addSubCategories = addSubCategories;
    this.selected = budgetArea == BudgetArea.ENVELOPES;
  }

  public String getName() {
    return name;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public boolean isSelected() {
    return selected;
  }

  public void createSeries(GlobRepository repository) {
    if (!selected) {
      return;
    }

    Glob series = repository.create(Series.TYPE,
                                    value(Series.LABEL, name),
                                    value(Series.NAME, name),
                                    value(Series.BUDGET_AREA, budgetArea.getId()),
                                    value(Series.PROFILE_TYPE, profileType.getId()),
                                    value(Series.DEFAULT_CATEGORY, categoryId));

    if (budgetArea.isMultiCategories()) {
      GlobList all = new GlobList();
      all.add(repository.get(Key.create(Category.TYPE, categoryId)));
      if (addSubCategories) {
        all.addAll(repository.getAll(Category.TYPE, PicsouMatchers.subCategories(master.getId())));
      }
      for (Glob subcat : all) {
        repository.create(SeriesToCategory.TYPE,
                          value(SeriesToCategory.CATEGORY, subcat.get(Category.ID)),
                          value(SeriesToCategory.SERIES, series.get(Series.ID)));
      }
    }
  }

  private Integer getCategoryId(GlobRepository repository) {
    if (subCategory == null) {
      return master.getId();
    }
    String innerName = Category.getInnerName(master, subCategory);
    Glob subcat = repository.findUnique(Category.TYPE, GlobMatchers.fieldEquals(Category.INNER_NAME, innerName));
    if (subcat == null) {
      throw new ItemNotFound(innerName);
    }
    return subcat.get(Category.ID);
  }
}
