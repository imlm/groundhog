package br.ufpe.cin.groundhog.search;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import br.ufpe.cin.groundhog.Commit;
import br.ufpe.cin.groundhog.Issue;
import br.ufpe.cin.groundhog.Language;
import br.ufpe.cin.groundhog.Project;
import br.ufpe.cin.groundhog.Release;
import br.ufpe.cin.groundhog.SCM;
import br.ufpe.cin.groundhog.User;
import br.ufpe.cin.groundhog.http.HttpModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class SearchGitHubTest {
	
	private SearchGitHub searchGitHub;
	private Project fakeProject;

	@Before
	public void setup() {
		Injector injector = Guice.createInjector(new SearchModule(), new HttpModule());
		searchGitHub = injector.getInstance(SearchGitHub.class);
		User user = new User("elixir-lang");
		fakeProject = new Project("elixir", "", SCM.GIT, "git@github.com:elixir-lang/elixir.git");
		fakeProject.setUser(user);
	}
	
	@Test
	public void testSearchByProjectName() {
		try {			
			List<Project> projects = searchGitHub.getProjects("groundhog", 1, SearchGitHub.INFINITY);
			searchGitHub.getProjectLanguages(projects.get(0));
			Assert.assertNotNull(projects);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testFetchByProjectLanguages() {
		try {
			List<Language> langs = searchGitHub.getProjectLanguages(fakeProject);
			Assert.assertNotNull(langs);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testGetAllProjects() {
		try {
			List<Project> projects = searchGitHub.getAllProjects(0, 5);
			Assert.assertNotNull(projects);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testGetProjectsByLanguage() {
		try {
			List<Project> projects = searchGitHub.getAllProjectsByLanguage("java");
			Assert.assertNotNull(projects);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testGetAllProjectCommits() {
		try {
			User u = new User("vkostyukov");
			Project project = new Project(u, "la4j");
					
			List<Commit> commits = searchGitHub.getAllProjectCommits(project);
			Assert.assertNotNull(commits);	
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testGetAllProjectContributors() {
		try {
			
			Project project = new Project("twitter", "ambrose");
			List<User> contributors = searchGitHub.getAllProjectContributors(project);
						
			Assert.assertNotNull(contributors);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testGetAllProjectReleases() {
		try {
			User u = new User("twbs");
			Project project = new Project(u, "bootstrap");
			
			List<Release> releases = searchGitHub.getAllProjectReleases(project);
						
			Assert.assertNotNull(releases);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testGetProjectIssues(){
		try {
			User u = new User("spgroup");
			Project project = new Project(u, "groundhog");
			
			List<Issue> projectIssues = searchGitHub.getProjectIssues(project, 2);
						
			Assert.assertNotNull(projectIssues);
			// The default number of items per page is 30 so for 2 pages we should have 60 items
			// As of 01-Jan-2014 the number of issues for project spgroup/groundhog is 315 so it
			// should be guaranteed to have 2 pages at least.
			Assert.assertEquals(projectIssues.size(), 60);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	// Note that in the future this test may have to be reworked as the number of issues in the
	// target project grows
	@Test
	public void testGetAllProjectIssues(){
		try {
			User u = new User("spgroup");
			Project project = new Project(u, "groundhog");
			
			List<Issue> projectIssues = searchGitHub.getAllProjectIssues(project);
						
			Assert.assertNotNull(projectIssues);
			// As of 01-Jan-2014 the number of issues for project spgroup/groundhog is 315 and 
			// this number can only grow
			Assert.assertTrue(projectIssues.size() >= 315);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
}