package net.mabako.common;

/// Collection of compatibility classes
public abstract class Compat {

    public static class HashMap {
        private HashMap() {}
        private static final float DEFAULT_LOAD_FACTOR = 0.75f;

        public static int calculateHashMapCapacity(int numMappings) {
            return (int) Math.ceil(numMappings / (double) DEFAULT_LOAD_FACTOR);
        }
    }

    public static class HashSet {
        private HashSet() {}

        public static <T> java.util.HashSet<T> newHashSet(int numElements) {
            if (numElements < 0) {
                throw new IllegalArgumentException("Negative number of elements: " + numElements);
            }
            return new java.util.HashSet<>(HashMap.calculateHashMapCapacity(numElements));
        }
    }
}
