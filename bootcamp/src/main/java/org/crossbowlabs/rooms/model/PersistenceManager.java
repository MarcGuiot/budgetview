package org.crossbowlabs.rooms.model;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;

public class PersistenceManager {
  private static PersistenceManager persistenceManager;
  private SessionFactory hibernateSessionFactory;

  public PersistenceManager() {
  Configuration cfg = new AnnotationConfiguration()
    .addAnnotatedClass(User.class)
    .addAnnotatedClass(Room.class)
    .addAnnotatedClass(Barco.class)
    .addAnnotatedClass(UseDateByRoom.class)
    .addAnnotatedClass(UseDateForBarco.class)
    .setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver")
    .setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:rooms")
    .setProperty("hibernate.connection.username", "sa")
    .setProperty("hibernate.connection.password", "")
    .setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider")
    .setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect")
    .setProperty("hibernate.show_sql", "true")
    .setProperty("hibernate.hbm2ddl.auto", "create");
  hibernateSessionFactory = cfg.buildSessionFactory();
  }

  public static PersistenceManager getInstance(){
    if (persistenceManager == null){
      persistenceManager = new PersistenceManager();
    }
    return persistenceManager;
  }


  public Session getNewSession(){
    return hibernateSessionFactory.openSession();
  }

}
