package org.globsframework.gui.utils;

import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class GlobBooleanNodeStyleUpdater extends AbstractGlobBooleanUpdater {
  private SplitsNode splitsNode;
  private String styleForTrue;
  private String styleForFalse;

  public static GlobBooleanNodeStyleUpdater init(BooleanField field, SplitsNode splitsNode,
                                                 String styleForTrue, String styleForFalse,
                                                 GlobRepository repository, Directory directory) {
    return new GlobBooleanNodeStyleUpdater(field, splitsNode, styleForTrue, styleForFalse, repository);
  }

  public GlobBooleanNodeStyleUpdater(BooleanField field, SplitsNode splitsNode,
                                     String styleForTrue, String styleForFalse,
                                     GlobRepository repository) {
    super(field, repository);
    this.splitsNode = splitsNode;
    this.styleForTrue = styleForTrue;
    this.styleForFalse = styleForFalse;
  }

  protected void doUpdate(boolean value) {
    String styleName = value ? styleForTrue : styleForFalse;
    splitsNode.applyStyle(styleName);
  }
}
