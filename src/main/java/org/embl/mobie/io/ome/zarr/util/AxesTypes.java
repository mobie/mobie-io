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

    public String getTypeName() {
        return typeName;
    }
}
