package application;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.primitives.ImmutableDoubleArray;
import components.editor.Editor;
import components.editor.impl.EditorImpl;
import components.menu.Menu;
import components.menu.impl.MenuImpl;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utils.math.vector.ImmutableVector;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Application extends javafx.application.Application {
    private final Lock lock = new ReentrantLock();
    private final SimpleObjectProperty<Stage> stage = new SimpleObjectProperty<>();
    private final Menu menu = new MenuImpl();
    private final Editor editor = new EditorImpl();
    private final Label messageLabel = new Label();
    private final BorderPane root = new BorderPane(editor.view(), menu.view(), null, messageLabel, null);

    private final SimpleObjectProperty<File> openedFile = new SimpleObjectProperty<>();
    private final AtomicReference<ImmutableVector> predictionData = new AtomicReference<>(new ImmutableVector(ImmutableDoubleArray.of(1, 1)));
    private final AtomicReference<Iterable<ImmutableDoubleArray>> trainingData = new AtomicReference<>();

    private final AtomicReference<Editor.Prediction> prediction = new AtomicReference<>();
    private final AtomicReference<Editor.Training> training = new AtomicReference<>();
    private final SimpleObjectProperty<String> message = new SimpleObjectProperty<>();

    public Application() {
        message.addListener((observable, oldValue, newValue) -> messageLabel.setText(newValue));
        if (predictionData.get() == null) menu.disable(Menu.Button.PREDICTION, Menu.Button.PREDICTIONDEBUG);
        if (trainingData.get() == null) menu.disable(Menu.Button.TRAINING, Menu.Button.TRAININGDEBUG);
        try {
            menu.setListener(new MenuListener());
            editor.setListener(new EditorListener());
            editor.close();
        } catch (Exception e) {
            final Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Fatal Error");
            final StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            alert.setContentText(stringWriter.toString());
            alert.showAndWait();
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage.setValue(stage);
        this.stage.addListener((observableValue, stage1, t1) -> {
            final Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Fatal Error");
            alert.setContentText("Stage must not be changed");
            alert.showAndWait();
            Platform.exit();
        });
        stage.setScene(new Scene(root, 720, 720));
        stage.show();
    }

    private final class MenuListener implements Menu.Listener {
        @Override
        public void onButtonClicked(Menu.Button button) {
            lock.lock();
            try {
                switch (button) {
                    case CREATE: {
                        editor.create();
                        editor.applyToNeuralNetwork(neuralNetwork -> {
                            if (neuralNetwork.featuresCount() == 0) menu.disable(Menu.Button.REMOVEFEATURE);
                            else menu.enable(Menu.Button.REMOVEFEATURE);
                            if (neuralNetwork.layersCount() == 0) menu.disable(Menu.Button.REMOVELAYER);
                            else menu.enable(Menu.Button.REMOVELAYER);
                        });
                        break;
                    }
                    case OPEN: {
                        final FileChooser fileChooser = new FileChooser();
                        final File file = fileChooser.showOpenDialog(stage());
                        if (file != null) editor.open(file);
                        break;
                    }
                    case SAVE: {
                        editor.save(openedFile.get());
                        break;
                    }
                    case SAVEAS: {
                        final FileChooser fileChooser = new FileChooser();
                        final File file = fileChooser.showOpenDialog(stage());
                        if (file != null) {
                            editor.save(file);
                            openedFile.setValue(file);
                        }
                        break;
                    }
                    case CLOSE: {
                        editor.close();
                        prediction.getAndSet(null);
                        training.getAndSet(null);
                        break;
                    }
                    case ADDFEATURE: {
                        editor.applyToNeuralNetwork(neuralNetwork -> {
                           neuralNetwork.addFeature(neuralNetwork.featuresCount());
                            if (neuralNetwork.featuresCount() == 0) menu.disable(Menu.Button.REMOVEFEATURE);
                            else menu.enable(Menu.Button.REMOVEFEATURE);
                        });
                        break;
                    }
                    case REMOVEFEATURE: {
                        editor.applyToNeuralNetwork(neuralNetwork -> {
                            neuralNetwork.removeFeature(neuralNetwork.featuresCount() - 1);
                            if (neuralNetwork.featuresCount() == 0) menu.disable(Menu.Button.REMOVEFEATURE);
                            else menu.enable(Menu.Button.REMOVEFEATURE);
                        });
                        break;
                    }
                    case ADDDENSELAYER: {
                        editor.applyToNeuralNetwork(neuralNetwork -> {
                            neuralNetwork.addDenseLayer(neuralNetwork.layersCount());
                            if (neuralNetwork.layersCount() == 0) menu.disable(Menu.Button.REMOVELAYER);
                            else menu.enable(Menu.Button.REMOVELAYER);
                        });
                        break;
                    }
                    case REMOVELAYER: {
                        editor.applyToNeuralNetwork(neuralNetwork -> {
                            neuralNetwork.removeLayer(neuralNetwork.layersCount() - 1);
                            if (neuralNetwork.layersCount() == 0) menu.disable(Menu.Button.REMOVELAYER);
                            else menu.enable(Menu.Button.REMOVELAYER);
                        });
                        break;
                    }
                    case PREDICTION: {
                        final Editor.Prediction prediction = editor.predict(predictionData());
                        while (!prediction.result().isPresent()) {
                            prediction.nextStep();
                        }
                        break;
                    }
                    case PREDICTIONDEBUG: {
                        prediction.getAndSet(editor.predict(predictionData()));
                        menu.enable(Menu.Button.NEXTSTEP, Menu.Button.SKIPREMAININGSTEPS);
                        break;
                    }
                    case PREDICTIONDATA: {
                        final File file = new FileChooser().showOpenDialog(stage());
                        if (file != null) {
                            CSVReader reader = new CSVReader(new FileReader(file), ',', '"');
                            String[] row = reader.readNext();
                            final ImmutableDoubleArray.Builder builder = ImmutableDoubleArray.builder();
                            for (String s : row) {
                                builder.add(Double.parseDouble(s));
                            }
                            predictionData.getAndSet(new ImmutableVector(builder.build()));
                            menu.enable(Menu.Button.PREDICTION, Menu.Button.PREDICTIONDEBUG);
                        } else {
                            menu.disable(Menu.Button.PREDICTION, Menu.Button.PREDICTIONDEBUG);
                        }
                        break;
                    }
                    case TRAINING: {
                        for (int i = 0; i < 1000; i++) {
                            final Editor.Training training = editor.train(trainingData());
                            while (!training.error().isPresent()) {
                                training.nextStep();
                            }
                        }
                        break;
                    }
                    case TRAININGDEBUG: {
                        training.getAndSet(editor.train(trainingData()));
                        menu.enable(Menu.Button.NEXTSTEP, Menu.Button.SKIPREMAININGSTEPS);
                        break;
                    }
                    case TRAININGDATA: {
                        final File file = new FileChooser().showOpenDialog(stage());
                        if (file != null) {
                            CSVReader reader = new CSVReader(new FileReader(file), ',', '"');
                            final List<String[]> lines = reader.readAll();
                            final List<ImmutableDoubleArray> trainingSet = new ArrayList<>();
                            lines.forEach(line -> {
                                final ImmutableDoubleArray.Builder builder = ImmutableDoubleArray.builder();
                                for (String s : line) {
                                    builder.add(Double.parseDouble(s));
                                }
                                trainingSet.add(builder.build());
                            });
                            trainingData.getAndSet(trainingSet);
                            menu.enable(Menu.Button.TRAINING, Menu.Button.TRAININGDEBUG);
                        } else {
                            menu.disable(Menu.Button.TRAINING, Menu.Button.TRAININGDEBUG);
                        }
                        break;
                    }
                    case NEXTSTEP: {
                        boolean prediction = false;
                        boolean training = false;
                        if (Application.this.prediction.get() != null) prediction = true;
                        if (Application.this.training.get() != null) training = true;
                        if (prediction && training) {
                            final Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setHeaderText("Fatal Error");
                            alert.setContentText("Prediction must not be active while training is active");
                            alert.showAndWait();
                            Platform.exit();
                        }
                        if (!prediction && !training) {
                            final Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setHeaderText("It's a bug");
                            alert.setContentText("None of prediction or training is active");
                            alert.showAndWait();
                        } else {
                            if (prediction) {
                                prediction().nextStep();
                                if (prediction().result().isPresent()) {
                                    menu.disable(Menu.Button.NEXTSTEP, Menu.Button.SKIPREMAININGSTEPS);
                                    Application.this.prediction.getAndSet(null);
                                }
                            }
                            else {
                                training().nextStep();
                                if (training().error().isPresent()) {
                                    menu.disable(Menu.Button.NEXTSTEP, Menu.Button.SKIPREMAININGSTEPS);
                                    Application.this.training.getAndSet(null);
                                }
                            }
                        }
                        break;
                    }
                    case SKIPREMAININGSTEPS: {
                        boolean prediction = false;
                        boolean training = false;
                        if (Application.this.prediction.get() != null) prediction = true;
                        if (Application.this.training.get() != null) training = true;
                        if (prediction && training) {
                            final Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setHeaderText("Fatal Error");
                            alert.setContentText("Prediction must not be active while training is active");
                            alert.showAndWait();
                            Platform.exit();
                        }
                        if (!prediction && !training) {
                            final Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setHeaderText("It's a bug");
                            alert.setContentText("None of prediction or training is active");
                            alert.showAndWait();
                        } else {
                            if (prediction) {
                                final Editor.Prediction process = prediction();
                                while (!process.result().isPresent()) process.nextStep();
                            } else {
                                final Editor.Training process = training();
                                while (!process.error().isPresent()) process.nextStep();
                            }
                        }
                        menu.disable(Menu.Button.NEXTSTEP, Menu.Button.SKIPREMAININGSTEPS);
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException();
                    }
                }
            } catch (Exception e) {
                final Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Fatal Error");
                final StringWriter stringWriter = new StringWriter();
                e.printStackTrace(new PrintWriter(stringWriter));
                alert.setContentText(stringWriter.toString());
                alert.showAndWait();
                Platform.exit();
            } finally {
                lock.unlock();
            }
        }
    }

    private final class EditorListener implements Editor.Listener {
        @Override
        public void onNeuralNetworkOpened() {
            menu.enable(
                    Menu.Button.CREATE, Menu.Button.CLOSE,
                    Menu.Button.ADDFEATURE, Menu.Button.REMOVEFEATURE,
                    Menu.Button.ADDDENSELAYER, Menu.Button.REMOVELAYER,
                    Menu.Button.PREDICTIONDATA,
                    Menu.Button.TRAININGDATA
            );
            menu.disable(Menu.Button.NEXTSTEP, Menu.Button.SKIPREMAININGSTEPS);
        }

        @Override
        public void onNeuralNetworkClosed() {
            menu.disable(
                    Menu.Button.OPEN, Menu.Button.SAVE, Menu.Button.SAVE, Menu.Button.SAVEAS, Menu.Button.CLOSE,
                    Menu.Button.ADDFEATURE, Menu.Button.REMOVEFEATURE,
                    Menu.Button.ADDDENSELAYER, Menu.Button.REMOVELAYER,
                    Menu.Button.PREDICTION, Menu.Button.PREDICTIONDEBUG, Menu.Button.PREDICTIONDATA,
                    Menu.Button.TRAINING, Menu.Button.TRAININGDEBUG, Menu.Button.TRAININGDATA,
                    Menu.Button.NEXTSTEP, Menu.Button.SKIPREMAININGSTEPS
            );
            menu.enable(Menu.Button.CREATE);
        }

        @Override
        public void predictionStarted() {
            menu.disable(
                    Menu.Button.CLOSE,
                    Menu.Button.PREDICTION, Menu.Button.PREDICTIONDEBUG, Menu.Button.PREDICTIONDATA,
                    Menu.Button.TRAINING, Menu.Button.TRAININGDEBUG, Menu.Button.TRAININGDATA
            );
            menu.enable(Menu.Button.NEXTSTEP, Menu.Button.SKIPREMAININGSTEPS);
        }

        @Override
        public void predictionFinished() {
            menu.disable(Menu.Button.NEXTSTEP, Menu.Button.SKIPREMAININGSTEPS);
            menu.enable(
                    Menu.Button.CLOSE,
                    Menu.Button.PREDICTION, Menu.Button.PREDICTIONDEBUG, Menu.Button.PREDICTIONDATA,
                    Menu.Button.TRAINING, Menu.Button.TRAININGDEBUG, Menu.Button.TRAININGDATA
            );
            if (predictionData.get() == null) menu.disable(Menu.Button.PREDICTION, Menu.Button.PREDICTIONDEBUG);
            if (trainingData.get() == null) menu.disable(Menu.Button.TRAINING, Menu.Button.TRAININGDEBUG);
            prediction.getAndSet(null);
        }

        @Override
        public void trainingStarted() {
            menu.disable(
                    Menu.Button.CLOSE,
                    Menu.Button.PREDICTION, Menu.Button.PREDICTIONDEBUG, Menu.Button.PREDICTIONDATA,
                    Menu.Button.TRAINING, Menu.Button.TRAININGDEBUG, Menu.Button.TRAININGDATA
            );
            menu.enable(Menu.Button.NEXTSTEP, Menu.Button.SKIPREMAININGSTEPS);
        }

        @Override
        public void trainingFinished() {
            menu.disable(Menu.Button.NEXTSTEP, Menu.Button.SKIPREMAININGSTEPS);
            menu.enable(
                    Menu.Button.CLOSE,
                    Menu.Button.PREDICTION, Menu.Button.PREDICTIONDEBUG, Menu.Button.PREDICTIONDATA,
                    Menu.Button.TRAINING, Menu.Button.TRAININGDEBUG, Menu.Button.TRAININGDATA
            );
            if (predictionData.get() == null) menu.disable(Menu.Button.PREDICTION, Menu.Button.PREDICTIONDEBUG);
            if (trainingData.get() == null) menu.disable(Menu.Button.TRAINING, Menu.Button.TRAININGDEBUG);
            training.getAndSet(null);
        }

        @Override
        public void messageReceived(String message) {
            Application.this.message.setValue(message);
        }
    }

    private Stage stage() {
        return stage.getValue();
    }

    private ImmutableVector predictionData() {
        return predictionData.get();
    }

    private Iterable<Editor.TrainingExample> trainingData() {
        AtomicInteger inputSize = new AtomicInteger();
        editor.applyToNeuralNetwork(neuralNetwork -> {
            inputSize.getAndSet(neuralNetwork.featuresCount());
        });
        List<Editor.TrainingExample> examples = new ArrayList<>();
        trainingData.get().forEach(immutableDoubleArray -> {
            final ImmutableDoubleArray.Builder inputBuilder = ImmutableDoubleArray.builder();
            final ImmutableDoubleArray.Builder outputBuilder = ImmutableDoubleArray.builder();
            for (int i = 0; i < inputSize.get(); i++) {
                inputBuilder.add(immutableDoubleArray.get(i));
            }
            for (int i = inputSize.get(); i < immutableDoubleArray.length(); i++) {
                outputBuilder.add(immutableDoubleArray.get(i));
            }
            examples.add(new Editor.TrainingExample(new ImmutableVector(inputBuilder.build()), new ImmutableVector(outputBuilder.build())));
        });
        return examples;
    }

    private Editor.Prediction prediction() {
        return prediction.get();
    }

    private Editor.Training training() {
        return training.get();
    }
}