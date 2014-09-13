package org.designup.picsou.gui.components.filtering.components;

import org.designup.picsou.gui.components.filtering.FilterClearer;
import org.designup.picsou.gui.components.filtering.FilterManager;
import org.designup.picsou.gui.utils.ApplicationColors;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.utils.AbstractDocumentListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public abstract class TextFilterPanel {
  private FilterManager filterManager;
  private JTextField textField;
  private JPanel panel;
  private Color backgroundColor;
  private ColorService colorService;
  private ColorChangeListener listener;

  public static final String SEARCH_FILTER = "textSearch";

  public TextFilterPanel(FilterManager filterManager, GlobRepository repository, Directory directory) {
    this.filterManager = filterManager;

    createPanel(repository, directory);

    this.colorService = directory.get(ColorService.class);
    this.listener = new ColorChangeListener() {
      public void colorsChanged(ColorLocator colorLocator) {
        backgroundColor = colorLocator.get(ApplicationColors.TRANSACTION_SEARCH_FIELD);
        if (textField != null) {
          if (Strings.isNotEmpty(textField.getText())) {
            textField.setBackground(backgroundColor);
          }
        }
      }
    };
    this.colorService.addListener(listener);

    filterManager.addClearer(new FilterClearer() {
      public List<String> getAssociatedFilters() {
        return Arrays.asList(SEARCH_FILTER);
      }

      public void clear() {
        textField.setText(null);
      }
    });
  }

  private void createPanel(GlobRepository repository, Directory directory) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/utils/textFilterPanel.splits",
                                                      repository, directory);

    createTextField();
    builder.add("searchField", textField);

    panel = builder.load();
  }

  public JPanel getPanel() {
    return panel;
  }

  private void createTextField() {
    textField = new JTextField();
    textField.setOpaque(true);
    textField.getDocument().addDocumentListener(new AbstractDocumentListener() {
      protected void documentChanged(DocumentEvent e) {
        updateSearch(textField);
      }
    });
  }

  private void updateSearch(JTextField textField) {
    String text = textField.getText();
    if (Strings.isNullOrEmpty(text)) {
      filterManager.remove(SEARCH_FILTER);
      textField.setBackground(Color.WHITE);
    }
    else {
      filterManager.set(SEARCH_FILTER, Lang.get("filter.text", text), createMatcher(text));
      textField.setBackground(backgroundColor);
    }
  }

  public void reapplyFilterIfActive() {
    String text = textField.getText();
    if (Strings.isNullOrEmpty(text)) {
      return;
    }
    filterManager.set(SEARCH_FILTER, Lang.get("filter.text", text), createMatcher(text));
  }

  protected abstract GlobMatcher createMatcher(String searchFilter);

  protected void finalize() throws Throwable {
    super.finalize();
    colorService.removeListener(listener);
  }

  public boolean isActive() {
    return Strings.isNotEmpty(textField.getText());
  }

  public void reset() {
    textField.setText("");
  }
}
