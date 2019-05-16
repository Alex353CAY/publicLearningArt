package javafx.network;

import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.layer.DenseLayer;
import javafx.neuron.Neuron;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import utils.math.vector.ImmutableVector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class NeuralNetwork implements model.network.NeuralNetwork, components.editor.components.network.NeuralNetwork {
    private final Pane root = new Pane();
    private final javafx.layer.Layer featureLayer = new FeatureLayer(root);
    private final List<javafx.layer.DenseLayer> hiddenLayers = new ArrayList<>();
    private Listener listener;

    private final void fixLayout() {
        DoubleExpression translateXProperty = new SimpleDoubleProperty();
        DoubleExpression translateYProperty = new SimpleDoubleProperty();
        ((SimpleDoubleProperty) translateXProperty).bind(featureLayer.translateXProperty().add(30 + 25));
        for (javafx.layer.Layer layer : hiddenLayers) {
            layer.translateXProperty().unbind();
            layer.translateYProperty().unbind();
            layer.translateXProperty().bind(translateXProperty);
            layer.translateYProperty().bind(translateYProperty);
            translateXProperty = layer.translateXProperty().add(30 + 25);
        }
    }

    @Override
    public void addFeature(int featureIndex) {
        featureLayer.addFeature(featureIndex);
    }

    @Override
    public void removeFeature(int featureIndex) {
        featureLayer.removeFeature(featureIndex);
    }

    @Override
    public DenseLayer addDenseLayer(int index) {
        final javafx.layer.DenseLayer layer = new javafx.layer.DenseLayer(root);
        hiddenLayers.add(index, layer);
        if (index == 0) {
          layer.setPreviousLayer(featureLayer);
          featureLayer.setNextLayer(layer);
        } else if (index > 0) {
            hiddenLayers.get(index - 1).setNextLayer(layer);
            layer.setPreviousLayer(hiddenLayers.get(index - 1));
        }
        if (index < hiddenLayers.size() - 1) {
            hiddenLayers.get(index + 1).setPreviousLayer(layer);
            layer.setNextLayer(hiddenLayers.get(index + 1));
        }
        fixLayout();
        if (listener!= null) listener.onDenseLayerAdded(index, layer);
        return layer;
    }

    @Override
    public model.layer.Layer removeLayer(int index) {
        final javafx.layer.DenseLayer removedLayer = hiddenLayers.remove(index);
        if (index == 0 && hiddenLayers.size() > 0) {
            hiddenLayers.get(0).setPreviousLayer(featureLayer);
            featureLayer.setNextLayer(hiddenLayers.get(0));
        } else if (index == 0) {
            featureLayer.setNextLayer(null);
        } else if (index > 0 && index < hiddenLayers.size()) {
            hiddenLayers.get(index).setPreviousLayer(hiddenLayers.get(index - 1));
            hiddenLayers.get(index - 1).setNextLayer(hiddenLayers.get(index));
        }
        removedLayer.markedAsRemoved();
        fixLayout();
        if (listener!= null) listener.onLayerRemoved(index, removedLayer);
        return removedLayer;
    }

    public ImmutableVector predict(ImmutableVector input, components.editor.components.network.NeuralNetwork.Prediction.Builder predictionBuilder) {
        hiddenLayers.forEach(javafx.layer.DenseLayer::reset);
        for (int i = 0; i < featuresCount(); i++) {
            featureLayer.getNeuronView(i).setPrediction(input.get(i));
        }
        for (int i = 0; i < hiddenLayers.size(); i++) {
            input = hiddenLayers.get(i).predict(input, predictionBuilder);
        }
        return input;
    }

    public ImmutableVector train(ImmutableVector input, ImmutableVector expectedOutput, components.editor.components.network.NeuralNetwork.Training.Builder trainingBuilder) {
        hiddenLayers.forEach(javafx.layer.DenseLayer::reset);
        for (int i = 0; i < featuresCount(); i++) {
            featureLayer.getNeuronView(i).setPrediction(input.get(i));
        }
        final ImmutableVector[] predictions = new ImmutableVector[hiddenLayers.size() + 1];
        predictions[0] = input;
        for (int i = 0; i < hiddenLayers.size(); i++) {
            predictions[i + 1] = hiddenLayers.get(i).predict(predictions[i], trainingBuilder);
        }
        ImmutableVector error = expectedOutput.remainder(predictions[predictions.length - 1]).immutable();
        for (int i = hiddenLayers.size() - 1; i >= 0; i--) {
            ImmutableVector inputError = hiddenLayers.get(i).inputError(error, trainingBuilder);
            hiddenLayers.get(i).train(predictions[i], error, 0.1, trainingBuilder);
            error = inputError;
        }
        return expectedOutput.remainder(predictions[predictions.length - 1]).immutable();
    }

    @Override
    public int featuresCount() {
        return featureLayer.featuresCount();
    }

    @Override
    public int layersCount() {
        return hiddenLayers.size();
    }

    @Override
    public Region view() {
        return root;
    }


    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private final class FeatureLayer extends javafx.layer.Layer {
        public FeatureLayer(Pane root) {
            super(root);
        }

        @Override
        public void addFeature(int featureIndex) {
            super.addFeature(featureIndex);
            addNeuronView(featureIndex, Neuron::unselect, neuron -> {});
        }

        @Override
        public void removeFeature(int featureIndex) {
            super.removeFeature(featureIndex);
            removeNeuronView(featureIndex);
        }
    }
}
