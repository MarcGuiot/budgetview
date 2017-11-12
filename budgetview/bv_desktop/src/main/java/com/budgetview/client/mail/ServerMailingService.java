package com.budgetview.client.mail;

import com.budgetview.client.ConnectionStatus;
import com.budgetview.shared.http.Http;
import com.budgetview.shared.license.LicenseConstants;
import com.budgetview.shared.mobile.MobileConstants;
import com.budgetview.utils.Lang;
import org.apache.http.HttpResponse;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;

import javax.swing.*;
import java.io.IOException;

public class ServerMailingService {
  synchronized public void sendMail(final String toMail, final String fromMail,
                                    final String title, final String content,
                                    final Listener listener, final GlobRepository repository) {
    Thread thread = new SendMailThread(fromMail, toMail, title, content, listener, repository);
    thread.setDaemon(true);
    thread.start();
  }

  public interface Listener {
    void sent(String mail, String title, String content);

    void sendFailed(String mail, String title, String content);
  }

  private class SendMailThread extends Thread {
    private final String fromMail;
    private final String toMail;
    private final String title;
    private final String content;
    private final Listener listener;
    private GlobRepository repository;

    private SendMailThread(String fromMail, String toMail, String title, String content, Listener listener, GlobRepository repository) {
      this.fromMail = fromMail;
      this.toMail = toMail;
      this.title = title;
      this.content = content;
      this.listener = listener;
      this.repository = repository;
    }

    public void run() {
      final String url = LicenseConstants.getServerUrl(LicenseConstants.SEND_MAIL_TO_US);
      Http.Post post = createPost(url);
      HttpResponse response;
      try {
        try {
          response = post.execute();
        }
        catch (IOException e) {
          Log.write("[ServerMailing] Connection error (retrying): ", e);
          post.dispose();
          post = createPost(url);
          response = post.execute();
        }
        ConnectionStatus.setOk(repository);
      }
      catch (final Exception e) {
        Log.write("[ServerMailing] Send mail raised exception", e);
        ConnectionStatus.checkException(repository, e);
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            Log.write("[ServerMailing] Mail not sent", e);
            listener.sendFailed(fromMail, title, content);
          }
        });
        return;
      }
      finally {
        post.dispose();
      }
      final int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == 200) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            Log.write("[ServerMailing] Mail sent");
            listener.sent(fromMail, title, content);
          }
        });
      }
      else {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            Log.write("[ServerMailing] Mail not sent - error code " + statusCode + " for " + url + " with content: " + content);
            listener.sendFailed(fromMail, title, content);
          }
        });
      }
    }

    private Http.Post createPost(String url) {
      return Http.post(url)
        .setHeader(MobileConstants.HEADER_LANG, Lang.get("lang"))
        .setHeader(LicenseConstants.HEADER_MAIL_FROM, fromMail)
        .setHeader(LicenseConstants.HEADER_MAIL_TO, toMail)
        .setHeader(LicenseConstants.HEADER_MAIL_TITLE, title)
        .setUtf8Content(content);
    }
  }

}
