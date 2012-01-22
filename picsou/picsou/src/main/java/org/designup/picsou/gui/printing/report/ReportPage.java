package org.designup.picsou.gui.printing.report;

import org.designup.picsou.gui.printing.PrintColors;
import org.designup.picsou.gui.printing.PrintFonts;
import org.designup.picsou.gui.printing.PrintMetrics;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

public abstract class ReportPage implements Printable {
  public int print(Graphics g, PageFormat format, int page) throws PrinterException {
    Graphics2D g2 = (Graphics2D)g;
    g2.translate(format.getImageableX(),
                 format.getImageableY());
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    PrintFonts fonts = new PrintFonts();
    PrintMetrics metrics = new PrintMetrics(format, g2, fonts);
    PrintColors colors = new PrintColors();

    g.setColor(colors.getTitleColor());
    g2.setFont(fonts.getTitleFont());
    g2.drawString(getTitle(), metrics.titleX(), metrics.titleY());
    g2.drawLine(metrics.titleLineX(), metrics.titleLineY(),
                metrics.getTitleLineXEnd(), metrics.titleLineY());

    try {
      return printContent(g2, fonts, metrics, colors);
    }
    catch (RuntimeException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  protected abstract String getTitle();

  protected abstract int printContent(Graphics2D g2, PrintFonts fonts, PrintMetrics metrics, PrintColors colors);
}
