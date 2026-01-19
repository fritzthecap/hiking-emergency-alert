package fri.servers.hiking.emergencyalert.persistence;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/** Test for the library "Gson". */
class GsonSerializationTest extends AbstractJsonSerializationTest
{
    @Test
    void javaJsonRoundTripShouldWork() throws IOException {
        final Hike hike = newHike();
        
        final String uniqueMailId = hike.uniqueMailId;
        assertNotNull(uniqueMailId);
        // mail UUID must not be longer than maximum mail line length
        // else it would be broken by newline and maybe not recognized any more
        final int uuidLength = hike.uniqueMailId.length();
        assertTrue(uuidLength < 70 && uuidLength > 0);
        
        final JsonGsonSerializer<Hike> serializer = new JsonGsonSerializer<>();
        final String jsonHike = serializer.toJson(hike);
        
        System.out.println(jsonHike);
        
        final Hike hikeFromJson = serializer.fromJson(jsonHike, Hike.class);
        
        // every Hike must have its own UUID
        assertNotEquals(uniqueMailId, hikeFromJson.uniqueMailId);
        assertTestHike(hike, hikeFromJson);
    }
}