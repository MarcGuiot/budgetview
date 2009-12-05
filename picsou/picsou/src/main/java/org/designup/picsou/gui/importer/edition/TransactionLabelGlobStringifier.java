package org.designup.picsou.gui.importer.edition;

import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.designup.picsou.model.ImportedTransaction;

public class TransactionLabelGlobStringifier extends AbstractGlobStringifier {

  public String toString(Glob glob, GlobRepository repository) {
    if (glob.isTrue(ImportedTransaction.IS_OFX)) {
      StringBuilder builder = new StringBuilder();
      complete(builder, glob.get(ImportedTransaction.OFX_NAME));
      complete(builder, glob.get(ImportedTransaction.OFX_CHECK_NUM));
      complete(builder, glob.get(ImportedTransaction.OFX_MEMO));
      return builder.toString();
    }
    else {
      StringBuilder builder = new StringBuilder();
      complete(builder, glob.get(ImportedTransaction.QIF_M));
      complete(builder, glob.get(ImportedTransaction.QIF_P));
      return builder.toString();
    }

  }

  void complete(StringBuilder builder, String s) {
    if (s == null) {
      return;
    }
    if (builder.length() != 0) {
      builder.append(":");
    }
    builder.append(s);
  }
}
