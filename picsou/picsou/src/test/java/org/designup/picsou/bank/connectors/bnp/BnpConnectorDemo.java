package org.designup.picsou.bank.connectors.bnp;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import java.awt.*;

public class BnpConnectorDemo {

  private void initAndShowGUI() {
    JFrame frame = new JFrame("JavaFX");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    final JFXPanel fxPanel = new JFXPanel();
    frame.add(fxPanel);
    fxPanel.setVisible(true);
    fxPanel.setSize(new Dimension(800, 900));
    frame.getContentPane().setPreferredSize(new Dimension(800, 900));
    frame.pack();
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        init(fxPanel);
      }
    });

    frame.setVisible(true);
  }

  private void init(final JFXPanel fxPanel) {
    Group group = new Group();
    Scene scene = new Scene(group);
    fxPanel.setScene(scene);


    WebView webView = new WebView();
    final WebEngine engine = webView.getEngine();

    Button button = new Button("ok");
    button.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        System.out.println("Compo.click");
        engine.executeScript("var keyboard = $('#client-nbr');" +
                             "keyboard.val('987654');" +
                             "keyboard.keyup();");

        engine.executeScript("var secret = $('#secret-nbr-keyboard');" +
                             "secret.find('a[data-value=3]')" +
                             ".click().click().click()" +
                             ".click().click().click();");
        engine.executeScript("$('#submitIdent').click();");

      }
    });


    VBox box = new VBox(button, webView);

    group.getChildren().add(box);

    engine.getLoadWorker().stateProperty().addListener(
      new ChangeListener<Worker.State>() {
        public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
          if (newState == Worker.State.SUCCEEDED) {
            Document document = engine.getDocument();
            if (document == null) {
              return;
            }
            // ordre reverse
            if (document.getElementById("mainContent") != null) {
              // Enregistrement de l'objet "app" visible du javascript pour appeler les méthodes de JavaBridge
              JSObject window = (JSObject)engine.executeScript("window");

              Action action = new ClickAction("nav3-virement-services");
              action.setNext(new ProtectedAction("n3-factures") {
                public void run(WebEngine engine) {
                  engine.executeScript("$('#" + id + "').find('a').click();");
                }
              })
                .setNext(new ProtectedAction("main-iframe") {
                  public void run(WebEngine engine) {
                    engine.executeScript("$('#main-iframe').contents().find('#input-pays-residence1 option:contains(ofx)').prop('selected', true);");
                  }
                }).setNext(new IdAction("main-iframe") {
                public Action action(Document document, WebEngine engine) {
                  engine.executeScript("$('#main-iframe').contents().find('a:contains(Suivant)').click();");
                  return next;
                }
              })
              .setNext(new LastAction(){
                public Action action(Document document, WebEngine engine) {
                  System.out.println("Done ident");
                  engine.executeScript("clearInterval(intervalTimer);");
                  return null;
                }
              });
              window.setMember("app", new JavaBridge(document, engine, action));
              engine.executeScript("intervalTimer = setInterval(" +
                                   "function() {\n" +
                                   "  app.nextState();\n" +
                                   "}, 1000);");

            } else if (document.getElementById("nav-connect") != null) {

              JSObject window = (JSObject)engine.executeScript("window");
              Action action = new ProtectedAction("client-nbr") {
                public void run(WebEngine engine) {
                  engine.executeScript("var keyboard = $('#client-nbr');" +
                                       "keyboard.val('987654');" +
                                       "keyboard.keyup();");
                }
              };
              action.setNext(new ProtectedAction("secret-nbr-keyboard") {
                public void run(WebEngine engine) {
                  engine.executeScript("var secret = $('#secret-nbr-keyboard');" +
                                       "secret.find('a[data-value=3]')" +
                                       ".click().click().click()" +
                                       ".click().click().click();");
                }
              }).setNext(new ProtectedAction("submitIdent") {
                public void run(WebEngine engine) {
                  engine.executeScript("$('#submitIdent').click();");
                }
              })
              .setNext(new LastAction(){
                public Action action(Document document, WebEngine engine) {
                  System.out.println("Done content.");
                  engine.executeScript("clearInterval(intervalTimer);");
                  return null;
                }
              });

              window.setMember("app", new JavaBridge(document, engine, action));

              engine.executeScript("intervalTimer = setInterval(" +
                                   "function() {\n" +
                                   "  app.nextState();\n" +
                                   "}, 1000);");

            }

//            document.

            // Notification de fin de téléchargement initial
//            engine.executeScript("$(document).ready(function() {\n" +
//                                 "  app.loaded();\n" +
//                                 "});");
//            engine.executeScript("$(document).on(\"DOMNodeInserted\", " +
//                                 "function() {\n" +
//                                 "  app.nextState();\n" +
//                                 "});");


          }
        }
      });
