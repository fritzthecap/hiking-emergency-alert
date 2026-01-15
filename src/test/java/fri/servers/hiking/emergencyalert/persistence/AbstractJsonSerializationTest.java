package fri.servers.hiking.emergencyalert.persistence;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * Provides assertions for different JSON library usages.
 */
class AbstractJsonSerializationTest extends TestData
{
    protected Hike buildTestHike() {
        return buildHike();
    }
    
    protected void assertTestHike(Hike hike, Hike hikeFromJson) {
        assertNotEquals(hike.uniqueMailId, hikeFromJson.uniqueMailId);
        // every Hike instance must have its own UUID!
        
        assertEquals(hike.getAlertIntervalMinutes(), hikeFromJson.getAlertIntervalMinutes());
        assertEquals(hike.getPlannedBegin(), hikeFromJson.getPlannedBegin());
        assertEquals(hike.getPlannedHome(), hikeFromJson.getPlannedHome());
        assertEquals(hike.getRoute(), hikeFromJson.getRoute());
        assertEquals(hike.getRouteImages(), hikeFromJson.getRouteImages());
        
        assertTestAlert(hike.getAlert(), hikeFromJson.getAlert());
    }
    
    protected Alert buildTestAlert() {
        return buildAlert();
    }
    
    protected void assertTestAlert(Alert alert, Alert alertFromJson) {
        assertEquals(alert.getAddressOfHiker(), alertFromJson.getAddressOfHiker());
        assertEquals(alert.getHelpRequestText(), alertFromJson.getHelpRequestText());
        assertEquals(alert.getProcedureTodos(), alertFromJson.getProcedureTodos());
        assertEquals(alert.getPassingToNextText(), alertFromJson.getPassingToNextText());
        
        assertEquals(alert.getHikerContact().getFirstName(), alertFromJson.getHikerContact().getFirstName());
        assertEquals(alert.getHikerContact().getLastName(), alertFromJson.getHikerContact().getLastName());
        assertEquals(alert.getHikerContact().getMailAddress(), alertFromJson.getHikerContact().getMailAddress());
        
        final List<Contact> alertContacts = alert.getHikerContact().getAlertContacts();
        final List<Contact> alertContactsFromJson = alertFromJson.getHikerContact().getAlertContacts();
        assertEquals(alertContacts.size(), alertContactsFromJson.size());
        
        for (int i = 0; i < alertContacts.size(); i++) {
            final Contact alertContact = alertContacts.get(i);
            final Contact alertContact2 = alertContactsFromJson.get(i);
            
            assertEquals(alertContact.getFirstName(), alertContact2.getFirstName());
            assertEquals(alertContact.getLastName(), alertContact2.getLastName());
            assertEquals(alertContact.getMailAddress(), alertContact2.getMailAddress());
            assertEquals(Boolean.FALSE, alertContact.isAbsent());
        }
    }
}