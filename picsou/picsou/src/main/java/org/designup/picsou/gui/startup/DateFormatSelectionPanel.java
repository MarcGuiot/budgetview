package org.designup.picsou.gui.startup;

import org.designup.picsou.importer.utils.DateFormatAnalyzer;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class DateFormatSelectionPanel {
  private JPanel panel;
  private JComboBox combo;
  private String selectedFormat;
  private Callback callback;
  private JLabel messageLabel;

  public DateFormatSelectionPanel(GlobRepository repository, Directory directory, final Callback callback,
                                  JLabel messageLabel) {
    this.callback = callback;
    this.messageLabel = messageLabel;
    GlobsPanelBuilder builder = GlobsPanelBuilder.init(repository, directory);
    combo = new JComboBox();
    builder.add("dateFormatCombo", combo);
    combo.setRenderer(new DateFormatRenderer());
    panel = (JPanel)builder.parse(getClass(), "/layout/dateFormatSelectionPanel.splits");
    combo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        callback.dateFormatSelected(getSelectedFormat());
      }
    });

  }

  interface Callback {
    void dateFormatSelected(String format);
  }

  public JPanel getPanel() {
    return panel;
  }

  public void init(List<String> dateFormats) {
    selectedFormat = null;
    if (dateFormats.size() == 0) {
      dateFormats = DateFormatAnalyzer.getAllFormats();
      panel.setVisible(true);
    }
    if (dateFormats.size() != 1) {
      combo.setModel(new DefaultComboBoxModel(dateFormats.toArray(new String[dateFormats.size()])));
      combo.setSelectedIndex(-1);
      panel.setVisible(true);
    }
    else {
      selectedFormat = dateFormats.get(0);
      panel.setVisible(false);
    }
    callback.dateFormatSelected(selectedFormat);
  }

  public boolean check() {
    messageLabel.setText(Lang.get("import.dateformat.undefined"));
    return getSelectedFormat() != null;
  }


  public String getSelectedFormat() {
    return selectedFormat != null ? selectedFormat : (String)combo.getSelectedItem();
  }

  private static class DateFormatRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
      String translatedFormat = "";
      if (value != null) {
        translatedFormat = Lang.get("import.dateformat." + ((String)value).toLowerCase().replace("/", ""));
      }
      return super.getListCellRendererComponent(list, translatedFormat, index, isSelected, cellHasFocus);
    }
  }
}
