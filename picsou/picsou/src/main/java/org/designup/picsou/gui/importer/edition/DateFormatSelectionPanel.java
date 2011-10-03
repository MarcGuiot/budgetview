package org.designup.picsou.gui.importer.edition;

import org.designup.picsou.importer.utils.DateFormatAnalyzer;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.SplitsNode;
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
  private JEditorPane messageLabel;
  protected GlobsPanelBuilder builder;

  public DateFormatSelectionPanel(GlobRepository repository, Directory directory, final Callback callback,
                                  JEditorPane messageLabel) {
    this.callback = callback;
    this.messageLabel = messageLabel;
    combo = new JComboBox();
    combo.setRenderer(new DateFormatRenderer());
    combo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        callback.dateFormatSelected(getSelectedFormat());
      }
    });

    builder = new GlobsPanelBuilder(getClass(), "/layout/utils/dateFormatSelectionPanel.splits",
                                    repository, directory);
    builder.add("dateFormatCombo", combo);
    builder.addLoader(new SplitsLoader() {
      public void load(Component component, SplitsNode node) {
        panel = (JPanel)component;
      }
    });
  }

  public interface Callback {
    void dateFormatSelected(String format);
  }

  public GlobsPanelBuilder getBuilder() {
    return builder;
  }

  public JPanel getPanel() {
    return panel;
  }

  public void init(List<String> dateFormats) {
    selectedFormat = null;
    if (dateFormats == null){
      panel.setVisible(false);
      selectedFormat = "";
      return;
    }
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
    boolean b = getSelectedFormat() != null;
    if (!b) {
      messageLabel.setText(Lang.get("import.dateformat.undefined"));
    }
    return b;
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
