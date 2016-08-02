package com.budgetview.functests.checkers.printing.pages;

import com.budgetview.desktop.printing.budget.gauges.BudgetAreaGaugeBlock;
import com.budgetview.desktop.printing.budget.gauges.SeriesGaugeBlock;
import com.budgetview.desktop.printing.utils.BlockMultiColumnsPage;
import com.budgetview.desktop.printing.utils.EmptyBlock;
import com.budgetview.desktop.printing.utils.PageBlock;
import junit.framework.Assert;
import org.globsframework.utils.Utils;

import java.util.List;

public class BudgetGaugePageChecker {
  private BlockMultiColumnsPage page;
  private List<PageBlock> blocks;

  public BudgetGaugePageChecker(BlockMultiColumnsPage page) {
    this.page = page;
    this.blocks = page.getBlocks();
  }

  public BudgetGaugePageChecker checkTitle(String title) {
    Assert.assertEquals(title, page.getTitle());
    return this;
  }

  public BudgetGaugePageChecker checkBudget(int index, String label, String actualAmount, String plannedAmount) {
    PageBlock block = blocks.get(index);
    if (!(block instanceof BudgetAreaGaugeBlock)) {
      Assert.fail("Unexpected block type " + block.getClass().getSimpleName() + " at index " + index + " - actual content:\n" + getContent());
    }
    BudgetAreaGaugeBlock budgetBlock = (BudgetAreaGaugeBlock) block;
    if (!Utils.equal(label, budgetBlock.getLabel())) {
      Assert.fail("Unexpected label at index " + index + ": expected " + label + " but was " + budgetBlock.getLabel() + " - actual content:\n" + getContent());
    }
    if (!Utils.equal(actualAmount, budgetBlock.getActualAmount())) {
      Assert.fail("Unexpected actual amount at index " + index + ": expected " + actualAmount +
                  " but was " + budgetBlock.getActualAmount() + " - actual content:\n" + getContent());
    }
    if (!Utils.equal(plannedAmount, budgetBlock.getPlannedAmount())) {
      Assert.fail("Unexpected planned amount at index " + index + ": expected " + plannedAmount +
                  " but was " + budgetBlock.getPlannedAmount() + " - actual content:\n" + getContent());
    }
    return this;
  }

  public BudgetGaugePageChecker checkSeries(int index, String label, String actualAmount, String plannedAmount) {
    PageBlock block = blocks.get(index);
    if (!(block instanceof SeriesGaugeBlock)) {
      Assert.fail("Unexpected block type " + block.getClass().getSimpleName() + " at index " + index + " - actual content:\n" + getContent());
    }
    SeriesGaugeBlock seriesBlock = (SeriesGaugeBlock) blocks.get(index);
    if (!Utils.equal(label, seriesBlock.getLabel())) {
      Assert.fail("Unexpected label at index " + index + ": expected " + label + " but was " + seriesBlock.getLabel() + " - actual content:\n" + getContent());
    }
    if (!Utils.equal(actualAmount, seriesBlock.getActualAmount())) {
      Assert.fail("Unexpected actual amount at index " + index + ": expected " + actualAmount +
                  " but was " + seriesBlock.getActualAmount() + " - actual content:\n" + getContent());
    }
    if (!Utils.equal(plannedAmount, seriesBlock.getPlannedAmount())) {
      Assert.fail("Unexpected planned amount at index " + index + ": expected " + plannedAmount +
                  " but was " + seriesBlock.getPlannedAmount() + " - actual content:\n" + getContent());
    }
    return this;
  }

  public BudgetGaugePageChecker checkSeparator(int index) {
    PageBlock block = blocks.get(index);
    if (!(block instanceof EmptyBlock)) {
      Assert.fail("Unexpected block type " + block.getClass().getSimpleName() + " at index " + index + " - actual content:\n" + getContent());
    }
    return this;
  }

  public BudgetGaugePageChecker checkBlockCount(int count) {
    if (count != blocks.size()) {
      Assert.fail("Unexpected size " + blocks.size() + " - actual content: ");
    }
    return this;
  }

  public void dumpCode() {

    StringBuilder builder = new StringBuilder();

    builder.append("  .checkTitle(\"").append(page.getTitle()).append("\")\n");
    builder.append("  .checkBlockCount(").append(blocks.size()).append(")\n");

    int index = 0;
    for (PageBlock block : blocks) {
      if (block instanceof BudgetAreaGaugeBlock) {
        BudgetAreaGaugeBlock budgetBlock = (BudgetAreaGaugeBlock) block;
        builder
          .append("  .checkBudget(")
          .append(Integer.toString(index++))
          .append(", \"")
          .append(budgetBlock.getLabel())
          .append("\", \"")
          .append(budgetBlock.getActualAmount())
          .append("\", \"")
          .append(budgetBlock.getPlannedAmount())
          .append("\")\n");
      }
      else if (block instanceof SeriesGaugeBlock) {
        SeriesGaugeBlock seriesBlock = (SeriesGaugeBlock) block;
        builder
          .append("    .checkSeries(")
          .append(Integer.toString(index++))
          .append(", \"")
          .append(seriesBlock.getLabel())
          .append("\", \"")
          .append(seriesBlock.getActualAmount())
          .append("\", \"")
          .append(seriesBlock.getPlannedAmount())
          .append("\")\n");
      }
      else if (block instanceof EmptyBlock) {
        builder
          .append("  .checkSeparator(")
          .append(Integer.toString(index++))
          .append(")\n");
      }
      else {
        Assert.fail("Unexpected block type: " + block.getClass().getSimpleName());
      }
    }
    Assert.fail("Add this:\n" + builder.toString());
  }

  public String getContent() {
    StringBuilder builder = new StringBuilder();

    builder.append("  .checkBlockCount(").append(blocks.size()).append(")\n");

    int index = 0;
    for (PageBlock block : blocks) {
      if (block instanceof BudgetAreaGaugeBlock) {
        BudgetAreaGaugeBlock budgetBlock = (BudgetAreaGaugeBlock) block;
        builder
          .append(Integer.toString(index++))
          .append(" - Budget: ")
          .append(", \"")
          .append(budgetBlock.getLabel())
          .append("\", ")
          .append(budgetBlock.getActualAmount())
          .append(", ")
          .append(budgetBlock.getPlannedAmount())
          .append("\n");
      }
      else if (block instanceof SeriesGaugeBlock) {
        SeriesGaugeBlock seriesBlock = (SeriesGaugeBlock) block;
        builder
          .append(Integer.toString(index++))
          .append(" - Series: ")
          .append(", \"")
          .append(seriesBlock.getLabel())
          .append("\", ")
          .append(seriesBlock.getActualAmount())
          .append(", ")
          .append(seriesBlock.getPlannedAmount())
          .append("\n");
      }
      else if (block instanceof EmptyBlock) {
        builder
          .append(Integer.toString(index++))
          .append(" - Separator\n");
      }
      else {
        Assert.fail("Unexpected block type: " + block.getClass().getSimpleName());
      }
    }

    builder.append(";");
    return builder.toString();
  }
}
