package org.designup.picsou.gui.components.table;

import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.utils.directory.Directory;

public class TransactionTableHeaderPainter extends TableHeaderPainter {

  public static TableHeaderPainter install(GlobTableView tableView, Directory directory) {
    TableHeaderPainter headerPainter = new TransactionTableHeaderPainter(tableView, directory);
    tableView.setHeaderCustomizer(new PicsouTableHeaderCustomizer(directory, "transactionTable.header.title"), headerPainter);
    return headerPainter;
  }

  private TransactionTableHeaderPainter(GlobTableView tableView, Directory directory) {
    super(tableView, directory);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    lightColor = colorLocator.get("transactionTable.header.std.light");
    mediumColor = colorLocator.get("transactionTable.header.std.medium");
    darkColor = colorLocator.get("transactionTable.header.std.dark");
    borderColor = colorLocator.get("transactionTable.header.std.border");

    filteredLightColor = colorLocator.get("transactionTable.header.filtered.light");
    filteredMediumColor = colorLocator.get("transactionTable.header.filtered.medium");
    filteredDarkColor = colorLocator.get("transactionTable.header.filtered.dark");
    filteredBorderColor = colorLocator.get("transactionTable.header.filtered.border");
  }

}
