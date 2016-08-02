package com.budgetview.bank.connectors.utils;

import com.budgetview.bank.BankConnector;
import com.budgetview.desktop.Application;

import java.io.PrintWriter;
import java.io.StringWriter;

public class WebTraces {
  public static String dump(Throwable exception, BankConnector connector) {
    StringWriter builder = initBuilder(connector);
    builder.append("exception:\n");
    exception.printStackTrace(new PrintWriter(builder));
    return builder.toString();
  }

  public static String dump(String page, BankConnector connector) {
    return initBuilder(connector)
      .append("page:\n")
      .append(page)
      .toString();
  }

  private static StringWriter initBuilder(BankConnector connector) {
    StringWriter builder = new StringWriter();
    if (connector != null) {
      builder.append("bank: ").append(connector.getLabel()).append("\n");
      builder.append("version: ").append(Long.toString(Application.JAR_VERSION)).append("\n");
      builder.append("location: ").append(connector.getCurrentLocation()).append("\n");
    }
    else {
      builder.append("no current connector\n");
    }
    return builder;
  }

  public static String anonymize(String page) {
    return page
      .replaceAll("[0-9,.]+[,.][0-9][0-9]", "99.99")
      .replaceAll("[0-9][0-9][0-9][0-9]+", "999");
  }
}
