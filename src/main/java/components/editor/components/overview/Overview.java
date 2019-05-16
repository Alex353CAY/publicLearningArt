package components.editor.components.overview;

import components.Component;

public interface Overview extends Component {
    Layer addDenseLayer(int index);
    void removeLayer(int index);

    interface Layer {
        void select();
        void unselect();
        void setListener(Listener listener);

        interface Listener {
            default void onLayerSelected() {}
            default void onLayerUnselected() {}
        }
    }
}
