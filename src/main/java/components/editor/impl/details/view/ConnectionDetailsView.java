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


        weightAspectValue.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                final String negativeInfinity = String.valueOf(Double.NEGATIVE_INFINITY);
                final String positiveInfinity = String.valueOf(Double.POSITIVE_INFINITY);
                if (newValue.equals(negativeInfinity) || newValue.equals(positiveInfinity)) return;
                int floatingPointPosition = newValue.indexOf(".");
                if (floatingPointPosition == -1) floatingPointPosition = newValue.length();
                final String integerPart = newValue.substring(0, floatingPointPosition);
                final String fractionalPart = newValue.substring(floatingPointPosition);
                final String modifiedValue = integerPart.replaceAll("[^\\d]", "") + '.' + fractionalPart.replaceAll("[^\\d]", "");
                weightAspectValue.setText(modifiedValue);

                this.weight = Double.parseDouble(modifiedValue);
                weightAspectValue.setText(String.valueOf(weight));
                if (listener.get() != null) listener.get().onWeightChangeRequest(weight);
            } catch (NumberFormatException e) {
                weightAspect.setText(String.valueOf(weight));
            }
        });

    }

    public void setWeight(double value) {
        weightAspectValue.setText(String.valueOf(value));
        this.weight = value;
    }

    public Node view() {
        return root;
    }
}
