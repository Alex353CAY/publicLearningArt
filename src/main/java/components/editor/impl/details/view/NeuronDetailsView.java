package components.editor.impl.details.view;

import components.editor.components.details.Details;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import utils.math.activation.Activation;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class NeuronDetailsView {
    private final BorderPane root = new BorderPane();
    private final ChoiceBox<Activation> activationChoiceBox = new ChoiceBox<>();

    public NeuronDetailsView(Set<Activation> activations, AtomicReference<Details.Listener> listener) {
        final GridPane aspects = new GridPane();
        root.setCenter(aspects);
        aspects.addRow(1, new Label("Активационная функция"), activationChoiceBox);

        activationChoiceBox.getItems().addAll(activations);
        activationChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (listener.get() != null) listener.get().onActivationChangeRequest(newValue);
        });
    }

    public void setActivation(Activation activation) {
        activationChoiceBox.getSelectionModel().select(activation);
    }

    public Node view() {
        return root;
    }
}
