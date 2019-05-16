package utils.math.vector;

import com.google.common.primitives.ImmutableDoubleArray;

import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings("UnstableApiUsage")
public class MutableVector implements Vector {
    private final double[] values;
    private ImmutableVector immutableVector;
    private final ReentrantReadWriteLock modificationLock = new ReentrantReadWriteLock();

    public MutableVector(ImmutableDoubleArray values) {
        this.values = values.toArray();
    }

    @Override
    public Vector sum(ImmutableVector operand) {
        if (length() != operand.length()) throw new IndexOutOfBoundsException();
        modificationLock.writeLock().lock();
        try {
            for (int i = 0; i < length(); i++) {
                values[i] += operand.get(i);
            }
            immutableVector = null;
            return this;
        } finally {
            modificationLock.writeLock().unlock();
        }
    }

    @Override
    public Vector remainder(ImmutableVector operand) {
        if (length() != operand.length()) throw new IndexOutOfBoundsException();
        modificationLock.writeLock().lock();
        try {
            for (int i = 0; i < length(); i++) {
                values[i] -= operand.get(i);
            }
            immutableVector = null;
            return this;
        } finally {
            modificationLock.writeLock().unlock();
        }
    }

    @Override
    public Vector multiplication(ImmutableVector operand) {
        if (length() != operand.length()) throw new IndexOutOfBoundsException();
        modificationLock.writeLock().lock();
        try {
            for (int i = 0; i < length(); i++) {
                values[i] *= operand.get(i);
            }
            immutableVector = null;
            return this;
        } finally {
            modificationLock.writeLock().unlock();
        }
    }

    @Override
    public Vector quotient(ImmutableVector operand) {
        if (length() != operand.length()) throw new IndexOutOfBoundsException();
        modificationLock.writeLock().lock();
        try {
            for (int i = 0; i < length(); i++) {
                values[i] /= operand.get(i);
            }
            immutableVector = null;
            return this;
        } finally {
            modificationLock.writeLock().unlock();
        }
    }

    @Override
    public synchronized ImmutableVector immutable() {
        modificationLock.readLock().lock();
        try {
            if (immutableVector == null) {
                immutableVector = new ImmutableVector(ImmutableDoubleArray.copyOf(values));
            }
            return immutableVector;
        } finally {
            modificationLock.readLock().unlock();
        }
    }

    @Override
    public int length() {
        return values.length;
    }
}
