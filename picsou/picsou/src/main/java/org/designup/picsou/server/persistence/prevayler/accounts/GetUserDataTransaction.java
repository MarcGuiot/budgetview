package org.designup.picsou.server.persistence.prevayler.accounts;

import org.prevayler.Query;

import java.util.Date;

class GetUserDataTransaction implements Query {

  public GetUserDataTransaction() {
  }

  public Object query(Object prevalentSystem, Date executionTime) throws Exception {
    return ((UserData) prevalentSystem).getUserData();
  }
}
