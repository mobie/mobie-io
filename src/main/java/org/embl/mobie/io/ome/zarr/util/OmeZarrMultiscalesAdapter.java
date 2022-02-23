package org.embl.mobie.io.ome.zarr.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class OmeZarrMultiscalesAdapter implements JsonSerializer<OmeZarrMultiscales> {

    @Override
    public JsonElement serialize(OmeZarrMultiscales src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.add("axes", context.serialize(src.zarrAxisList));
        obj.add("datasets", context.serialize(src.datasets));
        obj.add("name", context.serialize(src.name));
        obj.add("type", context.serialize(src.type));
        obj.add("version", context.serialize(src.version));
        obj.add("coordinateTransformations", context.serialize(src.coordinateTransformations));
        return obj;
    }
}