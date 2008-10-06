package org.globsframework.gui.editors;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.AbstractDocumentListener;
import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.metamodel.Field;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidFormat;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public abstract class AbstractGlobTextEditor<COMPONENT_TYPE extends JTextComponent, PARENT extends AbstractGlobTextEditor> extends AbstractGlobComponentHolder implements GlobSelectionListener {
  protected Field field;
  private GlobList lastSelectedGlobs;
  protected COMPONENT_TYPE textComponent;
  private Object valueForMultiSelection;
  private boolean forceNotEditable;
  private GlobList forcedSelection;
  private boolean isInitialized = false;
  private boolean notifyAtKeyPressed;
  private DocumentListener keyPressedListener;
  private boolean isAdjusting = false;

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

  private void initTextComponent() {
    textComponent.setName(field.getName());
    textComponent.setEnabled(false);
    textComponent.setEditable(false);
    textComponent.setText("");
    registerFocusLostListener();
    registerChangeListener();
    registerKeyPressedListener();
  }

  private void registerKeyPressedListener() {
    if (isNotifyAtKeyPressed()) {
      if (keyPressedListener == null) {
        keyPressedListener = new AbstractDocumentListener() {
          protected void documentChanged(DocumentEvent e) {
            applyChanges();
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
        if (!notifyAtKeyPressed) {
          applyChanges();
        }
      }
    });
  }

  protected abstract void registerChangeListener();

  public final COMPONENT_TYPE getComponent() {
    if (isInitialized) {
      return textComponent;
    }
    isInitialized = true;
    initTextComponent();
    if (forcedSelection != null) {
      selectionUpdated(GlobSelectionBuilder.init()
        .add(forcedSelection, type).get());
    }
    else {
      SelectionService service = directory.get(SelectionService.class);
      service.addListener(this, field.getGlobType());
    }
    return textComponent;
  }

  public PARENT forceSelection(Glob glob) {
    if (isInitialized && forcedSelection == null) {
      SelectionService service = directory.get(SelectionService.class);
      service.removeListener(this);
    }
    this.forcedSelection = new GlobList(glob);
    if (isInitialized) {
      selectionUpdated(GlobSelectionBuilder.init().add(forcedSelection, type).get());
    }
    return (PARENT)this;
  }

  protected void applyChanges() {
    if (isAdjusting) {
      return;
    }
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

  public PARENT setNotifyAtKeyPressed(boolean notifyAtKeyPressed) {
    this.notifyAtKeyPressed = notifyAtKeyPressed;
    registerKeyPressedListener();
    return (PARENT)this;
  }

  protected Object getValue() throws InvalidFormat {
    return textComponent.getText();
  }

  protected void setValue(Object value) {
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
  }

  public boolean isNotifyAtKeyPressed() {
    return notifyAtKeyPressed;
  }

  public boolean isAdjusting() {
    return isAdjusting;
  }

  public void setAdjusting(boolean adjusting) {
    this.isAdjusting = adjusting;
  }
}
