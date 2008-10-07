package org.designup.picsou.gui.series.wizard;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.ProfileType;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.MultiMap;

import java.util.List;

public class SeriesWizardModel {
  private MultiMap<BudgetArea, SeriesWizardEntry> entries = new MultiMap<BudgetArea, SeriesWizardEntry>();
  private BudgetArea[] budgetAreas = {BudgetArea.INCOME,
                                      BudgetArea.RECURRING,
                                      BudgetArea.ENVELOPES,
                                      BudgetArea.SAVINGS};
  private GlobRepository repository;

  public SeriesWizardModel(GlobRepository repository) {
    this.repository = repository;
    createEntries();
  }

  public BudgetArea[] getBudgetAreas() {
    return budgetAreas;
  }

  public List<SeriesWizardEntry> getEntries(BudgetArea budgetArea) {
    return entries.get(budgetArea);
  }

  public void createSeries(GlobRepository repository) {
    repository.enterBulkDispatchingMode();
    try {
      for (BudgetArea budgetArea : budgetAreas) {
        for (SeriesWizardEntry entry : entries.get(budgetArea)) {
          entry.createSeries(repository);
        }
      }
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
  }

  private void createEntries() {
    createEntry(BudgetArea.INCOME, ProfileType.EVERY_MONTH, "income1", MasterCategory.INCOME, false);
    createEntry(BudgetArea.INCOME, ProfileType.EVERY_MONTH, "income2", MasterCategory.INCOME, false);
    createEntry(BudgetArea.INCOME, ProfileType.IRREGULAR, "exceptional", MasterCategory.INCOME, false);

    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "rent", MasterCategory.HOUSE, "loyer", false);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "mortgage", MasterCategory.HOUSE, "credit", false);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "electricity", MasterCategory.HOUSE, "energie", false);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "gas", MasterCategory.HOUSE, "energie", false);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "carCredit", MasterCategory.TRANSPORTS, false);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "carInsurance", MasterCategory.TRANSPORTS, false);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "incomeTaxes", MasterCategory.TAXES, false);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "cellPhone1", MasterCategory.TELECOMS, false);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "cellPhone2", MasterCategory.TELECOMS, false);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "internet", MasterCategory.TELECOMS, false);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "fixedPhone", MasterCategory.TELECOMS, false);

    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "groceries", MasterCategory.FOOD, false);
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "health", MasterCategory.HEALTH, true);
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "leisures", MasterCategory.LEISURES, true);
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "clothing", MasterCategory.CLOTHING, true);
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "fuel", MasterCategory.TRANSPORTS, true);
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "cash", MasterCategory.CASH, false);

    createEntry(BudgetArea.SAVINGS, ProfileType.EVERY_MONTH, "regular", MasterCategory.SAVINGS, false);
    createEntry(BudgetArea.SAVINGS, ProfileType.IRREGULAR, "irregular", MasterCategory.SAVINGS, false);
  }

  private void createEntry(BudgetArea budgetArea,
                           ProfileType profileType,
                           String nameKey,
                           MasterCategory category,
                           boolean addSubCategories) {
    createEntry(budgetArea, profileType, nameKey, category, null, addSubCategories);
  }

  private void createEntry(BudgetArea budgetArea,
                           ProfileType profileType,
                           String nameKey,
                           MasterCategory category,
                           String subCategory,
                           boolean addSubCategories) {
    SeriesWizardEntry entry = new SeriesWizardEntry(budgetArea, profileType, nameKey,
                                                    category, subCategory, addSubCategories, repository);
    entries.put(budgetArea, entry);
  }
}
