package org.globsframework.gui.editors;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

public class GlobComboEditor
  extends AbstractGlobComponentHolder
  implements ChangeSetListener, GlobSelectionListener {

  private final IntegerField field;
  private final List<Integer> values;
  private Set<Key> selectedKeys = Collections.emptySet();
  private Key forcedKey;

  private JComboBox combo;

  public static GlobComboEditor init(IntegerField field,
                                     int[] values,
                                     GlobRepository repository,
                                     Directory directory) {
    return new GlobComboEditor(field, Utils.toObjectIntegers(values), repository, directory);
  }

  private GlobComboEditor(IntegerField field,
                          Integer[] values,
                          GlobRepository repository,
                          Directory directory) {
    super(field.getGlobType(), repository, directory);
    this.field = field;
    this.values = Arrays.asList(values);
    this.combo = new JComboBox(values);
    combo.setAction(new ComboSelectionAction());
    combo.setEnabled(false);
    repository.addChangeListener(this);
    selectionService.addListener(this, field.getGlobType());
    updateCombo();
  }

  public GlobComboEditor forceKey(Key key) {
    this.forcedKey = key;
    if (repository.contains(forcedKey)) {
      selectedKeys = Collections.singleton(forcedKey);
    }
    else {
      selectedKeys = Collections.emptySet();
    }
    updateCombo();
    return this;
  }

  public void selectionUpdated(GlobSelection selection) {
    if (forcedKey != null) {
      return;
    }

    selectedKeys = selection.getAll(field.getGlobType()).getKeySet();
    updateCombo();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (forcedKey != null) {
      updateSelectedWithForcedKey(repository);
      updateCombo();
      return;
    }

    for (Key key : selectedKeys) {
      if (changeSet.containsChanges(key)) {
        updateCombo();
        return;
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (!changedTypes.contains(field.getGlobType())) {
      return;
    }

    if (forcedKey != null) {
      updateSelectedWithForcedKey(repository);
    }

    updateCombo();
  }

  private void updateSelectedWithForcedKey(GlobRepository repository) {
    if (repository.contains(forcedKey)) {
      selectedKeys = Collections.singleton(forcedKey);
    }
    else {
      selectedKeys = Collections.emptySet();
    }
  }

  private void updateCombo() {
    Set<Integer> globsValues = new HashSet<Integer>();
    for (Key key : selectedKeys) {
      Glob glob = repository.find(key);
      if (glob != null) {
        globsValues.add(glob.get(field));
      }
    }

    if (globsValues.isEmpty()) {
      combo.setSelectedIndex(-1);
      combo.setEnabled(false);
      return;
    }

    if (globsValues.size() > 1) {
      combo.setSelectedIndex(-1);
      combo.setEnabled(true);
      return;
    }

    Integer selectedValue = globsValues.iterator().next();
    combo.setEnabled(true);
    if (values.contains(selectedValue)) {
      combo.setSelectedItem(selectedValue);
    }
    else {
      combo.setSelectedIndex(-1);
    }
  }

  public GlobComboEditor setName(String name) {
    combo.setName(name);
    return this;
  }

  public JComboBox getComponent() {
    return combo;
  }

  public void dispose() {
    if (combo != null){
      repository.removeChangeListener(this);
      selectionService.removeListener(this);
      combo = null;
    }
  }

  public GlobComboEditor setRenderer(ListCellRenderer renderer) {
    combo.setRenderer(renderer);
    return this;
  }

  private class ComboSelectionAction extends AbstractAction {
    public void actionPerformed(ActionEvent actionEvent) {
      repository.startChangeSet();
      try {
        Object selectedValue = combo.getSelectedItem();
        for (Key key : selectedKeys) {
          if (repository.contains(key)) {
            repository.update(key, field, selectedValue);
          }
        }

      }
      finally {
        repository.completeChangeSet();
      }
    }
  }
}
