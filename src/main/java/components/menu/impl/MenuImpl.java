package components.menu.impl;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

import java.util.concurrent.atomic.AtomicReference;

public class MenuImpl implements components.menu.Menu {
    private final MenuItem createNeuralNetworkBtn = new MenuItem("Создать");
    private final MenuItem openNeuralNetworkBtn = new MenuItem("Открыть...");
    private final MenuItem saveNeuralNetworkAsBtn = new MenuItem("Сохранить как...");
    private final MenuItem saveNeuralNetworkBtn = new MenuItem("Сохранить");
    private final MenuItem closeNeuralNetworkBtn = new MenuItem("Закрыть");
    private final MenuItem addFeatureBtn = new MenuItem("Добавить признак");
    private final MenuItem removeFeatureBtn = new MenuItem("Удалить признак");
    private final MenuItem addDenseLayerBtn = new MenuItem("Добавить полносвязный слой");
    private final MenuItem removeLayerBtn = new MenuItem("Удалить слой");
    private final MenuItem predictionBtn = new MenuItem("Предсказание");
    private final MenuItem predictionDebugBtn = new MenuItem("Отладка");
    private final MenuItem predictionSettingsBtn = new MenuItem("Настройка");
    private final MenuItem trainingBtn = new MenuItem("Обучение");
    private final MenuItem trainingDebugBtn = new MenuItem("Отладка");
    private final MenuItem trainingSettingsBtn = new MenuItem("Настройка");
    private final Menu fileMenu = new Menu("Файл", null, createNeuralNetworkBtn, openNeuralNetworkBtn, saveNeuralNetworkAsBtn, saveNeuralNetworkBtn, closeNeuralNetworkBtn);
    private final Menu editMenu = new Menu("Правка", null, addFeatureBtn, removeFeatureBtn, addDenseLayerBtn, removeLayerBtn);
    private final Menu predictionMenu = new Menu("Предсказание", null, predictionBtn, predictionDebugBtn, predictionSettingsBtn);
    private final Menu trainingMenu = new Menu("Обучение", null, trainingBtn, trainingDebugBtn, trainingSettingsBtn);
    private final Menu aboutMenu = new Menu("Справка");
    private final javafx.scene.control.Button nextStep = new javafx.scene.control.Button("Следующее действие");
    private final javafx.scene.control.Button skipRemainingSteps = new javafx.scene.control.Button("Пропустить оставшиееся действия");

    private final ButtonBar controls = new ButtonBar();
    private final MenuBar menuBar = new MenuBar(fileMenu, editMenu, predictionMenu, trainingMenu, aboutMenu);

    private final BorderPane root = new BorderPane(menuBar);

    private final AtomicReference<Listener> listener = new AtomicReference<>();

    public MenuImpl() {
        createNeuralNetworkBtn.setOnAction(actionEvent -> {
            if (listener.get() != null) listener.get().onButtonClicked(Button.CREATE);
        });
        openNeuralNetworkBtn.setOnAction(actionEvent -> {
            if (listener.get() != null) listener.get().onButtonClicked(Button.OPEN);
        });
        saveNeuralNetworkBtn.setOnAction(actionEvent -> {
            if (listener.get() != null) listener.get().onButtonClicked(Button.SAVE);
        });
        saveNeuralNetworkAsBtn.setOnAction(actionEvent -> {
            if (listener.get() != null) listener.get().onButtonClicked(Button.SAVEAS);
        });
        closeNeuralNetworkBtn.setOnAction(actionEvent -> {
            if (listener.get() != null) listener.get().onButtonClicked(Button.CLOSE);
        });
        addFeatureBtn.setOnAction(actionEvent -> {
            if (listener.get() != null) listener.get().onButtonClicked(Button.ADDFEATURE);
        });
        removeFeatureBtn.setOnAction(actionEvent -> {
            if (listener.get() != null) listener.get().onButtonClicked(Button.REMOVEFEATURE);
        });
        addDenseLayerBtn.setOnAction(actionEvent -> {
            if (listener.get() != null) listener.get().onButtonClicked(Button.ADDDENSELAYER);
        });
        removeLayerBtn.setOnAction(actionEvent -> {
            if (listener.get() != null) listener.get().onButtonClicked(Button.REMOVELAYER);
        });
        predictionBtn.setOnAction(actionEvent -> {
            if (listener.get() != null) listener.get().onButtonClicked(Button.PREDICTION);
        });
        predictionDebugBtn.setOnAction(actionEvent -> {
            if (listener.get() != null) listener.get().onButtonClicked(Button.PREDICTIONDEBUG);
        });
        predictionSettingsBtn.setOnAction(actionEvent -> {
            if (listener.get() != null) listener.get().onButtonClicked(Button.PREDICTIONDATA);
        });
        trainingBtn.setOnAction(actionEvent -> {
            if (listener.get() != null) listener.get().onButtonClicked(Button.TRAINING);
        });
        trainingDebugBtn.setOnAction(actionEvent -> {
            if (listener.get() != null) listener.get().onButtonClicked(Button.TRAININGDEBUG);
        });
        trainingSettingsBtn.setOnAction(actionEvent -> {
            if (listener.get() != null) listener.get().onButtonClicked(Button.TRAININGDATA);
        });
        nextStep.setOnAction(actionEvent -> listener.get().onButtonClicked(Button.NEXTSTEP));
        skipRemainingSteps.setOnAction(actionEvent -> {
            if (listener.get() != null) listener.get().onButtonClicked(Button.SKIPREMAININGSTEPS);
        });
        controls.getButtons().addAll(nextStep, skipRemainingSteps);
    }

