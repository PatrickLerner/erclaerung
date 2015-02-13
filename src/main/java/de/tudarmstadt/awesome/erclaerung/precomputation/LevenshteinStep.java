package de.tudarmstadt.awesome.erclaerung.precomputation;

/**
 * Represents a step in a transformation
 * 
 * @author Manuel
 *
 */
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

	/**
	 * Returns the operation in string form
	 * 
	 * @param op
	 *            The operation
	 * @return Insert, Delete, Substitution or Non-Op
	 */
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

	/**
	 * Returns the adjusted string after the operation was done on the source string
	 * 
	 * @param source
	 *            The string in its current step
	 * @param modifier
	 *            Number of already done inserts minus the number of already done deletes
	 * @return The adjusted string after the operation was completed.
	 */
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
				if (this.index + modifier == 0)
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

	/**
	 * Returns the operation.
	 * 
	 * @return The operation
	 */
	public Operation getOp() {
		return op;
	}

	/**
	 * Always returns the position of the operation in relation to the original word.
	 * 
	 * @return
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * For an insert operation the inserted letter, for a substitution the inserted letter, for a deletion the delteted
	 * letter and for a non-op the letter at that position.
	 * 
	 * @return The letter of the operation
	 */
	public char getLetter() {
		return letter;
	}

	/**
	 * Returns the second letter of a substitution else the default char.
	 * 
	 * @return The second letter of a substitution else the default char.
	 */
	public char getLetter2() {
		return letter2;
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

}
