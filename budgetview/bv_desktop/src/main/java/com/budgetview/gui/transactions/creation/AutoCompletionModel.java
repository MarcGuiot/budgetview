package com.budgetview.gui.transactions.creation;

import com.budgetview.model.Transaction;
import com.jidesoft.swing.Searchable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;

import javax.swing.*;
import java.util.*;

public class AutoCompletionModel extends Searchable {

  private List<String> labels = new ArrayList<String>();
  private SortedSet<Integer> selection = new TreeSet<Integer>();

  public AutoCompletionModel(GlobRepository repository) {
    super(new JLabel());
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
        changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
          public void visitCreation(Key key, FieldValues values) throws Exception {
            addLabel(values.get(Transaction.LABEL));
          }

          public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
            if (values.contains(Transaction.LABEL)) {
              removeLabelIfUnused(values.getPrevious(Transaction.LABEL), repository);
              addLabel(values.get(Transaction.LABEL));
            }
          }

          public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
            removeLabelIfUnused(previousValues.get(Transaction.LABEL), repository);
          }
        });
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(Transaction.TYPE)) {
          reloadLabels(repository);
        }
      }
    });
    reloadLabels(repository);
  }

  private void reloadLabels(GlobRepository repository) {
    labels.clear();
    SortedSet<String> transactionLabels = new TreeSet<String>();
    for (String label : repository
      .getAll(Transaction.TYPE, GlobMatchers.isNotTrue(Transaction.PLANNED))
      .getValueSet(Transaction.LABEL)) {
      if (Strings.isNotEmpty(label)) {
        transactionLabels.add(label.toUpperCase());
      }
    }
    labels.addAll(transactionLabels);
  }

  private void addLabel(String label) {
    if (label == null) {
      return;
    }
    String upperCaseLabel = label.toUpperCase();
    int index = Collections.binarySearch(labels, upperCaseLabel);
    if (index >= 0) {
      return;
    }
    labels.add(index < 0 ? -index - 1 : index, upperCaseLabel);
    selection.clear();
  }

  private void removeLabelIfUnused(String label, GlobRepository repository) {
    if (label == null) {
      return;
    }
    String upperCaseLabel = label.toUpperCase();
    if (repository.contains(Transaction.TYPE, GlobMatchers.fieldEqualsIgnoreCase(Transaction.LABEL, label))) {
      return;
    }
    labels.remove(upperCaseLabel);
    selection.clear();
  }

  protected int getSelectedIndex() {
    if (selection.isEmpty()) {
      return -1;
    }
    return selection.first();
  }

  protected void setSelectedIndex(int index, boolean incremental) {
    if (!incremental) {
      selection.clear();
    }
    selection.add(index);
  }

  protected int getElementCount() {
    return labels.size();
  }

  protected Object getElementAt(int index) {
    return labels.get(index);
  }

  protected String convertElementToString(Object element) {
    return Strings.toString(element);
  }

  public void showPopup(String searchingText) {
  }

  public void hidePopup() {
  }
}
