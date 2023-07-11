/*-
 * #%L
 * Readers and writers for image data in MoBIE projects
 * %%
 * Copyright (C) 2021 - 2023 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.embl.mobie.io;

import bdv.util.AxisOrder;
import com.google.common.collect.Lists;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;
import org.embl.mobie.io.ome.zarr.util.AxesTypes;
import org.embl.mobie.io.ome.zarr.util.UnitTypes;
import org.embl.mobie.io.ome.zarr.util.ZarrAxis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Axes
{
    public static final String C = "c";
    public static final String Z = "z";
    public static final String T = "t";

    public static < T > ArrayList< RandomAccessibleInterval< T > > getChannels(
            final RandomAccessibleInterval< T > rai, List< String > axes )
    {
        if ( rai.numDimensions() != axes.size() )
            throw new IllegalArgumentException( "provided Axes doesn't match dimensionality of image" );

        final ArrayList< RandomAccessibleInterval< T > > sourceStacks = new ArrayList< >();

        /*
         * If there a channels dimension, slice img along that dimension.
         */
        final int c = axes.indexOf( C );
        if ( c != -1 )
        {
            final int numSlices = ( int ) rai.dimension( c );
            for ( int s = 0; s < numSlices; ++s )
                sourceStacks.add( Views.hyperSlice( rai, c, s + rai.min( c ) ) );
        }
        else
            sourceStacks.add( rai );

        /*
         * If AxisOrder is a 2D variant (has no Z dimension), augment the
         * sourceStacks by a Z dimension.
         */
        final boolean addZ = !axes.contains( Z );
        if ( addZ )
            for ( int i = 0; i < sourceStacks.size(); ++i )
                sourceStacks.set( i, Views.addDimension( sourceStacks.get( i ), 0, 0 ) );

        /*
         * If at this point the dim order is XYTZ, permute to XYZT
         */
        final boolean flipZ = !axes.contains( Z ) && axes.contains( T );
        if ( flipZ )
            for ( int i = 0; i < sourceStacks.size(); ++i )
                sourceStacks.set( i, Views.permute( sourceStacks.get( i ), 2, 3 ) );

        return sourceStacks;
    }


//
//    public List<String> getAxesList() {
//        String pattern = "([a-z])";
//        List<String> allMatches = new ArrayList<>();
//        Matcher m = Pattern.compile(pattern)
//            .matcher(axes);
//        while (m.find()) {
//            allMatches.add(m.group());
//        }
//        return allMatches;
//    }
//
//    public List< ZarrAxis > toAxesList( String spaceUnit, String timeUnit) {
//        List<ZarrAxis> zarrAxesList = new ArrayList<>();
//        List<String> zarrAxesStrings = getAxesList();
//
//        String[] units = new String[]{spaceUnit, timeUnit};
//
//        // convert to valid ome-zarr units, if possible, otherwise just go ahead with
//        // given unit
//        for (int i = 0; i < units.length; i++) {
//            String unit = units[i];
//            if (!UnitTypes.contains(unit)) {
//                UnitTypes unitType = UnitTypes.convertUnit(unit);
//                if (unitType != null) {
//                    units[i] = unitType.getTypeName();
//                }
//            }
//        }
//
//        for (int i = 0; i < zarrAxesStrings.size(); i++) {
//            String axisString = zarrAxesStrings.get(i);
//            AxesTypes axisType = AxesTypes.getAxisType(axisString);
//
//            String unit;
//            if (axisType == AxesTypes.SPACE) {
//                unit = units[0];
//            } else if (axisType == AxesTypes.TIME) {
//                unit = units[1];
//            } else {
//                unit = null;
//            }
//
//            zarrAxesList.add(new ZarrAxis(i, axisString, axisType.getTypeName(), unit));
//        }
//
//        return zarrAxesList;
//    }
//
//    public boolean hasTimepoints() {
//        return this.axes.equals(TCYX.axes) || this.axes.equals(TZYX.axes) || this.axes.equals(TYX.axes) || this.axes.equals(TCZYX.axes);
//    }
//
//    public boolean hasChannels() {
//        return this.axes.equals(CZYX.axes) || this.axes.equals(CYX.axes) || this.axes.equals(TCYX.axes) || this.axes.equals(TCZYX.axes);
//    }
//
//    // the flag reverseAxes determines whether the index will be given w.r.t.
//    // reversedAxes=true corresponds to the java/bdv axis convention
//    // reversedAxes=false corresponds to the zarr axis convention
//    public int axisIndex(String axisName, boolean reverseAxes) {
//        if(reverseAxes) {
//            List<String> reverseAxesList = Lists.reverse(getAxesList());
//            return reverseAxesList.indexOf(axisName);
//        }
//        return getAxesList().indexOf(axisName);
//    }
//
//    public int timeIndex() {
//        return axisIndex("t", true);
//    }
//
//    public int channelIndex() {
//        return axisIndex("c", true);
//    }
//
//    // spatial: 0,1,2 (x,y,z)
//    public Map<Integer, Integer> spatialToArray() {
//        final HashMap<Integer, Integer> map = new HashMap<>();
//        map.put(0, 0);
//        map.put(1, 1);
//        if (hasZAxis()) {
//            map.put(2, 2);
//        }
//        return map;
//    }
//
//    public boolean hasZAxis() {
//        return this.axes.equals(TCZYX.axes) || this.axes.equals(CZYX.axes) || this.axes.equals(TZYX.axes) || this.axes.equals(ZYX.axes);
//    }
//
//    public int getNumDimension() {
//        return getAxesList().size();
//    }
}
