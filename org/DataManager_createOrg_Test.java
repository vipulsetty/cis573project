import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class DataManager_createOrg_Test {

//public Boolean createOrg(String name, String login, String password, String description)

    @Test(expected=IllegalArgumentException.class)
    public void void_name_test(){
        DataManager dm = new DataManager(new WebClient("localhost", 3001));
        dm.createOrg(null,"login","password","description");
    }

    @Test(expected=IllegalArgumentException.class)
    public void void_login_test(){
        DataManager dm = new DataManager(new WebClient("localhost", 3001));
        dm.createOrg("name",null,"password","description");
    }

    @Test(expected=IllegalArgumentException.class)
    public void void_password_test(){
        DataManager dm = new DataManager(new WebClient("localhost", 3001));
        dm.createOrg("name","login",null,"description");
    }

    @Test(expected=IllegalArgumentException.class)
    public void void_description_test(){
        DataManager dm = new DataManager(new WebClient("localhost", 3001));
        dm.createOrg("name","login","password",null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void empty_name_test(){
        DataManager dm = new DataManager(new WebClient("localhost", 3001));
        dm.createOrg("","login","password","description");
    }
    @Test(expected=IllegalArgumentException.class)
    public void empty_login_test(){
        DataManager dm = new DataManager(new WebClient("localhost", 3001));
        dm.createOrg("name","","password","description");
    }

    @Test(expected=IllegalArgumentException.class)
    public void empty_password_test(){
        DataManager dm = new DataManager(new WebClient("localhost", 3001));
        dm.createOrg("name","login","","description");
    }

    @Test
    public void null_response_test(){
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				//return "{\"status\":\"success\",\"data\":{\"_id\":\"12345\",\"name\":\"new fund\",\"description\":\"this is the new fund\",\"target\":10000,\"org\":\"5678\",\"donations\":[],\"__v\":0}}";
                return null;
			}
		});
        assertFalse(dm.createOrg("name","login","password","description"));
    }

    @Test
    public void bad_format_response_test(){
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "The quick brown fox jumped over the lazy dog.";
			}
		});

        assertFalse(dm.createOrg("name","login","password","description"));
    }

    @Test
    public void error_status_test(){
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"error\",\"data\":\"none\"}";
			}
		});

        assertFalse(dm.createOrg("name","login","password","description"));
    }

    @Test
    public void success_test(){
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"success\",\"data\":\"none\"}";
			}
		});

        assertTrue(dm.createOrg("name","login","password","description"));
    }
}
