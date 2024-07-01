public class Donation {
	
	private String fundId;
	private String contributorName;
	private long amount;
	private String date;
	private String fundName;
	
	public Donation(String fundId, String contributorName, long amount, String date) {
		this.fundId = fundId;
		this.contributorName = contributorName;
		this.amount = amount;
		this.date = date;
	}

	public String getFundId() {
		return fundId;
	}

	public String getContributorName() {
		return contributorName;
	}

	public long getAmount() {
		return amount;
	}

	public String getDate() {
		return date;
	}

	public String getFundName() {
		return fundName;
	}

	public void setFundName(String string){
		this.fundName=string;
	}
	
	

}
