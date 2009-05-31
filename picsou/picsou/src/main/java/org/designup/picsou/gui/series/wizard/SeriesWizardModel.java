package org.designup.picsou.gui.series.wizard;

import org.designup.picsou.model.BudgetArea;
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
    repository.startChangeSet();
    try {
      for (BudgetArea budgetArea : budgetAreas) {
        for (SeriesWizardEntry entry : entries.get(budgetArea)) {
          entry.createSeries(repository);
        }
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private void createEntries() {
    createEntry(BudgetArea.INCOME, ProfileType.EVERY_MONTH, "income1");
    createEntry(BudgetArea.INCOME, ProfileType.EVERY_MONTH, "income2");
    createEntry(BudgetArea.INCOME, ProfileType.IRREGULAR, "exceptional");

    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "rent", "loyer");
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "mortgage", "credit");
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "electricity", "energie");
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "gas", "energie");
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "water", "energie");
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "carCredit");
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "carInsurance");
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "incomeTaxes");
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "cellPhone1");
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "cellPhone2");
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "internet");
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "fixedPhone");

    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "groceries");
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "health");
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "leisures");
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "clothing");
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "fuel", "essence");
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "cash");
  }

  private void createEntry(BudgetArea budgetArea,
                           ProfileType profileType,
                           String nameKey,
                           String... subSeries) {
    SeriesWizardEntry entry = new SeriesWizardEntry(budgetArea, profileType, nameKey,
                                                    subSeries, repository);
    entries.put(budgetArea, entry);
  }
}
