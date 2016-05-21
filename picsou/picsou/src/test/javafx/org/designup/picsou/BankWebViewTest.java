package com.budgetview;


import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.html.HTMLInputElement;

public class BankWebViewTest extends Application {

  public BankWebViewTest() {
  }

  public void start(Stage stage) throws Exception {
    stage.setTitle("Web View");
    Scene scene = new Scene(new Compo(), 750, 500, Color.web("#666970"));
    stage.setScene(scene);
    // apply CSS style
//    scene.getStylesheets().add("webviewsample/BrowserToolbar.css");
    // show stage
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }

  static class Compo extends VBox {
    private final Browser browser;
    private final Button button;

    public Compo() {
      browser = new Browser();
      button = new Button("ok");
      button.setOnAction(new EventHandler<ActionEvent>() {
        public void handle(ActionEvent event) {
          System.out.println("Compo.click");
          Document document = browser.webEngine.getDocument();
          Element clientNbr = document.getElementById("client-nbr");
          if (clientNbr != null) {
            ((HTMLInputElement)clientNbr).setValue("1234");
          }
          Element id = document.getElementById("secret-nbr-keyboard");

          System.out.println("Browser.changed " + id);

        }
      });

      getChildren().addAll(button, browser);
    }
  }

  static class Browser extends Region {
    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();

    public Browser() {
      //apply the styles
      getStyleClass().add("browser");
      // load the home page
      webEngine.load("https://mabanque.bnpparibas/fr/connexion");
      webEngine.setOnError(new EventHandler<WebErrorEvent>() {
        public void handle(WebErrorEvent event) {
          System.out.println("Browser.handle " + event.getMessage() + event.getException().toString());
        }
      });
      webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
        public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
          if (newValue == Worker.State.SUCCEEDED) {
            Document document = webEngine.getDocument();
            Element clientNbr = document.getElementById("client-nbr");
            if (clientNbr != null){
              clientNbr.setTextContent("1234");
            }
          }
        }
      });
      //add components
      getChildren().add(browser);
    }

    protected double computePrefWidth(double height) {
      return 750;
    }

    protected double computePrefHeight(double width) {
      return 600;
    }
  }
}
