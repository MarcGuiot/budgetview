package org.designup.picsou.gui.components;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class JFilteredList extends JComponent {

  public interface Stringifier {

    String toString(Object object);

    public static Stringifier DEFAULT = new Stringifier() {
      public String toString(Object object) {
        if (object == null) {
          return null;
        }
        return object.toString();
      }
    };
  }

  JTextField filterField = new JTextField();
  JList resultList;
  private Object[] initialListData;
  private Stringifier stringifier;
  private List currentListData = new ArrayList();
  private MyListModel dataModel;

  private ActionListener actionListener;
  private String actionCommand = "";

  public JFilteredList() {
    this(new String[0]);
  }

  public JFilteredList(Object[] listData, Stringifier stringifier) {
    this.initialListData = listData;
    this.stringifier = stringifier;
    initList();
    initTextField();
    initLayout();
  }

  public JFilteredList(Object[] listData) {
    this(listData, Stringifier.DEFAULT);
  }

  private void initList() {
    dataModel = new MyListModel();
    resultList = new JList(dataModel);
    resultList.setCellRenderer(new Renderer());
    resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    resultList.setBorder(BorderFactory.createEtchedBorder());
    resultList.setVisibleRowCount(12);
    dataModel.update();
  }

  private class MyListModel extends AbstractListModel {
    public Object getElementAt(int index) {
      return currentListData.get(index);
    }

    public int getSize() {
      return currentListData.size();
    }

    void update() {
      currentListData.clear();
      for (int i = 0; i < initialListData.length; i++) {
        Object o = initialListData[i];
        if (stringifier.toString(o).toLowerCase().indexOf(filterField.getText().toLowerCase()) >= 0) {
          currentListData.add(o);
        }
      }
      fireContentsChanged(this, 0, currentListData.size());
      if (currentListData.size() > 0) {
        resultList.setSelectedIndex(0);
      }
      else {
        resultList.clearSelection();
      }
      if ((initialListData.length > 0) && (currentListData.size() == 0)) {
        filterField.setForeground(Color.RED);
      }
      else {
        filterField.setForeground(null);
      }
    }
  }

  private void initLayout() {
    BorderLayout layout = new BorderLayout();
    setLayout(layout);
    JScrollPane scrollPane = new JScrollPane(resultList);
    this.add(scrollPane, BorderLayout.CENTER);
    this.add(filterField, BorderLayout.NORTH);
  }

  private void initTextField() {
    filterField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) {
        dataModel.update();
      }

      public void removeUpdate(DocumentEvent e) {
        dataModel.update();
      }

      public void changedUpdate(DocumentEvent e) {
        dataModel.update();
      }
    });
    filterField.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        int size = resultList.getModel().getSize();
        if (size == 0) {
          return;
        }
        int currentIndex = resultList.getSelectedIndex();
        switch (e.getKeyCode()) {
          case KeyEvent.VK_DOWN:
            resultList.setSelectedIndex(Math.min(currentIndex + 1,
                                                 size - 1));
            break;
          case KeyEvent.VK_UP:
            resultList.setSelectedIndex(Math.max(currentIndex - 1, 0));
            break;
          case KeyEvent.VK_PAGE_DOWN:
            resultList.setSelectedIndex(size - 1);
            break;
          case KeyEvent.VK_PAGE_UP:
            resultList.setSelectedIndex(0);
            break;
          default: // ignore the event
        }
      }
    });
    filterField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        notifyActionListener();
      }
    });
  }

  public Object getSelectedValue() {
    return resultList.getSelectedValue();
  }

  public void setActionListener(ActionListener listener) {
    actionListener = listener;
  }

  private void notifyActionListener() {
    if (actionListener != null) {
      ActionEvent event = new ActionEvent(this, 0, actionCommand);
      actionListener.actionPerformed(event);
    }
  }

  private class Renderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
      return super.getListCellRendererComponent(list,
                                                stringifier.toString(value),
                                                index, isSelected, cellHasFocus);
    }
  }
}
