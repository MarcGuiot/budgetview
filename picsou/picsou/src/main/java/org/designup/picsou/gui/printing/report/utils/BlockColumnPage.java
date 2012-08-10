package org.designup.picsou.gui.printing.report.utils;

import org.designup.picsou.gui.printing.PrintMetrics;
import org.designup.picsou.gui.printing.PrintStyle;
import org.designup.picsou.gui.printing.report.ReportPage;

import java.awt.*;
import java.awt.print.PageFormat;
import java.util.*;

public class BlockColumnPage extends ReportPage {

  private final int MAX_COLUMNS = 3;
  private final int COLUMN_MARGIN = 10;

  private String title;

  private int maxY;
  private int yOffset;
  private int currentY;
  private int currentColumn;
  private int columnWidth;

  private java.util.List<BlockContext> blocks = new ArrayList<BlockContext>();
  private Rectangle contentArea;

  public BlockColumnPage(PageFormat format, String title) {
    this.title = title;

    PrintMetrics metrics = new PrintMetrics(format);
    this.contentArea = metrics.getContentArea();
    this.maxY = contentArea.height - 10;
    this.yOffset = contentArea.y;
    this.currentY = 0;
    this.currentColumn = 0;
    this.columnWidth = (contentArea.width - (MAX_COLUMNS - 1) * COLUMN_MARGIN) / MAX_COLUMNS;
  }

  public String getTitle() {
    return title;
  }

  public boolean hasSpaceLeftFor(PageBlock block) {
    return (currentColumn < MAX_COLUMNS - 1) || (currentY + block.getNeededHeight() <= maxY);
  }

  public void append(PageBlock block) {
    if (block.getNeededHeight() + currentY > maxY) {
      currentY = 0;
      currentColumn += 1;
    }

    int blockHeight = block.getHeight();
    Rectangle rectangle =
      new Rectangle(currentColumn * (columnWidth + COLUMN_MARGIN), currentY + yOffset,
                    columnWidth, blockHeight);
    blocks.add(new BlockContext(block, rectangle));

    if (blockHeight + currentY > maxY) {
      currentY = 0;
      currentColumn += 1;
    }
    else {
      currentY += blockHeight;
    }
  }

  protected int printContent(Graphics2D g2, PrintMetrics metrics, PrintStyle style) {
    for (BlockContext block : blocks) {
      block.print(g2, style);
    }
    return PAGE_EXISTS;
  }

  public java.util.List<PageBlock> getBlocks() {
    java.util.List<PageBlock> result = new ArrayList<PageBlock>();
    for (BlockContext block : blocks) {
      result.add(block.pageBlock);
    }
    return result;
  }

  private class BlockContext {
    private PageBlock pageBlock;
    private Rectangle rectangle;

    private BlockContext(PageBlock pageBlock, Rectangle rectangle) {
      this.pageBlock = pageBlock;
      this.rectangle = rectangle;
    }

    public void print(Graphics2D g2, PrintStyle style) {
      Graphics2D childGraphics = (Graphics2D)g2.create(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
      pageBlock.print(new Dimension(rectangle.width, rectangle.height), childGraphics, style);

      g2.setColor(style.getDividerColor());
      g2.setStroke(new BasicStroke(0.2f));
      for (int column = 1; column <= currentColumn; column++) {
        int x = column * (columnWidth + COLUMN_MARGIN) - COLUMN_MARGIN / 2;
        g2.drawLine(x, yOffset, x, yOffset + contentArea.height);
      }
    }
  }
}
