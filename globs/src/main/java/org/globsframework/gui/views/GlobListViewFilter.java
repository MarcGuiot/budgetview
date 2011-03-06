package org.globsframework.gui.views;

import org.globsframework.gui.ComponentHolder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GlobListViewFilter implements ComponentHolder {

  private JTextField textField = new JTextField();
  private GlobListView listView;
  private JList list;
  private Key defaultSelectionKey;
  private GlobRepository repository;
  private GlobMatcher defaultMatcher = GlobMatchers.ALL;
  private OnKeyActionListener filter;

  public static GlobListViewFilter init(final GlobListView listView) {
    return new GlobListViewFilter(listView);
  }

  private GlobListViewFilter(final GlobListView listView) {
    this.listView = listView;
    this.list = listView.getComponent();
    this.repository = listView.getRepository();

    this.filter = new OnKeyActionListener();
    this.textField.getDocument().addDocumentListener(filter);
    this.textField.setName(listView.getComponent().getName() + "Filter");
    installKeyListener();
    textField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        listView.triggerDoubleClickAction();
      }
    });
  }

  public GlobListViewFilter setDefaultMatcher(GlobMatcher defaultMatcher) {
    this.defaultMatcher = defaultMatcher;
    listView.setFilter(filter);
    return this;
  }

  public GlobListViewFilter setDefaultValue(Key key) throws InvalidParameter {
    if (key != null && !listView.getType().equals(key.getGlobType())) {
      throw new InvalidParameter("Key must be of type '" + listView.getType() +
                                 "' instead of '" + key.getGlobType() + "'");
    }
    this.defaultSelectionKey = key;
    return this;
  }

  public ComponentHolder setName(String name) {
    textField.setName(name);
    return this;
  }

  public JTextField getComponent() {
    return textField;
  }

  public void dispose() {
    this.repository = null;
    this.textField = null;
    this.list = null;
    this.listView = null;
  }

  private class OnKeyActionListener implements GlobMatcher, DocumentListener {
    private String[] filters = new String[0];

    public void update() {
      String filter = textField.getText();
      filters = filter.split(" ");
      for (int i = 0; i < filters.length; i++) {
        filters[i] = filters[i].toUpperCase();
      }

      listView.setFilter(this);

      GlobList listContent = listView.getGlobList();
      if (defaultSelectionKey != null) {
        Glob defaultGlob = repository.get(defaultSelectionKey);
        if (!textMatches(defaultGlob)) {
          listContent.remove(defaultGlob);
        }
      }

      if (listContent.size() == 1) {
        listView.select(listContent.getFirst());
      }
      if (listContent.size() == 0) {
        textField.setForeground(Color.RED);
        if (defaultSelectionKey != null) {
          listView.selectFirst();
        }
      }
      else {
        textField.setForeground(null);
      }
    }

    public boolean matches(Glob item, GlobRepository repository) {
      if ((defaultSelectionKey != null) && (item != null)
          && defaultSelectionKey.equals(item.getKey())) {
        return true;
      }

      return defaultMatcher.matches(item, repository) && textMatches(item);
    }

    private boolean textMatches(Glob item) {
      String text = listView.toString(item);
      if (text == null) {
        return false;
      }
      text = text.toUpperCase();
      for (String filter : filters) {
        if (text.indexOf(filter) < 0) {
          return false;
        }
      }
      return true;
    }

    public void insertUpdate(DocumentEvent e) {
      update();
    }

    public void removeUpdate(DocumentEvent e) {
      update();
    }

    public void changedUpdate(DocumentEvent e) {
      update();
    }
  }

  private void installKeyListener() {
    this.textField.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        int size = list.getModel().getSize();
        if (size == 0) {
          return;
        }
        int currentIndex = list.getSelectedIndex();
        int newIndex = -1;
        switch (e.getKeyCode()) {
          case KeyEvent.VK_DOWN:
            newIndex = currentIndex < 0 ? 0 : Math.min(currentIndex + 1, size - 1);
            break;
          case KeyEvent.VK_UP:
            newIndex = currentIndex < 0 ? 0 : Math.max(currentIndex - 1, 0);
            break;
          case KeyEvent.VK_PAGE_DOWN:
            newIndex = size - 1;
            break;
          case KeyEvent.VK_PAGE_UP:
            newIndex = 0;
            break;
          default: // ignore the event
        }
        list.setSelectedIndex(newIndex);
      }
    });
  }
}
