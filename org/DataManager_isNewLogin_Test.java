import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class DataManager_isNewLogin_Test {

    @Test(expected=IllegalArgumentException.class)
    public void null_login_test(){
        DataManager dm = new DataManager(new WebClient("localhost", 3001));

        dm.isNewLogin(null);
    }

    @Test
    public void blank_login_test(){
        DataManager dm = new DataManager(new WebClient("localhost", 3001));
        assertFalse(dm.isNewLogin(""));
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

        assertFalse(dm.isNewLogin("login"));

    }


    @Test
    public void bad_format_JSON_response_test(){

		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "Four score and seven years ago,";
			}
		});
        assertFalse(dm.isNewLogin("login"));
    }

    @Test
    public void status_error_test(){
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"error\",\"data\":{\"_id\":\"12345\",\"name\":\"new fund\",\"description\":\"this is the new fund\",\"target\":10000,\"org\":\"5678\",\"donations\":[],\"__v\":0}}";

			}
			
		});

        assertFalse(dm.isNewLogin("login"));        
    }


    @Test
    public void repeat_login_test(){
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"success\",\"data\":[{\"name\":\"orgname\",\"login\":\"testlogin\"}]}";

			}
			
		});

        assertFalse(dm.isNewLogin("testlogin"));     
    }

    @Test
    public void success_test(){
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"success\",\"data\":[{\"name\":\"orgname\",\"login\":\"testlogin\"}]}";

			}
			
		});

        assertTrue(dm.isNewLogin("newlogin"));     
    }



}
