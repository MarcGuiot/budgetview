package com.budgetview.gui.importer.edition;

import com.budgetview.gui.components.tips.ErrorTip;
import com.budgetview.gui.components.tips.TipPosition;
import com.budgetview.io.importer.utils.DateFormatAnalyzer;
import com.budgetview.utils.Lang;
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
  private Directory directory;
  private Callback callback;
  protected GlobsPanelBuilder builder;
  private ErrorTip errorTip;

  public DateFormatSelectionPanel(GlobRepository repository, Directory directory, final Callback callback) {
    this.directory = directory;
    this.callback = callback;
    combo = new JComboBox();
    combo.setRenderer(new DateFormatRenderer());
    combo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (errorTip != null) {
          errorTip.dispose();
          errorTip = null;
        }
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
      errorTip = ErrorTip.show(combo, Lang.get("import.dateformat.undefined"), directory, TipPosition.TOP_RIGHT);
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
        translatedFormat = Lang.get("import.dateformat." + ((String)value).toLowerCase().replaceAll("[-/\\.]", ""));
      }
      return super.getListCellRendererComponent(list, translatedFormat, index, isSelected, cellHasFocus);
    }
  }
  
  public void dispose(){
    builder.dispose();
  }
}
