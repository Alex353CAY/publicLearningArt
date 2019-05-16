package model.network;

import model.layer.DenseLayer;
import model.layer.Layer;

public interface NeuralNetwork {
    void addFeature(int featureIndex);
    void removeFeature(int featureIndex);

    DenseLayer addDenseLayer(int index);
    Layer removeLayer(int index);

    int featuresCount();
    int layersCount();
}
