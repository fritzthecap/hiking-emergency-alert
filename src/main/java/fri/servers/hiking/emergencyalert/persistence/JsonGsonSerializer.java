package fri.servers.hiking.emergencyalert.persistence;

import java.io.IOException;
import java.io.StringWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import fri.servers.hiking.emergencyalert.util.DateUtil;

/**
 * JSON serialization and back via the small 
 * <a href="https://github.com/google/gson">GSON</a> library.
 * Mind that the serialized Java object can be an object graph,
 * but IT MUST NOT CONTAIN CYCLES (else StackOverflowError)!
 *
 * @param <T> the type of the Java-object to serialize.
 */
public class JsonGsonSerializer<T>
{
    private static final String JSON_INDENT = "  ";
    
    private Gson gson;

    /** Serialize given Java-object to a JSON string. */
    public String toJson(T object) {
        // serialize to JSON
        final Gson gson = gson();
        final JsonElement jsonElement = gson.toJsonTree(object);
        
        // force a human readable JSON string via JsonWriter
        final StringWriter stringWriter = new StringWriter();
        final JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.setIndent(JSON_INDENT);
        gson.toJson(jsonElement, jsonWriter); // string is now in JsonWriter delegate
        
        try {
            jsonWriter.close();
            stringWriter.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        return stringWriter.toString();

    }
    
    /** De-serialize a Java-object from given JSON string. */
    public T fromJson(String json, Class<? extends T> clazz) {
        final Gson gson = gson();
        return gson.fromJson(json, clazz);
    }
    
    
    private Gson gson() {
        if (gson != null)
            return gson;
        
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat(DateUtil.DATE_FORMAT_MINUTES);
        //gsonBuilder.excludeFieldsWithModifiers(Modifier.TRANSIENT); // not needed, is GSON default!
        
        return gson = gsonBuilder.create();
    }
}