package com.budgetview.client.mail;

import com.budgetview.client.ClientParams;
import com.budgetview.client.ConnectionStatus;
import com.budgetview.client.http.Http;
import com.budgetview.http.HttpBudgetViewConstants;
import com.budgetview.shared.utils.MobileConstants;
import com.budgetview.utils.Lang;
import org.apache.http.HttpResponse;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;

import javax.swing.*;
import java.io.IOException;

public class MailService {
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
      String url = ClientParams.getLicenseServerUrl(HttpBudgetViewConstants.REQUEST_SEND_MAIL);
      Http.Post post = createPost(url);
      HttpResponse response;
      try {
        try {
          response = post.execute();
        }
        catch (IOException e) {
          Log.write("connection error (retrying): ", e);
          post.dispose();
          post = createPost(url);
          response = post.execute();
        }
        ConnectionStatus.setOk(repository);
        Log.write("Send mail ok");
      }
      catch (final Exception e) {
        Log.write("in send mail", e);
        ConnectionStatus.checkException(repository, e);
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            Log.write("mail not sent", e);
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
            listener.sent(fromMail, title, content);
          }
        });
      }
      else {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            Log.write("Mail not sent with error code " + statusCode);
            listener.sendFailed(fromMail, title, content);
          }
        });
      }
    }

    private Http.Post createPost(String url) {
      return Http.post(url)
        .setHeader(MobileConstants.HEADER_LANG, Lang.get("lang"))
        .setHeader(HttpBudgetViewConstants.HEADER_MAIL, fromMail)
        .setHeader(HttpBudgetViewConstants.HEADER_TO_MAIL, toMail)
        .setHeader(HttpBudgetViewConstants.HEADER_MAIL_TITLE, title)
        .setUtf8(content);
    }
  }

}
