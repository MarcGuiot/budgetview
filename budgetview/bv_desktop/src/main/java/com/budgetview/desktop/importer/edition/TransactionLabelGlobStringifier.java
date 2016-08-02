package com.budgetview.desktop.importer.edition;

import com.budgetview.model.ImportType;
import com.budgetview.model.ImportedTransaction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.utils.exceptions.InvalidState;

public class TransactionLabelGlobStringifier extends AbstractGlobStringifier {

  public String toString(Glob transaction, GlobRepository repository) {
    if (transaction == null) {
      return null;
    }
    ImportType importType = ImportedTransaction.getImportType(transaction);
    if (importType == null || importType == ImportType.OFX) {
      StringBuilder builder = new StringBuilder();
      complete(builder, transaction.get(ImportedTransaction.OFX_NAME));
      complete(builder, transaction.get(ImportedTransaction.OFX_CHECK_NUM));
      complete(builder, transaction.get(ImportedTransaction.OFX_MEMO));
      return builder.toString();
    }
    else if (importType == ImportType.QIF || importType == ImportType.CSV) {
      StringBuilder builder = new StringBuilder();
      complete(builder, transaction.get(ImportedTransaction.QIF_M));
      complete(builder, transaction.get(ImportedTransaction.QIF_P));
      return builder.toString();
    }
    else if (importType == ImportType.JSON) {
      return transaction.get(ImportedTransaction.OFX_NAME);
    }

    throw new InvalidState("Unexpected import type for " + transaction);
  }

  private void complete(StringBuilder builder, String s) {
    if (s == null) {
      return;
    }
    if (builder.length() != 0) {
      builder.append(":");
    }
    builder.append(s);
  }
}
