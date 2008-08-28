package org.globsframework.gui.editors;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.gui.utils.DefaultGlobSelection;
import org.globsframework.metamodel.Field;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidFormat;

import javax.swing.text.JTextComponent;
import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public abstract class AbstractGlobTextEditor<COMPONENT_TYPE extends JTextComponent> extends AbstractGlobComponentHolder implements GlobSelectionListener {
  protected Field field;
  private GlobList lastSelectedGlobs;
  protected COMPONENT_TYPE textComponent;
  private Directory directory;
  private Object valueForMultiSelection;
  private boolean forceNotEditable;
  private GlobList forcedSelection;

  protected AbstractGlobTextEditor(Field field, COMPONENT_TYPE component, GlobRepository repository, Directory directory) {
    super(field.getGlobType(), repository, directory);
    this.field = field;
    this.textComponent = component;
    this.directory = directory;
    initTextComponent();
  }

  public AbstractGlobTextEditor setMultiSelectionText(Object valueForMultiSelection) {
    this.valueForMultiSelection = valueForMultiSelection;
    return this;
  }

  public AbstractGlobTextEditor setEditable(boolean b) {
    forceNotEditable = !b;
    return this;
  }

  private void initTextComponent() {
    textComponent.setName(field.getName());
    textComponent.setEnabled(false);
    textComponent.setEditable(false);
    textComponent.setText("");
    registerFocusLostListener();
    registerChangeListener();
  }

  private void registerFocusLostListener() {
    getComponent().addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
      }

      public void focusLost(FocusEvent e) {
        applyChanges();
      }
    });
  }

  protected abstract void registerChangeListener();

  public final COMPONENT_TYPE getComponent() {
    if (forcedSelection != null) {
      selectionUpdated(new DefaultGlobSelection(forcedSelection, forcedSelection.getTypes()));
    }
    else {
      SelectionService service = directory.get(SelectionService.class);
      service.addListener(this, field.getGlobType());
    }
    return textComponent;
  }

  public AbstractGlobTextEditor forceSelection(Glob glob) {
    this.forcedSelection = new GlobList(glob);
    return this;
  }

  protected void applyChanges() {
    if (forceNotEditable) {
      return;
    }
    Object value;
    try {
      value = getValue();
    }
    catch (InvalidFormat e) {
      return;
    }
    try {
      repository.enterBulkDispatchingMode();
      for (Glob glob : lastSelectedGlobs) {
        repository.update(glob.getKey(), AbstractGlobTextEditor.this.field, value);
      }
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    if (!selection.isRelevantForType(field.getGlobType())) {
      return;
    }
    lastSelectedGlobs = selection.getAll(field.getGlobType());
    boolean selectionNotEmpty = !lastSelectedGlobs.isEmpty();
    textComponent.setEnabled(selectionNotEmpty);
    if (!forceNotEditable) {
      textComponent.setEditable(selectionNotEmpty);
    }

    Object value = null;
    for (Glob glob : lastSelectedGlobs) {
      Object globValue = glob.getValue(field);
      if ((value != null) && (!value.equals(globValue))) {
        value = valueForMultiSelection;
        break;
      }
      value = globValue;
    }
    setValue(value);
  }

  protected Object getValue() throws InvalidFormat {
    return textComponent.getText();
  }

  protected void setValue(Object value) {
    textComponent.setText((String)(value == null ? "" : value));
  }

  public void dispose() {
    selectionService.removeListener(this);
  }
}
