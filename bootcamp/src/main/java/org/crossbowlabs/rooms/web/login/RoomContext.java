package org.globsframework.rooms.web.login;

import org.apache.wicket.security.hive.authentication.LoginContext;
import org.apache.wicket.security.hive.authentication.Subject;
import org.apache.wicket.security.hive.authentication.DefaultSubject;
import org.apache.wicket.security.hive.authorization.SimplePrincipal;
import org.apache.wicket.security.strategies.LoginException;
import org.globsframework.rooms.model.PersistenceManager;
import org.globsframework.rooms.model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.io.Serializable;

public class RoomContext extends LoginContext implements Serializable {
  private String userName;
  private String password;
  private User user;

  public RoomContext(String userName, String password) {
    this.userName = userName;
    this.password = password;
  }

  public RoomContext() {
  }

  public Subject login() throws LoginException {
    DefaultSubject defaultSubject = new DefaultSubject();
    defaultSubject.addPrincipal(new SimplePrincipal("basic"));
    if (userName.equalsIgnoreCase("admin")){
      defaultSubject.addPrincipal(new SimplePrincipal("admin"));
    }
    Session session = PersistenceManager.getInstance().getNewSession();
    Transaction transaction = session.beginTransaction();
    try {
      transaction.begin();
      Criteria criteria = session.createCriteria(User.class);
      criteria.add(Restrictions.eq("login", userName));
      user = (User) criteria.uniqueResult();
      if (user == null){
        throw new LoginException("unknown user");
      }
    } finally {
      transaction.commit();
      session.close();
    }
    return defaultSubject;
  }

  public User getUser() {
    return user;
  }
}
