package org.globsframework.gui.splits.utils;

import junit.framework.Assert;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.layout.ComponentConstraints;
import org.globsframework.gui.splits.repeat.RepeatLayout;
import org.globsframework.utils.TestUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class DummyRepeatLayout implements RepeatLayout {

  public List<String> commands = new ArrayList<String>();
  public static DummyRepeatLayout lastLayout = null;

  public DummyRepeatLayout() {
    this.lastLayout= this;
  }

  public static void reset() {
    lastLayout = null;
  }

  public static void checkLastInstance(String... expectedCommands) {
    Assert.assertNotNull(lastLayout);
    TestUtils.assertEquals(lastLayout.commands, expectedCommands);
  }

  public void checkHeader(Splitter[] splitters, String repeatRef) {
    commands.add("checkHeader");
  }

  public void checkContent(Splitter[] splitterTemplates, String repeatRef) {
    commands.add("checkContent");
  }

  public void init(JPanel panel) {
    commands.add("init");
  }

  public void set(JPanel panel, List<ComponentConstraints[]> components) {
    commands.add("set");
  }

  public void insert(JPanel panel, ComponentConstraints[] constraints, int index) {
    commands.add("insert");
  }

  public void remove(JPanel panel, int index) {
    commands.add("remove");
  }

  public void move(JPanel panel, int previousIndex, int newIndex) {
    commands.add("move");
  }

  public boolean managesInsets() {
    return false;
  }
}
