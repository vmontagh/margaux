package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

public class SdNon extends Sd implements SdOrd {

	public SdNon(String rName, String sName, String sNext, String sFirst,
			String middleName, String endName, String rConcreteName,
			String sConcreteName, String sConcreteNext, String sConcreteFirst,
			String mConcreteName, String eConcreteName, String endNext,
			String endFirst, String middleNext, String middleFirst,
			String endConcreteNext, String endConcreteFirst,
			String middleConcreteNext, String middleConcreteFirst) {
		super(rName, sName, sNext, sFirst, middleName, endName, rConcreteName,
				sConcreteName, sConcreteNext, sConcreteFirst, mConcreteName,
				eConcreteName, endNext, endFirst, middleNext, middleFirst,
				endConcreteNext, endConcreteFirst, middleConcreteNext,
				middleConcreteFirst);
	}

	@Override
	protected String genBody() {
		return "";
	}

	@Override
	protected String getOther() {
		throw new RuntimeException("Invalid Procedure call");
	}

	@Override
	public String getFirst() {
		throw new RuntimeException("Invalid Procedure call");
	}

	@Override
	public String getNext() {
		throw new RuntimeException("Invalid Procedure call");
	}

	@Override
	public String genLetforLocality(String letVar, String quantifiedVar,
			final String quanitifiedOrderedVar) {
		final String letExpression = "let %1$s = (%2$s.%3$s) |";
		return String.format(letExpression, letVar, quanitifiedOrderedVar, RName);
	}

	@Override
	public boolean isConsistentOrdered() {
		if (!super.isConsistent())
			return false;

		if (!(EndFirst == null || EndFirst.equals("")))
			return false;
		if (!(EndNext == null || EndNext.equals("")))
			return false;
		if (!(MiddleFirst == null || MiddleFirst.equals("")))
			return false;
		if (!(MiddleNext == null || MiddleNext.equals("")))
			return false;

		return true;

	}

	@Override
	public String getConcreteFirst() {
		throw new RuntimeException("Invalid Procedure call");
	}

	@Override
	public String getConcreteNext() {
		throw new RuntimeException("Invalid Procedure call");
	}

	@Override
	int getPriorityInClass() {
		return 4;
	}


	
}
