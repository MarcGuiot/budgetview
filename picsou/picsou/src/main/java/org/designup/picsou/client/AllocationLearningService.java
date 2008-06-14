package org.designup.picsou.client;

import org.designup.picsou.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class AllocationLearningService {

  public void learn(final Glob transaction, final Integer categoryId, final GlobRepository repository) {
    Integer previousCategory = transaction.get(Transaction.CATEGORY);

    Transaction.setCategory(transaction, categoryId, repository);
    String label = extractCategorisationPattern(transaction);
    if (Strings.isNullOrEmpty(label)) {
      return;
    }
    if (previousCategory != null) {
      unlearnIfLastTransaction(previousCategory, label, repository);
    }
    learnLocal(label, categoryId, repository);
  }

  public GlobList getTransactionsToBeLearned(final Glob transaction, final Integer categoryId,
                                             final GlobRepository repository) {
    if (Utils.equal(Category.NONE, categoryId)) {
      return GlobList.EMPTY;
    }

    final String label = extractCategorisationPattern(transaction);
    if (label == null) {
      return GlobList.EMPTY;
    }

    return repository.findByIndex(Transaction.LABEL_FOR_CATEGORISATION_INDEX, label);
  }

  public void deleteCategory(Integer deletedCategory, Integer parentCategory, GlobRepository repository) {
    GlobList labelToCategories = repository.getAll(LabelToCategory.TYPE);
    MultiMap<String, Glob> labels = new MultiMap<String, Glob>();
    for (Glob labelToCategory : labelToCategories) {
      labels.put(labelToCategory.get(LabelToCategory.LABEL), labelToCategory);
    }
    for (Glob labelToCategory : labelToCategories) {
      if (labelToCategory.get(LabelToCategory.CATEGORY).equals(deletedCategory)) {
        if (!parentCategory.equals(Category.ALL) && !parentCategory.equals(Category.NONE)) {
          List<Glob> list = labels.get(labelToCategory.get(LabelToCategory.LABEL));
          boolean updated = false;
          for (Glob glob : list) {
            if (parentCategory.equals(glob.get(LabelToCategory.CATEGORY))) {
              repository.update(glob.getKey(), LabelToCategory.COUNT, glob.get(LabelToCategory.COUNT) + 1);
              updated = true;
              break;
            }
          }
          if (!updated) {
            FieldValues fieldValues = FieldValuesBuilder.init()
              .set(LabelToCategory.COUNT, 1)
              .set(LabelToCategory.CATEGORY, parentCategory)
              .set(LabelToCategory.LABEL, labelToCategory.get(LabelToCategory.LABEL)).get();
            repository.create(LabelToCategory.TYPE, fieldValues.toArray());
          }
        }
        repository.delete(labelToCategory.getKey());
      }
    }
  }

  private void unlearnIfLastTransaction(Integer previousCategory, String label, GlobRepository repository) {
    GlobList globList = repository.findByIndex(Transaction.LABEL_FOR_CATEGORISATION_INDEX, label);
    for (Glob transaction : globList) {
      Integer category = transaction.get(Transaction.CATEGORY);
      if (previousCategory.equals(category)) {
        return;
      }
    }
    GlobList labelToCategories = repository.findByIndex(LabelToCategory.LABEL_INDEX, label);
    for (Glob glob : labelToCategories) {
      if (previousCategory.equals(glob.get(LabelToCategory.CATEGORY))) {
        repository.delete(glob.getKey());
      }
    }
  }

  private void learnLocal(String label, Integer categoryId, GlobRepository repository) {
    if (Utils.equal(Category.NONE, categoryId)) {
      return;
    }
    GlobList transactions = repository.findByIndex(Transaction.LABEL_FOR_CATEGORISATION_INDEX, label);
    for (Glob transaction : transactions) {
      Integer category = transaction.get(Transaction.CATEGORY);
      if (category == null || category.equals(Category.NONE)) {
        Transaction.setCategory(transaction, categoryId, repository);
      }
    }
    GlobList all = repository.findByIndex(LabelToCategory.LABEL_INDEX, label);
    UpdateLabelToCategory toCategory = new UpdateLabelToCategory(label, categoryId, repository);
    all.safeApply(toCategory);
    if (!toCategory.isManaged()) {
      repository.create(LabelToCategory.TYPE,
                        value(LabelToCategory.CATEGORY, categoryId),
                        value(LabelToCategory.LABEL, label),
                        value(LabelToCategory.COUNT, 1));
    }
  }

  private boolean match(Glob transaction, String label) {
    String pattern = extractCategorisationPattern(transaction);
    return pattern != null && pattern.equals(label);
  }

  private String extractCategorisationPattern(Glob transaction) {
    return transaction.get(Transaction.LABEL_FOR_CATEGORISATION);
  }

  public void setCategories(List<Glob> transactions, GlobRepository repository) {
    for (Glob transaction : transactions) {
      if (TransactionType.isOfType(transaction, TransactionType.CREDIT_CARD)
          || TransactionType.isOfType(transaction, TransactionType.PRELEVEMENT)
          || TransactionType.isOfType(transaction, TransactionType.VIREMENT)) {
        String label = transaction.get(Transaction.LABEL_FOR_CATEGORISATION);
        if (!Strings.isNullOrEmpty(label)) {
          propagateLocal(label, transaction, repository);
        }
      }
      else if (TransactionType.isOfType(transaction, TransactionType.BANK_FEES)) {
        Transaction.setCategory(transaction, MasterCategory.BANK.getId(), repository);
      }
    }
  }

  private boolean propagateLocal(String label, Glob transaction, GlobRepository repository) {
    GlobList labelToCategories = repository.findByIndex(LabelToCategory.LABEL_INDEX, label);
    List<FieldValues> ttcToCreate = new ArrayList<FieldValues>();
    for (Glob labelToCategory : labelToCategories) {
      if (labelToCategory.get(LabelToCategory.LABEL).equals(label)) {
        ttcToCreate.add(FieldValuesBuilder.init()
          .set(TransactionToCategory.TRANSACTION, transaction.get(Transaction.ID))
          .set(TransactionToCategory.CATEGORY, labelToCategory.get(LabelToCategory.CATEGORY))
          .get());
      }
    }
    if (ttcToCreate.size() == 1) {
      Transaction.setCategory(transaction, ttcToCreate.get(0).get(TransactionToCategory.CATEGORY), repository);
    }
    else {
      for (FieldValues fieldValues : ttcToCreate) {
        repository.create(TransactionToCategory.TYPE, fieldValues.toArray());
      }
    }
    return !ttcToCreate.isEmpty();
  }

  public static String anonymise(String node, String label, Integer transactionType) {
    String labelOrNote;
    if (TransactionType.getId(TransactionType.CHECK).equals(transactionType)
        || TransactionType.getId(TransactionType.WITHDRAWAL).equals(transactionType)
        || TransactionType.getId(TransactionType.DEPOSIT).equals(transactionType)) {
      labelOrNote = node;
    }
    else {
      labelOrNote = label;
    }
    if (labelOrNote == null || labelOrNote.length() == 0) {
      return null;
    }
    return anonymise(labelOrNote);
  }

  private static String anonymise(String labelOrNote) {
    String[] strings = labelOrNote.split(" ");
    StringBuilder builder = new StringBuilder();
    for (String string : strings) {
      int i = string.length() - 1;
      boolean ok = true;
      while (i > 0) {
        char c = string.charAt(i);
        if (c >= '0' && c < '9') {
          ok = false;
          break;
        }
        i--;
      }
      if (ok) {
        builder.append(" ").append(string);
      }
    }
    return builder.toString().replaceAll("  *", " ").trim();
  }

  private static class UpdateLabelToCategory implements GlobFunctor {
    private boolean managed = false;
    private final String label;
    private final Integer categoryId;
    private final GlobRepository repository;

    public UpdateLabelToCategory(String label, Integer categoryId, GlobRepository repository) {
      this.label = label;
      this.categoryId = categoryId;
      this.repository = repository;
    }

    public void run(Glob glob) throws Exception {
      if (glob.get(LabelToCategory.LABEL).equals(label) && glob.get(LabelToCategory.CATEGORY).equals(categoryId)) {
        repository.update(glob.getKey(), LabelToCategory.COUNT, glob.get(LabelToCategory.COUNT) + 1);
        managed = true;
      }
    }

    public boolean isManaged() {
      return managed;
    }
  }
}
