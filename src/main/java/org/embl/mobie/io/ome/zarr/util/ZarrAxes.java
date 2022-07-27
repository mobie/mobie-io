package org.embl.mobie.io.ome.zarr.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    TCZYX("[\"t\",\"c\",\"z\",\"y\",\"x\"]"),  // v0.2

    NOT_SPECIFIED(""); // TODO: remove and use TCZYX instead

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

    public List<ZarrAxis> toAxesList( String spaceUnit, String timeUnit ) {
        List<ZarrAxis> zarrAxesList = new ArrayList<>();
        List<String> zarrAxesStrings = getAxesList();

        String[] units = new String[]{spaceUnit, timeUnit};

        // convert to valid ome-zarr units, if possible, otherwise just go ahead with
        // given unit
        for ( int i = 0; i< units.length; i++ ) {
            String unit = units[i];
            if ( !UnitTypes.contains(unit) ) {
                UnitTypes unitType = UnitTypes.convertUnit(unit);
                if (unitType != null) {
                    units[i] = unitType.getTypeName();
                }
            }
        }

        for ( int i = 0; i<zarrAxesStrings.size(); i++  ) {
            String axisString = zarrAxesStrings.get(i);
            AxesTypes axisType =  AxesTypes.getAxisType( axisString );

            String unit;
            if ( axisType == AxesTypes.SPACE ) {
                unit = units[0];
            } else if ( axisType == AxesTypes.TIME ) {
                unit = units[1];
            } else {
                unit = null;
            }

            zarrAxesList.add( new ZarrAxis( i, axisString, axisType.getTypeName(), unit) );
        }

        return zarrAxesList;
    }

    public boolean is2D() {
        return this.axes.equals(YX.axes); // XYT XYC
    }

    public boolean is5D() {
        return this.axes.equals(TCZYX.axes) || this.axes.equals(NOT_SPECIFIED.axes);
    }

    public boolean containsXYZCoordinates() {
        return this.axes.equals(CZYX.axes) || this.axes.equals(TZYX.axes) || this.axes.equals(ZYX.axes);
    }

    public boolean is4D() {
        return this.axes.equals(CZYX.axes) || this.axes.equals(TZYX.axes) || this.axes.equals(TCYX.axes);
    }

    public boolean is3DWithTimepoints() {
        return this.axes.equals(TYX.axes);
    }

    public boolean is3DWithChannels() {
        return this.axes.equals(CYX.axes);
    }

    public boolean is4DWithTimepoints() {
        return this.axes.equals(TZYX.axes);
    }

    public boolean is4DWithChannels() {
        return this.axes.equals(CZYX.axes);
    }

    // not used anymore
    public boolean is4DWithTimepointsAndChannels() {
        return this.axes.equals(TCYX.axes);
    }

    public boolean hasTimepoints() {
        return this.axes.equals(TCYX.axes) || this.axes.equals(TZYX.axes) || this.axes.equals(TYX.axes) ||
                this.axes.equals(TCZYX.axes);
    }

    public boolean hasChannels() {
        return this.axes.equals(CZYX.axes) || this.axes.equals(CYX.axes) || this.axes.equals(TCYX.axes) ||
                this.axes.equals(TCZYX.axes);
    }

    public int timeIndex() {
        List<String> zarrAxisList = getAxesList();
        return zarrAxisList.indexOf("t");
    }

    public int channelIndex() {
        List<String> zarrAxisList = getAxesList();
        return zarrAxisList.indexOf("c");
    }

    // spatial: 0,1,2 (x,y,z)
    public Map< Integer, Integer > spatialToZarr()
    {
        // TODO implement for all cases
        final HashMap< Integer, Integer > map = new HashMap<>();
        if ( this.axes.equals(TCYX.axes) )
        {
            map.put( 0, 0 );
            map.put( 1, 1 );
        }
        return map;
    }

    public boolean hasZAxis()
    {
        return spatialToZarr().size() == 3;
    }

    public int getNumDimension()
    {

    }
}
