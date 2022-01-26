package org.embl.mobie.io.ome.zarr.util;

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

    public String getTypeName() {
        return typeName;
    }

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
}
