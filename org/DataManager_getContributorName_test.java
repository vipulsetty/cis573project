import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class DataManager_getContributorName_test {
    @Test
	public void testSuccessfulName(){
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"success\",\"data\":\"test name\"}";
			}
			
		});	
        String name = dm.getContributorName("12345");
        String test= "test name";
        assertEquals(test,name);
	}

    @Test
	public void testNameNotFound(){
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"not found\"}";
			}
			
		});
        String name = dm.getContributorName("12345");
        assertNull(name);

	}

    @Test(expected=IllegalStateException.class)
	public void testServerError(){
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"error\",\"data\":{\"error_message\":\"test message\"}}";
			}
		});
        String name = dm.getContributorName("12345");
	}

    @Test(expected=IllegalStateException.class)
	public void testJsonException(){
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"success\":}";
			}
			
		});
        String name = dm.getContributorName("12345");
	}

}