//    engine.load("https://mabanque.bnpparibas/fr/connexion");
    engine.load("https://mabanque.bnpparibas/sitedemo/ident.html");
  }

  public static class JavaBridge {
    private Document document;
    private WebEngine engine;
    Action action;

    public JavaBridge(Document document, WebEngine engine, Action action) {
      System.out.println("JavaBridge.JavaBridge");
      this.document = document;
      this.engine = engine;
      this.action = action;
    }

    public void nextState() {
      if (action == null) {
        return;
      }
      if (action.preCheck(document)) {
        System.out.println("JavaBridge.nextState ok for " + action);
        Action action1 = action;
        action = null;
        action = action1.action(document, engine);
      }
    }

    public void loaded() {
      System.out.println("JavaBridge.loaded");

      // Les $() s'appuient sur JQuery, une lib qui permet d'interagir avec le DOM de manière plus sympa.
      engine.executeScript("$('button.setcookie-nav').click();");

      // L'appel à keyup() est utilisé pour déclencher les triggers du code javascript du site BNP.
      // Bizarrement cette ligne marche pour le site réel dans Chrome mais pas ici, je n'ai pas pu comprendre pourquoi.
//      engine.executeScript("var keyboard = $('#client-nbr');" +
//                           "keyboard.val('987654');" +
//                           "keyboard.keyup();");
//
//      engine.executeScript("$('#secret-nbr').change(\n" +
//                           "    function(){\n" +
//                           "        if ($(this).is(':enabled')) {\n" +
//                           "            app.print('enabled');\n" +
//                           "        }\n" +
//                           "    });" +
//                           "app.print($('.container-ident').html());");

//      engine.executeScript("var keyboard = $('#secret-nbr-keyboard');" +
//                           "app.print(keyboard);" +
//                           "var background = keyboard.css('background-image');" +
//                           "var url = background.replace(/^url\\([\"']?/, '');" +
//                           "var url = background.replace(/^url\\([\"']?/, '').replace(/[\"']?\\)$/, '');" +
//                           "app.loadKeyboard(url);"
//      );
    }

    public void loadKeyboard(String url) {
      System.out.println("JavaBridge.loadKeyboard: " + url);
    }

    public void print(Object o) {
      System.out.println("JavaBridge.print: " + o);
    }
  }

  public static void main(final String[] args) {
    BnpConnectorDemo demo = new BnpConnectorDemo();
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        demo.initAndShowGUI();
      }
    });
  }


  interface Action {
    boolean preCheck(Document document);

    Action action(Document document, WebEngine engine);

    Action setNext(Action action);
  }

  static abstract class IdAction implements Action {
    String id;
    protected Element elementById;
    protected Action next;

    public IdAction(String id) {
      this.id = id;
    }

    public boolean preCheck(Document document) {
      elementById = document.getElementById(id);
      return elementById != null;
    }

    public Action setNext(Action action) {
      if (next != null) {
        throw new RuntimeException("Already set");
      }
      this.next = action;
      return action;
    }

    public String toString() {
      return "Action : " + id;
    }
  }

  static class ClickAction extends IdAction {

    public ClickAction(String id) {
      super(id);
    }

    public Action action(Document document, WebEngine engine) {
      try {
        engine.executeScript("$('#" + id + "').click();");
      }
      catch (Throwable e) {
//        e.printStackTrace();
        System.out.println("fail " + id + " " + e.getMessage());
        return this;
      }
      System.out.println("ClickAction.action done");
      return next;
    }
  }

  static abstract class ProtectedAction extends IdAction {

    public ProtectedAction(String id) {
      super(id);
    }

    public Action action(Document document, WebEngine engine) {
      try {
        run(engine);
      }
      catch (Throwable e) {
//        e.printStackTrace();
        System.out.println("fail " + id + " " + e.getMessage());
        return this;
      }
      System.out.println("action done " + id);
      return next;
    }

    public abstract void run(WebEngine engine);
  }

  public static class SelectAction extends ProtectedAction {
    private String value;

    public SelectAction(String id, String value) {
      super(id);
      this.value = value;
    }

    public void run(WebEngine engine) {
        engine.executeScript("$('#" + id + "' :contains('" + value + "').prop('selected', true);");
    }
  }

  private static class LastAction implements Action {
    public boolean preCheck(Document document) {
      return true;
    }

    public Action action(Document document, WebEngine engine) {
      return null;
    }

    public Action setNext(Action action) {
      throw new RuntimeException("Last action.");
    }

    public String toString() {
      return "last action";
    }
  }
}
