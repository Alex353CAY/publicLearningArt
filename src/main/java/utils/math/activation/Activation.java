package utils.math.activation;

public interface Activation {
    double value(double input);
    double derivative(double input);
}
