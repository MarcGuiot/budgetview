package org.designup.picsou.server.session.impl;

import org.designup.picsou.client.exceptions.UnknownId;
import org.designup.picsou.server.session.ConnectingState;
import org.designup.picsou.server.session.Persistence;
import org.designup.picsou.server.session.SessionService;
import org.designup.picsou.server.session.SessionState;
import org.designup.picsou.server.model.User;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.SerializedOutput;
import org.globsframework.model.GlobList;
import org.globsframework.model.Glob;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultSessionService implements SessionService {
  private Directory directory;
  private ConcurrentMap<Long, SessionState> sessions = new ConcurrentHashMap<Long, SessionState>();
  private Random random = new Random();

  public DefaultSessionService(Directory directory) {
    this.directory = directory;
  }

  public ConnectingState createSessionState() {
    Long sessionId = getNewSessionId();
    byte[] privateId = generatePrivateId();
    return new DefaultConnectingState(directory.get(Persistence.class), this, privateId, sessionId);
  }

  private byte[] generatePrivateId() {
    byte[] bytes = new byte[30];
    random.nextBytes(bytes);
    return bytes;
  }

  public SessionState getSessionState(Long sessionId) {
    SessionState sessionState = sessions.get(sessionId);
    if (sessionState == null) {
      throw new UnknownId();
    }
    return sessionState;
  }

  private Long getNewSessionId() {
    Long sessionId = random.nextLong();
    while (sessions.containsKey(sessionId)) {
      sessionId = random.nextLong();
    }
    return sessionId;
  }

  public void register(Long sessionId, SessionState sessionState) {
    sessions.put(sessionId, sessionState);
  }

  public void remove(Long sessionId) {
    sessions.remove(sessionId);
  }

  public GlobModel getModel() {
    return directory.get(GlobModel.class);
  }

  public void flushStateBefore(long date) {
    for (Iterator<SessionState> iterator = sessions.values().iterator(); iterator.hasNext();) {
      SessionState sessionState = iterator.next();
      if (sessionState.getLastAccess() < date) {
        iterator.remove();
      }
    }
  }

  public void getLocalUsers(SerializedOutput output) {
    Persistence persistence = directory.get(Persistence.class);
    GlobList users = persistence.getLocalUsers();
    output.write(users.size());
    for (Glob user : users) {
      output.writeUtf8String(user.get(User.NAME));
      output.writeBoolean(user.get(User.HAS_PASSWORD));
    }
  }
}