    @Override
    public void enable(Button... buttons) {
        for (Button button : buttons) {
            switch (button) {
                case CREATE: {
                    createNeuralNetworkBtn.setDisable(false);
                    break;
                }
                case OPEN:  {
                    openNeuralNetworkBtn.setDisable(false);
                    break;
                }
                case SAVE: {
                    saveNeuralNetworkBtn.setDisable(false);
                    break;
                }
                case SAVEAS: {
                    saveNeuralNetworkAsBtn.setDisable(false);
                    break;
                }
                case CLOSE: {
                    closeNeuralNetworkBtn.setDisable(false);
                    break;
                }
                case ADDFEATURE: {
                    addFeatureBtn.setDisable(false);
                    break;
                }
                case REMOVEFEATURE: {
                    removeFeatureBtn.setDisable(false);
                    break;
                }
                case ADDDENSELAYER: {
                    addDenseLayerBtn.setDisable(false);
                    break;
                }
                case REMOVELAYER: {
                    removeLayerBtn.setDisable(false);
                    break;
                }
                case PREDICTION: {
                    predictionBtn.setDisable(false);
                    break;
                }
                case PREDICTIONDEBUG: {
                    predictionDebugBtn.setDisable(false);
                    break;
                }
                case PREDICTIONDATA: {
                    predictionSettingsBtn.setDisable(false);
                    break;
                }
                case TRAINING: {
                    trainingBtn.setDisable(false);
                    break;
                }
                case TRAININGDEBUG: {
                    trainingDebugBtn.setDisable(false);
                    break;
                }
                case TRAININGDATA: {
                    trainingSettingsBtn.setDisable(false);
                    break;
                }
                case NEXTSTEP: {
                    nextStep.setDisable(false);
                    if (!root.getChildren().contains(controls)) root.setBottom(controls);
                    break;
                }
                case SKIPREMAININGSTEPS: {
                    skipRemainingSteps.setDisable(false);
                    if (!root.getChildren().contains(controls)) root.setBottom(controls);
                    break;
                }
            }
        }
    }

    @Override
    public void disable(Button... buttons) {
        for (Button button : buttons) {
            switch (button) {
                case CREATE: {
                    createNeuralNetworkBtn.setDisable(true);
                    break;
                }
                case OPEN:  {
                    openNeuralNetworkBtn.setDisable(true);
                    break;
                }
                case SAVE: {
                    saveNeuralNetworkBtn.setDisable(true);
                    break;
                }
                case SAVEAS: {
                    saveNeuralNetworkAsBtn.setDisable(true);
                    break;
                }
                case CLOSE: {
                    closeNeuralNetworkBtn.setDisable(true);
                    break;
                }
                case ADDFEATURE: {
                    addFeatureBtn.setDisable(true);
                    break;
                }
                case REMOVEFEATURE: {
                    removeFeatureBtn.setDisable(true);
                    break;
                }
                case ADDDENSELAYER: {
                    addDenseLayerBtn.setDisable(true);
                    break;
                }
                case REMOVELAYER: {
                    removeLayerBtn.setDisable(true);
                    break;
                }
                case PREDICTION: {
                    predictionBtn.setDisable(true);
                    break;
                }
                case PREDICTIONDEBUG: {
                    predictionDebugBtn.setDisable(true);
                    break;
                }
                case PREDICTIONDATA: {
                    predictionSettingsBtn.setDisable(true);
                    break;
                }
                case TRAINING: {
                    trainingBtn.setDisable(true);
                    break;
                }
                case TRAININGDEBUG: {
                    trainingDebugBtn.setDisable(true);
                    break;
                }
                case TRAININGDATA: {
                    trainingSettingsBtn.setDisable(true);
                    break;
                }
                case NEXTSTEP: {
                    nextStep.setDisable(true);
                    if (nextStep.isDisabled() && skipRemainingSteps.isDisabled())
                        root.getChildren().remove(controls);
                    break;
                }
                case SKIPREMAININGSTEPS: {
                    skipRemainingSteps.setDisable(true);
                    if (nextStep.isDisabled() && skipRemainingSteps.isDisabled())
                        root.getChildren().remove(controls);
                    break;
                }
            }
        }
    }

    @Override
    public void setListener(Listener listener) {
        this.listener.getAndSet(listener);
    }

    @Override
    public Region view() {
        return root;
    }
}
