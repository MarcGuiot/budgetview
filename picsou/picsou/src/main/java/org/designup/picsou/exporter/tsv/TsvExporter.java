package org.designup.picsou.exporter.tsv;

import org.designup.picsou.exporter.Exporter;
import org.designup.picsou.gui.description.AmountStringifier;
import org.designup.picsou.gui.description.TransactionDateStringifier;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.GlobStringifiers;
import org.globsframework.model.utils.GlobMatchers;
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

    writer.write("Date\tBank date\tLabel\tAmount\tBudget area\tSeries\n");

    GlobList transactions =
      repository
        .getAll(Transaction.TYPE, GlobMatchers.fieldEquals(Transaction.PLANNED, Boolean.FALSE))
        .sort(TransactionComparator.DESCENDING_SPLIT_AFTER);

    List<GlobStringifier> stringifiers = Arrays.asList(
      new TransactionDateStringifier(TransactionComparator.DESCENDING_SPLIT_AFTER,
                                     Transaction.MONTH,
                                     Transaction.DAY),
      new TransactionDateStringifier(TransactionComparator.DESCENDING_SPLIT_AFTER,
                                     Transaction.BANK_MONTH,
                                     Transaction.BANK_DAY),
      GlobStringifiers.get(Transaction.LABEL),
      new AmountStringifier(Transaction.AMOUNT),
      GlobStringifiers.target(Transaction.SERIES, descriptionService.getStringifier(Series.BUDGET_AREA)),
      descriptionService.getStringifier(Transaction.SERIES)
    );

    for (Glob transaction : transactions) {
      boolean firstItem = true;
      for (GlobStringifier stringifier : stringifiers) {
        if (!firstItem) {
          writer.write("\t");
        }
        writer.write(stringifier.toString(transaction, repository));
        firstItem = false;
      }
      writer.write("\n");
    }
  }
}