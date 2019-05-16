package utils.javafx;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public final class Borders {
    private static final Border selectionBorder = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));

    public static Border selectionBorder() {
        return selectionBorder;
    }
}
