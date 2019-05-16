package javafx;

import javafx.beans.binding.DoubleExpression;
import javafx.scene.Node;

public interface Component {
    Node view();
    DoubleExpression translateXProperty();
    DoubleExpression translateYProperty();
}
