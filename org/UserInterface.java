import java.text.SimpleDateFormat;
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

	public void start(String password,String login){
		start(password,login,false);
	}

	public void start(String password,String login, Boolean justCreatedNewOrganization) {
		mainloop:
		while (true) {
			System.out.println("\n\n");
			int numFunds = org.getFunds().size();
			if (numFunds > 0) {
				System.out.println("There are " + org.getFunds().size() + " funds in this organization:");

				int count = 1;
				for (Fund f : org.getFunds()) {
					System.out.println(count + ": " + f.getName());
					count++;
				}
			}
    
			while(true){
				if (!justCreatedNewOrganization){
					System.out.println("Please pick from one of the options by typing a number response: ");
					System.out.println("Enter 0 to create a new fund");
					System.out.println("Enter the fund number to see more information.");
					System.out.println("Enter "+Integer.toString(numFunds+1)+" to logout.");
					System.out.println("Enter "+Integer.toString(numFunds+2)+" to quit.");
					System.out.println("Enter -1 to list all contributions");
					System.out.println("Enter -2 to change password");
					System.out.println("Enter -3 to edict account info");
				



					String userString = in.nextLine();
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
								}
							}
						}
						else if (option>0 && option <= org.getFunds().size()){
							
							System.out.println("Type 'y' to display donations to fund " + option + " aggregated by contributor. All other input will be un-aggregated.");
							String response = in.nextLine();
							displayFund(option,response.toLowerCase().startsWith("y"));
							continue mainloop;
						}
						else if (option == -1) {
							listAllContributions();
							continue mainloop;
						}
						else if (option == -2) {
							changePassword(password,login);
							continue mainloop;
						}
						else if (option == -3) {
							editAccountInfo(password,login);
							continue mainloop;
						}
						else if (option==numFunds+1) {
							break;
						}
						else if (option==numFunds+2){
							break mainloop;
						}
						else {
							System.out.println("Invalid number option.");
						}
					}
					catch (NumberFormatException e){
						System.out.println("Please enter a number.");
					}
				}
				else{ // just created a new organization, so jump to fund creation
					while(true){
						justCreatedNewOrganization = false;
						System.out.println("Please create a fund for the new organization.");
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
						}
					}
				}
			}
			while(true){
				System.out.println("Please log in to an org.");
				System.out.println("Please enter a login:");
				login = in.nextLine();
				System.out.println("Please enter a password:");
				password = in.nextLine();
				try{
					org = dataManager.attemptLogin(login, password);
					if (org == null) {
						System.out.println("Login failed.");
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
				}
			}
		}			
	}

	public void changePassword(String correctPassword,String login){
		while (true){
			System.out.println("Please type in your current password, or click enter to exit the operation: ");
			String inputPassword = in.nextLine();
			if(inputPassword.equals("")){
				return;
			}
			if(!inputPassword.equals(correctPassword)){
				System.out.println("Incorrect current password");
				return;
			}
			System.out.println("Please type in your new password, or click enter to exit the operation.");
			String newPassword1 = in.nextLine();
			System.out.println("Please re-type in your new password, or click enter to exit the operation.");
			String newPassword2 = in.nextLine();
			if(newPassword1.equals("")|| newPassword2.equals("")){
				return;
			}
			if(!newPassword1.equals(newPassword2)){
				System.out.println("New passwords do not match");
				return;
			}
			boolean update = dataManager.changeOrgPassword(org, newPassword2, login);
			if(update){
				System.out.println("Successful update");
				break;
			}
			else{
				System.out.println("Unsuccessful update");
			}
		}
	}

	public void editAccountInfo(String correctPassword,String login){
		while (true){
			System.out.println("Please type in your current password, or click enter to exit the operation: ");
			String inputPassword = in.nextLine();
			if(inputPassword.equals("")){
				return;
			}
			if(!inputPassword.equals(correctPassword)){
				System.out.println("Incorrect current password");
				return;
			}

			System.out.println("Please type in the new organization name, or click enter to keep the existing one.");
			String name = in.nextLine();
			System.out.println("Please type in the new organizationd description, or click enter to keep the existing one.");
			String description = in.nextLine();
			if(name.equals("")){
				name = org.getName();
			}
			if(description.equals("")){
				description=org.getDescription();
			}

			boolean update = dataManager.updateAccountInfo(name,description,org.getId(), correctPassword, login);
			if(update){
				System.out.println("Successful update");
				break;
			}
			else{
				System.out.println("Unsuccessful update");
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
		//System.out.println("aggregated: "+aggregated);
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
			double totalDonations = (double)dataManager.getTotalDonationsForFund(fund);
			double targetAmount = (double)dataManager.getTargetAmountForFund(fund);
			double percentageOfTarget = totalDonations/targetAmount * 100;

			System.out.println("Total donation amount: $" + totalDonations);
			System.out.println("Percentage of target achieved: " + String.format("%.2f", percentageOfTarget) + "%");
		
	}

	public void listAllContributions() {
		List<Donation> allDonations = dataManager.getAllContributions(org);
		System.out.println("\n\nAll Contributions:");
		for (Donation donation : allDonations) {
			System.out.println("Fund: " + donation.getFundId() +", Contributor: "+donation.getContributorName()+ ", Amount: $" + donation.getAmount() + ", Date: " + formatDate(donation.getDate()));
		}
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

	
	
	public static String[] createNewOrganization(DataManager ds){
		String[] loginAndPassword = new String[2];
		Scanner input = new Scanner(System.in);
		String name;
		String login;
		String password;
		String description;

		while(true){ // get the name of the organization
			System.out.println("Name of the new organization?");
			name = input.nextLine().strip();
			if (name.equals("")){
				System.out.println("The name can't be empty.");
			}
			else
				break;	

		}
		while(true){ // get the login
			System.out.println("Login for the new organization?");
			login = input.nextLine().strip();
			if (login.equals("")){
				System.out.println("The login can't be empty.");
			}
			else{
				if (ds.isNewLogin(login)){
					break;
				}
				else{
					System.out.println("That login might be taken by an existing organization. Please try another.");
				}

			}
				
		}
		while(true){ // get the password
			System.out.println("Password for the new organization?");
			password = input.nextLine().strip();
			if (password.equals("")){
				System.out.println("The password can't be empty.");
			}
			else
				break;
		}
		while(true){ // get the description
			System.out.println("Description of the new organization? (Just one line.)");
			description = input.nextLine().strip();
			if (description.equals("")){
				System.out.println("The description can't be empty.");
			}
			else
				break;
		}

		ds.createOrg(name,login,password,description);

		loginAndPassword[0] = login;
		loginAndPassword[1] = password;
		
		return loginAndPassword;
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataManager ds = new DataManager(new WebClient("localhost", 3001));

		String login;
		String password;
		Scanner input = new Scanner(System.in);
		Organization org=null;
		Boolean justCreatedNewOrganization = false;


		if (args.length >=2){
			login = args[0];
			password = args[1];
		}
		else{
			String choice ;
			while(true){
				System.out.println("Create a new organization (N) or log in to an existing organization (L)?");
				choice = input.nextLine();
				if (choice !=""){
					if (choice.toLowerCase().startsWith("n") || choice.toLowerCase().startsWith("l") )
						break;
				}
			}
			if (choice.toLowerCase().startsWith("l")){
				System.out.println("Please enter a login:");
				login = input.nextLine();
				System.out.println("Please enter a password:");
				password = input.nextLine();		
			}
			else{ //Creating a new organization. This must have a login different from any current organization.
				String[] loginAndPassword = createNewOrganization(ds);
				login = loginAndPassword[0];
				password = loginAndPassword[1];
				justCreatedNewOrganization=true;
			}


		}


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
		ui.start(password,login,justCreatedNewOrganization);
		input.close();
		System.out.println("Good-bye!");
	}
}
