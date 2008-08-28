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

public class GlobButtonView extends AbstractGlobTextView<GlobButtonView> {
  private JButton button;

  public static GlobButtonView init(GlobType type, GlobRepository repository,
                                   Directory directory) {
    GlobListStringifier stringifier = directory.get(DescriptionService.class).getListStringifier(type);
    return new GlobButtonView(type, repository, directory, stringifier);
  }

  public static GlobButtonView init(GlobType type, GlobRepository repository,
                                   Directory directory, GlobListStringifier stringifier) {
    return new GlobButtonView(type, repository, directory, stringifier);
  }

  public static GlobButtonView init(Field field, GlobRepository repository, Directory directory) {
    GlobListStringifier stringifier = directory.get(DescriptionService.class).getListStringifier(field);
    return init(field.getGlobType(), repository, directory, stringifier);
  }

  public static GlobButtonView init(Link link, GlobRepository repository, Directory directory) {
    GlobListStringifier stringifier = directory.get(DescriptionService.class).getListStringifier(link);
    return init(link.getSourceType(), repository, directory, stringifier);
  }

  public static GlobButtonView init(LinkField link, GlobRepository repository, Directory directory) {
    GlobListStringifier stringifier = directory.get(DescriptionService.class).getListStringifier(link);
    return init(link.getSourceType(), repository, directory, stringifier);
  }

  private GlobButtonView(GlobType type, GlobRepository repository, Directory directory, GlobListStringifier stringifier) {
    super(type, repository, directory, stringifier);
    this.button = new JButton();
  }

  public JButton getComponent() {
    if (!initCompleted) {
      initCompleted = true;
      complete();
      update();
    }
    return button;
  }

  protected void doUpdate(String text) {
    button.setText(text);
  }

  protected String getText() {
    return button.getText();
  }
}