package org.designup.picsou.gui.printing.transactions;

import com.budgetview.shared.utils.AmountFormat;
import org.designup.picsou.gui.description.stringifiers.AccountStringifier;
import org.designup.picsou.gui.description.stringifiers.TransactionDateStringifier;
import org.designup.picsou.gui.description.stringifiers.TransactionSeriesStringifier;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;

import java.awt.*;

public class TransactionsContext {

  private GlobStringifier seriesStringifier = new TransactionSeriesStringifier();
  private GlobStringifier dateStringifier =
    new TransactionDateStringifier(TransactionComparator.ASCENDING,
                                   Transaction.BANK_MONTH, Transaction.BANK_DAY);
  private GlobStringifier accountStringifier = new AccountStringifier();
  private GlobRepository repository;

  public TransactionsContext(GlobRepository repository) {
    this.repository = repository;
  }

  public TransactionBlockMetrics getMetrics(Dimension area, Graphics2D g2, Font labelFont, Font defaultFont) {
    return new TransactionBlockMetrics(area, g2, labelFont, defaultFont);
  }

  public String getSeriesName(Glob transaction) {
    return seriesStringifier.toString(transaction, repository);
  }

  public String getBankDate(Glob transaction) {
    return dateStringifier.toString(transaction, repository);
  }

  public String getAccountLabel(Glob transaction) {
    Glob account = repository.findLinkTarget(transaction, Transaction.ACCOUNT);
    if (account == null) {
      return "";
    }
    String amount = AmountFormat.toString(transaction.get(Transaction.ACCOUNT_POSITION));

    return accountStringifier.toString(account, repository) + " : " + amount;
  }
}
