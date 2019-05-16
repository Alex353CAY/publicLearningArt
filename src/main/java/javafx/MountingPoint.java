package javafx;

import javafx.beans.binding.DoubleExpression;

public interface MountingPoint {
    DoubleExpression translateXProperty();
    DoubleExpression translateYProperty();
}
