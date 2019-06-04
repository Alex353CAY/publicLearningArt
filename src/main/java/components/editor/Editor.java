package components.editor;

import components.Component;
import model.network.NeuralNetwork;
import utils.math.vector.ImmutableVector;

import java.io.File;
import java.util.IllegalFormatException;
import java.util.Optional;
import java.util.function.Consumer;

public interface Editor extends Component {
    void create();
    void open(File file);
    void save(File file);
    void close();

    void applyToNeuralNetwork(Consumer<NeuralNetwork> consumer);

    Prediction predict(ImmutableVector input);
    Training train(Iterable<TrainingExample> dataSet);

    void setListener(Listener listener);

    interface Listener {
        default void onNeuralNetworkOpened() {}
        default void onNeuralNetworkClosed() {}

        default void predictionStarted() {}
        default void predictionFinished() {}
        default void trainingStarted() {}
        default void trainingFinished() {}
        default void messageReceived(String message) {}
    }

    interface Prediction {
        Optional<ImmutableVector> result();
        void nextStep();
    }

    interface Training {
        Optional<ImmutableVector> error();
        void nextStep();
    }

    class TrainingExample {
        public final ImmutableVector input;
        public final ImmutableVector expectedOutput;

        public TrainingExample(ImmutableVector input, ImmutableVector expectedOutput) {
            this.input = input;
            this.expectedOutput = expectedOutput;
        }
    }
}
