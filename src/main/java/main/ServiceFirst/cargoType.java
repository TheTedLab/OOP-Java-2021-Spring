package main.ServiceFirst;

public enum cargoType {
    FRIABLE,
    FLUID,
    CONTAINER,
    NONE;

    static cargoType getType(int number) {
        switch (number) {
            case 0 -> {
                return FRIABLE;
            }
            case 1 -> {
                return FLUID;
            }
            case 2 -> {
                return CONTAINER;
            }
            default -> {
                return NONE;
            }
        }
    }
}
