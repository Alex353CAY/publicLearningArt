package javafx.connection;

import javafx.neuron.Neuron;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class Connection {
    private Runnable onSelected = () -> {};
    private Runnable onUnselected = () -> {};
    private boolean selected = false;
    private final Line line = new Line();
    private Neuron neuron;

    public void setColor(Color color) {
        line.setStroke(color);
    }

    public void select() {
        line.setStrokeWidth(5);
    }

    public void unselect() {
        line.setStrokeWidth(1);
    }

    public Connection(Neuron target) {
        line.startXProperty().bind(target.mountingPoint().translateXProperty());
        line.startYProperty().bind(target.mountingPoint().translateYProperty());

        line.endXProperty().bind(target.mountingPoint().translateXProperty());
        line.endYProperty().bind(target.mountingPoint().translateYProperty());

        line.setOnMouseEntered(event -> {
            if (!selected) line.setStrokeWidth(2.5);
        });
        line.setOnMouseExited(event -> {
            if (!selected) line.setStrokeWidth(1);
        });

        line.setOnMouseClicked(event -> {
            if (!selected) {
                onSelected.run();
                selected = true;
            } else {
                onUnselected.run();
                selected = false;
            }
        });
    }

    public Neuron getNeuron() {
        return neuron;
    }

    public void setOnSelected(Runnable runnable) {
        onSelected = runnable;
    }

    public void setOnUnselected(Runnable runnable) {
        onUnselected = runnable;
    }

    public void setSource(Neuron neuron) {
        this.neuron = neuron;
        line.endXProperty().bind(neuron.mountingPoint().translateXProperty());
        line.endYProperty().bind(neuron.mountingPoint().translateYProperty());
    }

    public Node view() {
        return line;
    }
}
