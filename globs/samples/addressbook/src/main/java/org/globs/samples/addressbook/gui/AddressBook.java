package org.globs.samples.addressbook.gui;

import org.globs.samples.addressbook.model.Contact;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.Formats;
import org.globsframework.model.format.utils.DefaultDescriptionService;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.color.ColorService;

import javax.swing.*;
import java.util.Locale;

public class AddressBook {

  public static void main(String... args) throws Exception {

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

    GlobRepository repository = GlobRepositoryBuilder.createEmpty();

    Directory directory = initDirectory();

    GlobsPanelBuilder builder = new GlobsPanelBuilder(AddressBook.class, "/addressbook.splits",repository, directory);
    builder.addTable("contact", Contact.TYPE, new GlobFieldComparator(Contact.FIRST_NAME))
      .addColumn(Contact.FIRST_NAME)
      .addColumn(Contact.LAST_NAME)
      .addColumn(Contact.PHONE)
      .addColumn(Contact.EMAIL);
    builder.addCreateAction("+", "newContact", Contact.TYPE);
    builder.addDeleteAction("-", "deleteContact", Contact.TYPE);
    builder.addEditor(Contact.FIRST_NAME);
    builder.addEditor(Contact.LAST_NAME);
    builder.addEditor(Contact.PHONE);
    builder.addEditor(Contact.EMAIL);

    JFrame frame = (JFrame)builder.load();
    frame.setVisible(true);
  }

  private static Directory initDirectory() {
    Directory directory = new DefaultDirectory();
    directory.add(new ColorService());
    directory.add(IconLocator.class, IconLocator.NULL);
    directory.add(new SelectionService());
    directory.add(DescriptionService.class,
                  new DefaultDescriptionService(Formats.DEFAULT, "lang", Locale.ENGLISH, AddressBook.class.getClassLoader()));
    return directory;
  }
}
