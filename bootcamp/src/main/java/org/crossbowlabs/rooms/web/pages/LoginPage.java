package org.crossbowlabs.rooms.web.pages;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.security.WaspSession;
import org.apache.wicket.security.login.http.HttpAuthenticationLoginPage;
import org.apache.wicket.security.strategies.LoginException;
import org.crossbowlabs.rooms.web.login.RoomContext;

import java.util.ResourceBundle;

public class LoginPage extends HttpAuthenticationLoginPage {

  public LoginPage() {
    ResourceBundle messages = ResourceBundle.getBundle("translation/rooms");
    final TextField field = new TextField("loginUserName", new Model(""));
    final PasswordTextField password = new PasswordTextField("loginPassword", new Model(""));
    WebMarkupContainer loginForm = new Form("loginForm") {
      protected void onSubmit() {
        try {
          ((WaspSession) getSession()).login(new RoomContext(field.getModelObjectAsString(),
                                                             password.getModelObjectAsString()));
          if (!getPage().continueToOriginalDestination())
          {
            setResponsePage(Application.get().getHomePage());
          }
        }
        catch (LoginException e) {
          e.printStackTrace();
        }
      }
    };
    add(loginForm);
    loginForm.add(field);
    loginForm.add(new Label("trUserName", messages.getString("login")));
    loginForm.add(password);
    loginForm.add(new Label("trPassword", messages.getString("password")));
    loginForm.add(new SubmitLink("submitPassword"));
  }

  public String getRealm(WebRequest request, WebResponse response) {
    return "what-is-that";
  }

  protected Object getBasicLoginContext(String username, String password) {
    return new RoomContext(username, password);
  }
}
