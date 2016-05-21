package com.budgetview.gui.addons.dev;

import com.budgetview.model.AddOns;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ToggleAllAddOnsAction extends AbstractAction {
  public static final String ENABLE_ALL = "Enable all add-ons";
  public static final String DISABLE_ALL = "Disable all add-ons";
  private boolean value;
  private GlobRepository repository;

  public static ToggleAllAddOnsAction enableAll(GlobRepository repository) {
    return new ToggleAllAddOnsAction(true, ENABLE_ALL, repository);
  }

  public static ToggleAllAddOnsAction disableAll(GlobRepository repository) {
    return new ToggleAllAddOnsAction(false, DISABLE_ALL, repository);
  }

  public ToggleAllAddOnsAction(boolean value, String label, GlobRepository repository) {
    super(label);
    this.value = value;
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    repository.startChangeSet();
    for (Field field : AddOns.TYPE.getFields()) {
      if (field instanceof BooleanField) {
        repository.update(AddOns.KEY, field, value);
      }
    }
    repository.completeChangeSet();
  }
}
