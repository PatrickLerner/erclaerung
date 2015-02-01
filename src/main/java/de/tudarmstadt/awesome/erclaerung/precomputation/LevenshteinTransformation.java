package de.tudarmstadt.awesome.erclaerung.precomputation;

import java.util.List;

public class LevenshteinTransformation {
	private String string1;
	private String string2;
	private List<LevenshteinStep> steps;

	public LevenshteinTransformation(String sourceString, String destString, List<LevenshteinStep> steps) {
		this.string1 = sourceString;
		this.string2 = destString;
		this.steps = steps;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.string1 + "->" + this.string2 + ":");
		for (LevenshteinStep levenshteinStep : steps) {
			sb.append("\n" + levenshteinStep.toString());
		}
		return sb.toString();
	}

	public String toStringWithTransformation() {
		StringBuilder sb = new StringBuilder();
		String string = string1;
		sb.append(this.string1 + "->" + this.string2 + ":");
		int modifier = 0;
		for (LevenshteinStep levenshteinStep : steps) {
			sb.append("\n" + levenshteinStep.toString());
			string = levenshteinStep.getAdjustedString(string, modifier);
			sb.append(": " + string);
			if (levenshteinStep.getOp() == LevenshteinStep.Operation.INSERT)
				modifier++;
			else if (levenshteinStep.getOp() == LevenshteinStep.Operation.DELETE)
				modifier--;
		}
		return sb.toString();
	}

}
