package utils.math.vector;

import com.google.common.primitives.ImmutableDoubleArray;

@SuppressWarnings("UnstableApiUsage")
public class ImmutableVector implements Vector {
    private final double[] values;

    public ImmutableVector(ImmutableDoubleArray values) {
        this.values = values.toArray();
    }

    @Override
    public Vector sum(ImmutableVector operand) {
        final MutableVector mutableVector = mutable();
        return mutableVector.sum(operand);
    }

    @Override
    public Vector remainder(ImmutableVector operand) {
        final MutableVector mutableVector = mutable();
        return mutableVector.remainder(operand);
    }

    @Override
    public Vector multiplication(ImmutableVector operand) {
        final MutableVector mutableVector = mutable();
        return mutableVector.multiplication(operand);
    }

    @Override
    public Vector quotient(ImmutableVector operand) {
        final MutableVector mutableVector = mutable();
        return mutableVector.quotient(operand);
    }

    @Override
    public ImmutableVector immutable() {
        return this;
    }

    public MutableVector mutable() {
        return new MutableVector(ImmutableDoubleArray.copyOf(values));
    }

    @Override
    public int length() {
        return values.length;
    }

    public double get(int index) {
        return values[index];
    }
}
