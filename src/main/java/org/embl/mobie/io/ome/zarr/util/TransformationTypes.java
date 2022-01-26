package org.embl.mobie.io.ome.zarr.util;

public enum TransformationTypes {
    IDENTITY ("identity"),
    TRANSLATION("translation"),
    SCALE("scale");

    private final String typeName;

    public String getTypeName() {
        return typeName;
    }

    TransformationTypes(String typeName) {
        this.typeName = typeName;
    }

    public static boolean contains(String test) {
        for (TransformationTypes c : TransformationTypes.values()) {
            if (c.typeName.equals(test)) {
                return true;
            }
        }
        return false;
    }
}
