package org.designup.picsou.gui.importer.edition;

import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.utils.Utils;
import org.designup.picsou.model.ImportedTransaction;
import org.designup.picsou.gui.description.Formatting;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Comparator;

public class ImportedTransactionDateRenderer implements LabelCustomizer {
  private GlobTableView transactionTable;
  private SimpleDateFormat format;

  public void changeDateFormat(String dateFormat) {
    if (dateFormat == null) {
      format = null;
    }
    else {
      format = new SimpleDateFormat(dateFormat);
    }
    transactionTable.refresh(false);
  }

  public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
    if (format == null) {
      label.setText(glob.get(ImportedTransaction.BANK_DATE));
      return;
    }
    Date date;
    try {
      date = format.parse(glob.get(ImportedTransaction.BANK_DATE));
      label.setText(Formatting.toString(date));
    }
    catch (ParseException e) {
      label.setText("Failed to parse date");
    }
  }

  public void setTable(GlobTableView transactionTable) {
    this.transactionTable = transactionTable;
  }

  public Comparator<Glob> getComparator() {
    return new Comparator<Glob>() {
      public int compare(Glob transaction1, Glob transaction2) {
        if (format == null) {
          return Utils.compare(transaction1.get(ImportedTransaction.BANK_DATE), transaction2.get(ImportedTransaction.BANK_DATE));
        }
        try {
          Date date1 = format.parse(transaction1.get(ImportedTransaction.BANK_DATE));
          Date date2 = format.parse(transaction2.get(ImportedTransaction.BANK_DATE));
          int compareResult = date2.compareTo(date1);
          if (compareResult == 0) {
            Integer id2 = transaction2.get(ImportedTransaction.ID);
            Integer id1 = transaction1.get(ImportedTransaction.ID);
            return Utils.compare(id2, id1);
          }
          return compareResult;
        }
        catch (ParseException e) {
          return Utils.compare(transaction1.get(ImportedTransaction.BANK_DATE), transaction2.get(ImportedTransaction.BANK_DATE));
        }
      }
    };
  }
}
