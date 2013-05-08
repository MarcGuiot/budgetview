package org.designup.picsou.gui.printing.transactions;

import com.budgetview.shared.utils.AmountFormat;
import org.designup.picsou.gui.printing.PrintStyle;
import org.designup.picsou.gui.printing.utils.PageBlock;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.utils.Strings;

import java.awt.*;

public class TransactionBlock implements PageBlock {

  private static final int DEFAULT_HEIGHT = 30;
  private static final int HEIGHT_WITH_NOTES = 40;

  private TransactionsContext context;
  private Glob transaction;

  public TransactionBlock(Glob transaction, TransactionsContext context) {
    this.transaction = transaction;
    this.context = context;
  }

  public int getHeight() {
    return Strings.isNotEmpty(getNote()) ? HEIGHT_WITH_NOTES : DEFAULT_HEIGHT;
  }

  public int getNeededHeight() {
    return getHeight();
  }

  public void print(Dimension area, Graphics2D g2, PrintStyle style) {
    Font labelFont = style.getTextFont(true);
    Font defaultFont = style.getTextFont(false);
    TransactionBlockMetrics metrics = context.getMetrics(area, g2, labelFont, defaultFont);

    g2.setColor(style.getRowSeparatorColor());
    g2.drawLine(0, 1, area.width, 1);

    g2.setColor(style.getTextColor());
    g2.setFont(labelFont);
    g2.setClip(metrics.getLabelBounds());
    g2.drawString(getLabel(), metrics.getLabelX(), metrics.getLabelY());
    g2.setClip(null);
    String amount = getAmount();
    g2.drawString(amount, metrics.getAmountX(amount), metrics.getAmountY());

    g2.setColor(style.getSubTextColor());
    g2.setFont(defaultFont);
    g2.drawString(getUserDate(), metrics.getUserDateX(), metrics.getUserDateY());
    g2.drawString(getBankDate(), metrics.getBankDateX(), metrics.getBankDateY());
    g2.drawString(getSeriesName(), metrics.getSeriesX(), metrics.getSeriesY());
    String accountLabel = getAccountLabel();
    g2.drawString(accountLabel, metrics.getAccountLabelX(accountLabel), metrics.getAccountLabelY());

    String note = getNote();
    if (Strings.isNotEmpty(note)) {
      g2.drawString(note, metrics.getNoteX(), metrics.getNoteY());
    }
  }

  public String getUserDate() {
    return context.getUserDate(transaction);
  }

  public String getBankDate() {
    return context.getBankDate(transaction);
  }

  public String getLabel() {
    return transaction.get(Transaction.LABEL);
  }

  public String getAmount() {
    return AmountFormat.toString(transaction.get(Transaction.AMOUNT));
  }

  public String getSeriesName() {
    return context.getSeriesName(transaction);
  }

  public String getNote() {
    return transaction.get(Transaction.NOTE);
  }

  public String getAccountLabel() {
    return context.getAccountLabel(transaction);
  }
}
