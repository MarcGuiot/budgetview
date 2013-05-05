package org.designup.picsou.gui.printing.utils;

import org.designup.picsou.gui.printing.PrintMetrics;
import org.designup.picsou.gui.printing.PrintStyle;
import org.designup.picsou.gui.printing.PrintablePage;

import java.awt.*;
import java.awt.print.PageFormat;
import java.util.ArrayList;

public class BlockColumnPage extends PrintablePage {

  private String title;

  private final int xOffset;
  private final int yOffset;
  private final int columnWidth;
  private final int availableHeight;
  private int currentY;

  private java.util.List<BlockContext> blocks = new ArrayList<BlockContext>();
  private Rectangle contentArea;

  public BlockColumnPage(PageFormat format, String title) {
    this.title = title;

    PrintMetrics metrics = new PrintMetrics(format);
    this.contentArea = metrics.getContentArea();
    this.xOffset = contentArea.x;
    this.yOffset = contentArea.y;
    this.columnWidth = contentArea.width;
    this.availableHeight = contentArea.height - 10;
    this.currentY = 0;
  }

  public String getTitle() {
    return title;
  }

  public boolean hasSpaceLeftFor(PageBlock block) {
    return (currentY + block.getNeededHeight()) <= availableHeight;
  }

  public void append(PageBlock block) {
    int blockHeight = block.getHeight();
    Rectangle rectangle = new Rectangle(xOffset, currentY + yOffset, columnWidth, blockHeight);
    blocks.add(new BlockContext(block, rectangle));
    currentY += blockHeight;
  }

  protected int printContent(Graphics2D g2, PrintMetrics metrics, PrintStyle style) {
    for (BlockContext block : blocks) {
      block.print(g2, style);
    }
    return PAGE_EXISTS;
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
    }
  }
}
