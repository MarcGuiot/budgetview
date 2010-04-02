package org.designup.picsou.gui.budget.footers;

import org.globsframework.model.Key;

import javax.swing.*;
import java.util.List;

public interface BudgetAreaSeriesFooter {

  void init(JEditorPane editorPane);

  void update(List<Key> displayedKeys);
}
