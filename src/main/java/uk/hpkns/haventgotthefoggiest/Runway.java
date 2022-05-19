package uk.hpkns.haventgotthefoggiest;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.Objects;

public class Runway implements Renderable {

    private static final Image[] runways = {
            new Image(Objects.requireNonNull(Main.class.getResource("/runway-vertical.png")).toExternalForm()),
            new Image(Objects.requireNonNull(Main.class.getResource("/runway-trbl.png")).toExternalForm()),
            new Image(Objects.requireNonNull(Main.class.getResource("/runway-horizontal.png")).toExternalForm()),
            new Image(Objects.requireNonNull(Main.class.getResource("/runway-tlbr.png")).toExternalForm()),
    };

    public double x;
    public double y;
    private final int direction;
    private final Player ply;

    public Runway(int direction, Player ply) {
        this.direction = direction;
        this.ply = ply;
    }

    @Override
    public void render(GraphicsContext gc, double width, double height) {
        gc.drawImage(runways[Math.abs(direction) % 4], ply.x - x - 64, ply.y - y - 64);
    }
}
