package com.budgetview.desktop.signpost;

import com.budgetview.desktop.components.tips.DetailsTip;
import com.budgetview.desktop.components.tips.TipAnchor;
import com.budgetview.desktop.components.tips.TipPosition;
import com.budgetview.desktop.signpost.components.SignpostStyleUpdater;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.ItemNotFound;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class SignpostService {

  private Map<String, JComponent> components = new HashMap<String, JComponent>();
  private Directory directory;

  public SignpostService(Directory directory) {
    this.directory = directory;
  }

  public void registerComponent(String id, JComponent component) {
    components.put(id, component);
  }

  public Disposable show(String id, String text, TipPosition position, TipAnchor anchor) {
    JComponent component = components.get(id);
    if (component == null) {
      throw new ItemNotFound("No component registered with id: " + id);
    }
    DetailsTip detailsTip = new DetailsTip(component, text, directory) {
      protected Disposable initUpdater(Directory directory) {
        return SignpostStyleUpdater.install(this, directory);
      }
    };
    detailsTip.setPosition(position);
    detailsTip.setAnchor(anchor);
    detailsTip.setClickThrough();
    detailsTip.show();
    return detailsTip;
  }
}
