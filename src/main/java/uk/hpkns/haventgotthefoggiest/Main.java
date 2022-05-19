package uk.hpkns.haventgotthefoggiest;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    public static final boolean DEBUG_FPS = false;

    public static void main(String[] args) {
        launch();
    }

    private long lastTick;

    @Override
    public void start(Stage stage) throws Exception {
        Group root = new Group();
        Scene scene = new Scene(root, 600, 400, Color.BLACK);

        Canvas canvas = new Canvas();
        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty());

        GraphicsContext gc = canvas.getGraphicsContext2D();

        lastTick = System.nanoTime();
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long time) {
                long deltaNanos = time - lastTick;
                lastTick = time;
                double delta = ((double) deltaNanos) / 1000000000.0f;

                gc.clearRect(0d, 0d, canvas.getWidth(), canvas.getHeight());

                gc.setFill(Color.WHITE);
                if (DEBUG_FPS) {
                    double fps = 1f / delta;
                    gc.fillText(String.format("FPS: %.1f", fps), 10d, 10d);
                }
                gameLoop(gc, delta, canvas.getWidth(), canvas.getHeight());
            }
        };
        timer.start();

        root.getChildren().add(canvas);
        stage.setScene(scene);
        stage.show();
    }

    private void gameLoop(GraphicsContext gc, double delta, double width, double height) {
        // Render world background

        // Render fog

        // Render plane

        // Render UI

    }
}
