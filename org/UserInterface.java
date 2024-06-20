import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.*;

public class UserInterface {

	private DataManager dataManager;
	private Organization org;
	private Scanner in = new Scanner(System.in);
	private Map<Integer,Map<String,Integer>> cachedDonationCounts; // If funds are ever renumbered (such as if a fund is deleted), this cache should be invalidated.
	private Map<Integer,Map<String,Long>> cachedDonationTotals; // If funds are ever renumbered (such as if a fund is deleted), this cache should be invalidated.
	public UserInterface(DataManager dataManager, Organization org) {
		this.dataManager = dataManager;
		this.org = org;
		this.cachedDonationCounts = new HashMap<>();
		this.cachedDonationTotals = new HashMap<>();
	}

	public void start() {
		mainloop:
		while (true) {
			System.out.println("\n\n");
			if (org.getFunds().size() > 0) {
				System.out.println("There are " + org.getFunds().size() + " funds in this organization:");

				int count = 1;
				for (Fund f : org.getFunds()) {
					System.out.println(count + ": " + f.getName());
					count++;
				}
				System.out.println("Enter the fund number to see more information.");
			}
			System.out.println("Enter 0 to create a new fund, 'logout' to log back in as the same or different org,or 'q' to quit.");

			while(true){
				String userString = in.nextLine();
				if (userString.equals("q") || userString.equals("quit")){
					break mainloop;
				}
				if (userString.equals("logout")){
					break;
				}
				try{
					int option = Integer.parseInt(userString);
					if (option == 0) {
						while(true){
							try {
								createFund();
								continue mainloop;
							} 
							catch (Exception e) {
								System.out.println(e.getMessage());
								System.out.println("Type 'yes' to retry the operation, all other responses will cause the operation to end:");
								String response = in.nextLine();
									if(!response.equals("yes")){
										continue mainloop;
									}
								continue;
							}
						}
					}
					else if (option>0 && option <= org.getFunds().size()){
						
						System.out.println("Display donations to fund " + option + " aggregated by contributor?");
						String response = in.nextLine();
						displayFund(option,response.toLowerCase().startsWith("y"));
						continue mainloop;
	
					}
					else {
						System.out.println("Please enter a number of a fund, 0 to create a fund, 'logout' to log back in as the same or different org,or 'q' to quit.");
					}
				}
				catch (NumberFormatException e){
					System.out.println("Please enter a number, 'logout' to logout, or 'q' to quit. ");
				}
			}
			while(true){
				System.out.println("Please log in to an org.");
				System.out.println("Please enter a login:");
				String login = in.nextLine();
				System.out.println("Please enter a password:");
				String password = in.nextLine();
				try{
					org = dataManager.attemptLogin(login, password);
					if (org == null) {
						System.out.println("Login failed.");
						continue;
					}
					else{
						break;
					}
				}
				catch(Exception e){
					System.out.println(e.getMessage());
					System.out.println("Type 'yes' to retry the operation, all other responses will cause the operation to end and program to terminate:");
					String response = in.nextLine();
					if(!response.equals("yes")){
						break mainloop;
					}
					continue;
				}
			}
		}			
			
	}

	public void createFund() {
		System.out.print("Enter the fund name: ");
		String name;
		while (true){
			name = in.nextLine().trim();
			if (name.equals("")){
				System.out.println("The fund name can't be blank. Please enter the fund name. ");
			}
			else{
				break;
			}
		}
		
		System.out.print("Enter the fund description: ");
		String description;
		while (true){
			description = in.nextLine().trim();
			if (description.equals("")){
				System.out.println("The description can't be blank. Please enter the description. ");
			}
			else{
				break;
			}
		}

		System.out.print("Enter the fund target: ");
		long target;
		while(true){
			String userString = in.nextLine();
			try{
				target = Integer.parseInt(userString);
				if (target < 0){
					System.out.println("The target must be positive.");
				}
				else {
					break;
				}

			}
			catch (NumberFormatException e){
				System.out.println("Please enter an integer.");
			} 			

		}


		// long target = in.nextInt();
		// in.nextLine();

		Fund fund = dataManager.createFund(org.getId(), name, description, target);
		org.getFunds().add(fund);
	}

