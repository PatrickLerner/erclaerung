package de.tudarmstadt.awesome.erclaerung.precomputation;

public class LevenshteinStep implements Comparable<LevenshteinStep> {
	private int index;
	// private int indexOfString2;
	private Operation op;
	private char letter;

	public static enum Operation {
		INSERT, DELETE, SUBSTITUTION, NONOP
	}

	private String operationToString(Operation op) {
		switch (op) {
			case INSERT:
				return "Insert";
			case DELETE:
				return "Delete";
			case SUBSTITUTION:
				return "Substitution";
			case NONOP:
				return "Non-Op";
			default:
				return "Case not set.";

		}
	}

	@Override
	public boolean equals(Object o) {
		if (!o.getClass().equals(this.getClass()))
			return false;
		else if (o.toString().equals(this.toString()))
			return true;
		return false;
	}

	// Modifier is the number of inserts already done minus the deletes already done.
	public String getAdjustedString(String source, int modifier) {
		switch (op) {
			case INSERT:
				if (this.index == 0)
					return this.letter + source;
				else
					return source.substring(0, this.index + modifier) + this.letter
					                + source.substring(this.index + modifier);
			case DELETE:
				if (this.index == 0)
					return source.substring(1);
				else
					return source.substring(0, this.index + modifier) + source.substring(index + 1 + modifier);
			case SUBSTITUTION:
				if (this.index == 0)
					return this.letter + source.substring(1);
				else
					return source.substring(0, this.index + modifier) + this.letter
					                + source.substring(this.index + 1 + modifier);
			case NONOP:
				return source;
			default:
				return "Case not set.";

		}
	}

	public LevenshteinStep(int position, Operation operation, char letter) {
		this.index = position;
		this.op = operation;
		this.letter = letter;
	}

	@Override
	public String toString() {
		return operationToString(op) + ": " + letter + " at " + index;
	}

	public int compareTo(LevenshteinStep o) {
		int comIndex = Integer.compare(this.index, o.index);
		if (comIndex == 0) {
			int comOp = this.getOp().compareTo(o.getOp());
			if (comOp == 0) {
				return Character.compare(this.getLetter(), o.getLetter());
			}
			return comOp;
		}
		return comIndex;
	}

	public Operation getOp() {
		return op;
	}

	public int getIndex() {
		return index;
	}

	public char getLetter() {
		return letter;
	}

}
