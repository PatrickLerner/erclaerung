package de.tudarmstadt.awesome.erclaerung.precomputation;

public class LevenshteinStep implements Comparable<LevenshteinStep> {
	private int index;
	// private int indexOfString2;
	private Operation op;
	private char letter;
	private char letter2;

	public LevenshteinStep(int position, Operation operation, char letter) {
		this.index = position;
		this.op = operation;
		this.letter = letter;
	}

	public LevenshteinStep(int position, Operation operation, char letter, char letter2) {
		this(position, operation, letter);
		this.letter2 = letter2;
	}

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

	@Override
	public String toString() {
		if (op == Operation.SUBSTITUTION)
			return operationToString(op) + ": " + letter + " at " + index + " substituting " + this.letter2;
		return operationToString(op) + ": " + letter + " at " + index;
	}

	public int compareTo(LevenshteinStep o) {
		int comIndex = Integer.compare(this.index, o.index);
		if (comIndex == 0) {
			int comOp = this.getOp().compareTo(o.getOp());
			if (comOp == 0) {
				int comChar1 = Character.compare(this.getLetter(), o.getLetter());
				if (comChar1 == 0)
					return Character.compare(this.letter2, o.letter2);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		result = prime * result + letter;
		result = prime * result + letter2;
		result = prime * result + ((op == null) ? 0 : op.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LevenshteinStep other = (LevenshteinStep) obj;
		if (index != other.index)
			return false;
		if (letter != other.letter)
			return false;
		if (letter2 != other.letter2)
			return false;
		if (op != other.op)
			return false;
		return true;
	}

	public char getLetter2() {
		return letter2;
	}

}
