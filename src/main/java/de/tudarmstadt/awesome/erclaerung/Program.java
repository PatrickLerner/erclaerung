package de.tudarmstadt.awesome.erclaerung;

import java.io.File;

import org.kohsuke.args4j.CmdLineParser;

import de.tudarmstadt.awesome.erclaerung.pipeline.AnalysisPipeline;

/**
 * erclaerung main entry point
 * 
 * @author Patrick Lerner
 */
public class Program {
	/**
	 * The main entry point for the program when it is launched from the command line.
	 * 
	 * @param args
	 *            the command line parameters passed into the program
	 */
	public static void main(String[] args) {
		new Program().doMain(args);
	}

	/**
	 * @see main
	 * @param args
	 *            the command line parameters passed into the program
	 */
	private void doMain(String[] args) {
		AnalysisPipeline pipeline = new AnalysisPipeline();
		File target = new File("target/");
		if (!target.exists()) {
			target.mkdir();
		}
		CmdLineParser parser = new CmdLineParser(pipeline);

		try {
			// set the pipeline up according to the parameters the user specifies
			parser.parseArgument(args);

			// now run the pipeline
			pipeline.run();

		}
		catch (Exception e) {
			System.err.println(e.getMessage());
			System.err.println("java erclaerung [options...] inputFiles...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();
			e.printStackTrace();
		}
	}
}
