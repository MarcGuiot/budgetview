package com.budgetview.importer.utils;

import com.budgetview.model.Transaction;
import com.budgetview.model.ImportedTransaction;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.repository.GlobIdGenerator;

public class ImportedTransactionIdGenerator implements GlobIdGenerator {
  private final GlobIdGenerator originalGenerator;

  public ImportedTransactionIdGenerator(GlobIdGenerator originalGenerator) {
    this.originalGenerator = originalGenerator;
  }

  public int getNextId(IntegerField keyField, int idCount) {
    if (keyField.equals(ImportedTransaction.ID)) {
      return originalGenerator.getNextId(Transaction.ID, idCount);
    }
    return originalGenerator.getNextId(keyField, idCount);
  }
}
