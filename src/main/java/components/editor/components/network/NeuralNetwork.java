package components.editor.components.network;

import components.Component;
import components.editor.Editor;
import model.neuron.Neuron;
import utils.math.activation.Activation;
import utils.math.vector.ImmutableVector;

public interface NeuralNetwork extends model.network.NeuralNetwork, Component {
    ImmutableVector predict(ImmutableVector input, NeuralNetwork.Prediction.Builder predictionBuilder);
    ImmutableVector train(ImmutableVector input, ImmutableVector train, NeuralNetwork.Training.Builder trainingBuilder);
    void setListener(Listener listener);

    interface Listener {
        default void onDenseLayerAdded(int layerIndex, DenseLayer layer) {}
        default void onLayerRemoved(int layerIndex, Layer layer) {}
    }

    interface Layer extends model.layer.Layer {
        void select();
        void unselect();
    }

    interface DenseLayer extends Layer, model.layer.DenseLayer {
        void setListener(Listener listener);

        interface Listener {
            default void onNeuronAdded(int neuronIndex, ActivationNeuron neuron) {}
            default void onNeuronRemoved(int neuronIndex, ActivationNeuron neuron) {}
            default void onSelected() {}
            default void onUnselected() {}
        }
    }

    interface ActivationNeuron extends Neuron {
        void select();
        void unselect();
        void setListener(Listener listener);

        interface Listener {
            default void onActivationChanged(Activation activation) {}
            default void onConnectionAdded(int featureIndex, Connection connection) {}
            default void onConnectionRemoved(int featureIndex, Connection connection) {}
            default void onSelected() {}
            default void onUnselected() {}
        }
    }

    interface Connection extends model.connection.Connection {
        void select();
        void unselect();
        void setListener(Listener listener);

        interface Listener {
            default void onWeightChanged(double value) {}
            default void onSelection() {}
        }
    }

    interface Prediction extends Editor.Prediction {
        interface Builder {
            void addStep(Runnable step, String msg);
        }
    }

    interface Training extends Editor.Training {
        interface Builder extends Prediction.Builder {
            void addStep(Runnable step, String msg);
        }
    }
}
