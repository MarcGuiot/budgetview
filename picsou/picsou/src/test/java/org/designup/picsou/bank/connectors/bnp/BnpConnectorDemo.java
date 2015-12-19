package org.designup.picsou.bank.connectors.bnp;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.w3c.dom.Document;

import javax.swing.*;
import java.awt.*;

public class BnpConnectorDemo {

  private void initAndShowGUI() {
    JFrame frame = new JFrame("JavaFX");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    final JFXPanel fxPanel = new JFXPanel();
    frame.add(fxPanel);
    fxPanel.setVisible(true);
    fxPanel.setSize(new Dimension(500, 700));
    frame.getContentPane().setPreferredSize(new Dimension(500, 700));
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

    group.getChildren().add(webView);

    final WebEngine engine = webView.getEngine();
    engine.getLoadWorker().stateProperty().addListener(
      new ChangeListener<Worker.State>() {
        public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
          if (newState == Worker.State.SUCCEEDED) {
            Document document = engine.getDocument();
            if (document == null) {
              return;
            }

            // Enregistrement de l'objet "app" visible du javascript pour appeler les méthodes de JavaBridge
            JSObject window = (JSObject) engine.executeScript("window");
            window.setMember("app", new JavaBridge(engine));

            // Notification de fin de téléchargement initial
            engine.executeScript("$(document).ready(function() {\n" +
                                 "  app.loaded();\n" +
                                 "});");

          }
        }
      });
    engine.load("https://mabanque.bnpparibas/fr/connexion");
//    engine.load("https://mabanque.bnpparibas/sitedemo/ident.html");
  }

  public static class JavaBridge {
    private WebEngine engine;

    public JavaBridge(WebEngine engine) {
      this.engine = engine;
    }

    public void loaded() {
      System.out.println("JavaBridge.loaded");

      // Les $() s'appuient sur JQuery, une lib qui permet d'interagir avec le DOM de manière plus sympa.
      engine.executeScript("$('button.setcookie-nav').click();");

      // L'appel à keyup() est utilisé pour déclencher les triggers du code javascript du site BNP.
      // Bizarrement cette ligne marche pour le site réel dans Chrome mais pas ici, je n'ai pas pu comprendre pourquoi.
      engine.executeScript("var keyboard = $('#client-nbr');keyboard.val('987654');keyboard.keyup();");

      engine.executeScript("$('#secret-nbr').change(\n" +
                           "    function(){\n" +
                           "        if ($(this).is(':enabled')) {\n" +
                           "            app.print('enabled');\n" +
                           "        }\n" +
                           "    });" +
                           "app.print($('.container-ident').html());");

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
}
