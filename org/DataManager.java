import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

	/**
	 * Attempt to log the user into an Organization account using the login and password.
	 * This method uses the /findOrgByLoginAndPassword endpoint in the API.
	 * @return an Organization object if successful; null if unsuccessful.
	 */
	public Organization attemptLogin(String login, String password) {
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("login", login);
			map.put("password", password);
			String response = client.makeRequest("/findOrgByLoginAndPassword", map);

			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(response);
			String status = (String) json.get("status");

			if (status.equals("success")) {
				JSONObject data = (JSONObject) json.get("data");
				String fundId = (String) data.get("_id");
				String name = (String) data.get("name");
				String description = (String) data.get("descrption");
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
			} else return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
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

		try {
			Map<String, Object> map = new HashMap<>();
			map.put("_id", id);
			String response = client.makeRequest("/findContributorNameById", map);

			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(response);
			String status = (String) json.get("status");

			if (status.equals("success")) {
				String name = (String) json.get("data");
				// Cache the contributor name before returning it
				contributorCache.put(id, name);
				return name;
			} else return null;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * This method creates a new fund in the database using the /createFund endpoint in the API.
	 * @return a new Fund object if successful; null if unsuccessful.
	 */
	public Fund createFund(String orgId, String name, String description, long target) {
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("orgId", orgId);
			map.put("name", name);
			map.put("description", description);
			map.put("target", target);
			String response = client.makeRequest("/createFund", map);

			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(response);
			String status = (String) json.get("status");

			if (status.equals("success")) {
				JSONObject fund = (JSONObject) json.get("data");
				String fundId = (String) fund.get("_id");
				return new Fund(fundId, name, description, target);
			} else return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
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
