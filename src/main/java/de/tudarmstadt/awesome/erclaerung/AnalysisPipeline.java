package de.tudarmstadt.awesome.erclaerung;

import java.io.File;

import org.kohsuke.args4j.Argument;

/**
 * This the main pipeline which is called when an analysis is supposed to take place
 * 
 * @author Patrick Lerner
 *
 */
public class AnalysisPipeline {
	private File input;

	@Argument
	public void setInputFile(File input) {
		this.input = input;
	}

	/**
	 * This method is called once the pipeline has been properly set up to start the process
	 */
	public void run() {
		if (this.input == null)
			throw new RuntimeException("Input of pipeline is empty.");
	}
}
