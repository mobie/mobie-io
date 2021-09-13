package org.embl.mobie.viewer.ome.zarr.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    ZarrAxes(String axes) {
        this.axes = axes;
    }

    @JsonCreator
    public static ZarrAxes decode(final String axes) {
        return Stream.of(ZarrAxes.values()).filter(targetEnum ->
                targetEnum.axes.equals(axes)).findFirst().orElse(NOT_SPECIFIED);
    }

    public List<String> getAxesList() {
        String pattern = "([a-z])";
        List<String> allMatches = new ArrayList<>();
        Matcher m = Pattern.compile(pattern)
                .matcher(axes);
        while (m.find()) {
            allMatches.add(m.group());
        }

        return allMatches;
    }

    public boolean is2D() {
        return this.axes.equals(YX.axes);
    }

    public boolean is5D() {
        return this.axes.equals(TCZYX.axes) || this.axes.equals(NOT_SPECIFIED.axes);
    }

    public boolean is4D() {
        return this.axes.equals(CZYX.axes) || this.axes.equals(TZYX.axes) || this.axes.equals(TCYX.axes);
    }

    public boolean is4DWithTimepoints() {
        return this.axes.equals(TZYX.axes);
    }

    public boolean is4DWithChannels() {
        return this.axes.equals(CZYX.axes);
    }

    public boolean is4DWithTimepointsAndChannels() {
        return this.axes.equals(TCYX.axes);
    }
}
