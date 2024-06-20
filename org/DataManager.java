
import java.util.ArrayList;
import java.util.Collections;
import java.nio.channels.IllegalSelectorException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;



import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class DataManager {

	private final WebClient client;
	private final Map<String, String> contributorCache; // Cache for contributor names

	public DataManager(WebClient client) {
		this.client = client;
		this.contributorCache = new HashMap<>();
	}

	public static String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for(byte b: a)
		   sb.append(String.format("%02x", b));
		return sb.toString();
	 }

	/**
	 * Attempt to log the user into an Organization account using the login and password.
	 * This method uses the /findOrgByLoginAndPassword endpoint in the API.
	 * @return an Organization object if successful; null if unsuccessful.
	 */
	public Organization attemptLogin(String login, String password) {
		if (this.client==null){
			throw new IllegalStateException("Web Client is null");
		}
		if(login==null||password==null){
			throw new IllegalArgumentException("login or password is null");
		}
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] temp = password.getBytes(StandardCharsets.UTF_8);
			String hash = byteArrayToHex(md.digest(temp));
			Map<String, Object> map = new HashMap<>();
			map.put("login", login);
			map.put("password", hash);
			String response = client.makeRequest("/findOrgByLoginAndPassword", map);
			if (response==null){
				throw new IllegalStateException("Cannot connect to server or server response is null for login");
			}

			JSONParser parser = new JSONParser();
			JSONObject json=null;
			try {
				json = (JSONObject) parser.parse(response);
			} catch (Exception e) {
				throw new IllegalStateException("JSON Response for login is not in correct format");
			}
			String status = (String)json.get("status");

			if (status.equals("success")) {
				JSONObject data = (JSONObject) json.get("data");
				String fundId = (String) data.get("_id");
				String name = (String) data.get("name");
				String description = (String) data.get("description");
				Organization org = new Organization(fundId, name, description);

				JSONArray funds = (JSONArray) data.get("funds");
				Iterator it = funds.iterator();
				while (it.hasNext()) {
					JSONObject fund = (JSONObject) it.next();
					fundId = (String) fund.get("_id");
					name = (String) fund.get("name");
					description = (String) fund.get("description");
					long target = (Long) fund.get("target");

					Fund newFund = new Fund(fundId, name, description, target);

					JSONArray donations = (JSONArray) fund.get("donations");
					List<Donation> donationList = new LinkedList<>();
					Iterator it2 = donations.iterator();
					while (it2.hasNext()) {
						JSONObject donation = (JSONObject) it2.next();
						String contributorId = (String) donation.get("contributor");
						String contributorName = this.getContributorName(contributorId);
						long amount = (Long) donation.get("amount");
						String date = (String) donation.get("date");
						donationList.add(new Donation(fundId, contributorName, amount, date));
					}

					newFund.setDonations(donationList);
					org.addFund(newFund);
				}

				return org;
			}
			else if (status.equals("login failed")){
				return null;
				
			}
			else{
				throw new IllegalStateException("Error in communicating with server during login");
			}
		}
		catch(IllegalStateException e){
			throw new IllegalStateException(e.getMessage());
		}
		catch (Exception e) {
			throw new IllegalStateException("Unknown error when logging in: "+e.getMessage());
		}
	}

	/**
	 * Look up the name of the contributor with the specified ID.
	 * This method uses the /findContributorNameById endpoint in the API.
	 * @return the name of the contributor on success; null if no contributor is found.
	 */
	public String getContributorName(String id) {
		// Check if the contributor name is already in the cache
		if (contributorCache.containsKey(id)) {
			return contributorCache.get(id);
		}
		if (this.client==null){
			throw new IllegalStateException("Web Client is null");
		}
		if(id==null){
			throw new IllegalArgumentException("id is null");
		}
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("id", id);
			String response = client.makeRequest("/findContributorNameById", map);
			if (response==null){
				throw new IllegalStateException("Cannot connect to server or server response is null for contributor name");
			}

			JSONParser parser = new JSONParser();
			JSONObject json=null;
			try {
				json = (JSONObject) parser.parse(response);
			} catch (Exception e) {
				throw new IllegalStateException("JSON Response for contributor name not in correct format");
			}
			String status = (String)json.get("status");

			if (status.equals("success")) {
				System.out.println(json);
				String name = (String)json.get("data");
        contributorCache.put(id, name);
				return name;
			}
			else if (status.equals("not found")){
				return null;
			}
			else{
				throw new IllegalStateException("Error in communicating with server when getting contributor name");
			}
		}
		catch(IllegalStateException e){
			throw new IllegalStateException(e.getMessage());
		}
		catch (Exception e) {
			throw new IllegalStateException("Unknown error when getting contributor name: "+e.getMessage());
		}
	}

	/**
	 * This method creates a new fund in the database using the /createFund endpoint in the API.
	 * @return a new Fund object if successful; null if unsuccessful.
	 */
	public Fund createFund(String orgId, String name, String description, long target) {
		if (this.client==null){
			throw new IllegalStateException("Web Client is null");
		}
		if(orgId==null||name==null||description==null){
			throw new IllegalArgumentException("orgId or name or description is null");
		}
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("orgId", orgId);
			map.put("name", name);
			map.put("description", description);
			map.put("target", target);
			String response = client.makeRequest("/createFund", map);
			if (response==null){
				throw new IllegalStateException("Cannot connect to server or server response is null");
			}

			JSONParser parser = new JSONParser();
			JSONObject json=null;
			try {
				json = (JSONObject) parser.parse(response);
			} catch (Exception e) {
				throw new IllegalStateException("JSON Response not in correct format");
			}
			String status = (String)json.get("status");

			if (status.equals("success")) {
				JSONObject fund = (JSONObject) json.get("data");
				String fundId = (String) fund.get("_id");
				return new Fund(fundId, name, description, target);
			}
			else {
				throw new IllegalStateException("Error in communicating with server when creating fund");
			}
		}
		catch(IllegalStateException e){
			throw new IllegalStateException(e.getMessage());
		}
		catch (Exception e) {
			throw new IllegalStateException("Unknown error when creating fund: "+e.getMessage());
		}
	}

	/**
	 * Calculate the total donations for a given fund.
	 * @return the total donation amount.
	 */
	public double getTotalDonationsForFund(Fund fund) {
		return fund.getDonations().stream().mapToDouble(Donation::getAmount).sum();
	}

	/**
	 * Retrieve the target amount for a given fund.
	 * @return the target amount.
	 */
	public long getTargetAmountForFund(Fund fund) {
		return fund.getTarget();
	}

	/**
	 * Retrieve all contributions across all funds for the organization.
	 * @return a list of all donations, ordered by date in descending order.
	 */
	public List<Donation> getAllContributions(Organization org) {
		List<Donation> allDonations = new ArrayList<>();

		for (Fund fund : org.getFunds()) {
			allDonations.addAll(fund.getDonations());
		}

		// Sort donations by date in descending order
		Collections.sort(allDonations, (d1, d2) -> d2.getDate().compareTo(d1.getDate()));

		return allDonations;
	}
}
