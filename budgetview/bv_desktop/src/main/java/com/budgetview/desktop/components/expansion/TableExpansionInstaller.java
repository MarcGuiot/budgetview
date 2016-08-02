package com.budgetview.desktop.components.expansion;

import com.budgetview.desktop.utils.Gui;
import org.globsframework.gui.utils.TableUtils;
import org.globsframework.model.Glob;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TableExpansionInstaller {
  public static void setUp(final ExpandableTable tableView,
                           final TableExpansionModel expansionModel,
                           final JTable table,
                           final TableExpansionColumn expandColumn,
                           final int labelColumnIndex) {

    TableUtils.setSize(table, 0, expandColumn.getPreferredWidth());

    Gui.installRolloverOnButtons(table, labelColumnIndex);

    table.setDragEnabled(false);

    table.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() != 2) {
          return;
        }
        Glob category = tableView.getSelectedGlob();
        expansionModel.toggleExpansion(category, true);
      }
    });
  }
}
