package org.uispec4j.utils;

import java.util.ArrayList;
import java.util.List;

public class ThreadManager {
  private static ThreadManager manager = new ThreadManager();
  private List<Thread> threads = new ArrayList<Thread>();
  private long id = Long.MIN_VALUE + 1;

  public static ThreadManager getInstance() {
    return manager;
  }

  class Thread extends java.lang.Thread {
    private Runnable runnable;
    private String name;
    private Long id = Long.MIN_VALUE + 1;

    Thread() {
      setPriority(MAX_PRIORITY);
    }

    public void run() {
      while (true) {
        try {
          synchronized (this) {
            if (runnable == null) {
              wait();
            }
          }
          if (runnable != null) {
            setName(name);
            runnable.run();
          }
        }
        catch (Exception e) {
        }
        finally {
          synchronized (id) {
            id.notify();
            id = Long.MIN_VALUE;
          }
          synchronized (this) {
            runnable = null;
            interrupted();   //clear interruptedStatus
          }
          synchronized (ThreadManager.this) {
            threads.add(this);
          }
        }
      }
    }

    public void push(Long id, String name, Runnable runnable) {
      synchronized (this) {
        this.id = id;
        this.runnable = runnable;
        this.name = name;
        notify();
      }
    }

    public void waitEnd(Long id) throws InterruptedException {
      if (id.equals(this.id)) {
        id.wait();
      }
    }

    public void waitEnd(Long id, int duration) throws InterruptedException {
      if (id.equals(this.id)) {
        id.wait(duration);
      }
    }

    public void interrupt(Long id) {
      if (id.equals(this.id)) {
        super.interrupt();
      }
    }
  }

  public interface ThreadDelegate {
    void interrupt();

    void join() throws InterruptedException;

    void join(int duration) throws InterruptedException;
  }

  public ThreadDelegate addRunnable(String name, Runnable runnable) {
    synchronized (this) {
      id++;
      if (threads.isEmpty()) {
        Thread thread = new Thread();
        thread.start();
        return new ThreadDelegateImpl(id, thread, name, runnable);
      }
      else {
        ThreadManager.Thread thread = threads.remove(threads.size() - 1);
        return new ThreadDelegateImpl(id, thread, name, runnable);
      }
    }
  }

  private static class ThreadDelegateImpl implements ThreadDelegate {
    private final Long id;
    private Thread thread;

    public ThreadDelegateImpl(Long id, Thread thread, String name, Runnable runnable) {
      this.id = id;
      this.thread = thread;
      thread.push(this.id, name, runnable);
    }

    public void interrupt() {
      synchronized (id) {
        thread.interrupt(id);
      }
    }

    public void join() throws InterruptedException {
      synchronized (id) {
        thread.waitEnd(id);
      }
    }

    public void join(int duration) throws InterruptedException {
      synchronized (id) {
        thread.waitEnd(id, duration);
      }
    }
  }
}
