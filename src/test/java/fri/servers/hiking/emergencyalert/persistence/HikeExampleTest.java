package fri.servers.hiking.emergencyalert.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.junit.jupiter.api.Test;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;

class HikeExampleTest
{
    @Test
    void hikeExampleSerializationShouldWork() throws IOException {
        final InputStream jsonStream = getClass().getResourceAsStream("HikeExample.json");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(jsonStream));
        
        final StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            sb.append(line+"\n");
        
        final String json1 = sb.toString().trim();
        final JsonGsonSerializer<Hike> serializer = new JsonGsonSerializer<>();
        final Hike hike = serializer.fromJson(json1, Hike.class);
        
        assertEquals("From Mount Everest to Kilimanjaro via Antarctica.", hike.getRoute());
        
        final String json2 = serializer.toJson(hike);
        assertEquals(json1, json2);
    }
}