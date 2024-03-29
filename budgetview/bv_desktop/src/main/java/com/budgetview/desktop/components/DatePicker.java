package com.budgetview.desktop.components;

import com.budgetview.desktop.description.Formatting;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;
import org.jdesktop.swingx.JXDatePicker;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class DatePicker {
  private JXDatePicker datePicker;
  private Key selectedKey;

  public DatePicker(final DateField dateField, final GlobRepository repository, Directory directory) {
    datePicker = new JXDatePicker();
    datePicker.setFormats(Formatting.getDateFormat());
    datePicker.getEditor().setName(dateField.getName() + "Field");
    datePicker.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if ("date".equals(evt.getPropertyName())) {
          if (selectedKey != null) {
            repository.update(selectedKey, dateField, datePicker.getDate());
          }
        }
      }
    });

    SelectionService service = directory.get(SelectionService.class);
    service.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        if (selection.isRelevantForType(dateField.getGlobType())) {
          Glob selected = selection.getAll(dateField.getGlobType()).getFirst();
          if (selected != null) {
            selectedKey = selected.getKey();
            datePicker.setDate(repository.get(selectedKey).get(dateField));
          }
        }
      }
    }, dateField.getGlobType());
    repository.addChangeListener(new AbstractChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.getDeleted(dateField.getGlobType()).contains(selectedKey)) {
          selectedKey = null;
          datePicker.setDate(null);
        }
        else {
          if (selectedKey != null) {
            if (changeSet.containsChanges(selectedKey)) {
              datePicker.setDate(repository.get(selectedKey).get(dateField));
            }
          }
        }
      }
    });
  }

  public JXDatePicker getComponent() {
    return datePicker;
  }
}
