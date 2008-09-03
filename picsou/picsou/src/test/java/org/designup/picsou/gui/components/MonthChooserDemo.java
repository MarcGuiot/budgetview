package org.designup.picsou.gui.components;

import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.PicsouInit;
import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.model.Month;

import javax.swing.*;

public class MonthChooserDemo {
  public static void main(String[] args) throws Exception {

    Directory directory = PicsouApplication.createDirectory();
    GlobRepository repository = PicsouInit.init(ServerAccess.NULL, "user", true, directory).getRepository();
    for (int monthId = 200701; monthId < 200809; monthId = Month.next(monthId)) {
      repository.findOrCreate(Key.create(Month.TYPE, monthId));
    }

    MonthChooser chooser = new MonthChooser(directory);
    chooser.show(new JFrame(), 2008, 6);
  }
}
