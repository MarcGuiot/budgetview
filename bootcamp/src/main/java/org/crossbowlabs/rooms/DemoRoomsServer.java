package org.globsframework.rooms;

import org.apache.log4j.xml.DOMConfigurator;
import org.apache.wicket.protocol.http.WicketServlet;
import org.globsframework.rooms.model.PersistenceManager;
import org.globsframework.rooms.model.Room;
import org.globsframework.rooms.model.UseDateByRoom;
import org.globsframework.rooms.model.User;
import org.globsframework.rooms.web.RoomsApplication;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import java.util.Date;
import java.util.Calendar;

public class DemoRoomsServer {
  private static final int PORT = 8081;

  public static void main(String[] args) throws Exception {
//    Locale.setDefault(Locale.ENGLISH);
    DOMConfigurator.configure(DemoRoomsServer.class.getClassLoader().getResource("log4j.xml"));
    initRooms();
    org.mortbay.jetty.Server jetty = new org.mortbay.jetty.Server(PORT);
    Context context = new Context(jetty, "/", Context.SESSIONS);
    context.setResourceBase("target/classes");
    ServletHolder holder = new ServletHolder(new WicketServlet());
    holder.setInitParameter("applicationClassName", RoomsApplication.class.getName());
    context.addServlet(holder, "/*");

    jetty.start();
  }

  private static void initRooms() {
    Session session = PersistenceManager.getInstance().getNewSession();
    Transaction transaction = session.getTransaction();
    transaction.begin();
    User admin = new User("admin", "admin", "admin", "admin@admin");
    session.save(admin);
    Room verdi = new Room("verdi", 100);
    session.save(verdi);
    Room mozart = new Room("mozart", 20);
    session.save(mozart);
    mozart.setCanHaveBarco(false);
    UseDateByRoom from_15_oclock = new UseDateByRoom(admin, verdi, getToDay());
    from_15_oclock.setFromInMinutes(15 * 60);
    session.save(from_15_oclock);
    UseDateByRoom until_10_oclock = new UseDateByRoom(admin, mozart, getToDay());
    until_10_oclock.setToInMinutes(10 * 60);
    session.save(until_10_oclock);
    UseDateByRoom between_10_to_12_oclock = new UseDateByRoom(admin, verdi, getToDay());
    between_10_to_12_oclock.setFromInMinutes(10 * 60);
    between_10_to_12_oclock.setToInMinutes(12 * 60);
    session.save(between_10_to_12_oclock);
    transaction.commit();
    session.close();
  }

  private static Date getToDay() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.add(Calendar.DAY_OF_WEEK, 1);
    return calendar.getTime();
  }
}
