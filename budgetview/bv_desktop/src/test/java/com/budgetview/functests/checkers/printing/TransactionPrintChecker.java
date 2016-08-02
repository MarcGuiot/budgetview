package com.budgetview.functests.checkers.printing;

import com.budgetview.desktop.printing.PrintableReport;
import com.budgetview.desktop.printing.transactions.TransactionBlock;
import com.budgetview.desktop.printing.transactions.TransactionHeaderBlock;
import com.budgetview.desktop.printing.transactions.TransactionsReport;
import com.budgetview.desktop.printing.utils.BlockColumnPage;
import com.budgetview.desktop.printing.utils.PageBlock;
import junit.framework.Assert;
import org.globsframework.utils.TablePrinter;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.util.List;

public class TransactionPrintChecker {
  private TablePrinter expected = new TablePrinter(true);
  private TablePrinter actual = new TablePrinter(true);

  public TransactionPrintChecker(PrintableReport report) {
    if (!(report instanceof TransactionsReport)) {
      Assert.fail("Unexpected report type: " + report.getClass());
    }
    TransactionsReport transactionsReport = (TransactionsReport) report;

    Paper paper = new Paper();
    paper.setImageableArea(0, 0, 1000, 900);
    PageFormat format = new PageFormat();
    format.setPaper(paper);
    transactionsReport.init(format);
    fillActual(transactionsReport);
  }

  private void fillActual(TransactionsReport transactionsReport) {
    BlockColumnPage page = (BlockColumnPage) transactionsReport.getPrintable(0);
    List<PageBlock> blocks = page.getBlocks();
    Assert.assertTrue(blocks.get(0) instanceof TransactionHeaderBlock);
    for (int i = 1; i < blocks.size(); i++) {
      TransactionBlock block = (TransactionBlock) blocks.get(i);
      actual.addRow(block.getUserDate(),
                    block.getBankDate(),
                    block.getLabel(),
                    block.getAmount(),
                    block.getSeriesName(),
                    block.getNote(),
                    block.getAccountLabel());
    }
  }

  public TransactionPrintChecker add(String userDate, String bankDate, String label, String amount, String seriesName, String note, String accountLabel) {
    expected.addRow(userDate, bankDate, label, amount, seriesName, note, accountLabel);
    return this;
  }

  public void dump() {
    System.out.println(actual.toString());
  }

  public void check() {
    Assert.assertEquals(expected.toString(), actual.toString());
  }
}
