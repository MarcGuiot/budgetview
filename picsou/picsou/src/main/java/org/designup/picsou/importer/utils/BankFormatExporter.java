package org.designup.picsou.importer.utils;

import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.Account;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.saxstack.writer.XmlTag;
import org.saxstack.writer.XmlWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class BankFormatExporter {
  public static String export(GlobRepository repository, GlobList transactions) throws IOException {

    StringWriter writer = new StringWriter();
    XmlTag root = XmlWriter.startTag(writer, "bankFormat");
    Set<Glob> accounts = new HashSet<Glob>();

    for (Glob transaction : transactions) {
      if (transaction.isTrue(Transaction.IS_OFX)) {
        accounts.add(repository.findLinkTarget(transaction, Transaction.ACCOUNT));
        root.createChildTag("ofxEntry")
          .addAttribute("bankId", transaction.get(Transaction.ACCOUNT))
          .addAttribute("originalLabel", transaction.get(Transaction.ORIGINAL_LABEL))
          .addAttribute("date", Transaction.fullDate(transaction))
          .addAttribute("name", transaction.get(Transaction.OFX_NAME))
          .addAttribute("memo", transaction.get(Transaction.OFX_MEMO))
          .addAttribute("checkNum", transaction.get(Transaction.OFX_CHECK_NUM))
          .addAttribute("bankType", transaction.get(Transaction.BANK_TRANSACTION_TYPE))
          .end();
      }
      else {
        accounts.add(repository.findLinkTarget(transaction, Transaction.ACCOUNT));
        root.createChildTag("qifEntry")
          .addAttribute("bankId", transaction.get(Transaction.ACCOUNT))
          .addAttribute("originalLabel", transaction.get(Transaction.ORIGINAL_LABEL))
          .addAttribute("date", Transaction.fullDate(transaction))
          .addAttribute("m", transaction.get(Transaction.QIF_M))
          .addAttribute("p", transaction.get(Transaction.QIF_P))
          .addAttribute("bankType", transaction.get(Transaction.BANK_TRANSACTION_TYPE))
          .end();
      }
    }

    for (Glob account : accounts) {
      XmlTag bankTag = root.createChildTag("bank");
      bankTag.addAttribute("id", account.get(Account.ID));
      bankTag.addAttribute("bankEntityLabel", account.get(Account.BANK_ENTITY_LABEL));
      bankTag.addAttribute("bankEntityId", account.get(Account.BANK_ENTITY));
      bankTag.addAttribute("bankId", account.get(Account.BANK));
      bankTag.end();
    }

    root.end();
    return writer.toString();
  }
}
