package org.designup.picsou.importer.utils;

import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.saxstack.writer.XmlTag;
import org.saxstack.writer.XmlWriter;

import java.io.IOException;
import java.io.StringWriter;

public class BankFormatExporter {
  public static String export(GlobList transactions) throws IOException {
    StringWriter writer = new StringWriter();
    XmlTag root = XmlWriter.startTag(writer, "bankFormat");

    for (Glob transaction : transactions) {
      if (Boolean.TRUE.equals(transaction.get(Transaction.IS_OFX))) {
        root.createChildTag("ofxEntry")
          .addAttribute("originalLabel", transaction.get(Transaction.ORIGINAL_LABEL))
          .addAttribute("date", Transaction.fullDate(transaction))
          .addAttribute("name", transaction.get(Transaction.OFX_NAME))
          .addAttribute("memo", transaction.get(Transaction.OFX_MEMO))
          .addAttribute("checkNum", transaction.get(Transaction.OFX_CHECK_NUM))
          .end();
      }
      else {
        root.createChildTag("qifEntry")
          .addAttribute("originalLabel", transaction.get(Transaction.ORIGINAL_LABEL))
          .addAttribute("date", Transaction.fullDate(transaction))
          .addAttribute("m", transaction.get(Transaction.QIF_M))
          .addAttribute("p", transaction.get(Transaction.QIF_P))
          .end();
      }
    }

    root.end();
    return writer.toString();
  }
}
