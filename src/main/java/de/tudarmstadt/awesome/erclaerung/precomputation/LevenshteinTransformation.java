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
	public boolean equals(Object o) {
		if (!o.getClass().equals(this.getClass()))
			return false;
		else if (o.toString().equals(this.toString()))
			return true;
		else
			return false;
	}

	public boolean indexReversedEquals(LevenshteinTransformation other) {
		if (this.getIndexReversedLevenshteinSteps(false).equals(other.getIndexReversedLevenshteinSteps(false)))
			return true;
		return false;
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

	/**
	 * Returns a String <strong>including intermediate results of the applied changes.</strong>
	 * 
	 * @param withNonOp
	 *            When true Non-Operations are returned. Generally this should be false.
	 * @return A printable String showing the complete calculation.
	 */
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

	/**
	 * Returns the steps taken during the Transformation from one string to the other.
	 * 
	 * @param withNonOps
	 *            When true Non-Operations are returned. Generally this should be false.
	 * @return A List of {@link LevenshteinStep}s.
	 */
	public List<LevenshteinStep> getLevenshteinSteps(boolean withNonOps) {
		List<LevenshteinStep> _return = new ArrayList<LevenshteinStep>();
		for (LevenshteinStep step : this.steps) {
			if (withNonOps || !step.getOp().equals(LevenshteinStep.Operation.NONOP))
				_return.add(step);
		}
		return _return;
	}

	/**
	 * Returns the steps taken during the Transformation from one string to the other <strong>with the indexes of the
	 * steps reversed regarding the length of the source string.</strong> This makes it easier to analyze changes of
	 * suffixes.
	 * 
	 * @param withNonOps
	 *            When true Non-Operations are returned. Generally this should be false.
	 * @return A List of {@link LevenshteinStep}s.
	 */
	public List<LevenshteinStep> getIndexReversedLevenshteinSteps(boolean withNonOps) {
		List<LevenshteinStep> reversed = new ArrayList<LevenshteinStep>();
		for (LevenshteinStep step : this.steps) {
			if (withNonOps || !step.getOp().equals(LevenshteinStep.Operation.NONOP))
				reversed.add(new LevenshteinStep(this.string1.length() - step.getIndex() - 1, step.getOp(), step
				                .getLetter()));
		}
		return reversed;
	}

}
