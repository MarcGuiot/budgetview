package com.budgetview.desktop.startup.components;

import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

public class UserSelectionDialog {

  private SplitsBuilder builder;
  private PicsouDialog dialog;
  private String result = null;
  private JList userList;

  public static String select(List<String> names, Directory directory) {
    if (names.isEmpty()) {
      return null;
    }
    UserSelectionDialog dialog = new UserSelectionDialog(getSortedArray(names), directory);
    return dialog.show();
  }

  private static String[] getSortedArray(List<String> names) {
    Collections.sort(names);
    return names.toArray(new String[names.size()]);
  }

  public UserSelectionDialog(String[] names, Directory directory) {
    builder = SplitsBuilder.init(directory).setSource(getClass(), "/layout/general/userSelectionDialog.splits");
    userList = new JList(names);
    builder.add("userList", userList);
    userList.setSelectedIndex(0);
    userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    userList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          validate();
        }
      }
    });
    dialog = PicsouDialog.create(this, directory.get(JFrame.class), true, directory);

    JPanel panel = builder.load();
    dialog.addPanelWithButtons(panel, new OkAction(), new CancelAction());
  }

  private void validate() {
    result = (String)userList.getSelectedValue();
    dialog.setVisible(false);
  }

  private String show() {
    dialog.pack();
    GuiUtils.showCentered(dialog);
    builder.dispose();
    return result;
  }

  private class OkAction extends AbstractAction {

    public OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      validate();
    }
  }

  private class CancelAction extends AbstractAction {

    public CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      result = null;
      dialog.setVisible(false);
    }
  }

}
