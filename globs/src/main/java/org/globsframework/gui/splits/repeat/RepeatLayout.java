package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.Splitter;

import javax.swing.*;
import java.util.List;

public interface RepeatLayout {
  
  void check(Splitter[] splitterTemplates, String repeatRef);

  void init(JPanel panel);

  void set(JPanel panel, List<ComponentStretch[]> stretches);

  void insert(JPanel panel, ComponentStretch[] stretches, int index);

  void remove(JPanel panel, int index);

  boolean managesInsets();

}
