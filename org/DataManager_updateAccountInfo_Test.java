import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class DataManager_updateAccountInfo_Test {
    @Test
	public void testNullInputs() {
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"success\",\"data\":\"success\"}";
			}
		});
		
		
		Boolean check = dm.updateAccountInfo(null, "temp", "temp", "temp", "temp");
        Boolean check1 = dm.updateAccountInfo("temp", null, "temp", "temp", "temp");
        Boolean check2 = dm.updateAccountInfo("temp", "temp", null, "temp", "temp");
        Boolean check3 = dm.updateAccountInfo("temp", "temp", "temp", null, "temp");
        Boolean check4 = dm.updateAccountInfo("temp", "temp", "temp", "temp", null);


		assertFalse(check);
        assertFalse(check1);
        assertFalse(check2);
        assertFalse(check3);
        assertFalse(check4);
	}

    @Test
	public void testSuccessfulChange() {
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"success\",\"data\":\"success\"}";
			}
		});
		
		
		Boolean check = dm.updateAccountInfo("temp", "temp", "temp", "temp", "temp");
		
		assertTrue(check);
	}

    @Test
	public void testBadJson() {
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{";
			}
		});
		
		
		Boolean check = dm.updateAccountInfo("temp", "temp", "temp", "temp", "temp");
		
		assertFalse(check);
	}

    @Test
	public void testBadServerResponse() {
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return null;
			}
		});
		
		
		Boolean check = dm.updateAccountInfo("temp", "temp", "temp", "temp", "temp");
		
		assertFalse(check);
	}

    @Test
	public void testServerError() {
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"error\",\"data\":\"error\"}";
			}
		});
		
		
		Boolean check = dm.updateAccountInfo("temp", "temp", "temp", "temp", "temp");
		
		assertFalse(check);
	}
}
