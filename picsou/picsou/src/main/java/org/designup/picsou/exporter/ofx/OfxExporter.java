package org.designup.picsou.exporter.ofx;

import org.designup.picsou.exporter.Exporter;
import org.designup.picsou.gui.utils.PicsouMatchers;
import org.designup.picsou.importer.ofx.OfxImporter;
import org.designup.picsou.importer.ofx.OfxWriter;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Date;

public class OfxExporter implements Exporter {

  private GlobRepository repository;
  private OfxWriter writer;
  private boolean exportCustomFields;

  public static void write(GlobRepository repository, Writer writer, boolean exportCustomFields) throws IOException {
    OfxExporter exporter = new OfxExporter(exportCustomFields);
    exporter.export(repository, writer);
  }

  public OfxExporter(boolean exportCustomFields) {
    this.exportCustomFields = exportCustomFields;
  }

  public String getType() {
    return "ofx";
  }

  public String getExtension() {
    return "ofx";
  }

  public void export(GlobRepository repository, Writer writer) throws IOException {
    this.repository = repository;
    this.writer = new OfxWriter(writer);
    write();
  }

  private void write() {
    writer.writeHeader();

    GlobList accounts = repository.getAll(Account.TYPE).sort(Account.ID);
    for (Glob account : accounts) {
      if (Account.SUMMARY_ACCOUNT_IDS.contains(account.get(Account.ID))) {
        continue;
      }
      if (!Boolean.TRUE.equals(account.get(Account.IS_CARD_ACCOUNT))) {
        writer.writeBankMsgHeader(account.get(Account.BANK_ENTITY), account.get(Account.BRANCH_ID), account.get(Account.NUMBER));
        Date date = writeTransactions(account);
        Date balanceDate = account.get(Account.POSITION_DATE);
        if (balanceDate == null) {
          balanceDate = date;
        }
        writer.writeBankMsgFooter(account.get(Account.POSITION), toString(balanceDate));
      }
    }

    for (Glob account : accounts) {
      if (Account.SUMMARY_ACCOUNT_IDS.contains(account.get(Account.ID))) {
        continue;
      }
      if (Boolean.TRUE.equals(account.get(Account.IS_CARD_ACCOUNT))) {
        writer.writeCardMsgHeader(account.get(Account.NUMBER));
        Date date = writeTransactions(account);
        Date balanceDate = account.get(Account.POSITION_DATE);
        if (balanceDate == null) {
          balanceDate = date;
        }
        writer.writeCardMsgFooter(account.get(Account.POSITION), toString(balanceDate));
      }
    }
    writer.writeFooter();
  }

  private Date writeTransactions(Glob account) {
    GlobList transactionsToWrite = new GlobList(repository.findLinkedTo(account, Transaction.ACCOUNT));
    transactionsToWrite.filterSelf(PicsouMatchers.exportableTransactions(), repository);
    Collections.sort(transactionsToWrite, TransactionComparator.ASCENDING_SPLIT_AFTER);
    Date lastDate = new Date(0);
    for (Glob transaction : transactionsToWrite) {
      if (Boolean.TRUE.equals(transaction.get(Transaction.PLANNED))) {
        continue;
      }
      writeTransaction(transaction);
      Date date = Month.toDate(transaction.get(Transaction.BANK_MONTH),
                               transaction.get(Transaction.BANK_DAY));
      if (date.after(lastDate)) {
        lastDate = date;
      }
    }
    return lastDate;
  }

  private void writeTransaction(Glob transaction) {

    OfxWriter.OfxTransactionWriter writer =
      this.writer.startTransaction(Long.toString(Transaction.fullDate(transaction)),
                                   Long.toString(Transaction.fullBankDate(transaction)),
                                   transaction.get(Transaction.AMOUNT),
                                   transaction.get(Transaction.ID),
                                   transaction.get(Transaction.ORIGINAL_LABEL));

    if (exportCustomFields) {
      writer.add("note", transaction.get(Transaction.NOTE));
      if (transaction.get(Transaction.SPLIT_SOURCE) != null) {
        writer.add("PARENT", "PICSOU" + transaction.get(Transaction.SPLIT_SOURCE));
      }
    }

    writer.end();
  }

  private String toString(Date updateDate) {
    return (OfxImporter.DATE_FORMAT.format(updateDate) + "000000");
  }
}
