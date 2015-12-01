import java.util.Map;

import nlp.util.CommandLineUtils;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Map<String, String> argMap = CommandLineUtils
				.simpleCommandLineParser(args);
		
		String basePath = ".";
		String model = "baseline";
		boolean verbose = false;
		
		if (argMap.containsKey("--path")) {
			basePath = argMap.get("-path");
		}
		
		if (argMap.containsKey("-method")) {
			model = argMap.get("-method");
		}
	}

}
