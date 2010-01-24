package org.globsframework.gui.views;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class GlobLabelView extends AbstractGlobTextView<GlobLabelView> {
  private JLabel label;

  public static GlobLabelView init(GlobType type, GlobRepository repository,
                                   Directory directory) {
    GlobListStringifier stringifier = directory.get(DescriptionService.class).getListStringifier(type);
    return new GlobLabelView(type, repository, directory, stringifier);
  }

  public static GlobLabelView init(GlobType type, GlobRepository repository,
                                   Directory directory, GlobListStringifier stringifier) {
    return new GlobLabelView(type, repository, directory, stringifier);
  }

  public static GlobLabelView init(Field field, GlobRepository repository, Directory directory) {
    GlobListStringifier stringifier = directory.get(DescriptionService.class).getListStringifier(field);
    return init(field.getGlobType(), repository, directory, stringifier);
  }

  public static GlobLabelView init(Link link, GlobRepository repository, Directory directory) {
    GlobListStringifier stringifier = directory.get(DescriptionService.class).getListStringifier(link);
    return init(link.getSourceType(), repository, directory, stringifier);
  }

  public static GlobLabelView init(LinkField link, GlobRepository repository, Directory directory) {
    GlobListStringifier stringifier = directory.get(DescriptionService.class).getListStringifier(link);
    return init(link.getSourceType(), repository, directory, stringifier);
  }

  public static GlobLabelView init(LinkField link, String textForEmptySelection, String textForMultipleValues,
                                   GlobRepository repository, Directory directory) {
    GlobListStringifier stringifier = directory.get(DescriptionService.class)
      .getListStringifier(link, textForEmptySelection, textForMultipleValues);
    return init(link.getSourceType(), repository, directory, stringifier);
  }

  private GlobLabelView(GlobType type, GlobRepository repository, Directory directory, GlobListStringifier stringifier) {
    super(type, repository, directory, stringifier);
    this.label = new JLabel();
  }

  public JLabel getComponent() {
    if (!initCompleted) {
      initCompleted = true;
      complete();
      update();
    }
    return label;
  }

  protected void doUpdate(String text) {
    label.setText(text);
  }

  protected String getText() {
    return label.getText();
  }
}