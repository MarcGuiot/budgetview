package org.globsframework.gui.splits.color;

import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class ColorServiceEditor implements ColorCreationListener {
  private ColorService colorService;
  private SplitsBuilder builder;

  private JColorChooser colorChooser;
  private JTextField textField;
  private String currentKey;
  private JList keyList;

  private PrintStream outputStream = System.out;

  public ColorServiceEditor(ColorService service) {
    colorService = service;
    Directory directory = new DefaultDirectory();
    directory.add(new ColorService());
    directory.add(IconLocator.class, IconLocator.NULL);
    builder = SplitsBuilder.init(directory);
    colorChooser = createColorChooser();
    textField = createTextField();
    keyList = createList();
    builder.add(keyList, textField, colorChooser, createComboBox(), createButton());
    colorService.addListener(new ColorChangeListener() {
      public void colorsChanged(ColorLocator colorLocator) {
        updateComponents();
      }
    });
    keyList.setSelectedIndex(0);
    builder.setSource(getClass(), "/splits/coloreditor.splits");
    colorService.addCreationListener(this);
  }

  public void setOutputStream(PrintStream outputStream) {
    this.outputStream = outputStream;
  }

  private void updateComponents() {
    if (currentKey != null) {
      Color color = colorService.get(currentKey);
      colorChooser.setColor(color);
      textField.setText(Colors.toString(color).toUpperCase());
    }
    else {
      textField.setText("");
    }
  }

  private JList createList() {
    final JList jList = new JList(getKeyNames());
    jList.setName("colorList");
    jList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent event) {
        currentKey = (String)jList.getSelectedValue();
        updateComponents();
      }

    });

    jList.setCellRenderer(new CellRenderer());
    return jList;
  }

  private String[] getKeyNames() {
    List<String> keys = colorService.getKeys();
    Collections.sort(keys);
    return keys.toArray(new String[keys.size()]);
  }

  private JComboBox createComboBox() {
    final JComboBox comboBox = new JComboBox(new DefaultComboBoxModel(new Vector(colorService.getColorSets())));
    comboBox.setSelectedItem(colorService.getCurrentColorSet());
    comboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String previousKey = (String)keyList.getSelectedValue();

        ColorSet colorSet = (ColorSet)comboBox.getSelectedItem();
        colorService.setCurrentSet(colorSet);
        String[] keyNames = getKeyNames();
        keyList.setModel(new DefaultComboBoxModel(keyNames));

        int index = (previousKey != null) ? Arrays.binarySearch(keyNames, previousKey) : -1;
        if (index >= 0) {
          keyList.setSelectedIndex(index);
        }
        else {
          keyList.clearSelection();
        }
      }
    });
    comboBox.setRenderer(new DefaultListCellRenderer() {
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        ColorSet set = (ColorSet)value;
        this.setText(set.getName());
        return this;
      }
    });
    comboBox.setName("colorSets");
    return comboBox;
  }

  private JColorChooser createColorChooser() {
    final JColorChooser colorChooser = new JColorChooser();
    colorChooser.setPreviewPanel(new JPanel());
    colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent event) {
        if (currentKey != null) {
          colorService.set(currentKey, colorChooser.getColor());
          keyList.repaint();
        }
      }
    });
    colorChooser.setName("colorChooser");
    return colorChooser;
  }

  private JTextField createTextField() {
    final JTextField textField = new JTextField();
    textField.setName("colorName");
    textField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        colorService.set(currentKey, Colors.toColor(textField.getText()));
      }
    });
    textField.setDocument(new PlainDocument() {
      public void insertString(int offs, String str, AttributeSet a)
        throws BadLocationException {
        StringBuilder buf = new StringBuilder();
        for (char c : str.toCharArray()) {
          if (Character.isLetterOrDigit(c)) {
            buf.append(c);
          }
        }
        super.insertString(offs, buf.toString(), a);
      }
    });
    return textField;
  }

  private JButton createButton() {
    JButton button = new JButton("Print");
    button.setName("printButton");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        outputStream.println("========== New colors ==========");
        colorService.printCurrentSet(outputStream);
        outputStream.println();
      }
    });
    return button;
  }

  public SplitsBuilder getBuilder() {
    return builder;
  }

  public void colorCreated(String key) {
    final String[] listData = getKeyNames();
    keyList.setModel(new AbstractListModel() {
      public int getSize() {
        return listData.length;
      }

      public Object getElementAt(int i) {
        return listData[i];
      }
    });
  }

  private class CellRenderer implements ListCellRenderer {

    private JLabel label = new JLabel();
    private ColorRectIcon icon = new ColorRectIcon();

    private CellRenderer() {
      label.setIcon(icon);
      label.setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      icon.setColor(colorService.get(value));
      label.setText((String)value);
      label.setForeground(colorService.isSet(value) ? Color.BLACK : Color.RED);
      label.setBackground(isSelected ? Color.LIGHT_GRAY : Color.WHITE);
      return label;
    }
  }

  private static class ColorRectIcon implements Icon {

    private Color color = Color.WHITE;

    public void setColor(Color color) {
      this.color = color;
    }

    public int getIconHeight() {
      return 12;
    }

    public int getIconWidth() {
      return 15;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      int w = getIconWidth();
      int h = getIconHeight();

      Graphics2D g2 = (Graphics2D)g;
      g2.setColor(color);
      g.fillRect(x, y + 1, x + w, y + h - 2);
      g2.setColor(Color.DARK_GRAY);
      g.drawRect(x, y + 1, x + w, y + h - 2);
    }
  }
}
