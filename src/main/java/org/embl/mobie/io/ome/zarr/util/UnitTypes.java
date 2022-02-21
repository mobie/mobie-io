package org.embl.mobie.io.ome.zarr.util;

import lombok.extern.slf4j.Slf4j;
import ucar.units.*;

@Slf4j
public enum UnitTypes {
    ANGSTROM("angstrom"),
    ATTOMETER("attometer"),
    CENTIMETER("centimeter"),
    DECIMETER("decimeter"),
    EXAMETER("exameter"),
    FEMTOMETER("femtometer"),
    FOOT("foot"),
    GIGAMETER("gigameter"),
    HECTOMETER("hectometer"),
    INCH("inch"),
    KILOMETER("kilometer"),
    MEGAMETER("megameter"),
    METER("meter"),
    MICROMETER("micrometer"),
    MILE("mile"),
    MILLIMETER("millimeter"),
    NANOMETER("nanometer"),
    PARSEC("parsec"),
    PETAMETER("petameter"),
    PICOMETER("picometer"),
    TERAMETER("terameter"),
    YARD("yard"),
    YOCTOMETER("yoctometer"),
    YOTTAMETER("yottameter"),
    ZEPTOMETER("zeptometer"),
    ZETTAMETER("zettameter"),

    ATTOSECOND("attosecond"),
    CENTISECOND("centisecond"),
    DAY("day"),
    DECISECOND("decisecond"),
    EXASECOND("exasecond"),
    FEMTOSECOND("femtosecond"),
    GIGASECOND("gigasecond"),
    HECTOSECOND("hectosecond"),
    HOUR("hour"),
    KILOSECOND("kilosecond"),
    MEGASECOND("megasecond"),
    MICROSECOND("microsecond"),
    MILLISECOND("millisecond"),
    MINUTE("minute"),
    NANOSECOND("nanosecond"),
    PETASECOND("petasecond"),
    PICOSECOND("picosecond"),
    SECOND("second"),
    TERASECOND("terasecond"),
    YOCTOSECOND("yoctosecond"),
    YOTTASECOND("yottasecond"),
    ZEPTOSECOND("zeptosecond"),
    ZETTASECOND("zettasecond");

    private final String typeName;

    UnitTypes(String typeName) {
        this.typeName = typeName;
    }

    public static boolean contains(String test) {
        for (UnitTypes c : UnitTypes.values()) {
            if (c.typeName.equals(test)) {
                return true;
            }
        }
        return false;
    }

    public static UnitTypes convertUnit(String unit) {
        // Convert the mu symbol into "u".
        String unitString = unit.replace("\\u00B5", "u");

        try {
            UnitFormat unitFormatter = UnitFormatManager.instance();
            Unit inputUnit = unitFormatter.parse(unitString);

            for (UnitTypes unitType: UnitTypes.values()) {
                Unit zarrUnit = unitFormatter.parse(unitType.typeName);
                if (zarrUnit.getCanonicalString().equals(inputUnit.getCanonicalString())) {
                    log.info("Converted unit: " + unit + " to recommended ome-zarr unit: " + unitType.getTypeName());
                    return unitType;
                }
            }
        } catch (SpecificationException | UnitDBException | PrefixDBException | UnitSystemException e) {
            e.printStackTrace();
        }

        log.warn(unit + " is not one of the recommended units for ome-zarr");
        return null;
    }

    public String getTypeName() {
        return typeName;
    }
}
