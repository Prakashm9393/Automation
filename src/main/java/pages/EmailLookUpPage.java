package pages;

import wrappers.ApplicationWrapper;

public class EmailLookUpPage extends ApplicationWrapper{
	
	public EmailLookUpPage(){
		if(!verifyTitleOfThePage("Email Lookup, Checker, Verifier | Check Email Address For Free")){
			reportStep("This is not a Email Lookup Page", "Pass");
		}		
	}
	
	public EmailLookUpPage enterEmailAddress(String emailId){
		scrollToTheGivenWebElement("id&txtOne");
		enterText("id&txtOne", emailId);
		return this;
	}
	
	public EmailLookUpPage clickOnTheVerifyEmailButton(){
		clickOn("classname&inputinyext");
		return this;
	}	
	

}
