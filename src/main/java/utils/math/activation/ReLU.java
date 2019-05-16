package utils.math.activation;

public class ReLU implements Activation {
    @Override
    public double value(double input) {
        return (input > 0) ? input : 0;
    }

    @Override
    public double derivative(double input) {
        return (input > 0) ? 1 : 0;
    }

    @Override
    public String toString() {
        return "ReLU";
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ReLU;
    }
}
