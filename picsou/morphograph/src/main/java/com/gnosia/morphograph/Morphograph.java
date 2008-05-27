package com.gnosia.morphograph;

import com.gnosia.morphograph.gui.MainView;
import com.gnosia.morphograph.model.ExerciseType;
import com.gnosia.morphograph.model.Model;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.GlobRepositoryBuilder;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.format.utils.DefaultDescriptionService;
import org.crossbowlabs.globs.utils.directory.DefaultDirectory;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.xml.XmlGlobParser;
import org.crossbowlabs.splits.IconLocator;
import org.crossbowlabs.splits.color.ColorService;
import org.crossbowlabs.splits.utils.JarIconLocator;

import java.io.InputStreamReader;
import java.io.Reader;

public class Morphograph {

  private static final String[] EXO_FILES = {
    "Morphographe_age.xml",
    "Morphographe_ette.xml",
    "Morphographe_eur.xml",
    "Morphographe_ier.xml",
    "Morphographe_ment.xml",
    "Morphographe_oir.xml",
    "Morphographe_synthese.xml",
    "Morphographe_synthese2.xml",
    "Morphographe_tion.xml",
  };

  public static void main(String[] args) throws Exception {
    Reader[] readers = new Reader[EXO_FILES.length];
    for (int i = 0; i < EXO_FILES.length; i++) {
      readers[i] =
        new InputStreamReader(Morphograph.class.getResourceAsStream("/files/" + EXO_FILES[i]), "UTF-8");

    }
    run(readers);
  }

  public static void run(Reader... readers) throws Exception {
    GlobRepository globRepository =
      GlobRepositoryBuilder.init()
        .add(ExerciseType.values())
        .get();

    for (Reader reader : readers) {
      XmlGlobParser.parse(Model.get(), globRepository, reader, "morphograph");
    }

    Directory directory = new DefaultDirectory();

    directory.add(new SelectionService());
    directory.add(DescriptionService.class, new DefaultDescriptionService());
    directory.add(new ColorService(Morphograph.class, "/morphcolors.properties"));
    directory.add(IconLocator.class, new JarIconLocator(Morphograph.class, "/images"));

    MainView view = new MainView(globRepository, directory);
    view.show();
  }
}
