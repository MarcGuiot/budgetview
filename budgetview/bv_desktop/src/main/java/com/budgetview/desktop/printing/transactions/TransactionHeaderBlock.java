package com.budgetview.desktop.printing.transactions;

import com.budgetview.desktop.printing.PrintStyle;
import com.budgetview.desktop.printing.utils.PageBlock;
import com.budgetview.utils.Lang;

import java.awt.*;

public class TransactionHeaderBlock implements PageBlock {
  private TransactionsContext context;

  public TransactionHeaderBlock(TransactionsContext context) {
    this.context = context;
  }

  public int getHeight() {
    return 20;
  }

  public int getNeededHeight() {
    return getHeight();
  }

  public void print(Dimension area, Graphics2D g2, PrintStyle style) {
    Font labelFont = style.getTextFont(true);
    Font defaultFont = style.getTextFont(false);
    TransactionBlockMetrics metrics = context.getMetrics(area, g2, labelFont, defaultFont);

    g2.setFont(style.getTableHeaderFont());
    g2.setColor(style.getTableHeaderColor());
    g2.drawString(Lang.get("print.transactions.userDate"), metrics.getUserDateX(), metrics.getHeaderY());
    g2.drawString(Lang.get("print.transactions.bankDate"), metrics.getBankDateX(), metrics.getHeaderY());
    g2.drawString(Lang.get("print.transactions.label"), metrics.getLabelX(), metrics.getHeaderY());
    String amount = Lang.get("print.transactions.amount");
    g2.drawString(amount, metrics.getAmountX(amount), metrics.getHeaderY());
  }
}
