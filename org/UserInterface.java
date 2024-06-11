import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class UserInterface {

	private DataManager dataManager;
	private Organization org;
	private Scanner in = new Scanner(System.in);

	public UserInterface(DataManager dataManager, Organization org) {
		this.dataManager = dataManager;
		this.org = org;
	}

	public void start() {

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
			System.out.println("Enter 0 to create a new fund");

			while(true){
				String userString = in.nextLine();
				if (userString.equals("q") || userString.equals("quit")){
					System.out.println("Good-bye!");
					System.exit(0);
				}
				try{
					int option = Integer.parseInt(userString);
					if (option == 0) {
						createFund();
						break;
					}
					else if (option>0 && option <= org.getFunds().size()){
						displayFund(option);
						break;
	
					}
					else {
						System.out.println("Please enter a number of a fund, 0 to create a fund, or q to quit.");
	
					}
				}
				catch (NumberFormatException e){
					System.out.println("Please enter a number or q to quit. ");
				}
	


			}





			// int option = in.nextInt();
			// in.nextLine();
			// if (option == 0) {
			// 	createFund(); 
			// }
			// else {
			// 	displayFund(option);
			// }
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

	public void displayFund(int fundNumber) {
		Fund fund = org.getFunds().get(fundNumber - 1);

		System.out.println("\n\n");
		System.out.println("Here is information about this fund:");
		System.out.println("Name: " + fund.getName());
		System.out.println("Description: " + fund.getDescription());
		System.out.println("Target: $" + fund.getTarget());

		List<Donation> donations = fund.getDonations();
		System.out.println("Number of donations: " + donations.size());
		for (Donation donation : donations) {
			System.out.println("* " + donation.getContributorName() + ": $" + donation.getAmount() + " on " + formatDate(donation.getDate()));
		}

		// Calculate and display total donations and percentage of target
		double totalDonations = dataManager.getTotalDonationsForFund(fund);
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
		//System.out.println(login + " " + password);
		try {
			Organization org = ds.attemptLogin(login, password);
			
			if (org == null) {
				System.out.println("Login failed.");
			}
			else {
	
				UserInterface ui = new UserInterface(ds, org);
			
				ui.start();
			
			}
		}
		catch(IllegalStateException e){
			System.out.println("Error in Communicating with server");	
		}
	}
}
