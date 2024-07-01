import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class DataManager_addDonation_Test {

    @Test
    public void testAddDonation_Success() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                // Simulate a successful response from the server
                return "{\"status\":\"success\"}";
            }
        });

        boolean result = dm.addDonation("orgId", "fundId", "contributorName", 100, "2024-07-01T12:00:00Z");

        assertTrue(result);
    }

    @Test(expected = IllegalStateException.class)
    public void testAddDonation_NullClient() {
        DataManager dm = new DataManager(null); // Simulate null WebClient

        dm.addDonation("orgId", "fundId", "contributorName", 100, "2024-07-01T12:00:00Z");
    }

    @Test(expected = IllegalStateException.class)
    public void testAddDonation_NullResponse() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                // Simulate a null response from the server
                return null;
            }
        });

        dm.addDonation("orgId", "fundId", "contributorName", 100, "2024-07-01T12:00:00Z");
    }

    @Test(expected = IllegalStateException.class)
    public void testAddDonation_ErrorResponse() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                // Simulate an error response from the server
                return "{\"status\":\"error\",\"error_message\":\"test error\"}";
            }
        });

        dm.addDonation("orgId", "fundId", "contributorName", 100, "2024-07-01T12:00:00Z");
    }

    @Test(expected = IllegalStateException.class)
    public void testAddDonation_ParseException() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                // Simulate an invalid JSON response from the server
                return "invalid JSON response";
            }
        });

        dm.addDonation("orgId", "fundId", "contributorName", 100, "2024-07-01T12:00:00Z");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDonation_NullArguments() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001));

        dm.addDonation(null, "fundId", "contributorName", 100, "2024-07-01T12:00:00Z");
    }

    @Test(expected = IllegalStateException.class)
    public void testAddDonation_UnknownError() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                throw new RuntimeException("Simulated unknown error");
            }
        });

        dm.addDonation("orgId", "fundId", "contributorName", 100, "2024-07-01T12:00:00Z");
    }
}
