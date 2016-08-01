package com.budgetview.gui.printing;

import com.budgetview.gui.description.Formatting;
import com.budgetview.gui.time.TimeService;
import com.budgetview.utils.Lang;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

public abstract class PrintablePage implements Printable {
  public int print(Graphics g, PageFormat format, int page) throws PrinterException {
    Graphics2D g2 = (Graphics2D)g;
    g2.translate(format.getImageableX(), format.getImageableY());
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    PrintStyle style = new PrintStyle();
    PrintMetrics metrics = new PrintMetrics(format);

    g.setColor(style.getTitleColor());
    g2.setFont(style.getTitleFont());
    g2.drawString(getTitle(), metrics.titleX(), metrics.titleY());
    g2.drawLine(metrics.titleLineX(), metrics.titleLineY(),
                metrics.getTitleLineXEnd(), metrics.titleLineY());

    g2.setColor(style.getFooterColor());
    Font footerFont = style.getFooterFont();
    g2.setFont(footerFont);
    String footerText = getFooterText(page);
    g2.drawString(footerText,
                  metrics.getFooterX(footerText, g2.getFontMetrics(footerFont)),
                  metrics.getFooterY());
    
    try {
      return printContent(g2, metrics, style);
    }
    catch (RuntimeException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private String getFooterText(int page) {
    return Lang.get("print.footer", Formatting.toString(TimeService.getToday()), page + 1);
  }

  protected abstract String getTitle();

  protected abstract int printContent(Graphics2D g2, PrintMetrics metrics, PrintStyle style);
}
