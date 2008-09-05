package org.globsframework.gui.editors;

import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.splits.layout.WrappedColumnLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.*;

public class GlobTextEditor extends AbstractGlobTextEditor<JTextField> {
  public static GlobTextEditor init(StringField field, GlobRepository repository, Directory directory) {
    return new GlobTextEditor(field, repository, directory, new JTextField());
  }

  public GlobTextEditor(StringField field, GlobRepository repository, Directory directory, JTextField component) {
    super(field, component, repository, directory);
  }

  protected void registerChangeListener() {
    textComponent.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        applyChanges();
      }
    });
  }

  public static void main(String[] args) {
    final JTextField field1 = new JTextField();
    field1.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
      }

      public void focusLost(FocusEvent e) {
        System.out.println("Action called " + field1.getText());
      }
    });

    JFrame frame = new JFrame();
    JButton button = new JButton("OK");
    button.setAction(new AbstractAction(){
      public void actionPerformed(ActionEvent e) {
        System.out.println("button ok     " + field1.getText());
      }
    });
    JPanel panel = new JPanel();
    frame.setContentPane(panel);
    panel.setLayout(new WrappedColumnLayout(3));

    JPanel p2 = new JPanel();
    p2.add(button);
    button.setPreferredSize(new Dimension(50, 10));
    button.setMinimumSize(new Dimension(50, 10));
    panel.add(p2);

    JPanel p1 = new JPanel();
    p1.add(field1);
    field1.setPreferredSize(new Dimension(50, 10));
    field1.setMinimumSize(new Dimension(50, 10));
    panel.add(p1);

    frame.setVisible(true);

  }
}
