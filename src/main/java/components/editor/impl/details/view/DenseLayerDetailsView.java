package components.editor.impl.details.view;

import components.editor.components.details.Details;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.util.concurrent.atomic.AtomicReference;

public class DenseLayerDetailsView {
    private final BorderPane root = new BorderPane();
    private final Label neuronsCountAspect = new Label("?");
    final Button addNeuron = new Button("Добавить нейрон");
    final Button removeNeuron = new Button("Удалить нейрон");
    private final AtomicReference<Details.Listener> listener;

    private int neuronsCount = 0;

    public DenseLayerDetailsView(AtomicReference<Details.Listener> listener) {
        this.listener = listener;
        final GridPane aspects = new GridPane();
        final ButtonBar controls = new ButtonBar();
        root.setCenter(aspects);
        aspects.addRow(1, new Label("Кол-во нейронов"), neuronsCountAspect);

        root.setBottom(controls);
        controls.getButtons().addAll(addNeuron, removeNeuron);
        addNeuron.setOnAction(event -> {
            if (listener.get() != null) listener.get().onNeuronAdditionRequest(neuronsCount);
        });
        removeNeuron.setOnAction(event -> {
            if (listener.get() != null) listener.get().onNeuronRemovalRequest(neuronsCount - 1);
        });
    }

    public void setNeuronsCount(int neuronsCount) {
        if (neuronsCount < 0) throw new IllegalArgumentException();
        neuronsCountAspect.setText(String.valueOf(neuronsCount));
        if (neuronsCount > 0) removeNeuron.setDisable(false);
        else removeNeuron.setDisable(true);
        this.neuronsCount = neuronsCount;
    }

    public Node view() {
        return root;
    }
}
