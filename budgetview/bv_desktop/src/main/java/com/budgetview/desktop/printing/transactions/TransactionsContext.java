package com.budgetview.desktop.printing.transactions;

import com.budgetview.desktop.description.stringifiers.AccountStringifier;
import com.budgetview.desktop.description.stringifiers.TransactionDateStringifier;
import com.budgetview.desktop.description.stringifiers.TransactionSeriesStringifier;
import com.budgetview.model.Transaction;
import com.budgetview.shared.utils.AmountFormat;
import com.budgetview.utils.TransactionComparator;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;

import java.awt.*;

public class TransactionsContext {

  private GlobStringifier seriesStringifier = new TransactionSeriesStringifier();
  private GlobStringifier userDateStringifier =
    new TransactionDateStringifier(TransactionComparator.ASCENDING,
                                   Transaction.MONTH, Transaction.DAY);
  private GlobStringifier bankDateStringifier =
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

  public String getUserDate(Glob transaction) {
    return userDateStringifier.toString(transaction, repository);
  }

  public String getBankDate(Glob transaction) {
    return bankDateStringifier.toString(transaction, repository);
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
