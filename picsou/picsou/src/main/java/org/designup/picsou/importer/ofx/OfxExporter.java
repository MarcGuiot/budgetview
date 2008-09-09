package org.designup.picsou.importer.ofx;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import java.io.Writer;
import java.util.Collections;
import java.util.Date;

public class OfxExporter {

  private GlobRepository repository;
  private OfxWriter writer;

  public static void write(GlobRepository globRepository, Writer writer) {
    OfxExporter exporter = new OfxExporter(globRepository, writer);
    exporter.write();
  }

  private OfxExporter(GlobRepository globRepository, Writer writer) {
    this.repository = globRepository;
    this.writer = new OfxWriter(writer);
  }

  private void write() {
    writer.writeHeader();

    GlobList accounts = repository.getAll(Account.TYPE).sort(Account.ID);
    for (Glob account : accounts) {
      if (Account.SUMMARY_ACCOUNT_ID == account.get(Account.ID)) {
        continue;
      }
      if (!Boolean.TRUE.equals(account.get(Account.IS_CARD_ACCOUNT))) {
        writer.writeBankMsgHeader(account.get(Account.BANK_ENTITY), account.get(Account.BRANCH_ID), account.get(Account.NUMBER));
        writeTransactions(account);
        Date updateDate = account.get(Account.UPDATE_DATE);
        writer.writeBankMsgFooter(account.get(Account.BALANCE), toString(updateDate != null ? updateDate : new Date()));
      }
    }

    for (Glob account : accounts) {
      if (Account.SUMMARY_ACCOUNT_ID == account.get(Account.ID)) {
        continue;
      }
      if (Boolean.TRUE.equals(account.get(Account.IS_CARD_ACCOUNT))) {
        writer.writeCardMsgHeader(account.get(Account.NUMBER));
        writeTransactions(account);
        writer.writeCardMsgFooter(account.get(Account.BALANCE), toString(account.get(Account.UPDATE_DATE)));
      }
    }
    writer.writeFooter();
  }

  private void writeTransactions(Glob account) {
    GlobList transactionsToWrite = new GlobList(repository.findLinkedTo(account, Transaction.ACCOUNT));
    Collections.sort(transactionsToWrite, TransactionComparator.ASCENDING);
    for (Glob transaction : transactionsToWrite) {
      writeTransaction(transaction);
    }
  }

  private void writeTransaction(Glob transaction) {

    OfxWriter.OfxTransactionWriter writer =
      this.writer.startTransaction(Long.toString(Transaction.fullDate(transaction)),
                                   Long.toString(Transaction.fullBankDate(transaction)),
                                   transaction.get(Transaction.AMOUNT),
                                   transaction.get(Transaction.ID),
                                   transaction.get(Transaction.ORIGINAL_LABEL));

    Glob category = repository.findLinkTarget(transaction, Transaction.CATEGORY);
    if (category != null && !category.get(Category.ID).equals(MasterCategory.NONE.getId())) {
      if (Category.isMaster(category)) {
        writeCategory("", writer, category);
      }
      else {
        writeCategory("", writer, repository.get(Key.create(Category.TYPE, category.get(Category.MASTER))));
        writeCategory("sub", writer, category);
      }
    }

    writer.add("note", transaction.get(Transaction.NOTE));
    if (transaction.get(Transaction.SPLIT_SOURCE) != null) {
      writer.add("PARENT", "PICSOU" + transaction.get(Transaction.SPLIT_SOURCE));
    }

    if (Boolean.TRUE.equals(transaction.get(Transaction.DISPENSABLE))) {
      writer.add("DISPENSABLE", "true");
    }
    writer.end();
  }

  private void writeCategory(String prefix, OfxWriter.OfxTransactionWriter writer, Glob category) {
    if (category.get(Category.INNER_NAME) == null) {
      writer.add(prefix + "category", category.get(Category.NAME));
    }
    else {
      writer.add(prefix + "category", Integer.toString(category.get(Category.ID)));
    }
  }

  private String toString(Date updateDate) {
    return (OfxImporter.DATE_FORMAT.format(updateDate) + "000000");
  }
}
