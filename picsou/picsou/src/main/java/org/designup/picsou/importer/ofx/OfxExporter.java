package org.designup.picsou.importer.ofx;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.GlobPrinter;
import org.crossbowlabs.globs.model.utils.GlobUtils;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.TransactionComparator;

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

    GlobList accounts = repository.getAll(Account.TYPE);
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
    Collections.sort(transactionsToWrite, new TransactionComparator(true));
    for (Glob transaction : transactionsToWrite) {
      writeTransaction(transaction);
    }
  }

  private void writeTransaction(Glob transaction) {
    String stringifiedDate = Long.toString(Transaction.fullDate(transaction));

    OfxWriter.OfxTransactionWriter writer =
      this.writer.startTransaction(stringifiedDate,
                                   transaction.get(Transaction.AMOUNT),
                                   transaction.get(Transaction.ID),
                                   transaction.get(Transaction.ORIGINAL_LABEL));

    GlobList categories =
      GlobUtils.getTargets(repository.findLinkedTo(transaction, TransactionToCategory.TRANSACTION),
                           TransactionToCategory.CATEGORY, repository)
        .sort(Category.NAME);
    categories.add(repository.findLinkTarget(transaction, Transaction.CATEGORY));
    for (Glob category : categories) {
      if (category != null) {
        if (Category.isMaster(category)) {
          writer.add("category", category.get(Category.NAME).toLowerCase());
        }
        else {
          MasterCategory master = MasterCategory.getMaster(category.get(Category.MASTER));
          writer.add("category", master.getName().toLowerCase());
          writer.add("subcategory", category.get(Category.NAME));
        }
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

  private String toString(Date updateDate) {
    return (OfxImporter.DATE_FORMAT.format(updateDate) + "000000");
  }
}
