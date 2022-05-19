package uk.hpkns.haventgotthefoggiest;

import javafx.scene.canvas.GraphicsContext;

public interface Renderable {
    void render(GraphicsContext gc, double width, double height);
}
