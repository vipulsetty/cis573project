import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class DataManager_changeOrgPassword_Test {
    @Test
	public void testNullInputs() {
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"success\",\"data\":\"success\"}";
			}
		});
		
		
		Boolean check = dm.changeOrgPassword(null, "temp", "temp");
        Boolean check1 = dm.changeOrgPassword(new Organization("temp","temp","temp"), null, "temp");
		Boolean check2 = dm.changeOrgPassword(new Organization("temp","temp","temp"), "temp", null);

		assertFalse(check);
        assertFalse(check1);
        assertFalse(check2);
	}

    @Test
	public void testSuccessfulChange() {
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"success\",\"data\":\"success\"}";
			}
		});
		
		
		Boolean check = dm.changeOrgPassword(new Organization("temp","temp","temp"), "temp", "temp");
		
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
		
		
		Boolean check = dm.changeOrgPassword(new Organization("temp","temp","temp"), "temp", "temp");
		
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
		
		
		Boolean check = dm.changeOrgPassword(new Organization("temp","temp","temp"), "temp", "temp");
		
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
		
		
		Boolean check = dm.changeOrgPassword(new Organization("temp","temp","temp"), "temp", "temp");
		
		assertFalse(check);
	}


}
