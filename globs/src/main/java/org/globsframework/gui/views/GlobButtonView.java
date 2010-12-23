package org.globsframework.gui.views;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class GlobButtonView extends AbstractGlobTextView<GlobButtonView> {
  private JButton button;

  public static GlobButtonView init(GlobType type, GlobRepository repository,
                                    Directory directory, GlobListFunctor callback) {
    GlobListStringifier stringifier = directory.get(DescriptionService.class).getListStringifier(type);
    return new GlobButtonView(type, repository, directory, stringifier, callback);
  }

  public static GlobButtonView init(GlobType type, GlobRepository repository,
                                    Directory directory, GlobListStringifier stringifier,
                                    GlobListFunctor callback) {
    return new GlobButtonView(type, repository, directory, stringifier, callback);
  }

  public static GlobButtonView init(Field field, GlobRepository repository, Directory directory, GlobListFunctor callback) {
    GlobListStringifier stringifier = directory.get(DescriptionService.class).getListStringifier(field);
    return init(field.getGlobType(), repository, directory, stringifier, callback);
  }

  public static GlobButtonView init(Link link, GlobRepository repository, Directory directory, GlobListFunctor callback) {
    GlobListStringifier stringifier = directory.get(DescriptionService.class).getListStringifier(link);
    return init(link.getSourceType(), repository, directory, stringifier, callback);
  }

  public static GlobButtonView init(LinkField link, GlobRepository repository, Directory directory, GlobListFunctor callback) {
    GlobListStringifier stringifier = directory.get(DescriptionService.class).getListStringifier(link);
    return init(link.getSourceType(), repository, directory, stringifier, callback);
  }

  private GlobButtonView(GlobType type, final GlobRepository repository, Directory directory,
                         GlobListStringifier stringifier, final GlobListFunctor callback) {
    super(type, repository, directory, stringifier);
    this.button = new JButton(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        callback.run(getFilteredSelection(), repository);
      }
    });
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

  public GlobButtonView setName(String name) {
    if (button != null) {
      button.setName(name);
    }
    return super.setName(name);
  }
}