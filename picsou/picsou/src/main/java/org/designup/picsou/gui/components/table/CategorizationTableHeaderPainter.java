package org.designup.picsou.gui.components.table;

import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.utils.directory.Directory;

public class CategorizationTableHeaderPainter extends TableHeaderPainter {

  public static TableHeaderPainter install(GlobTableView tableView, Directory directory) {
    TableHeaderPainter headerPainter = new CategorizationTableHeaderPainter(tableView, directory);
    tableView.setHeaderCustomizer(new PicsouTableHeaderCustomizer(directory, "categorizationTable.header.title"), headerPainter);
    return headerPainter;
  }

  private CategorizationTableHeaderPainter(GlobTableView tableView, Directory directory) {
    super(tableView, directory);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    lightColor = colorLocator.get("categorizationTable.header.std.light");
    mediumColor = colorLocator.get("categorizationTable.header.std.medium");
    darkColor = colorLocator.get("categorizationTable.header.std.dark");
    borderColor = colorLocator.get("categorizationTable.header.std.border");

    filteredLightColor = colorLocator.get("categorizationTable.header.filtered.light");
    filteredMediumColor = colorLocator.get("categorizationTable.header.filtered.medium");
    filteredDarkColor = colorLocator.get("categorizationTable.header.filtered.dark");
    filteredBorderColor = colorLocator.get("categorizationTable.header.filtered.border");
  }

}
