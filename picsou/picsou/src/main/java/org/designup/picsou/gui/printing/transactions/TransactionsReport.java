package org.designup.picsou.gui.printing.transactions;

import org.designup.picsou.gui.printing.PrintablePage;
import org.designup.picsou.gui.printing.PrintableReport;
import org.designup.picsou.gui.printing.utils.BlockColumnPage;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.List;

public class TransactionsReport implements PrintableReport {
  private PageFormat format;
  private GlobList transactions;
  private List<PrintablePage> pages = new ArrayList<PrintablePage>();
  private GlobRepository repository;

  public TransactionsReport(GlobList transactions, GlobRepository repository) {
    this.transactions = transactions;
    this.repository = repository;
  }

  public void init(PageFormat defaultFormat) {
    setFormat(defaultFormat);
    createPages();
  }

  private void setFormat(PageFormat defaultFormat) {
    this.format = defaultFormat;
    Paper paper = new Paper();
    double margin = 20;
    paper.setImageableArea(margin, margin,
                           paper.getWidth() - margin * 2,
                           paper.getHeight() - margin * 2);
    defaultFormat.setPaper(paper);
    defaultFormat.setOrientation(PageFormat.PORTRAIT);
  }

  private void createPages() {
    BlockColumnPage page = null;
    TransactionsContext context = new TransactionsContext(repository);
    for (Glob transaction : transactions) {
      TransactionBlock block = new TransactionBlock(transaction, context);
      if (page == null || !page.hasSpaceLeftFor(block)) {
        page = new BlockColumnPage(format, Lang.get("print.transactions.title"));
        pages.add(page);
      }
      page.append(block);
    }
  }

  public int getNumberOfPages() {
    return pages.size();
  }

  public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
    if (format == null) {
      throw new InvalidState("init(PageFormat) should be called first");
    }
    return format;
  }

  public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
    return pages.get(pageIndex);
  }
}
