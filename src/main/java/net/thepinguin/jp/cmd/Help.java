package net.thepinguin.jp.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import gnu.getopt.LongOpt;
import net.thepinguin.jp.App;

public class Help implements ICommand {
	
	private boolean _handled = false;

	public boolean canHandle(List<String> cs) {
		if (cs.size() > 1)
			return cs.get(1).equals(this.getId());
		return false;
	}
	
	public boolean isEnabled() {
		return true;
	}
	
	public boolean isHandled() {
		return _handled;
	}
	
	public String getId() {
		return "help";
	}
	
	public String getDescription(){
		return "Prints this help";
	}
	
	public int compareTo(ICommand o) {
		if(this.getId() == o.getId())
			return 0;
		else
			return 1;
	}

	public void handle(List<String> args) throws Exception {
		this.handleOpt(null);
		_handled = true;
	}
	
	public String getOptString() {
		return "h";
	}

	public LongOpt getLongOptInstance() {
		return new LongOpt("help", LongOpt.NO_ARGUMENT, null, this.getOptVowel());
	}

	public char getOptVowel() {
		return 'h';
	}

	public boolean handleOpt(String optarg) {
		List<ICommand> cmds = new ArrayList<ICommand>(App.getCommands().values());
		//TODO: fix sorting!
		Collections.sort(cmds);
			
		StringBuilder options = new StringBuilder();
		StringBuilder arguments = new StringBuilder();
		
		for (ICommand cmd : cmds) {
			if(!cmd.isEnabled())
				continue;
			// append options
			if(cmd.hasOptions()){
				StringBuilder opt = new StringBuilder();
				opt.append("    ").append(cmd.getId());
				while(opt.length() < 15){
					opt.append(" ");
				}
				opt.append(cmd.getDescription());
				options.append(opt).append("\r\n");
			}
			if(cmd.hasArguments()){
				// append arguments
				StringBuilder arg = new StringBuilder();
				if(cmd.getOptString().length() <= 0)
					continue;
				arg.append("    -").append(cmd.getOptVowel());
				while(arg.length() < 15){
					arg.append(" ");
				}
				arg.append(cmd.getDescription());
				arguments.append(arg).append("\r\n");
			}
		}
		
		System.out.println("Usage: jp [-options] [args...]");
		if(options.length() > 0){
			System.out.println("  Options:");
			System.out.print(options.toString());
		}
		if(arguments.length() > 0){
			System.out.println("  Arguments:");
			System.out.print(arguments.toString());
		}
		return true;
	}
	
	public boolean exitAfterHandleOpt() {
		return true;
	}

	public boolean hasOptions() {
		return true;
	}
	
	public boolean hasArguments() {
		return true;
	}


}