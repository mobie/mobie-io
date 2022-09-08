package org.embl.mobie.io.ome.zarr.util;

public enum AxesTypes {
    TIME("time"),
    CHANNEL("channel"),
    SPACE("space");

    private final String typeName;

    AxesTypes(String typeName) {
        this.typeName = typeName;
    }

    public static boolean contains(String test) {
        for (AxesTypes c : AxesTypes.values()) {
            if (c.typeName.equals(test)) {
                return true;
            }
        }
        return false;
    }

    public static AxesTypes getAxisType(String axisString) {
        if (axisString.equals("x") || axisString.equals("y") || axisString.equals("z")) {
            return AxesTypes.SPACE;
        } else if (axisString.equals("t")) {
            return AxesTypes.TIME;
        } else if (axisString.equals("c")) {
            return AxesTypes.CHANNEL;
        } else {
            return null;
        }
    }

    public String getTypeName() {
        return typeName;
    }
}
