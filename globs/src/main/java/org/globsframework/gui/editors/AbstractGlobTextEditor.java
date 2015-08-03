package org.globsframework.gui.editors;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.AbstractDocumentListener;
import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidFormat;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractGlobTextEditor<COMPONENT_TYPE extends JTextComponent, PARENT extends AbstractGlobTextEditor>
  extends AbstractGlobComponentHolder implements GlobSelectionListener, ChangeSetListener {
  private static final Object NULL = new Object();
  private static final Object MULTI_SELECTION = new Object();
  protected Field field;
  private GlobList currentGlobs = GlobList.EMPTY;
  protected COMPONENT_TYPE textComponent;
  private Object valueForMultiSelection;
  private boolean forceNotEditable;
  private List<Key> forcedSelection;
  private boolean isInitialized = false;
  private boolean notifyOnKeyPressed;
  private DocumentListener keyPressedListener;
  private boolean isAdjusting = false;
  private String name;

  protected AbstractGlobTextEditor(Field field, COMPONENT_TYPE component, GlobRepository repository, Directory directory) {
    super(field.getGlobType(), repository, directory);
    this.field = field;
    this.textComponent = component;
  }

  public PARENT setMultiSelectionText(Object valueForMultiSelection) {
    this.valueForMultiSelection = valueForMultiSelection;
    return (PARENT)this;
  }

  public PARENT setEditable(boolean b) {
    forceNotEditable = !b;
    return (PARENT)this;
  }

  public PARENT setName(String name) {
    this.name = name;
    this.textComponent.setName(name);
    return (PARENT)this;
  }

  private void initTextComponent() {
    textComponent.setName(name != null ? name : field.getName());
    textComponent.setEnabled(false);
    textComponent.setEditable(false);
    textComponent.setText("");
    registerFocusLostListener();
    registerActionListener();
    registerKeyPressedListener();
  }

  private void registerKeyPressedListener() {
    if (isNotifiedOnKeyPressed()) {
      if (keyPressedListener == null) {
        keyPressedListener = new AbstractDocumentListener() {
          protected void documentChanged(DocumentEvent e) {
            apply();
          }
        };
        textComponent.getDocument().addDocumentListener(keyPressedListener);
      }
    }
    else {
      if (keyPressedListener != null) {
        textComponent.getDocument().removeDocumentListener(keyPressedListener);
        keyPressedListener = null;
      }
    }
  }

  private void registerFocusLostListener() {
    getComponent().addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
      }

      public void focusLost(FocusEvent e) {
        if (!notifyOnKeyPressed) {
          apply();
        }
      }
    });
  }

  protected abstract void registerActionListener();

  public final COMPONENT_TYPE getComponent() {
    if (isInitialized) {
      return textComponent;
    }
    isInitialized = true;
    initTextComponent();
    repository.addChangeListener(this);
    if (forcedSelection != null) {
      GlobList elements = getForceSelectedGlob();
      selectionUpdated(GlobSelectionBuilder.init().add(elements, type).get());
    }
    else {
      SelectionService service = directory.get(SelectionService.class);
      service.addListener(this, field.getGlobType());
    }
    return textComponent;
  }

  private GlobList getForceSelectedGlob() {
    GlobList elements = new GlobList();
    for (Key key : forcedSelection) {
      Glob glob = repository.find(key);
      if (glob != null) {
        elements.add(glob);
      }
    }
    return elements;
  }

  public PARENT forceSelection(Key key) {
    if (isInitialized && forcedSelection == null) {
      SelectionService service = directory.get(SelectionService.class);
      service.removeListener(this);
    }
    this.forcedSelection = new ArrayList<Key>();
    forcedSelection.add(key);
    if (isInitialized) {
      GlobList elements = getForceSelectedGlob();
      selectionUpdated(GlobSelectionBuilder.init().add(elements, type).get());
    }
    return (PARENT)this;
  }

  public void apply() {
    if (isAdjusting || forceNotEditable || currentGlobs.isEmpty()) {
      return;
    }
    Object value;
    try {
      value = getConvertedDisplayedValue();
    }
    catch (InvalidFormat e) {
      return;
    }
    try {
      isAdjusting = true;
      repository.startChangeSet();
      for (Glob glob : currentGlobs) {
        repository.update(glob.getKey(), AbstractGlobTextEditor.this.field, value);
      }
    }
    finally {
      repository.completeChangeSet();
      isAdjusting = false;
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    if (!selection.isRelevantForType(field.getGlobType())) {
      return;
    }
    currentGlobs = selection.getAll(field.getGlobType());

    boolean selectionNotEmpty = !currentGlobs.isEmpty();
    textComponent.setEnabled(selectionNotEmpty);
    if (!forceNotEditable) {
      textComponent.setEditable(selectionNotEmpty);
    }

    Object value = NULL;
    for (Glob glob : currentGlobs) {
      Object globValue = glob.getValue(field);
      if (value != NULL) {
        if ((value == null && globValue != null) || (value != null && !value.equals(globValue))) {
          value = MULTI_SELECTION;
          break;
        }
      }
      value = globValue;
    }
    if (value == null) {
      value = getValueForNull();
    }
    if (value == NULL) {
      value = null;
    }
    if (value == MULTI_SELECTION) {
      value = valueForMultiSelection;
    }
    setDisplayedValue(value);
  }

  public PARENT setNotifyOnKeyPressed(boolean notifyOnKeyPressed) {
    this.notifyOnKeyPressed = notifyOnKeyPressed;
    registerKeyPressedListener();
    return (PARENT)this;
  }

  public boolean isNotifiedOnKeyPressed() {
    return notifyOnKeyPressed;
  }

  protected Object getConvertedDisplayedValue() throws InvalidFormat {
    return textComponent.getText();
  }

  protected void setDisplayedValue(Object value) {
    isAdjusting = true;
    try {
      textComponent.setText((String)(value == null ? "" : value));
    }
    finally {
      isAdjusting = false;
    }
  }

  public void dispose() {
    selectionService.removeListener(this);
    repository.removeChangeListener(this);
    for (FocusListener listener : textComponent.getFocusListeners()) {
      textComponent.removeFocusListener(listener);
    }
    if (keyPressedListener != null){
      textComponent.getDocument().removeDocumentListener(keyPressedListener);
    }
  }

  public boolean isAdjusting() {
    return isAdjusting;
  }

  protected void setAdjusting(boolean adjusting) {
    this.isAdjusting = adjusting;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (isAdjusting) {
      return;
    }
    if ((forcedSelection == null) && currentGlobs.isEmpty()) {
      return;
    }
    if (!changeSet.containsCreationsOrDeletions(type) &&
        !changeSet.containsUpdates(field)) {
      return;
    }
    if (forcedSelection != null) {
      GlobList elements = getForceSelectedGlob();
      selectionUpdated(GlobSelectionBuilder.init().add(elements, type).get());
    }
    else {
      currentGlobs.keepExistingGlobsOnly(this.repository);
      selectionUpdated(GlobSelectionBuilder.init().add(currentGlobs, type).get());
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (forcedSelection != null && changedTypes.contains(type)) {
      GlobList elements = getForceSelectedGlob();
      selectionUpdated(GlobSelectionBuilder.init().add(elements, type).get());
    }
    else {
      GlobList newCurrentsGlob = new GlobList();
      for (Glob glob : currentGlobs) {
        Glob newGlob = repository.find(glob.getKey());
        if (newGlob != null) {
          newCurrentsGlob.add(newGlob);
        }
      }
      currentGlobs = newCurrentsGlob;
      selectionUpdated(GlobSelectionBuilder.init().add(currentGlobs, type).get());
    }
  }

  public Object getValueForNull() {
    return null;
  }
}
