package uk.hpkns.haventgotthefoggiest;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Objects;

public class Player implements Updatable, Renderable {

    public static final double SPEED = 1.5d;
    private final Image PLANE = new Image(Objects.requireNonNull(Main.class.getResource("/plane.png")).toExternalForm());
    private final Image PLANE_LEFT = new Image(Objects.requireNonNull(Main.class.getResource("/plane-bank-left.png")).toExternalForm());
    private final Image PLANE_RIGHT = new Image(Objects.requireNonNull(Main.class.getResource("/plane-bank-right.png")).toExternalForm());

    public double x;
    public double y;
    public double heading;
    public int dHeading = 0;

    public Player() {
        heading = 90;
    }

    @Override
    public void update(ArrayList<String> input) {
        if (input.contains("A") || input.contains("LEFT"))
            dHeading = -1;
        else if (input.contains("D") || input.contains("RIGHT"))
            dHeading = 1;
        else
            dHeading = 0;

        heading += dHeading;
        heading = heading % 360;

        double headingRad = (heading - 90) * Math.PI / 180;
        x -= SPEED * Math.cos(headingRad);
        y -= SPEED * Math.sin(headingRad);
    }

    @Override
    public void render(GraphicsContext gc, double width, double height) {
        double planeX = width / 2;
        double planeY = height / 2;
        gc.save();
        gc.translate(planeX, planeY);
        gc.rotate(heading);
        gc.drawImage((dHeading < 0) ? PLANE_LEFT : ((dHeading > 0) ? PLANE_RIGHT : PLANE), -PLANE.getWidth() / 2, -PLANE.getHeight() / 2);
        gc.translate(-planeX, -planeY);
        gc.restore();
    }
}
