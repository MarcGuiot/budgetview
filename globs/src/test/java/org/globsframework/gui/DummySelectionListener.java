package org.globsframework.gui;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;
import org.globsframework.xml.XmlTestLogger;

import java.io.IOException;
import java.util.*;

public class DummySelectionListener extends XmlTestLogger implements GlobSelectionListener {

  public static DummySelectionListener register(Directory directory, GlobType... types) {
    SelectionService service = directory.get(SelectionService.class);
    return register(service, types);
  }

  public static DummySelectionListener register(SelectionService service, GlobType... types) {
    DummySelectionListener listener = new DummySelectionListener();
    service.addListener(listener, types);
    return listener;
  }

  private boolean dumpSelections;

  private DummySelectionListener() {
  }

  public void dumpSelections() {
    this.dumpSelections = true;
  }

  public void selectionUpdated(GlobSelection selection) {

    if (dumpSelections) {
      System.out.println(selection);
      Thread.dumpStack();
    }

    List<String> keys = new ArrayList<String>();
    for (GlobType type : selection.getRelevantTypes()) {
      for (Glob glob : selection.getAll(type)) {
        keys.add(glob.getKey().toString());
      }
    }
    Collections.sort(keys);
    try {
      LoggerTag tag = log("selection");
      tag.addAttribute("types", getRelevantTypes(selection));
      for (String key : keys) {
        tag.createChildTag("item").addAttribute("key", key).end();
      }
      tag.end();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getRelevantTypes(GlobSelection selection) {
    StringBuilder builder = new StringBuilder();
    List<GlobType> relevantTypes = Arrays.asList(selection.getRelevantTypes());
    Collections.sort(relevantTypes, new Comparator<GlobType>() {
      public int compare(GlobType t1, GlobType t2) {
        return t1.getName().compareTo(t2.getName());
      }
    });
    for (GlobType type : relevantTypes) {
      if (builder.length() > 0) {
        builder.append(',');
      }
      builder.append(type.getName());
    }
    return builder.toString();
  }
}
