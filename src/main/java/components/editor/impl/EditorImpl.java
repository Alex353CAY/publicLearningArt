package components.editor.impl;

import com.google.common.primitives.ImmutableDoubleArray;
import com.google.gson.*;
import components.editor.Editor;
import components.editor.components.details.Details;
import components.editor.components.network.NeuralNetwork;
import components.editor.components.overview.Overview;
import components.editor.impl.details.DetailsImpl;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import model.layer.DenseLayer;
import utils.math.activation.Activation;
import utils.math.activation.ReLU;
import utils.math.activation.Sigmoid;
import utils.math.vector.ImmutableVector;
import utils.math.vector.MutableVector;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class EditorImpl implements Editor {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final Overview overview = new OverviewImpl(new SimpleDoubleProperty(100));
    private final javafx.network.NeuralNetwork neuralNetwork = new javafx.network.NeuralNetwork();
    private final Details details = new DetailsImpl();

    private final ScrollPane neuralNetworkView = new ScrollPane(neuralNetwork.view());
    private final BorderPane root = new BorderPane(new AnchorPane(neuralNetworkView), new BorderPane(overview.view(), null, details.view(), null, null), null, null, null);
    private final AtomicReference<Listener> listener = new AtomicReference<>();

    private final AtomicBoolean modificationIsAllowed = new AtomicBoolean(true);

    private final AtomicReference<NeuralNetwork.DenseLayer> selectedLayer = new AtomicReference<>();
    private final AtomicReference<NeuralNetwork.ActivationNeuron> selectedNeuron = new AtomicReference<>();
    private final AtomicReference<NeuralNetwork.Connection> selectedConnection = new AtomicReference<>();

    public EditorImpl() {
        AnchorPane.setTopAnchor(neuralNetworkView, 0d);
        AnchorPane.setRightAnchor(neuralNetworkView, 0d);
        AnchorPane.setLeftAnchor(neuralNetworkView, 0d);
        AnchorPane.setBottomAnchor(neuralNetworkView, 0d);
        neuralNetwork.setListener(new NeuralNetworkListener());
        details.setListener(new DetailsListener());
    }

    @Override
    public void create() {
        lock.writeLock().lock();
        try {
            close();
            if (listener.get() != null) {
                listener.get().onNeuralNetworkOpened();
            }
            neuralNetwork.addFeature(0);
            neuralNetwork.addFeature(1);
            final DenseLayer h0 = neuralNetwork.addDenseLayer(0);
            final DenseLayer h1 = neuralNetwork.addDenseLayer(1);
            h0.addNeuron(0, new Sigmoid());
            h0.addNeuron(1, new Sigmoid());
            h0.addNeuron(2, new Sigmoid());
            h1.addNeuron(0, new Sigmoid());
            h1.addNeuron(1, new Sigmoid());
            h1.addNeuron(2, new Sigmoid());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void open(File file) {
        lock.writeLock().lock();
        try {
            close();
            JsonObject root = new JsonParser().parse(new BufferedReader(new FileReader(file))).getAsJsonObject();
            root.getAsJsonPrimitive("features");
            for (int i = 0; i < root.getAsJsonPrimitive("features").getAsInt(); i++) {
                neuralNetwork.addFeature(i);
            }
            final JsonArray layers = root.get("layers").getAsJsonArray();
            for (JsonElement layerElement: layers) {
                final JsonObject layer = layerElement.getAsJsonObject();
                if (layer.get("type").getAsString().equals("dense")) {
                    final DenseLayer denseLayer = neuralNetwork.addDenseLayer(neuralNetwork.layersCount());
                    if (layer.has("activations")) {
                        for (JsonElement jsonActivation : layer.get("activations").getAsJsonArray()) {
                            Activation activation;
                            switch (jsonActivation.getAsString()) {
                                case "Sigmoid": {
                                    activation = new Sigmoid();
                                    break;
                                }
                                case "ReLU": {
                                    activation = new ReLU();
                                    break;
                                }
                                default: {
                                    throw new IllegalArgumentException();
                                }
                            }
                            denseLayer.addNeuron(denseLayer.labelsCount(), activation);
                        }
                    }
                }
            }
            if (listener.get() != null) {
                listener.get().onNeuralNetworkOpened();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IllegalStateException e) {
          throw new IllegalArgumentException();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void save(File file) {
        lock.writeLock().lock();
        try (final FileWriter fileWriter = new FileWriter(file)) {
            JsonObject root = new JsonObject();
            JsonArray layers = new JsonArray();
            root.addProperty("features", neuralNetwork.featuresCount());
            root.add("layers", layers);
            for (int layerIndex = 0; layerIndex < neuralNetwork.layersCount(); layerIndex++) {
                JsonObject layer = new JsonObject();
                JsonArray activations = new JsonArray();
                final NeuralNetwork.DenseLayer denseLayer = neuralNetwork.getDenseLayer(layerIndex);
                for (int neuronIndex = 0; neuronIndex < denseLayer.labelsCount(); neuronIndex++) {
                    activations.add(denseLayer.getNeuron(neuronIndex).getActivation().toString());
                }
                layer.addProperty("type", "dense");
                layer.add("activations", activations);
                layers.add(layer);
            }
            fileWriter.write(root.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void close() {
        lock.writeLock().lock();
        try {
            while (neuralNetwork.layersCount() > 0) neuralNetwork.removeLayer(neuralNetwork.layersCount() - 1);
            while (neuralNetwork.featuresCount() > 0) neuralNetwork.removeFeature(0);
            if (listener.get() != null) {
                listener.get().onNeuralNetworkClosed();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void applyToNeuralNetwork(Consumer<model.network.NeuralNetwork> consumer) {
        lock.readLock().lock();
        consumer.accept(neuralNetwork);
        lock.readLock().unlock();
    }

    @Override
    public Editor.Prediction predict(ImmutableVector input) {
        lock.writeLock().lock();
        if (listener.get() != null) listener.get().predictionStarted();
        try {
            final Prediction.Builder builder = new Prediction.Builder();
            final ImmutableVector prediction = neuralNetwork.predict(input, builder);
            return builder.build(prediction, () -> {
                synchronized (listener) {
                    if (listener.get() != null) listener.get().predictionFinished();
                }
            }, listener.get());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Training train(Iterable<TrainingExample> dataSet) {
        lock.writeLock().lock();
        if (listener.get() != null) listener.get().trainingStarted();
        try {
            final Training.Builder builder = new Training.Builder();
            MutableVector error = null;
            for (TrainingExample trainingExample : dataSet) {
                final ImmutableVector partialError = neuralNetwork.train(trainingExample.input, trainingExample.expectedOutput, builder);
                if (error == null) error = partialError.mutable();
                else error.sum(partialError);
            }
            final Runnable onComplete = () -> {
                synchronized (listener) {
                    if (listener.get() != null) listener.get().trainingFinished();
                }
            };
            if (error == null) return builder.build(new ImmutableVector(ImmutableDoubleArray.of()), onComplete, listener.get());
            return builder.build(error.immutable(), onComplete, listener.get());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public synchronized void setListener(Listener listener) {
        lock.readLock().lock();
        this.listener.getAndSet(listener);
        lock.readLock().unlock();
    }

    @Override
    public Region view() {
        return root;
    }

    public static class Prediction implements NeuralNetwork.Prediction {
        private final Iterator<Runnable> steps;
        private final Iterator<String> messages;
        private final Listener listener;
        private final ImmutableVector result;
        private final Runnable onComplete;

        private Prediction(Iterator<Runnable> steps, Iterator<String> messages, ImmutableVector result, Runnable onComplete, Listener listener) {
            this.steps = steps;
            this.result = result;
            this.onComplete = onComplete;
            this.messages = messages;
            this.listener = listener;
            if (!steps.hasNext()) onComplete.run();
        }

        @Override
        public Optional<ImmutableVector> result() {
            if (steps.hasNext()) return Optional.empty();
            else return Optional.of(result);
        }

        @Override
        public void nextStep() {
            synchronized (this) {
                if (steps.hasNext()) {
                    steps.next().run();
                    if (listener != null) listener.messageReceived(messages.next());
                    if (!steps.hasNext()) onComplete.run();
                }
            }
        }

        public static class Builder implements Cloneable, NeuralNetwork.Prediction.Builder {
            private boolean isValid = true;
            private final ArrayDeque<Runnable> steps;
            private final ArrayDeque<String> messages;

            public Builder() {
                steps = new ArrayDeque<>();
                messages = new ArrayDeque<>();
            }

            public Builder(ArrayDeque<Runnable> steps, ArrayDeque<String> messages) {
                this.steps = steps;
                this.messages = messages;
            }

            @Override
            public void addStep(Runnable step, String msg) {
                steps.addLast(step);
                messages.addLast(msg);
            }

            public synchronized Prediction build(ImmutableVector result, Runnable onComplete, Listener listener) {
                if (isValid) {
                    isValid = false;
                    return new Prediction(steps.iterator(), messages.iterator(), result, onComplete, listener);
                }
                else throw new IllegalStateException();
            }

            private Builder copy() {
                synchronized (this) {
                    return new Builder(new ArrayDeque<>(steps), new ArrayDeque<>(messages));
                }
            }
        }
    }

    public static class Training implements NeuralNetwork.Training {
        private final Iterator<Runnable> steps;
        private final Iterator<String> messages;
        private final ImmutableVector result;
        private final Runnable onComplete;
        private final Listener listener;

        private Training(Iterator<Runnable> steps, Iterator<String> messages, ImmutableVector result, Runnable onComplete, Listener listener) {
            this.steps = steps;
            this.messages = messages;
            this.result = result;
            this.onComplete = onComplete;
            this.listener = listener;
            if (!steps.hasNext()) onComplete.run();
        }

        @Override
        public Optional<ImmutableVector> error() {
            if (steps.hasNext()) return Optional.empty();
            else return Optional.of(result);
        }

        @Override
        public void nextStep() {
            synchronized (this) {
                if (steps.hasNext()) {
                    steps.next().run();
                    if (listener != null) listener.messageReceived(messages.next());
                    if (!steps.hasNext()) onComplete.run();
                }
            }
        }

        public static class Builder implements NeuralNetwork.Training.Builder {
            private boolean isValid = true;
            private final ArrayDeque<Runnable> steps;
            private final ArrayDeque<String> messages;

            public Builder() {
                steps = new ArrayDeque<>();
                messages = new ArrayDeque<>();
            }

            public Builder(ArrayDeque<Runnable> steps, ArrayDeque<String> messages) {
                this.steps = steps;
                this.messages = messages;
            }

            @Override
            public void addStep(Runnable step, String msg) {
                steps.addLast(step);
                messages.addLast(msg);
            }

            public synchronized Training build(ImmutableVector result, Runnable onComplete, Listener listener) {
                if (isValid) {
                    isValid = false;
                    return new Training(steps.iterator(), messages.iterator(), result, onComplete, listener);
                }
                else throw new IllegalStateException();
            }

            private Training.Builder copy() {
                synchronized (this) {
                    return new Training.Builder(new ArrayDeque<>(steps), new ArrayDeque<>(messages));
                }
            }
        }
    }

    private class NeuralNetworkListener implements NeuralNetwork.Listener {
        @Override
        public void onDenseLayerAdded(int layerIndex, NeuralNetwork.DenseLayer layer) {
            final Overview.Layer overviewItem = overview.addDenseLayer(layerIndex);
            layer.setListener(new DenseLayerListener(layer, overviewItem));
            overviewItem.setListener(new Overview.Layer.Listener() {
                @Override
                public void onLayerSelected() {
                    layer.select();
                    if (selectedNeuron.get() != null) selectedNeuron.getAndSet(null).unselect();
                    details.showDenseLayersDetails(layer.featuresCount(), layer.labelsCount(), modificationIsAllowed.get());
                    if (selectedConnection.get() != null) selectedConnection.getAndSet(null).unselect();
                    selectedLayer.getAndSet(layer);
                }

                @Override
                public void onLayerUnselected() {
                    selectedLayer.getAndSet(null);
                    layer.unselect();
                    details.showEmptyDetails();
                }
            });
        }

        @Override
        public void onLayerRemoved(int layerIndex, NeuralNetwork.Layer layer) {
            if (selectedLayer.get() == layer) {
                selectedLayer.getAndSet(null);
                details.showEmptyDetails();
            }
            overview.removeLayer(layerIndex);
        }
    }

    private class DenseLayerListener implements NeuralNetwork.DenseLayer.Listener {
        private final NeuralNetwork.DenseLayer model;
        private final Overview.Layer view;

        public DenseLayerListener(NeuralNetwork.DenseLayer model, Overview.Layer view) {
            this.model = model;
            this.view = view;
        }

        @Override
        public void onNeuronAdded(int neuronIndex, NeuralNetwork.ActivationNeuron neuron) {
            if (selectedLayer.get() == model) details.showDenseLayersDetails(model.featuresCount(), model.labelsCount(), modificationIsAllowed.get());
            neuron.setListener(new NeuronListener(neuron));
        }

        @Override
        public void onNeuronRemoved(int neuronIndex, NeuralNetwork.ActivationNeuron neuron) {
            if (selectedLayer.get() == model) details.showDenseLayersDetails(model.featuresCount(), model.labelsCount(), modificationIsAllowed.get());
        }

        @Override
        public void onSelected() {
            view.select();
            details.showDenseLayersDetails(model.featuresCount(), model.labelsCount(), modificationIsAllowed.get());
            if (selectedConnection.get() != null) selectedConnection.getAndSet(null).unselect();
            selectedLayer.getAndSet(model);
        }

        @Override
        public void onUnselected() {
            selectedLayer.getAndSet(null);
            view.unselect();
            details.showEmptyDetails();
        }
    }

    private class NeuronListener implements NeuralNetwork.ActivationNeuron.Listener {
        private final NeuralNetwork.ActivationNeuron model;

        public NeuronListener(NeuralNetwork.ActivationNeuron model) {
            this.model = model;
        }

        @Override
        public void onActivationChanged(Activation activation) {
            if (selectedNeuron.get() == model) details.showActivationNeuronsDetails(model.getActivation(), model.featuresCount(), modificationIsAllowed.get());
        }

        @Override
        public void onConnectionAdded(int featureIndex, NeuralNetwork.Connection connection) {
            connection.setListener(new ConnectionListener(connection));
        }

        @Override
        public void onConnectionRemoved(int featureIndex, NeuralNetwork.Connection connection) {

        }

        @Override
        public void onSelected() {
            final NeuralNetwork.ActivationNeuron previouslySelectedNeuron = selectedNeuron.getAndSet(model);
            if (previouslySelectedNeuron != null) previouslySelectedNeuron.unselect();
            if (selectedLayer.get() != null) selectedLayer.getAndSet(null).unselect();
            if (selectedConnection.get() != null) selectedConnection.getAndSet(null).unselect();
            details.showActivationNeuronsDetails(model.getActivation(), model.featuresCount(), modificationIsAllowed.get());
        }

        @Override
        public void onUnselected() {
            selectedNeuron.getAndSet(null);
            details.showEmptyDetails();
        }
    }

    private class ConnectionListener implements NeuralNetwork.Connection.Listener {
        private final NeuralNetwork.Connection model;

        private ConnectionListener(NeuralNetwork.Connection model) {
            this.model = model;
        }

        @Override
        public void onWeightChanged(double value) {
            model.setWeight(value);
            if (selectedConnection.get() == model) details.showConnectionsDetails(model.getWeight(), true);
        }

        @Override
        public void onSelection() {
            final NeuralNetwork.Connection previouslySelectedConnection = selectedConnection.getAndSet(model);
            if (previouslySelectedConnection != null) previouslySelectedConnection.unselect();
            if (selectedNeuron.get() != null) selectedNeuron.getAndSet(null).unselect();
            if (selectedLayer.get() != null) selectedLayer.getAndSet(null).unselect();
            details.showConnectionsDetails(model.getWeight(), true);
        }
    }

    private class DetailsListener implements Details.Listener {
        @Override
        public void onNeuronAdditionRequest(int index) {
            selectedLayer.get().addNeuron(index, new Sigmoid());
        }

        @Override
        public void onNeuronRemovalRequest(int index) {
            selectedLayer.get().removeNeuron(index);
        }

        @Override
        public void onActivationChangeRequest(Activation activation) {
            selectedNeuron.get().setActivation(activation);
        }

        @Override
        public void onWeightChangeRequest(double weight) {
            selectedConnection.get().setWeight(weight);
        }
    }
}
