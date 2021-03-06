package net.thepinguin.jp.json.jpacker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.thepinguin.jp.Common;
import net.thepinguin.jp.Mvn;

import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;

import com.google.gson.annotations.SerializedName;

public class Dependency {
	
	private File _jarLocation;
	
	private List<String> _errorMessages = new LinkedList<String>();
	
	@SerializedName("name")
	public String name = "";
	
	@SerializedName("file")
	public String file = "";
	
	@SerializedName("github")
	public String github = "";
	
//	@SerializedName("version")
//	public String version = "";
	
	@SerializedName("commit")
	public String commit = "";
	
	@SerializedName("target")
	public String target = "";
	
	@SerializedName("scope")
	public String scope = "";
	
	@SerializedName("goal")
	public String goal = "";
	
	public String getArtifactId(){
		String[] ns = name.split("#");
		if(ns.length > 1)
			return ns[1];
		return "";
	}
	
	public String getGroupId(){
		String[] ns = name.split("#");
		if(ns.length > 0)
			return ns[0];
		return "";
//		return name.split("#")[0];
	}
	
	public String getVersion(){
		String[] ns = name.split("#");
//		System.out.println("Version" + ns);
		if(ns.length > 2)
			return ns[2];
		return "";
//		return name.split("#")[2];
	}
	
	public String getCommit(){
		if(commit.isEmpty()) commit = "HEAD";
		return commit;
	}
	
	public String getScope(){
		if(scope.isEmpty()) scope = "compile";
		return scope;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		sb.append("artifactId: `").append(this.getArtifactId()).append("`, ");
		sb.append("groupId: `").append(this.getGroupId()).append("`, ");
		sb.append("file: `").append(file).append("`, ");
		sb.append("github: `").append(github).append("`, ");
		sb.append("version: `").append(this.getVersion()).append("`, ");
		sb.append("commit: `").append(commit).append("`");
		sb.append(" ]");
		return sb.toString();
	}
	
	public String getCannonicalName(){
		if(this.isFile()){
			return " " + this.getArtifactId() + " (.jar)";
		} else if(this.isGithub()){
			return " " + this.getArtifactId() + " (" + github + ")";
 		}
		return "";
	}

	public boolean resolve() {
		if(this.isGithub()){
			try{
				File repo = this.cloneRepository();
				_jarLocation = new File(repo, target);
			} catch (Exception e){
				return false;
			}
			return true;
		} else if(this.isFile()){
			_jarLocation = new File(file);
			return true;
		}
		return false;
	}
	
	public boolean isGithub(){
		boolean https = github.startsWith("http");
		boolean fileEmpty = file.isEmpty();
		boolean targetEmpty = target.isEmpty();
		boolean goalEmpty = goal.isEmpty();
		// github specific error messages
		boolean ret = fileEmpty && !targetEmpty && !goalEmpty;
		if(ret){
			if(fileEmpty && targetEmpty) _errorMessages.add("github requires `target` key");
			if(fileEmpty && goalEmpty) _errorMessages.add("github requires `goal` key");
			if(fileEmpty && !https) _errorMessages.add("github ssh not supported (use https)");
		}
		return ret && https;
	}
	
	public boolean isBuildIn(){
		boolean fileEmpty = file.isEmpty();
		boolean targetEmpty = target.isEmpty();
		boolean nameEmpty = name.isEmpty();
		boolean versionEmpty = this.getVersion().isEmpty();
		return fileEmpty && targetEmpty && !nameEmpty && !versionEmpty;
	}
	
	public boolean isFile(){
		boolean fileEmpty = file.isEmpty();
		boolean targetEmpty = target.isEmpty();
		// jar file specific error messages
		if(!fileEmpty && !targetEmpty) _errorMessages.add("file has `target` key, ignoring");
		return !fileEmpty;
	}
	
	public File getFile(){
		return _jarLocation;
	}
	
	public File cloneRepository() throws Exception{
		String ref = this.getCommit();
		if(github.startsWith("git")){
			_errorMessages.add("cannot clone git via ssh");
			throw new Exception("cannot clone git via ssh");
		}
		String groupId = this.getGroupId();
		String artifactId = this.getArtifactId();
		String uuid = UUID.randomUUID().toString();
		groupId = "deps/" + groupId.replace('.', '/') + "/" + artifactId + "/" + this.getVersion() + "/";
		File deps = new File(Common.JP_HOME, groupId);
		if(!deps.isDirectory() && !deps.mkdirs()){
			_errorMessages.add("cannot create home: `" + deps + "`");
			throw new Exception("cannot create home: `" + deps + "`");
		}
		// set tmp clone directory
		File tmpClone = new File(deps, uuid);
		// clone repository
		Git git = Git.cloneRepository().setURI(github).setDirectory(tmpClone).call();
		Repository repo = git.getRepository();
		repo.getRef(ref);
		// get last commit, and rename folder
		String name = repo.resolve(ref).name();		
		File newClone = new File(deps, name);
		if(!newClone.exists()){
			// rename to commit ref
			tmpClone.renameTo(newClone);
		}else{
			// remove, already 
			FileUtils.deleteDirectory(tmpClone);
		}
		git.close();
		
		// build cloned repo
		List<String> goals = Arrays.asList( goal );
		Mvn.invokeMaven(new File( newClone, "pom.xml" ), goals);
		//TODO: handle return code!
		return newClone;
	}

	public boolean isValid() {
		this.reset();
		if(this.isFile() || this.isGithub() || this.isBuildIn()){
			boolean nameEmpty = name.isEmpty();
			boolean versionEmpty = this.getVersion().isEmpty();
			if(nameEmpty) _errorMessages.add("dep. requires `name` key");
			if(versionEmpty) _errorMessages.add("dep. requires `version` key");
			if(!nameEmpty && !versionEmpty)
				return true;
			else
				return false;
		}
		else
			return false;
	}

	public List<String> getErrorMessages() {
		return _errorMessages;
	}
	
	public void reset(){
		_errorMessages.clear();
	}

	public boolean deployToProjectRepo(String pomXml, String base) {
		String target = this.getFile().toString();
		List<String> goals = Arrays.asList( "deploy:deploy-file", "-Durl=file://" + base + "repo/ ",
				"-Dfile=" + target,
				"-DgroupId=" + this.getGroupId(), 
				"-DartifactId=" + this.getArtifactId(), 
				"-Dpackaging=jar",
				"-Dversion=" + this.getVersion());
		boolean invoke = Mvn.invokeMaven(new File(pomXml), goals);
		_errorMessages.addAll(Mvn.getErrorMessages());
		Mvn.resetErrorMessages();
		if(invoke)
			return true;
		else{
			return false;
		}
	}



}
