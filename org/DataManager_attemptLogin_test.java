import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class DataManager_attemptLogin_test {
    @Test
	public void testSuccessfulLogin() {

		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"success\",\"data\":{\"_id\":\"12345\",\"name\":\"new org\",\"description\":\"this is the new org\",\"funds\":[]}}";

			}
			
		});
		
		
		Organization o = dm.attemptLogin("test","test");
		
		assertNotNull(o);
		
	}
}
