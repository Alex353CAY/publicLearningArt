package utils.math.vector;

public interface Vector {
    Vector sum(ImmutableVector operand);
    Vector remainder(ImmutableVector operand);
    Vector multiplication(ImmutableVector operand);
    Vector quotient(ImmutableVector operand);

    ImmutableVector immutable();

    int length();
}
