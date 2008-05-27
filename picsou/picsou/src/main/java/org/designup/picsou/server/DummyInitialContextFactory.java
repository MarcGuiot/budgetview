package org.designup.picsou.server;

import org.crossbowlabs.globs.utils.exceptions.UnexpectedApplicationState;

import javax.naming.*;
import javax.naming.spi.InitialContextFactory;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class DummyInitialContextFactory implements InitialContextFactory {
  private static final DummyContext DUMMY_CONTEXT = new DummyContext();

  public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
    return DUMMY_CONTEXT;
  }

  private static class DummyContext implements Context {
    private Object value;
    private Map<String, DummyContext> childs = new HashMap<String, DummyContext>();

    public Object getValue() {
      return value;
    }

    public Object lookup(Name name) throws NamingException {
      DummyContext dummyContext = getOrCreateContext(name);
      return dummyContext.getValue();
    }

    private DummyContext getOrCreateContext(Name name) {
      if (name.isEmpty()) {
        throw new UnexpectedApplicationState("Name should have at least one level");
      }
      DummyContext child = null;
      for (int i = 0; i < name.size(); i++) {
        String key = name.get(i);
        child = getOrCreateContext(key);
      }
      return child;
    }

    private DummyContext getOrCreateContext(String key) {
      DummyContext child;
      child = childs.get(key);
      if (child == null) {
        child = new DummyContext();
        childs.put(key, child);
      }
      return child;
    }

    public Object lookup(String name) throws NamingException {
      return getOrCreateContext(name).getValue();
    }

    public void bind(Name name, Object obj) throws NamingException {
      getOrCreateContext(name).setValue(obj);
    }

    private void setValue(Object obj) {
      value = obj;
    }

    public void bind(String name, Object obj) throws NamingException {
      getOrCreateContext(name).setValue(obj);
    }

    public void rebind(Name name, Object obj) throws NamingException {
      bind(name, obj);
    }

    public void rebind(String name, Object obj) throws NamingException {
      bind(name, obj);
    }

    public void unbind(Name name) throws NamingException {
      getOrCreateContext(name).setValue(null);
    }

    public void unbind(String name) throws NamingException {
      getOrCreateContext(name).setValue(null);
    }

    public void rename(Name oldName, Name newName) throws NamingException {
    }

    public void rename(String oldName, String newName) throws NamingException {
    }

    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
      return null;
    }

    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
      return null;
    }

    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
      return null;
    }

    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
      return null;
    }

    public void destroySubcontext(Name name) throws NamingException {
    }

    public void destroySubcontext(String name) throws NamingException {
    }

    public Context createSubcontext(Name name) throws NamingException {
      DummyContext context = getOrCreateContext(name);
      context.value = context;
      return (Context) value;
    }

    public Context createSubcontext(String name) throws NamingException {
      DummyContext context = getOrCreateContext(name);
      context.value = context;
      return context;
    }

    public Object lookupLink(Name name) throws NamingException {
      return null;
    }

    public Object lookupLink(String name) throws NamingException {
      return null;
    }

    public NameParser getNameParser(Name name) throws NamingException {
      return null;
    }

    public NameParser getNameParser(String name) throws NamingException {
      return null;
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
      return null;
    }

    public String composeName(String name, String prefix) throws NamingException {
      return null;
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
      return null;
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
      return null;
    }

    public Hashtable<?, ?> getEnvironment() throws NamingException {
      return null;
    }

    public void close() throws NamingException {
    }

    public String getNameInNamespace() throws NamingException {
      return null;
    }
  }
}
