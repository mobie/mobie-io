package org.embl.mobie.io.ome.zarr.util;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ZarrAxesAdapter implements JsonDeserializer<ZarrAxes>, JsonSerializer<ZarrAxes> {

    @Override
    public ZarrAxes deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        if (array.size() > 0) {
            StringBuilder axisString = new StringBuilder("[");
            for (int i = 0; i < array.size(); i++) {
                String element;
                try {
                    element = array.get(i).getAsString();
                } catch (UnsupportedOperationException e) {
                    try {
                        JsonElement jj = array.get(i);
                        element = jj.getAsJsonObject().get("name").getAsString();
                    } catch (Exception exception) {
                        throw new JsonParseException("" + e);
                    }
                }
                if (i != 0) {
                    axisString.append(",");
                }
                axisString.append("\"");
                axisString.append(element);
                axisString.append("\"");

            }
            axisString.append("]");
            return ZarrAxes.decode(axisString.toString());
        } else {
            return null;
        }
    }

    @Override
    public JsonElement serialize(ZarrAxes axes, Type typeOfSrc, JsonSerializationContext context) {
        List<String> axisList = axes.getAxesList();
        JsonArray jsonArray = new JsonArray();
        for (String axis : axisList) {
            jsonArray.add(axis);
        }
        return jsonArray;
    }
}