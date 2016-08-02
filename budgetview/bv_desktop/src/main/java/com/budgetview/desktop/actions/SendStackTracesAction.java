package com.budgetview.desktop.actions;

import com.budgetview.client.mail.MailService;
import com.budgetview.model.User;
import com.budgetview.shared.license.LicenseConstants;
import com.budgetview.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Dates;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.Map;

public class SendStackTracesAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;
  private Thread thread;
  private boolean stop;
  private StringBuilder buffer = new StringBuilder();

  public SendStackTracesAction(GlobRepository repository, Directory directory) {
    super(Lang.get("dumpThread"));
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {
    if (thread == null) {
      stop = false;
      dump();
      this.putValue(Action.NAME, Lang.get("sendThread"));
    }
    else {
      this.putValue(Action.NAME, Lang.get("dumpThread"));
      stop();
    }
  }

  private void stop() {
    try {
      synchronized (this) {
        stop = true;
        notifyAll();
      }
      while (thread.isAlive()) {
        thread.interrupt();
        thread.join(200);
      }
      thread = null;

      Glob glob = repository.find(User.KEY);
      String mail = "";
      if (glob != null) {
        mail = glob.get(User.EMAIL);
      }
      String content = buffer.toString();
      Log.write(content);
      directory.get(MailService.class).sendMail(LicenseConstants.SUPPORT_EMAIL,
                                                mail,
                                                "Thread dump",
                                                content,
                                                new MailService.Listener() {
                                                    public void sent(String mail, String title, String content) {
                                                      Log.write("Mail sent from " + mail + " - title : " + title + "\n" + content);
                                                    }

                                                    public void sendFailed(String mail, String title, String content) {
                                                      Log.write("Failed to send mail from " + mail + " - title : " + title + "\n" + content);
                                                    }
                                                  },
                                                repository);
    }
    catch (InterruptedException e) {
      Log.write("interrupted thread in send");
    }
  }

  private void dump() {
    thread = new Thread() {
      public void run() {
        try {
          synchronized (this) {
            wait(1000);
            while (!stop) {
              buffer.append("-------------------").append(Dates.toTimestampString(new Date())).append('\n');
              Runtime runtime = Runtime.getRuntime();
              buffer
                .append("free mem: ").append(runtime.freeMemory())
                .append(" max mem: ").append(runtime.maxMemory())
                .append(" total mem: ").append(runtime.totalMemory())
                .append('\n');
              Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
              for (Map.Entry<Thread, StackTraceElement[]> entry : map.entrySet()) {
                if (thread != entry.getKey()) {
                  buffer.append("Thread: \n")
                    .append(entry.getKey().getName())
                    .append("\n");
                  for (StackTraceElement element : entry.getValue()) {
                    buffer
                      .append("at ")
                      .append(element.getClassName())
                      .append(".")
                      .append(element.getMethodName())
                      .append("(")
                      .append(element.getFileName())
                      .append(":")
                      .append(element.getLineNumber())
                      .append(")")
                      .append("\n");
                  }
                }
              }
              wait(4000);
            }
          }
        }
        catch (InterruptedException e) {
        }
      }
    };
    thread.setDaemon(true);
    thread.start();

  }
}
