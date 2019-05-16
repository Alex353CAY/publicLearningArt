package components.editor.impl.details;

import components.editor.components.details.Details;
import components.editor.impl.details.view.ConnectionDetailsView;
import components.editor.impl.details.view.DenseLayerDetailsView;
import components.editor.impl.details.view.NeuronDetailsView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import utils.math.activation.Activation;
import utils.math.activation.ReLU;
import utils.math.activation.Sigmoid;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

public class DetailsImpl implements Details {
    private final Pane view = new Pane();
    private final DenseLayerDetailsView denseLayerDetailsView;
    private final NeuronDetailsView neuronDetailsView;
    private final ConnectionDetailsView connectionDetailsView;
    private final Pane emptyDetails = new Pane();

    private final AtomicReference<Listener> listener = new AtomicReference<>();

    public DetailsImpl() {
        denseLayerDetailsView = new DenseLayerDetailsView(listener);
        final HashSet<Activation> activations = new HashSet<>();
        //activations.add(new Linear());
        activations.add(new Sigmoid());
        activations.add(new ReLU());
        neuronDetailsView = new NeuronDetailsView(activations, listener);
        connectionDetailsView = new ConnectionDetailsView(listener);
        view.getChildren().add(emptyDetails);
    }

    @Override
    public void showDenseLayersDetails(int featuresCount, int neuronsCount, boolean modifiable) {
        denseLayerDetailsView.setNeuronsCount(neuronsCount);
        view.getChildren().set(0, denseLayerDetailsView.view());
    }

    @Override
    public void showActivationNeuronsDetails(Activation activation, int featuresCount, boolean modifiable) {
        neuronDetailsView.setActivation(activation);
        view.getChildren().set(0, neuronDetailsView.view());
    }

    @Override
    public void showConnectionsDetails(double weight, boolean modifiable) {
        connectionDetailsView.setWeight(weight);
        view.getChildren().set(0, connectionDetailsView.view());
    }

    @Override
    public void showEmptyDetails() {
        view.getChildren().set(0, emptyDetails);
    }

    @Override
    public void setListener(Listener listener) {
        this.listener.getAndSet(listener);
    }

    @Override
    public Region view() {
        return view;
    }
}
