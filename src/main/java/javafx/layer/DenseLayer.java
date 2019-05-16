package javafx.layer;

import com.google.common.primitives.ImmutableDoubleArray;
import components.editor.components.network.NeuralNetwork;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import utils.math.activation.Activation;
import utils.math.vector.ImmutableVector;
import utils.math.vector.MutableVector;
import utils.math.vector.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class DenseLayer extends Layer implements model.layer.DenseLayer, NeuralNetwork.DenseLayer {
    private final List<Neuron> neurons = new ArrayList<>();
    private Listener listener;

    public DenseLayer(Pane root) {
        super(root);
    }

    public void reset() {
        neurons.forEach(neuron -> {
            neuron.reset();
        });
    }

    @Override
    public void markedAsRemoved() {
        while (neurons.size() > 0)
            removeNeuron(neurons.size() - 1);
    }

    @Override
    public void addFeature(int featureIndex) {
        super.addFeature(featureIndex);
        for (int i = 0; i < labelsCount(); i++) {
            neurons.get(i).addConnection(featureIndex, addConnectionView(featureIndex, i));
        }
    }

    @Override
    public void removeFeature(int featureIndex) {
        super.removeFeature(featureIndex);
        neurons.forEach(neuron -> neuron.removeConnection(featureIndex));
    }

    @Override
    public void addNeuron(int neuronIndex, Activation activation) {
        final Neuron neuron = new Neuron(activation, neuronIndex);
        neurons.add(neuronIndex, neuron);
        if (listener != null) listener.onNeuronAdded(neuronIndex, neuron);
        for (int i = 0; i < featuresCount(); i++) {
            neuron.addConnection(i, addConnectionView(i, neuronIndex));
        }
    }

    @Override
    public void removeNeuron(int neuronIndex) {
        removeNeuronView(neuronIndex);
        final Neuron neuron = neurons.remove(neuronIndex);
        if (listener != null) listener.onNeuronRemoved(neuronIndex, neuron);
    }

    @SuppressWarnings("UnstableApiUsage")
    public ImmutableVector predict(ImmutableVector input, components.editor.components.network.NeuralNetwork.Prediction.Builder predictionBuilder) {
        final ImmutableDoubleArray.Builder outputBuilder = ImmutableDoubleArray.builder();
        neurons.forEach(neuron -> outputBuilder.add(neuron.predict(input, predictionBuilder)));
        return new ImmutableVector(outputBuilder.build());
    }

    @SuppressWarnings("UnstableApiUsage")
    public ImmutableVector inputError(ImmutableVector error, components.editor.components.network.NeuralNetwork.Training.Builder trainingBuilder) {
        Vector inputError = null;
        for (int i = 0; i < neurons.size(); i++) {
            Vector partialError = neurons.get(i).inputError(error.get(i), trainingBuilder);
            if (inputError == null) inputError = partialError;
            else inputError = inputError.sum(partialError.immutable());
        }
        return inputError.immutable();
    }

    public void train(ImmutableVector input, ImmutableVector error, double learningRate, NeuralNetwork.Training.Builder trainingBuilder) {
        for (int i = 0; i < neurons.size(); i++) {
            neurons.get(i).train(input, error.get(i), learningRate, trainingBuilder);
        }
    }

    @Override
    public void select() {

    }

    @Override
    public void unselect() {

    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public final class Neuron implements model.neuron.Neuron, NeuralNetwork.ActivationNeuron {
        private Activation activation;
        private final javafx.neuron.Neuron view;
        private final List<DenseLayer.Connection> connections = new ArrayList<>();
        private Listener listener;

        private Neuron(Activation activation, int neuronIndex) {
            this.activation = activation;
            this.view = addNeuronView(neuronIndex, neuron -> { if (listener != null) listener.onSelected(); }, neuron -> { if (listener != null) listener.onUnselected(); });
        }

        public void reset() {
            view.resetPrediction();
            view.resetError();
            connections.forEach(Connection::unmark);
        }

        @Override
        public void select() {
            view.select();
        }

        @Override
        public void unselect() {
            view.unselect();
        }

        public void addConnection(int featureIndex, javafx.connection.Connection connectionView) {
            final Connection connection = new Connection(connectionView, ThreadLocalRandom.current().nextDouble(-1, 1));
            connections.add(featureIndex, connection);
            if (listener != null) listener.onConnectionAdded(featureIndex, connection);
        }

        public void removeConnection(int featureIndex) {
            final Connection connection = connections.remove(featureIndex);
            if (listener != null) listener.onConnectionRemoved(featureIndex, connection);
        }

        public double predict(ImmutableVector input, NeuralNetwork.Prediction.Builder predictionBuilder) {
            double sum = 0;
            final AtomicReference<Connection> previousConnection = new AtomicReference<>();
            for (int i = 0; i < connections.size(); i++) {
                final double delta = input.get(i) * connections.get(i).getWeight();
                sum += delta;
                double tempSum = sum;
                final Connection connection = connections.get(i);
                predictionBuilder.addStep(() -> {
                    if (previousConnection.get() != null) {
                        previousConnection.get().markAsProcessed();
                    }
                    previousConnection.getAndSet(connection);
                    connection.markAsProcessing();
                    view.setPrediction(tempSum);
                }, "Корректировка суммы входных сигналов нейрона");
            }
            final double prediction = activation.value(sum);
            predictionBuilder.addStep(() -> {
                if (previousConnection.get() != null) {
                    previousConnection.get().markAsProcessed();
                }
                view.setPrediction(prediction);
            }, "Вычисление значения активационной функции для суммы входных сигналов нейрона");
            return prediction;
        }

        public ImmutableVector inputError(double error, components.editor.components.network.NeuralNetwork.Training.Builder trainingBuilder) {
            final ImmutableDoubleArray.Builder inputErrorBuilder = ImmutableDoubleArray.builder();
            final AtomicReference<Connection> previousConnection = new AtomicReference<>();
            for (int i = 0; i < connections.size(); i++) {
                final Connection connection = connections.get(i);
                final double connectionError = error * connection.getWeight();
                inputErrorBuilder.add(connectionError);
                trainingBuilder.addStep(() -> {
                    if (previousConnection.get() != null) {
                        previousConnection.get().markAsProcessed();
                    }
                    previousConnection.getAndSet(connection);
                    connection.markAsProcessing();
                    connection.view.getNeuron().setError(connectionError);
                }, "Вычисление ошибки " + i + " входного сигнала");
            }
            trainingBuilder.addStep(() -> {
                if (previousConnection.get() != null) {
                    previousConnection.get().markAsProcessed();
                }
            }, "");
            return new ImmutableVector(inputErrorBuilder.build());
        }

        public void train(ImmutableVector input, double error, double learningRate, components.editor.components.network.NeuralNetwork.Training.Builder trainingBuilder) {
            double sum = 0;
            for (int i = 0; i < connections.size(); i++) {
                sum += input.get(i) * connections.get(i).getWeight();
            }
            double weightsDelta = activation.derivative(sum) * error;
            final AtomicReference<Connection> previousConnection = new AtomicReference<>();
            for (int i = 0; i < connections.size(); i++) {
                final Connection connection = connections.get(i);
                connection.setWeight(connection.getWeight() + weightsDelta * input.get(i) * learningRate);
                trainingBuilder.addStep(() -> {
                    if (previousConnection.get() != null) {
                        previousConnection.get().markAsProcessed();
                    }
                    previousConnection.getAndSet(connection);
                    connection.markAsProcessing();
                }, "Корректировка веса связи для " + i + "-того признака");
            }
            trainingBuilder.addStep(() -> {
                if (previousConnection.get() != null) {
                    previousConnection.get().markAsProcessed();
                }
            }, "");
        }

        @Override
        public void setActivation(Activation activation) {
            this.activation = activation;
            if (listener != null) listener.onActivationChanged(activation);
        }

        @Override
        public Activation getActivation() {
            return activation;
        }

        @Override
        public model.connection.Connection getConnection(int featureIndex) {
            return connections.get(featureIndex);
        }

        @Override
        public void forEachConnection(Consumer<model.connection.Connection> consumer) {
            connections.forEach(consumer);
        }

        @Override
        public int featuresCount() {
            return connections.size();
        }

        @Override
        public void setListener(Listener listener) {
            this.listener = listener;
        }
    }

    private final class Connection implements model.connection.Connection, NeuralNetwork.Connection {
        private final javafx.connection.Connection view;
        private double weight;
        private Listener listener;

        private Connection(javafx.connection.Connection view, double weight) {
            this.view = view;
            this.weight = weight;
            view.setOnSelected(() -> {
                if (listener != null) listener.onSelection();
            });
            view.setOnUnselected(() -> {
                //if (listener != null) listener.onSelection();
            });
        }

        public void markAsProcessing() {
            view.setColor(Color.YELLOW);
        }

        public void markAsProcessed() {
            view.setColor(Color.GREEN);
        }

        public void unmark() {
            view.setColor(Color.BLACK);
        }

        @Override
        public void setWeight(double value) {
            boolean changed = (weight != value);
            weight = value;
            if (changed && listener != null) listener.onWeightChanged(value);
        }

        @Override
        public double getWeight() {
            return weight;
        }

        @Override
        public void select() {
            view.select();
        }

        @Override
        public void unselect() {
            view.unselect();
        }

        @Override
        public void setListener(Listener listener) {
            this.listener = listener;
        }
    }
}
