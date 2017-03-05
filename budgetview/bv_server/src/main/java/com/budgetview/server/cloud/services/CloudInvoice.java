package com.budgetview.server.cloud.services;

import java.util.Date;

public class CloudInvoice {
  public final String subscriptionId;
  public final String receiptNumber;
  public final Double total;
  public final Double tax;
  public final Date date;

  public CloudInvoice(String subscriptionId, String receiptNumber, Double total, Double tax, Date date) {
    this.subscriptionId = subscriptionId;
    this.receiptNumber = receiptNumber;
    this.total = total;
    this.tax = tax;
    this.date = date;
  }
}
