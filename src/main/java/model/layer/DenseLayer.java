package model.layer;

import utils.math.activation.Activation;

public interface DenseLayer extends Layer {
    void addNeuron(int neuronIndex, Activation activation);
    void removeNeuron(int neuronIndex);
}
