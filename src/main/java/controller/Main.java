package controller;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Cria um rótulo de texto para aparecer na tela
        Label label = new Label("Java Café POS - Pronto para começar!");

        // Cria um painel simples e adiciona o texto nele
        StackPane root = new StackPane(label);

        // Define o tamanho da janela (largura x altura)
        Scene scene = new Scene(root, 400, 300);

        // Configura e exibe a janela principal
        primaryStage.setTitle("Java Café");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Inicializa o ciclo de vida do JavaFX
        launch(args);
    }
}