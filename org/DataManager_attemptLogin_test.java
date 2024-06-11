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
		assertEquals(o.getDescription(),"this is the new org");
		assertEquals(o.getId(),"12345");
		assertEquals(o.getName(),"new org");
		assertTrue(o.getFunds().isEmpty());
	}

	@Test
	public void testUnsuccessfulLogin(){
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"login failed\"}";
			}
			
		});

		Organization o = dm.attemptLogin("test","test");
		
		assertNull(o);
	}

	@Test
	public void testBadServerLogin(){
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"error\",\"data\":{\"error_message\":\"test message\"}}";
			}
		});

		boolean error=false;
		try {
		Organization o = dm.attemptLogin("test","test");
		}
		catch(IllegalStateException e){
			error=true;
		}
		assertTrue(error);
	}

	@Test
	public void testSuccessfulLoginWithFunds(){
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"success\",\"data\":{\"_id\":\"12345\",\"name\":\"new org\",\"description\":\"this is the new org\",\"funds\":[{\"_id\":\"12345\",\"name\":\"new fund\",\"description\":\"this is new fund \",\"target\":10000,\"donations\":[{\"_id\":\"12345\",\"contributor\":\"test\",\"amount\":100,\"date\":\"2024-05-01\"}]}]}}";
			}
			
		});

		Organization o = dm.attemptLogin("test","test");
		
		assertNotNull(o);
		assertEquals(o.getDescription(),"this is the new org");
		assertEquals(o.getId(),"12345");
		assertEquals(o.getName(),"new org");
		assertEquals(1,o.getFunds().size());
		assertEquals(1,o.getFunds().get(0).getDonations().size());
	}

	@Test
	public void testException(){
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"success\"}";
			}
		});
		
		
		Organization o = dm.attemptLogin("test","test");

		assertNull(o);
	}
}