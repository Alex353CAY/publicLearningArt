package components.editor.impl.details.view;

import components.editor.components.details.Details;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.util.concurrent.atomic.AtomicReference;

public class ConnectionDetailsView {
    private final BorderPane root = new BorderPane();
    private final Label weightAspect = new Label("Вес");
    final TextField weightAspectValue = new TextField();
    private final AtomicReference<Details.Listener> listener;

    private double weight = 0;

    public ConnectionDetailsView(AtomicReference<Details.Listener> listener) {
        this.listener = listener;
        final GridPane aspects = new GridPane();
        root.setCenter(aspects);
        aspects.addRow(0, weightAspect, weightAspectValue);

        weightAspectValue.setOnAction(event -> {
            try {
                this.weight = Double.parseDouble(weightAspectValue.getText());
                weightAspectValue.setText(String.valueOf(weight));
                if (listener.get() != null) listener.get().onWeightChangeRequest(weight);
            } catch (NumberFormatException e) {
                weightAspectValue.setText(String.valueOf(weight));
            }
        });

        /*weightAspectValue.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                this.weight = Double.parseDouble(newValue);
                weightAspectValue.setText(String.valueOf(weight));
                if (listener.get() != null) listener.get().onWeightChangeRequest(weight);
            } catch (NumberFormatException e) {
                weightAspectValue.setText(String.valueOf(weight));
            }
        });*/

    }

    public void setWeight(double value) {
        weightAspectValue.setText(String.valueOf(value));
        this.weight = value;
    }

    public Node view() {
        return root;
    }
}
