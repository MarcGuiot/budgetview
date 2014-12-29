package org.designup.picsou.exporter.tsv;

import org.designup.picsou.exporter.Exporter;
import org.designup.picsou.gui.description.stringifiers.AmountStringifier;
import org.designup.picsou.gui.description.stringifiers.TransactionDateStringifier;
import org.designup.picsou.gui.transactions.utils.TransactionMatchers;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.GlobStringifiers;
import org.globsframework.utils.directory.Directory;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

public class TsvExporter implements Exporter {

  private DescriptionService descriptionService;

  public TsvExporter(Directory directory) {
    descriptionService = directory.get(DescriptionService.class);
  }

  public String getType() {
    return "tsv";
  }

  public String getExtension() {
    return "txt";
  }

  public void export(GlobRepository repository, Writer writer) throws IOException {

    boolean first = true;
    for (String header : new String[]{"transactionView.date.user",
                                      "transactionView.date.bank",
                                      "transactionView.date.budget",
                                      "label",
                                      "amount",
                                      "account",
                                      "budgetArea",
                                      "series"}) {
      if (!first) {
        writer.write('\t');
      }
      writer.write(Lang.get(header));
      first = false;
    }
    writer.write('\n');

    GlobList transactions =
      repository
        .getAll(Transaction.TYPE, TransactionMatchers.exportableTransactions())
        .sort(TransactionComparator.DESCENDING_SPLIT_AFTER);

    List<GlobStringifier> stringifiers = Arrays.asList(
      new TransactionDateStringifier(TransactionComparator.DESCENDING_SPLIT_AFTER,
                                     Transaction.MONTH,
                                     Transaction.DAY),
      new TransactionDateStringifier(TransactionComparator.DESCENDING_SPLIT_AFTER,
                                     Transaction.POSITION_MONTH,
                                     Transaction.POSITION_DAY),
      new TransactionDateStringifier(TransactionComparator.DESCENDING_SPLIT_AFTER,
                                     Transaction.BUDGET_MONTH,
                                     Transaction.BUDGET_DAY),
      GlobStringifiers.get(Transaction.LABEL),
      new AmountStringifier(Transaction.AMOUNT),
      descriptionService.getStringifier(Transaction.ACCOUNT),
      GlobStringifiers.target(Transaction.SERIES, descriptionService.getStringifier(Series.BUDGET_AREA)),
      descriptionService.getStringifier(Transaction.SERIES)
    );

    for (Glob transaction : transactions) {
      if (Transaction.isOpenCloseAccount(transaction)) {
        continue;
      }
      boolean firstItem = true;
      for (GlobStringifier stringifier : stringifiers) {
        if (!firstItem) {
          writer.write("\t");
        }
        String str = stringifier.toString(transaction, repository);
        writer.write(str == null ? "" : str);
        firstItem = false;
      }
      writer.write("\n");
    }
  }
}
