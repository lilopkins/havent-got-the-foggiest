package uk.hpkns.haventgotthefoggiest;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class Main extends Application {

    public static final Random RANDOM = new Random();
    public static boolean DEBUG = false;
    public static final double SCALE_FACTOR = 3d;
    public static final long UPDATE_INTERVAL = 1000000000 / 60;

    private double fps;
    private final ArrayList<String> input = new ArrayList<>();
    private Runway runway;
    private AnimationTimer timer;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, 1280, 720, Color.BLACK);

        Canvas canvas = new Canvas();
        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty());

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.scale(SCALE_FACTOR, SCALE_FACTOR);

        final long[] lastTick = {System.nanoTime()};
        final long[] timeSinceLastUpdate = {0};

        runway = new Runway(RANDOM.nextInt(), ply);
        runway.x = RANDOM.nextDouble() * 2000 - 1000;
        runway.y = RANDOM.nextDouble() * 2000 - 1000;

        timer = new AnimationTimer() {
            @Override
            public void handle(long time) {
                long deltaNanos = time - lastTick[0];
                lastTick[0] = time;
                double delta = ((double) deltaNanos) / 1000000000.0f;
                timeSinceLastUpdate[0] += deltaNanos;

                gc.clearRect(0d, 0d, canvas.getWidth(), canvas.getHeight());

                gc.setFill(Color.WHITE);
                if (DEBUG) {
                    fps = 1f / delta;
                }
                while (timeSinceLastUpdate[0] >= UPDATE_INTERVAL) {
                    update(canvas.getWidth() / SCALE_FACTOR, canvas.getHeight() / SCALE_FACTOR);
                    timeSinceLastUpdate[0] -= UPDATE_INTERVAL;
                }
                render(gc, canvas.getWidth() / SCALE_FACTOR, canvas.getHeight() / SCALE_FACTOR);
            }
        };
        timer.start();

        scene.setOnKeyPressed(
                e -> {
                    String code = e.getCode().toString();

                    if (Objects.equals(code, "F3"))
                        DEBUG = !DEBUG;
                    if (!input.contains(code))
                        input.add(code);
                });

        scene.setOnKeyReleased(
                e -> {
                    String code = e.getCode().toString();
                    input.remove( code );
                });

        root.getChildren().add(canvas);
        stage.setFullScreen(true);
        stage.setScene(scene);
        stage.show();
    }

    private final Player ply = new Player();
    private double fadeEntire = 1.0;
    private double fogAmount = 0.8;
    private double fogVelocity = 0;
    private double dmeNm;
    private double dmeKt;
    private double dmeMin;

    private void update(double width, double height) {
        ply.update(input);

        double fogAcceleration = RANDOM.nextDouble() - 0.5d;
        fogVelocity += fogAcceleration;
        fogVelocity = Math.max(-1, Math.min(fogVelocity, 1d));
        fogAmount += fogVelocity * 0.001d;
        fogAmount = Math.max(0.8d, Math.min(fogAmount, 0.98d));

        ply.fuel = Math.max(ply.fuel - 0.025, 0.0d);
        if (ply.fuel <= 0.0d) {
            fadeEntire = Math.min(fadeEntire + 0.002, 1.0d);
            if (fadeEntire >= 1.0) {
                timer.stop();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("You lost!");
                    alert.setHeaderText("You lost!");
                    alert.setContentText("You ran out of fuel and your plane crash landed!");
                    alert.showAndWait();
                    Platform.exit();
                });
            }
        } else if (fadeEntire > 0.0d) {
            fadeEntire = Math.max(0.0d, fadeEntire - 0.002);
        }

        // Calculate DME
        dmeNm = Math.sqrt(Math.pow(runway.x - ply.x + width / 2, 2) + Math.pow(runway.y - ply.y + height / 2, 2)) / 500;
        if (dmeNm < 0.5) {
            Platform.runLater(() -> {
                timer.stop();
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("You won!");
                alert.setHeaderText("You won!");
                alert.showAndWait();
                Platform.exit();
            });
        }

        double plyToRwyAng = Math.PI / 2 - Math.atan((runway.y - ply.y) / (runway.x - ply.x));
        dmeKt = Player.SPEED * Math.sin((plyToRwyAng - (ply.heading * Math.PI / 180) - Math.PI / 2));

        dmeMin = dmeNm / Player.SPEED;
    }

    private final Image dme = new Image(Objects.requireNonNull(Main.class.getResource("/dme.png")).toExternalForm());
    private final Image ground = new Image(Objects.requireNonNull(Main.class.getResource("/ground.png")).toExternalForm());
    private final Font seg7 = Font.loadFont(Objects.requireNonNull(Main.class.getResourceAsStream("/Segment7Standard.otf")), 18);

    private void render(GraphicsContext gc, double width, double height) {
        gc.setImageSmoothing(false);
        // Render world background
        double offsetX = ply.x % 128;
        double offsetY = ply.y % 128;
        for (double x = offsetX - 128; x < width; x += 127) {
            for (double y = offsetY - 128; y < height; y += 127) {
                gc.drawImage(ground, x, y);
            }
        }
        runway.render(gc, width, height);

        // Render fog
        gc.save();
        gc.setFill(Color.gray(0.5, fogAmount));
        gc.fillRect(0, 0, width, height);
        gc.restore();

        // Render plane
        ply.render(gc, width, height);

        // Render UI
        double dmeLeft = width - 128 - 16;
        double dmeTop = height - 64 - 16;
        gc.drawImage(dme, dmeLeft, dmeTop);
        gc.setFont(seg7);
        gc.setFill(Color.rgb(254, 103, 0));
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(String.format("%.1f", Math.min(dmeNm, 99.9)), dmeLeft + 47, dmeTop + 11 + gc.getFont().getSize());
        gc.fillText(String.format("%.0f", Math.min(dmeKt, 99)), dmeLeft + 82, dmeTop + 11 + gc.getFont().getSize());
        gc.fillText(String.format("%.0f", Math.min(dmeMin, 99)), dmeLeft + 111, dmeTop + 11 + gc.getFont().getSize());
        gc.fillText(String.format("FUEL: %.1f", ply.fuel), width - 16, dmeTop - 4);

        gc.save();
        gc.setFill(Color.gray(0.0d, fadeEntire));
        gc.fillRect(0, 0, width, height);
        gc.restore();

        if (DEBUG) {
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText(String.format("FPS: %.1f", fps), 16d, 20d);
            gc.fillText(String.format("PLY: (%04.02f, %04.02f)", ply.x, ply.y), 16d, 40d);
            gc.fillText(String.format("RWY: (%04.02f, %04.02f)", runway.x, runway.y), 16d, 60d);
            gc.fillText(String.format("DME: %.02f", dmeNm), 16d, 80d);
            gc.fillText(String.format("FOG: %.02f", fogAmount), 16d, 100d);
        }
    }
}
