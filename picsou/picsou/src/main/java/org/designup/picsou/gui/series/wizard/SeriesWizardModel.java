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
    createEntry(BudgetArea.INCOME, ProfileType.EVERY_MONTH, "income1", true);
    createEntry(BudgetArea.INCOME, ProfileType.EVERY_MONTH, "income2", false);
    createEntry(BudgetArea.INCOME, ProfileType.IRREGULAR, "exceptional", true);

    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "rent", false);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "mortgage", false);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "electricity", true);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "gas", true);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "water", true);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "carCredit", true);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "carInsurance", true);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "incomeTaxes", true);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "cellPhone1", true);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "cellPhone2", false);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "internet", true);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "fixedPhone", false);

    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "groceries", true);
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "health", true, "physician", "pharmacy", "reimbursements");
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "leisures", true);
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "clothing", true);
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "beauty", true);
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "fuel", true);
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "cash", true);
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "bankFees", true);
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "misc", true);
  }

  private void createEntry(BudgetArea budgetArea,
                           ProfileType profileType,
                           String nameKey,
                           boolean selected,
                           String... subSeries) {
    SeriesWizardEntry entry = new SeriesWizardEntry(budgetArea, profileType, nameKey,
                                                    subSeries, repository,
                                                    selected);
    entries.put(budgetArea, entry);
  }
}
