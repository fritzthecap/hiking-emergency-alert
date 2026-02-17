package fri.servers.hiking.emergencyalert.persistence;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import fri.servers.hiking.emergencyalert.persistence.entities.Alert;
import fri.servers.hiking.emergencyalert.persistence.entities.Contact;
import fri.servers.hiking.emergencyalert.persistence.entities.Day;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;

/**
 * Provides assertions for different JSON library usages.
 */
class AbstractJsonSerializationTest extends TestData
{
    protected void assertTestHike(Hike hike, Hike hikeFromJson) {
        assertNotEquals(hike.uniqueMailId, hikeFromJson.uniqueMailId);
        // every Hike instance must have its own UUID!
        
        assertEquals(hike.getAlert().getAlertIntervalMinutes(), hikeFromJson.getAlert().getAlertIntervalMinutes());
        assertEquals(hike.getPlannedBegin(), hikeFromJson.getPlannedBegin());
        
        assertEquals(hike.getDays().size(), hikeFromJson.getDays().size());
        for (int i = 0; i < hike.getDays().size(); i++) {
            final Day day = hike.getDays().get(i);
            final Day jsonDay = hikeFromJson.getDays().get(i);
            assertEquals(day.getPlannedHome(), jsonDay.getPlannedHome());
            assertEquals(day.getRoute(), jsonDay.getRoute());
            assertEquals(day.getRouteImages(), jsonDay.getRouteImages());
        }
        
        assertTestAlert(hike.getAlert(), hikeFromJson.getAlert());
    }
    
    protected void assertTestAlert(Alert alert, Alert alertFromJson) {
        assertEquals(alert.getHelpRequestIntroduction(), alertFromJson.getHelpRequestIntroduction());
        assertEquals(alert.getProcedureTodos(), alertFromJson.getProcedureTodos());
        assertEquals(alert.getPassingToNextText(), alertFromJson.getPassingToNextText());
        
        assertEquals(alert.getNameOfHiker(), alertFromJson.getNameOfHiker());
        assertEquals(alert.getAddressOfHiker(), alertFromJson.getAddressOfHiker());
        
        final List<Contact> alertContacts = alert.getAlertContacts();
        final List<Contact> alertContactsFromJson = alertFromJson.getAlertContacts();
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