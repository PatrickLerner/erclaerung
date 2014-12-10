package de.tudarmstadt.awesome.erclaerung;

import org.kohsuke.args4j.CmdLineParser;

import de.tudarmstadt.awesome.erclaerung.pipeline.AnalysisPipeline;
import de.tudarmstadt.awesome.erclaerung.pipeline.TCPipeline;

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

		CmdLineParser parser = new CmdLineParser(pipeline);

		try {
			// set the pipeline up according to the parameters the user specifies
			parser.parseArgument(args);

			// now run the pipeline
			// pipeline.run();
			TCPipeline tcpipeline = new TCPipeline();
			tcpipeline.run();

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
