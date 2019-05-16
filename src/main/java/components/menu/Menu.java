package components.menu;

import components.Component;

public interface Menu extends Component {
    void enable(Button... buttons);
    void disable(Button... buttons);

    void setListener(Listener listener);

    enum Button {
        CREATE, OPEN, SAVE, SAVEAS, CLOSE,
        ADDFEATURE, REMOVEFEATURE,
        ADDDENSELAYER, REMOVELAYER,
        PREDICTION, PREDICTIONDEBUG, PREDICTIONDATA,
        TRAINING, TRAININGDEBUG, TRAININGDATA,
        NEXTSTEP, SKIPREMAININGSTEPS
    }

    interface Listener {
        void onButtonClicked(Button button);
    }
}
