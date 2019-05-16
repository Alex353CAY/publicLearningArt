package components.editor.impl;

import components.editor.components.overview.Overview;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.DoubleExpression;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import utils.javafx.Borders;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class OverviewImpl implements Overview {
    private final VBox list = new VBox();
    private final ScrollPane scrollPane = new ScrollPane(list);
    private final AtomicReference<Layer> selectedLayer = new AtomicReference<>();

    private final List<Layer> layers = new ArrayList<>();

    public OverviewImpl(DoubleExpression maxWidthProperty) {
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(maxWidthProperty.doubleValue());
        maxWidthProperty.addListener((observable, oldValue, newValue) -> scrollPane.setMaxHeight(newValue.doubleValue()));
    }

    @Override
    public Layer addDenseLayer(int index) {
        final Layer layer = new Layer("Полносвязный слой");
        list.getChildren().add(layer.view);
        layers.add(index, layer);
        return layer;
    }

    @Override
    public void removeLayer(int index) {
        list.getChildren().remove(layers.remove(index).view);
    }

    @Override
    public Region view() {
        return scrollPane;
    }

    public class Layer implements Overview.Layer {
        private final Label view;
        private boolean selected = false;
        private Listener listener;

        private Layer(String name) {
            view = new Label(name);
            view.setOnMouseClicked(event -> {
                if (selected) {
                    final Layer previousSelection = selectedLayer.getAndSet(null);
                    unselect();
                    if (listener != null) listener.onLayerUnselected();
                } else {
                    final Layer previousSelection =selectedLayer.getAndSet(this);
                    if (previousSelection != null) previousSelection.unselect();
                    select();
                    if (listener != null) listener.onLayerSelected();
                }
            });
        }

        @Override
        public void select() {
            selected = true;
            view.setBorder(Borders.selectionBorder());
        }

        @Override
        public void unselect() {
            selected = false;
            view.setBorder(Border.EMPTY);
        }

        @Override
        public void setListener(Listener listener) {
            this.listener = listener;
        }
    }
}