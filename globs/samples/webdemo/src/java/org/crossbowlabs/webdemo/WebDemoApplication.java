package org.globsframework.webdemo;

import org.globsframework.globs.model.GlobList;
import org.globsframework.globs.model.format.DescriptionService;
import org.globsframework.globs.model.format.Formats;
import org.globsframework.globs.model.format.utils.DefaultDescriptionService;
import org.globsframework.globs.model.utils.GlobBuilder;
import org.globsframework.globs.sqlstreams.SqlConnection;
import org.globsframework.globs.sqlstreams.SqlService;
import org.globsframework.globs.sqlstreams.drivers.jdbc.JdbcSqlService;
import org.globsframework.globs.wicket.GlobApplication;
import org.globsframework.webdemo.model.Person;
import org.globsframework.webdemo.pages.HomePage;

import java.util.Locale;

public class WebDemoApplication extends GlobApplication {

  protected void init() {
    super.init();
    mountBookmarkablePage("/home", HomePage.class);
    getMarkupSettings().setStripWicketTags(true);
    getDebugSettings().setAjaxDebugModeEnabled(false);
    JdbcSqlService sqlService = new JdbcSqlService("jdbc:hsqldb:.", "sa", "");
    getDirectory().add(SqlService.class, sqlService);
    initDb(sqlService);
  }

  private void initDb(JdbcSqlService sqlService) {
    GlobList globs = new GlobList();
    globs.add(GlobBuilder.init(Person.TYPE)
            .set(Person.ID, 0)
            .set(Person.FIRST_NAME, "Bart")
            .set(Person.LAST_NAME, "Simpson")
            .set(Person.EMAIL, "bart@springfield.net")
            .set(Person.COMMENT, "As a 10-year old boy, Bart is a self-proclaimed underachiever who begins "
                                 + "each show in detention writing lines on the blackboard.\n"
                                 + " He is easily distracted, even, strangely enough by algebraic equations.\n"
                                 + "His penchant for shocking people (including Springfield's rich citizen"
                                 + " Charles Montgomery Burns) began before he was born: Bart \"mooned\" "
                                 + "Dr. Hibbert while he performed a sonogram on Marge, and moments after"
                                 + " being born he set fire to Homer's tie. [Source: Wikipedia]")
            .get());
    globs.add(GlobBuilder.init(Person.TYPE)
            .set(Person.ID, 1)
            .set(Person.FIRST_NAME, "Homer")
            .set(Person.LAST_NAME, "Simpson")
            .set(Person.EMAIL, "homer@simpson.com")
            .get());
    SqlConnection connection = sqlService.getDb();
    connection.createTable(Person.TYPE);
    connection.populate(globs);
    connection.commitAndClose();
  }

  public Class getHomePage() {
    return HomePage.class;
  }

  protected DescriptionService createDescriptionService() {
    return new DefaultDescriptionService(Formats.DEFAULT, "lang", Locale.ENGLISH, getClass().getClassLoader());
  }
}
