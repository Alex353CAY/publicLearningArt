package utils.math.activation;

public class Sigmoid implements Activation {
    @Override
    public double value(double input) {
        return 1/(1 + Math.exp(-input));
    }

    @Override
    public double derivative(double input) {
        final double sigmoid = value(input);
        return value(input) * (1 - sigmoid);
    }

    @Override
    public String toString() {
        return "Sigmoid";
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Sigmoid;
    }
}
