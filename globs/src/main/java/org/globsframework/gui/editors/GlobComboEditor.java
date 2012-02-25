package org.globsframework.gui.editors;

import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.*;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class GlobComboEditor
  extends AbstractGlobComponentHolder
  implements ChangeSetListener {

  private final Key key;
  private final Field field;
  private final List<Object> values;

  private JComboBox combo;

  public static GlobComboEditor init(Key key,
                                     StringField field,
                                     String[] values,
                                     GlobRepository repository,
                                     Directory directory) {
    return new GlobComboEditor(key, field, values, repository, directory);
  }

  public static GlobComboEditor init(Key key,
                                     IntegerField field,
                                     int[] values,
                                     GlobRepository repository,
                                     Directory directory) {
    return new GlobComboEditor(key, field, Utils.toObjectIntegers(values), repository, directory);
  }

  private GlobComboEditor(Key key,
                          Field field,
                          Object[] values,
                          GlobRepository repository,
                          Directory directory) {
    super(field.getGlobType(), repository, directory);
    this.key = key;
    this.field = field;
    this.values = Arrays.asList(values);
    this.combo = new JComboBox(values);
    combo.setAction(new ComboSelectionAction());
    repository.addChangeListener(this);
    update();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(key)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(field.getGlobType())) {
      update();
    }
  }

  private void update() {
    Glob glob = repository.find(key);
    if (glob == null) {
      combo.setSelectedIndex(-1);
      combo.setEnabled(false);
      return;
    }

    combo.setEnabled(true);
    Object value = glob.getValue(field);
    if (values.contains(value)) {
      combo.setSelectedItem(value);
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
    repository.addChangeListener(this);
    combo = null;
  }

  private class ComboSelectionAction extends AbstractAction {
    public void actionPerformed(ActionEvent actionEvent) {
      if (repository.contains(key)) {
        repository.update(key, field, combo.getSelectedItem());
      }
    }
  }
}
