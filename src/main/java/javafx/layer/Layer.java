package javafx.layer;

import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.connection.Connection;
import javafx.neuron.Neuron;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Layer implements model.layer.Layer {
    private final Pane root;
    private final DoubleProperty translateX = new SimpleDoubleProperty();
    private final DoubleProperty translateY = new SimpleDoubleProperty();

    private int featuresCount = 0;
    private final List<Neuron> neurons = new ArrayList<>();
    private Layer previousLayer;
    private Layer nextLayer;

    public Layer(Pane root) {
        this.root = root;
        fixLayout();
    }

    public void markedAsRemoved() {
        while (neurons.size() > 0) removeNeuronView(0);
    }

    private void fixLayout() {
        DoubleExpression translateXProperty = translateXProperty();
        DoubleExpression translateYProperty = translateYProperty();
        for (int i = 0; i < neurons.size(); i++) {
            neurons.get(i).view().translateXProperty().unbind();
            neurons.get(i).view().translateYProperty().unbind();
            neurons.get(i).view().translateXProperty().bind(translateXProperty);
            neurons.get(i).view().translateYProperty().bind(translateYProperty);
            translateYProperty = neurons.get(i).view().translateYProperty().add(neurons.get(i).view().heightProperty()).add(15);
        }
    }

    public void addFeature(int featureIndex) {
        featuresCount++;
        neurons.forEach(neuron -> neuron.addFeature(featureIndex));
    }

    public void removeFeature(int featureIndex) {
        neurons.forEach(neuron -> neuron.removeFeature(featureIndex).ifPresent(connection -> root.getChildren().remove(connection.view())));
        featuresCount--;
    }

    protected final Neuron addNeuronView(int neuronIndex, Consumer<Neuron> onSelected, Consumer<Neuron> onUnselected) {
        final Neuron neuron = new Neuron(onSelected, onUnselected);
        neurons.add(neuronIndex, neuron);
        root.getChildren().add(neuron.view());
        for (int i = 0; i < featuresCount(); i++) {
            neuron.addFeature(i);
        }
        fixLayout();
        if (nextLayer != null) nextLayer.addFeature(neuronIndex);
        return neuron;
    }

    public final Neuron getNeuronView(int neuronIndex) {
        return neurons.get(neuronIndex);
    }

    protected final void removeNeuronView(int neuronIndex) {
        final Neuron removedNeuron = neurons.remove(neuronIndex);
        while (removedNeuron.featuresCount() > 0) {
            removedNeuron.removeFeature(removedNeuron.featuresCount() - 1)
                    .ifPresent(
                            connection -> root.getChildren().remove(connection.view())
                    );
        }
        root.getChildren().remove(removedNeuron.view());
        if (nextLayer != null) nextLayer.removeFeature(neuronIndex);
        fixLayout();
    }

    protected final Connection addConnectionView(int featureIndex, int neuronIndex) {
        final Connection connection = new Connection(neurons.get(neuronIndex));
        neurons.get(neuronIndex).addConnection(featureIndex, connection)
                .ifPresent(removedConnection -> root.getChildren().remove(removedConnection.view()));
        root.getChildren().add(connection.view());
        connection.setSource(previousLayer.getNeuronView(featureIndex));
        connection.view().toBack();
        return connection;
    }

    protected final void removeConnectionView(int featureIndex, int neuronIndex) {
        neurons.get(neuronIndex).removeConnection(featureIndex)
                .ifPresent(connection -> root.getChildren().remove(connection.view()));
        if (nextLayer != null) nextLayer.removeFeature(neuronIndex);
    }

    public final void setPreviousLayer(Layer layer) {
        this.previousLayer = layer;
        if (layer != null) {
            while (layer.labelsCount() < featuresCount()) removeFeature(featuresCount() - 1);
            while (layer.labelsCount() > featuresCount())
                addFeature(featuresCount());
        }
        neurons.forEach(neuron -> neuron.setPreviousLayer(layer));
    }

    public final void setNextLayer(Layer layer) {
        nextLayer = layer;
    }

    public DoubleProperty translateXProperty() {
        return translateX;
    }

    public DoubleProperty translateYProperty() {
        return translateY;
    }

        @Override
    public final int featuresCount() {
        return featuresCount;
    }

    @Override
    public final int labelsCount() {
        return neurons.size();
    }
}
