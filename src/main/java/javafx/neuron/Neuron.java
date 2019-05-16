package javafx.neuron;

import javafx.Component;
import javafx.MountingPoint;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.layer.Layer;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.connection.Connection;
import utils.javafx.Borders;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class Neuron implements Component {
    private final List<Connection> connections = new ArrayList<>();

    private final DoubleProperty translateXProperty = new SimpleDoubleProperty();
    private final DoubleProperty translateYProperty = new SimpleDoubleProperty();
    private final View view = new View();
    private final MountingPoint mountingPoint = new MountingPointImpl(translateXProperty().add(view.circle.radiusProperty()), translateYProperty().add(view.circle.radiusProperty().add(view.prediction.heightProperty())));

    private boolean selected = false;
    private double prediction;
    private double error;
    private boolean predictionIsSet = false;
    private boolean errorIsSet = false;

    private static final DecimalFormat formatter = new DecimalFormat("#.##");

    public Neuron(Consumer<Neuron> onSelection, Consumer<Neuron> onUnselection) {
        view.setOnMouseClicked(event -> {
            if (selected) {
                selected = false;
                unselect();
                onUnselection.accept(this);
            } else {
                selected = true;
                select();
                onSelection.accept(this);
            }
        });
        translateXProperty.bind(view.translateXProperty());
        translateYProperty.bind(view.translateYProperty());
    }

    public void select() {
        view.setBorder(Borders.selectionBorder());
    }

    public void unselect() {
        view.setBorder(Border.EMPTY);
    }

    public void addFeature(int featureIndex) {
        connections.add(featureIndex, null);
    }

    public Optional<Connection> removeFeature(int featureIndex) {
        return Optional.ofNullable(connections.remove(featureIndex));
    }

    public Optional<Connection> addConnection(int featureIndex, Connection connection) {
        return Optional.ofNullable(connections.set(featureIndex, connection));
    }

    public Optional<Connection> removeConnection(int featureIndex) {
        return Optional.ofNullable(connections.set(featureIndex, null));
    }

    public final void setPreviousLayer(Layer layer) {
        for (int i = 0; i < connections.size(); i++) {
            connections.get(i).setSource(layer.getNeuronView(i));
        }
    }

    public Optional<Double> getPrediction() {
        return (predictionIsSet) ? Optional.of(prediction) : Optional.empty();
    }

    public Optional<Double> getError() {
        return (errorIsSet) ? Optional.of(error) : Optional.empty();
    }

    public void setPrediction(double value) {
        prediction = value;
        predictionIsSet = true;
        view.prediction.setText(formatter.format(value));
    }

    public void setError(double value) {
        error = value;
        errorIsSet = true;
        view.error.setText(formatter.format(value));
    }

    public void resetPrediction() {
        predictionIsSet = false;
        view.prediction.setText("");
    }

    public void resetError() {
        errorIsSet = false;
        view.error.setText("");
    }

    public int featuresCount() {
        return connections.size();
    }

    @Override
    public Region view() {
        return view;
    }

    @Override
    public DoubleProperty translateXProperty() {
        return translateXProperty;
    }

    @Override
    public DoubleProperty translateYProperty() {
        return translateYProperty;
    }

    public MountingPoint mountingPoint() {
        return mountingPoint;
    }

    private class View extends Region {
        private final Label prediction = new Label();
        private final Circle circle = new Circle(15);
        private final Label error = new Label();

        public View() {
            getChildren().addAll(prediction, circle, error);
            circle.translateXProperty().bind(circle.radiusProperty());
            circle.translateYProperty().bind(circle.radiusProperty().add(prediction.heightProperty()));
            error.translateYProperty().bind(circle.translateYProperty().add(circle.radiusProperty()));

            circle.setStrokeWidth(1);
            circle.setStroke(Color.BLACK);
            circle.setFill(Color.WHITE);
            prefHeightProperty().bind(prediction.heightProperty().add(circle.radiusProperty().multiply(2)).add(error.heightProperty()));
            prefWidthProperty().bind(circle.radiusProperty().multiply(2));
        }
    }

    private class MountingPointImpl implements MountingPoint {
        private final DoubleExpression translateXProperty;
        private final DoubleExpression translateYProperty;

        private MountingPointImpl(DoubleExpression translateXProperty, DoubleExpression translateYProperty) {
            this.translateXProperty = translateXProperty;
            this.translateYProperty = translateYProperty;
        }

        @Override
        public DoubleExpression translateXProperty() {
            return translateXProperty;
        }

        @Override
        public DoubleExpression translateYProperty() {
            return translateYProperty;
        }
    }
}
