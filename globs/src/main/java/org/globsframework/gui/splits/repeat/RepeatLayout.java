package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.layout.ComponentConstraints;

import javax.swing.*;
import java.util.List;

public interface RepeatLayout {

  void checkHeader(Splitter[] splitters, String repeatRef);

  void checkContent(Splitter[] splitterTemplates, String repeatRef);

  void init(JPanel panel);

  void set(JPanel panel, List<ComponentConstraints[]> components);

  void insert(JPanel panel, ComponentConstraints[] constraints, int index);

  void remove(JPanel panel, int index);

  void move(JPanel panel, int previousIndex, int newIndex);

  boolean managesInsets();
}
