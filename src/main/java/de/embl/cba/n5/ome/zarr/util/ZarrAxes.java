package de.embl.cba.n5.ome.zarr.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.stream.Stream;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public enum ZarrAxes {
    YX("[\"y\",\"x\"]"),
    CYX("[\"c\",\"y\",\"x\"]"),
    TYX("[\"t\",\"y\",\"x\"]"),
    ZYX("[\"z\",\"y\",\"x\"]"),
    CZYX("[\"c\",\"z\",\"y\",\"x\"]"),
    TZYX("[\"t\",\"z\",\"y\",\"x\"]"),
    TCYX("[\"t\",\"c\",\"y\",\"x\"]"),
    TCZYX("[\"t\",\"c\",\"z\",\"y\",\"x\"]"),

    NOT_SPECIFIED("");

    private final String axes;

    @JsonCreator
    public static ZarrAxes decode(final String axes) {
        if (axes.isEmpty()) return NOT_SPECIFIED;
        return Stream.of(ZarrAxes.values()).filter(targetEnum ->
                targetEnum.axes.equals(axes)).findFirst().orElse(NOT_SPECIFIED);
    }

    ZarrAxes(String axes) {
        this.axes = axes;
    }

    public boolean is2D() {
        return this.axes.equals(YX.axes);
    }

    public boolean is5D() {
        return this.axes.equals(TZYX.axes);
    }

    public boolean is4D() {
        return this.axes.equals(CZYX.axes) || this.axes.equals(TZYX.axes) || this.axes.equals(TCYX.axes);
    }

    public boolean is4DWithTimepoints(){
        return this.axes.equals(TZYX.axes);
    }

    public boolean is4DWithChannels() {
        return this.axes.equals(CZYX.axes);
    }

    public boolean is4DWithTimepointsAndChannels() {
        return this.axes.equals(TCYX.axes);
    }
}
