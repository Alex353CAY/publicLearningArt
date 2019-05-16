package components.editor.components.details;

import components.Component;
import utils.math.activation.Activation;

public interface Details extends Component {
    void showDenseLayersDetails(int featuresCount, int neuronsCount, boolean modifiable);

    void showActivationNeuronsDetails(Activation activation, int featuresCount, boolean modifiable);

    void showConnectionsDetails(double weight, boolean modifiable);

    void showEmptyDetails();

    void setListener(Listener listener);

    interface Listener {
        default void onNeuronAdditionRequest(int index) {}
        default void onNeuronRemovalRequest(int index) {}

        default void onActivationChangeRequest(Activation activation) {}

        default void onWeightChangeRequest(double weight) {}
    }
}
