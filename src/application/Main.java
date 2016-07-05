package application;

import java.io.Console;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.LabelService;

public class Main
{
	public static void main(String[] args)
	{		
		System.out.println("CopyGitHubLabels");
		System.out.println("=================");
		System.out.println("");
		
		Scanner scanner = new Scanner(System.in);
		
		System.out.print("Please enter the username for the source repo: ");
		String sourceUsername = scanner.nextLine();		
		
		System.out.print("Enter the name of source repo: ");
		String repoName = scanner.nextLine();
		
		System.out.print("Is this a public repo? [Y/N] ");		
		boolean sourcePublicCorrect = false;
		boolean sourcePublic = false;
		while(!sourcePublicCorrect)
		{
			String isSourcePublic = scanner.nextLine();
			switch(isSourcePublic.toLowerCase())
			{
				case "y": 	sourcePublic = true;
				sourcePublicCorrect = true;
							break;
				case "n": 	sourcePublic = false;
				sourcePublicCorrect = true;
							break;
				default:	System.out.print("Please answer Yes or No");
							break;
			}
		}		
		
		String password = "";
		if(!sourcePublic)
		{
			try
			{
				Console console = System.console();
				password = new String(console.readPassword("Please enter the password for the source repo: "));
			}
			catch(NullPointerException e)
			{
				System.out.print("Please enter the password for the source repo: ");
				password = scanner.nextLine();
			}
		}		

		GitHubClient sourceClient = new GitHubClient();
		sourceClient.setCredentials(sourceUsername, password);			

		LabelService labelService = new LabelService();
		ArrayList<Label> labels = new ArrayList<>();
		try
		{
			labels = new ArrayList<Label>(labelService.getLabels(sourceClient.getUser(), repoName));
			
			System.out.print("Please enter the username for the destination repo: ");
			String destinationUsername = scanner.nextLine();		
			
			System.out.print("Enter the name of destination repo: ");
			String destRepoName = scanner.nextLine();
			
			System.out.print("Is this a public repo? [Y/N] ");		
			boolean destinationPublicCorrect = false;
			boolean destinationPublic = false;
			while(!destinationPublicCorrect)
			{
				String isSourcePublic = scanner.nextLine();
				switch(isSourcePublic.toLowerCase())
				{
					case "y": 	destinationPublic = true;
								destinationPublicCorrect = true;
								break;
					case "n": 	destinationPublic = false;
								destinationPublicCorrect = true;
								break;
					default:	System.out.print("Please answer Yes or No");
								break;
				}
			}		
			
			String destinationPassword = "";
			if(!destinationPublic)
			{
				try
				{
					Console console = System.console();
					destinationPassword = new String(console.readPassword("Please enter the password for the destination repo: "));
				}
				catch(NullPointerException e)
				{
					System.out.print("Please enter the password for the destination repo: ");
					destinationPassword = scanner.nextLine();
				}
			}	
			
			GitHubClient destinationClient = new GitHubClient();
			destinationClient.setCredentials(destinationUsername, destinationPassword);
			
			System.out.println("");
			System.out.println("_________________________________________________________________________________________________________________");
			System.out.println("");
			System.out.println("Add labels from " + sourceUsername + "/" + repoName + " to " + destinationUsername + "/" + destRepoName);
			System.out.println("=================================================================================================================");
			System.out.println("");
			
			System.out.print("Do you want do delete all existing labels in the destination repo? [Y/N] ");
			boolean correct = false;
			boolean delete = false;
			while( ! correct)
			{
				String shouldDelete = scanner.nextLine();
				switch(shouldDelete.toLowerCase())
				{
					case "y": 	delete = true;
								correct = true;
								break;
					case "n": 	delete = false;
								correct = true;
								break;
					default:	System.out.print("Please answer Yes or No");
								break;
				}
			}			
			
			if(delete)
			{
				try
				{
					deleteExistingLabels(destinationClient, destRepoName);
				}
				catch(IOException e)
				{
					System.err.println("An error occurred while deleting the labels from the destination repo!");
				}
			}

			try
			{
				insertLabels(destinationClient, destRepoName, labels);
			}
			catch(IOException e)
			{
				e.printStackTrace();
				System.err.println("An error occurred while inserting the labels into the destination repo!");
			}

			System.out.println("Successfully finished.");
		}
		catch(IOException e1)
		{
			if(e1.getMessage().contains("rate limit"))
			{
				System.err.println("You have reached the API-Limit for unauthorized requests. Try again later or authenticate with password.");
			}
			else
			{
				System.err.println("An error occurred while getting the labels from the source repo!\nCheck your credentials and make sure that you have access to the source repo (if it's not public)");
			}
		}	
		
		scanner.close();
	}

	public static void deleteExistingLabels(GitHubClient client, String repoName) throws IOException
	{
		LabelService labelService = new LabelService(client);
		ArrayList<Label> existingLabels = new ArrayList<>();
		existingLabels = new ArrayList<Label>(labelService.getLabels(client.getUser(), repoName));
		
		System.out.println("");
		System.out.println(">>> [Deleting started]");

		for(int i = 0; i < existingLabels.size(); i++)
		{
			try
			{
				System.out.println("Deleting " + i+1 + "/" + existingLabels.size() + ": " + existingLabels.get(i).getName());
				labelService.deleteLabel(client.getUser(), repoName, existingLabels.get(i).getName().replace(" ", "%20"));
			}
			catch(Exception e)
			{
				System.out.println("[ERROR] Label can't be deleted.");
			}
		}
		System.out.println(">>> [Deleting finished]");
		System.out.println("");
	}

	public static void insertLabels(GitHubClient client, String repoName, ArrayList<Label> labels) throws IOException
	{
		LabelService labelService = new LabelService(client);		

		System.out.println("");
		System.out.println(">>> [Inserting started]");
		
		for(int i = 0; i < labels.size(); i++)
		{
			try
			{
				System.out.println("Inserting " + i+1 + "/" + labels.size() + ": " + labels.get(i).getName());
				labelService.createLabel(client.getUser(), repoName, labels.get(i));
			}
			catch(RequestException e)
			{			
				System.out.println("[ERROR] Label already exists. Label will be skipped.");
			}
		}
		
		System.out.println(">>> [Inserting finished]");
		System.out.println("");
	}
}