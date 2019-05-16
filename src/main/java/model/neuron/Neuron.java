package model.neuron;

import model.connection.Connection;
import utils.math.activation.Activation;

import java.util.function.Consumer;

public interface Neuron {
    void setActivation(Activation activation);

    Activation getActivation();
    Connection getConnection(int featureIndex);

    void forEachConnection(Consumer<Connection> consumer);

    int featuresCount();
}
