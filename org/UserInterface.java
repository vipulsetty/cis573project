import java.text.SimpleDateFormat;
import java.util.*;

public class UserInterface {

	private DataManager dataManager;
	private Organization org;
	private Scanner in = new Scanner(System.in);
	private Map<Integer, Map<String, Integer>> cachedDonationCounts;
	private Map<Integer, Map<String, Long>> cachedDonationTotals;

	public UserInterface(DataManager dataManager, Organization org) {
		this.dataManager = dataManager;
		this.org = org;
		this.cachedDonationCounts = new HashMap<>();
		this.cachedDonationTotals = new HashMap<>();
	}

	public void start(String password, String login) {
		start(password, login, false);
	}

	public void start(String password, String login, Boolean justCreatedNewOrganization) {
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

			while (true) {
				if (!justCreatedNewOrganization) {
					System.out.println("Please pick from one of the options by typing a number response: ");
					System.out.println("Enter 0 to create a new fund");
					System.out.println("Enter the fund number to see more information.");
					System.out.println("Enter " + Integer.toString(numFunds + 1) + " to logout.");
					System.out.println("Enter " + Integer.toString(numFunds + 2) + " to quit.");
					System.out.println("Enter -1 to list all contributions");
					System.out.println("Enter -2 to change password");
					System.out.println("Enter -3 to edit account info");

					String userString = in.nextLine();
					try {
						int option = Integer.parseInt(userString);
						if (option == 0) {
							while (true) {
								try {
									createFund();
									continue mainloop;
								} catch (Exception e) {
									System.out.println(e.getMessage());
									System.out.println("Type 'yes' to retry the operation, all other responses will cause the operation to end:");
									String response = in.nextLine();
									if (!response.equals("yes")) {
										continue mainloop;
									}
								}
							}
						} else if (option > 0 && option <= org.getFunds().size()) {
							System.out.println("Type 'y' to display donations to fund " + option + " aggregated by contributor. All other input will be un-aggregated.");
							String response = in.nextLine();
							displayFund(option, response.toLowerCase().startsWith("y"));
							continue mainloop;
						} else if (option == -1) {
							listAllContributions();
							continue mainloop;
						} else if (option == -2) {
							changePassword(password, login);
							continue mainloop;
						} else if (option == -3) {
							editAccountInfo(password, login);
							continue mainloop;
						} else if (option == numFunds + 1) {
							break;
						} else if (option == numFunds + 2) {
							break mainloop;
						} else {
							System.out.println("Invalid number option.");
						}
					} catch (NumberFormatException e) {
						System.out.println("Please enter a number.");
					}
				} else { // just created a new organization, so jump to fund creation
					while (true) {
						justCreatedNewOrganization = false;
						System.out.println("Please create a fund for the new organization.");
						try {
							createFund();
							continue mainloop;
						} catch (Exception e) {
							System.out.println(e.getMessage());
							System.out.println("Type 'yes' to retry the operation, all other responses will cause the operation to end:");
							String response = in.nextLine();
							if (!response.equals("yes")) {
								continue mainloop;
							}
						}
					}
				}
			}
		}
	}

	public void changePassword(String correctPassword, String login) {
		while (true) {
			System.out.println("Please type in your current password, or click enter to exit the operation: ");
			String inputPassword = in.nextLine();
			if (inputPassword.equals("")) {
				return;
			}
			if (!inputPassword.equals(correctPassword)) {
				System.out.println("Incorrect current password");
				return;
			}
			System.out.println("Please type in your new password, or click enter to exit the operation.");
			String newPassword1 = in.nextLine();
			System.out.println("Please re-type in your new password, or click enter to exit the operation.");
			String newPassword2 = in.nextLine();
			if (newPassword1.equals("") || newPassword2.equals("")) {
				return;
			}
			if (!newPassword1.equals(newPassword2)) {
				System.out.println("New passwords do not match");
				return;
			}
			boolean update = dataManager.changeOrgPassword(org, newPassword2, login);
			if (update) {
				System.out.println("Successful update");
				break;
			} else {
				System.out.println("Unsuccessful update");
			}
		}
	}

	public void editAccountInfo(String correctPassword, String login) {
		while (true) {
			System.out.println("Please type in your current password, or click enter to exit the operation: ");
			String inputPassword = in.nextLine();
			if (inputPassword.equals("")) {
				return;
			}
			if (!inputPassword.equals(correctPassword)) {
				System.out.println("Incorrect current password");
				return;
			}

			System.out.println("Please type in the new organization name, or click enter to keep the existing one.");
			String name = in.nextLine();
			System.out.println("Please type in the new organization description, or click enter to keep the existing one.");
			String description = in.nextLine();
			if (name.equals("")) {
				name = org.getName();
			}
			if (description.equals("")) {
				description = org.getDescription();
			}

			boolean update = dataManager.updateAccountInfo(name, description, org.getId(), correctPassword, login);
			if (update) {
				System.out.println("Successful update");
				break;
			} else {
				System.out.println("Unsuccessful update");
			}
		}
	}

	public void createFund() {
		System.out.print("Enter the fund name: ");
		String name;
		while (true) {
			name = in.nextLine().trim();
			if (!name.isEmpty()) {
				break;
			} else {
				System.out.println("Fund name cannot be blank. Please enter a valid name.");
			}
		}

		System.out.print("Enter the fund description: ");
		String description;
		while (true) {
			description = in.nextLine().trim();
			if (!description.isEmpty()) {
				break;
			} else {
				System.out.println("Fund description cannot be blank. Please enter a valid description.");
			}
		}

		System.out.print("Enter the fund target amount: ");
		long target = 0;
		while (true) {
			String input = in.nextLine().trim();
			try {
				target = Long.parseLong(input);
				if (target < 0) {
					System.out.println("Target amount must be a positive number.");
				} else {
					break;
				}
			} catch (NumberFormatException e) {
				System.out.println("Invalid input. Please enter a valid number.");
			}
		}

		Fund fund = dataManager.createFund(org.getId(), name, description, target);
		if (fund != null) {
			org.getFunds().add(fund);
			System.out.println("Fund created successfully.");
		} else {
			System.out.println("Failed to create fund. Please try again.");
		}
	}

	public void displayFund(int fundNumber, boolean aggregated) {
		Fund fund = org.getFunds().get(fundNumber - 1);

		System.out.println("\n\n");
		System.out.println("Here is information about this fund:");
		System.out.println("Name: " + fund.getName());
		System.out.println("Description: " + fund.getDescription());
		System.out.println("Target: $" + fund.getTarget());

		List<Donation> donations = fund.getDonations();

		if (aggregated) {
			Map<String, Integer> donationCounts = new HashMap<>();
			Map<String, Long> donationTotals = new HashMap<>();

			if (cachedDonationCounts.containsKey(fundNumber)) {
				donationCounts = cachedDonationCounts.get(fundNumber);
				donationTotals = cachedDonationTotals.get(fundNumber);
			} else {
				for (Donation d : donations) {
					String contributor = d.getContributorName();
					donationCounts.put(contributor, donationCounts.getOrDefault(contributor, 0) + 1);
					donationTotals.put(contributor, donationTotals.getOrDefault(contributor, 0L) + d.getAmount());
				}
				cachedDonationCounts.put(fundNumber, donationCounts);
				cachedDonationTotals.put(fundNumber, donationTotals);
			}

			List<String> contributorList = new ArrayList<>(donationCounts.keySet());
			final Map<String, Long> finalDonationTotals = donationTotals;
			contributorList.sort((c1, c2) -> Long.compare(finalDonationTotals.get(c2), finalDonationTotals.get(c1)));

			System.out.println("Number of donors: " + contributorList.size());
			for (String contributor : contributorList) {
				String pluralString = (donationCounts.get(contributor) == 1) ? "" : "s";
				System.out.println(contributor + " made " + donationCounts.get(contributor) + " donation" + pluralString + ", total $" + donationTotals.get(contributor) + ".");
			}
		} else {
			System.out.println("Number of donations: " + donations.size());
			for (Donation donation : donations) {
				System.out.println("* " + donation.getContributorName() + ": $" + donation.getAmount() + " on " + formatDate(donation.getDate()));
			}
		}

		double totalDonations = dataManager.getTotalDonationsForFund(fund);
		double targetAmount = fund.getTarget();
		double percentageOfTarget = (totalDonations / targetAmount) * 100;

		System.out.println("Total donation amount: $" + totalDonations);
		System.out.println("Percentage of target achieved: " + String.format("%.2f", percentageOfTarget) + "%");

		// Prompt for donation
		System.out.println("\nDo you want to make a donation to this fund? (Type 'y' for yes)");
		String response = in.nextLine();
		if (response.toLowerCase().startsWith("y")) {
			makeDonation(fund);
		}
	}


	public void makeDonation(Fund fund) {
		Scanner in = new Scanner(System.in);
		String contributorName;

		while (true) {
			try {
				System.out.println("Enter your name (contributor ID): ");
				contributorName = in.nextLine().trim();

				if (contributorName.isEmpty()) {
					System.out.println("Contributor ID cannot be blank. Please enter a valid ID.");
					continue; // Restart the loop to get a valid ID
				}

				String validatedName = dataManager.getContributorName(contributorName);
				if (validatedName != null) {
					// Valid contributor name found, proceed with donation
					break;
				} else {
					System.out.println("Contributor ID does not exist in the database. Please enter a valid ID.");
				}
			} catch (Exception e) {
				System.out.println("Please enter a valid ID.");
			}
		}

		System.out.println("Enter donation amount: ");
		long amount = 0;
		while (true) {
			String input = in.nextLine().trim();
			try {
				amount = Long.parseLong(input);
				if (amount <= 0) {
					System.out.println("Donation amount must be greater than zero.");
				} else {
					break;
				}
			} catch (NumberFormatException e) {
				System.out.println("Invalid input. Please enter a valid number.");
			}
		}

		// Once valid contributor name and amount are obtained, proceed with donation
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		String currentDate = sdf.format(new Date()); // Assuming you have a method to get the current date
		boolean donationAdded = dataManager.addDonation(fund.getId(), fund.getId(), contributorName, amount, currentDate);

		if (donationAdded) {
			System.out.println("Donation added successfully!");

			// Update local state with the new donation
			Donation newDonation = new Donation(fund.getId(), dataManager.getContributorName(contributorName), amount, currentDate);
			fund.addDonation(newDonation); // Assuming Fund class has a method to add a donation

			// Display updated donations for the fund
			List<Donation> allDonations = fund.getDonations();
			System.out.println("All donations for fund " + fund.getName() + ":");
			for (Donation donation : allDonations) {
				System.out.println(donation.getContributorName() + ": $" + donation.getAmount() + " on " + donation.getDate());
			}
		} else {
			System.out.println("Failed to add donation. Please try again.");
		}
	}







	public void listAllContributions() {
		List<Donation> allDonations = dataManager.getAllContributions(org);
		System.out.println("\n\nAll Contributions:");
		for (Donation donation : allDonations) {
			System.out.println("Fund: " + donation.getFundId() + ", Contributor: " + donation.getContributorName() + ", Amount: $" + donation.getAmount() + ", Date: " + formatDate(donation.getDate()));
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

	public static String[] createNewOrganization(DataManager ds) {
		String[] loginAndPassword = new String[2];
		Scanner input = new Scanner(System.in);
		String name;
		String login;
		String password;
		String description;

		while (true) { // get the name of the organization
			System.out.println("Name of the new organization?");
			name = input.nextLine().strip();
			if (!name.isEmpty()) {
				break;
			} else {
				System.out.println("The name can't be empty.");
			}
		}
		while (true) { // get the login
			System.out.println("Login for the new organization?");
			login = input.nextLine().strip();
			if (!login.isEmpty()) {
				if (ds.isNewLogin(login)) {
					break;
				} else {
					System.out.println("That login might be taken by an existing organization. Please try another.");
				}
			} else {
				System.out.println("The login can't be empty.");
			}
		}
		while (true) { // get the password
			System.out.println("Password for the new organization?");
			password = input.nextLine().strip();
			if (!password.isEmpty()) {
				break;
			} else {
				System.out.println("The password can't be empty.");
			}
		}
		while (true) { // get the description
			System.out.println("Description of the new organization? (Just one line.)");
			description = input.nextLine().strip();
			if (!description.isEmpty()) {
				break;
			} else {
				System.out.println("The description can't be empty.");
			}
		}

		ds.createOrg(name, login, password, description);

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
		Organization org = null;
		Boolean justCreatedNewOrganization = false;

		if (args.length >= 2) {
			login = args[0];
			password = args[1];
		} else {
			String choice;
			while (true) {
				System.out.println("Create a new organization (N) or log in to an existing organization (L)?");
				choice = input.nextLine();
				if (!choice.isEmpty()) {
					if (choice.toLowerCase().startsWith("n") || choice.toLowerCase().startsWith("l")) {
						break;
					}
				}
			}
			if (choice.toLowerCase().startsWith("l")) {
				System.out.println("Please enter a login:");
				login = input.nextLine();
				System.out.println("Please enter a password:");
				password = input.nextLine();
			} else { // Creating a new organization. This must have a login different from any current organization.
				String[] loginAndPassword = createNewOrganization(ds);
				login = loginAndPassword[0];
				password = loginAndPassword[1];
				justCreatedNewOrganization = true;
			}
		}

		while (true) {
			try {
				org = ds.attemptLogin(login, password);
				if (org == null) {
					System.out.println("Login failed.");
					System.out.println("Please enter a login:");
					login = input.nextLine();
					System.out.println("Please enter a password:");
					password = input.nextLine();
				} else {
					break;
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println("Type 'yes' to retry the operation, all other responses will cause the operation to end:");
				String response = input.nextLine();
				if (!response.equals("yes")) {
					System.exit(0);
				}
				System.out.println("Please enter a login:");
				login = input.nextLine();
				System.out.println("Please enter a password:");
				password = input.nextLine();
			}
		}
		UserInterface ui = new UserInterface(ds, org);
		ui.start(password, login, justCreatedNewOrganization);
		input.close();
		System.out.println("Good-bye!");
	}
}
