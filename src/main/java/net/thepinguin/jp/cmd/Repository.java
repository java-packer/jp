package net.thepinguin.jp.cmd;

import java.util.List;

import gnu.getopt.LongOpt;

public class Repository implements ICommand {

	private boolean _handled = false;

	public int compareTo(ICommand o) {
		if(this.getId() == o.getId())
			return 0;
		else
			return 1;
	}

	public boolean canHandle(List<String> cs) {
		if (cs.size() > 1)
			return cs.get(1).equals(this.getId());
		return false;
	}

	public boolean isHandled() {
		return _handled;
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean hasOptions() {
		return false;
	}

	public boolean hasArguments() {
		return false;
	}

	public String getId() {
		return "repo";
	}

	public String getDescription() {
		return null;
	}

	public void handle(List<String> args) throws Exception {
		String pomXml = args.get(0);
		
	}

	public LongOpt getLongOptInstance() {
		return null;
	}

	public String getOptString() {
		// TODO Auto-generated method stub
		return null;
	}

	public char getOptVowel() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean handleOpt(String optarg) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean exitAfterHandleOpt() {
		return false;
	}
	
	public boolean isCallable() {
		return this.hasOptions() || this.hasArguments();
	}

}
