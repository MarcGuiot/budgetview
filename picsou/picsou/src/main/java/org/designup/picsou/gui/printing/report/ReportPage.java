package org.designup.picsou.gui.printing.report;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.printing.PrintStyle;
import org.designup.picsou.gui.printing.PrintMetrics;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

public abstract class ReportPage implements Printable {
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
    
    return Lang.get("print.footer", Formatting.toString(TimeService.getToday()), page);
  }

  protected abstract String getTitle();

  protected abstract int printContent(Graphics2D g2, PrintMetrics metrics, PrintStyle style);
}
