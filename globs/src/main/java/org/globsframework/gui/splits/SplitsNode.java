package org.globsframework.gui.splits;

import java.awt.*;

public interface SplitsNode<T extends Component> {

  T getComponent();

  void applyStyle(String styleNames);
}
