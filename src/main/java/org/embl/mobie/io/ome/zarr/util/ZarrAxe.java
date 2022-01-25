package org.embl.mobie.io.ome.zarr.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.List;

public class ZarrAxe {
    private final int index;
    private final String name;
    private final String type;
    private String unit;

    public ZarrAxe(int index,  String name, String type, String unit) {
        this.index = index;
        this.name = name;
        this.type = type;
        this.unit = unit;
    }
    public ZarrAxe(int index,  String name, String type) {
        this.index = index;
        this.name = name;
        this.type = type;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getUnit() {
        return unit;
    }

    public static JsonElement convertToJson(List<ZarrAxe> zarrAxes) {
        StringBuilder axes = new StringBuilder();
        axes.append("[");
        for (ZarrAxe axe : zarrAxes) {
            axes.append("\"").append(axe.getName()).append("\"");
            if (axe.getIndex() < zarrAxes.size() - 1) {
                axes.append(",");
            }
        }
        axes.append("]");
        Gson gson = new Gson();
        return gson.fromJson (axes.toString(), JsonElement.class);
    }
}