	public void displayFund(int fundNumber,boolean aggregated) {
		System.out.println("aggregated: "+aggregated);
		Fund fund = org.getFunds().get(fundNumber - 1);

		System.out.println("\n\n");
		System.out.println("Here is information about this fund:");
		System.out.println("Name: " + fund.getName());
		System.out.println("Description: " + fund.getDescription());
		System.out.println("Target: $" + fund.getTarget());
		List<Donation> donations = fund.getDonations();

		// for testing purposes, make a nonempty list of donations
		// donations = new ArrayList<>();
		// donations.add(new Donation("ID","Bert",100,"July 01, 1976"));
		// donations.add(new Donation("ID","Ernie",150,"07 01 1976"));	
		// donations.add(new Donation("ID","Bert",100,"07-02-2024"));
		// end creation of fake donations


		if (aggregated){

			// compute aggregation
			Map<String,Integer> donationCounts = new HashMap<>();
			Map<String,Long> donationTotals = new HashMap<>();

			// Check if these are in the cache. If not, compute them.
			if (cachedDonationCounts.containsKey(fundNumber)){
				donationCounts = cachedDonationCounts.get(fundNumber);
				donationTotals = cachedDonationTotals.get(fundNumber);
			}
			else{
				for (Donation d: donations){
					String contributor = d.getContributorName();
					if (!donationCounts.containsKey(contributor)){
						donationCounts.put(contributor,1);
						donationTotals.put(contributor,d.getAmount());
					}
					else{
						donationCounts.put(contributor,donationCounts.get(contributor)+1);
						donationTotals.put(contributor,donationTotals.get(contributor)+d.getAmount());
					}

				}
				cachedDonationCounts.put(fundNumber,donationCounts);
				cachedDonationTotals.put(fundNumber,donationTotals);

			} // end else 
			
			List<String> contributorList = new ArrayList<String>(donationCounts.keySet());
			// Sort the list

			final Map<String,Long> fDonationTotals = donationTotals;
			Collections.sort(contributorList, (c1,c2) -> -fDonationTotals.get(c1).compareTo(fDonationTotals.get(c2))); // - to sort in descending order, opposite Long's natural order


			System.out.println("Number of donors: " + contributorList.size());
			//Print out the list
			for (String contributor:contributorList){
				String pluralString = "s";
				if (donationCounts.get(contributor) == 1) pluralString = "";

				System.out.println(contributor + " made " + donationCounts.get(contributor) + " donation" + pluralString +", total $"+donationTotals.get(contributor)+".");

			}



		}
		else{

			System.out.println("Number of donations: " + donations.size());
			for (Donation donation : donations) {
				System.out.println("* " + donation.getContributorName() + ": $" + donation.getAmount() + " on " + formatDate(donation.getDate()));
			}
		}

			// Calculate and display total donations and percentage of target
			long totalDonations = (long)dataManager.getTotalDonationsForFund(fund);
			long targetAmount = dataManager.getTargetAmountForFund(fund);
			double percentageOfTarget = (targetAmount > 0) ? (totalDonations / targetAmount) * 100 : 0;

			System.out.println("Total donation amount: $" + totalDonations);
			System.out.println("Percentage of target achieved: " + String.format("%.2f", percentageOfTarget) + "%");

			System.out.println("Press the Enter key to go back to the listing of funds");
			in.nextLine();
		
	}

	private String formatDate(String dateString) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			Date date = sdf.parse(dateString);
			sdf.applyPattern("MMMM dd, yyyy");
			return sdf.format(date);
		} catch (Exception e) {
			e.printStackTrace();
			return dateString; // return original string in case of error
		}
	}

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataManager ds = new DataManager(new WebClient("localhost", 3001));

		String login = args[0];
		String password = args[1];
		Organization org=null;
		Scanner input = new Scanner(System.in);

		while(true){
			try{
				org = ds.attemptLogin(login, password);
				if (org == null) {
					System.out.println("Login failed.");
					System.out.println("Please enter a login:");
					login = input.nextLine();
					System.out.println("Please enter a password:");
					password = input.nextLine();
				}
				else{
					break;
				}
			}
			catch(Exception e){
				System.out.println(e.getMessage());
				System.out.println("Type 'yes' to retry the operation, all other responses will cause the operation to end:");
				String response = input.nextLine();
				if(!response.equals("yes")){
					System.exit(0);
				}
				System.out.println("Please enter a login:");
				login = input.nextLine();
				System.out.println("Please enter a password:");
				password = input.nextLine();
			}
		}
		UserInterface ui = new UserInterface(ds, org);
		ui.start();
		System.out.println("Good-bye!");
	}
}
