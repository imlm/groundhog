package br.cin.ufpe.epona.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import br.cin.ufpe.epona.entity.ForgeProject;
import br.cin.ufpe.epona.entity.SCM;
import br.cin.ufpe.epona.http.ParamBuilder;
import br.cin.ufpe.epona.http.Requests;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;

public class SearchGoogleCode implements ForgeSearch {
	
	private static String root = "http://code.google.com";
	private static SearchGoogleCode instance;
	
	public static SearchGoogleCode getInstance() {
		if (instance == null) {
			instance = new SearchGoogleCode();
		}
		return instance;
	}
	
	private SearchGoogleCode() {
		
	}
	
	private String parseCheckoutCommand(String html) throws IOException {
		Document doc = Jsoup.parse(html);
		Elements es = doc.select("#checkoutcmd");
		if (es.isEmpty()) {
			return "";
		} else {
			return es.first().text();
		}
	}
	
	private void setCheckoutCommandToProject(String command, ForgeProject project) {
		if (command.startsWith("svn")) {
			String url = command.split(" ")[2];
			project.setSCM(SCM.SVN);
			project.setScmURL(url);
		} else if (command.startsWith("git")) {
			String url = command.split(" ")[2];
			project.setSCM(SCM.GIT);
			project.setScmURL(url);
		} else if (command.equals("")) {
			project.setSCM(SCM.NONE);
		} else {
			project.setSCM(SCM.UNKNOWN);
		}
	}
	
	public List<ForgeProject> getProjects(String term, int page) throws IOException, InterruptedException, ExecutionException {
		List<ForgeProject> projects = new ArrayList<ForgeProject>();
		String paramsStr =
			new ParamBuilder().
			addParam("q", term + " label:Java").
			addParam("start", String.valueOf((page - 1) * 10)).
			build();
		
		Document doc = Jsoup.parse(Requests.getInstance().get(root + "/hosting/search?" + paramsStr));
		for (Element tr : doc.select("#serp table tbody tr")) {
			Element a = tr.child(0).child(0);
			String projectName = a.attr("href").split("/")[2];
			String description = tr.child(1).ownText();
			String imgSrc = a.child(0).attr("src");
			String iconURL = imgSrc;
			if (imgSrc.startsWith("/")) {
				iconURL = root + iconURL;
			}
			ForgeProject forgeProject = new ForgeProject(projectName, description, iconURL);
			projects.add(forgeProject);
		}
		
		// get checkout commands for each project in parallel (asynchronously)
		List<Future<Integer>> futures = new ArrayList<Future<Integer>>();
		for (final ForgeProject forgeProject : projects) {
			String projectName = forgeProject.getName();
			String checkoutPageURL = String.format("http://code.google.com/p/%s/source/checkout", projectName);
			Future<Integer> f = Requests.getInstance().getAsync(checkoutPageURL, new AsyncCompletionHandler<Integer>() {
				@Override
				public Integer onCompleted(Response response) throws Exception {
					String command = parseCheckoutCommand(response.getResponseBody());
					setCheckoutCommandToProject(command, forgeProject);
					return response.getStatusCode();
				}
			});
			futures.add(f);
		}
		
		// wait for all futures to have all 
		for (Future<Integer> f : futures) {
			f.get();
		}
		return projects;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(SearchGoogleCode.getInstance().getProjects("", 1));
	}
	
}
