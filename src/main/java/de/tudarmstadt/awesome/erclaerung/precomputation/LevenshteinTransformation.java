package de.tudarmstadt.awesome.erclaerung.precomputation;

import java.util.ArrayList;
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

	public String toStringWithTransformation(boolean withNonOp) {
		StringBuilder sb = new StringBuilder();
		String string = string1;
		sb.append(this.string1 + "->" + this.string2 + ":");
		int modifier = 0;
		for (LevenshteinStep levenshteinStep : steps) {
			if (withNonOp || !levenshteinStep.getOp().equals(LevenshteinStep.Operation.NONOP)) {
				sb.append("\n" + levenshteinStep.toString());
				string = levenshteinStep.getAdjustedString(string, modifier);
				sb.append(": " + string);
				if (levenshteinStep.getOp() == LevenshteinStep.Operation.INSERT)
					modifier++;
				else if (levenshteinStep.getOp() == LevenshteinStep.Operation.DELETE)
					modifier--;
			}
		}
		return sb.toString();
	}

	public List<LevenshteinStep> getLevenshteinSteps() {
		return this.steps;
	}

	public List<LevenshteinStep> getIndexReversedLevenshteinSteps() {
		List<LevenshteinStep> reversed = new ArrayList<LevenshteinStep>();
		for (LevenshteinStep step : steps) {
			reversed.add(new LevenshteinStep(this.string1.length() - step.getIndex() - 1, step.getOp(), step
			                .getLetter()));
		}
		return reversed;
	}

}
