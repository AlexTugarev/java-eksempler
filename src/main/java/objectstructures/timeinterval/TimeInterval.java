package objectstructures.timeinterval;

public class TimeInterval {

	// tag::variables[]
	private TimePoint start;
	private TimePoint end;
	// end::variables[]

	public TimeInterval(int startHour, int startMin, int endHour, int endMin) {
		checkInt(startHour, 0, 24);
		checkInt(startMin, 0, 60);
		checkInt(minutes(startHour, startMin, endHour, endMin), 0, 24 * 60);
		this.start = new TimePoint(startHour, startMin);
		this.end = new TimePoint(endHour, endMin);
	}

	@Override
	public String toString() {
		return String.format("[TimeInterval %02d:%02d-%02d:%02d]", getStartHour(), getStartMinutes(), getEndHour(), getEndMinutes());
	}
	
	// hjelpemetode for å sjekke om et tall er i riktig intervall
	private void checkInt(int i, int min, int max) {
		if (i < min || i >= max) {
			throw new IllegalArgumentException(String.format("%d isn't between %d (inclusive) and %d (exclusive)", i, min, max));
		}
	}

	// tag::startHour-methods[]
	public int getStartHour() {
		return start.getHour();
	}

	public void setStartHour(int hour) {
		checkInt(hour, 0, 24);
		// husk den gamle intervall-lengden
		int intervalLength = getIntervalLength();
		// sjekk om den nye kombinasjon av start og lengde er gyldig
		checkInt(intervalLength, 0, 24 * 60 - minutes(hour, start.getMinutes()));

		start.setHour(hour);
		// juster endHour og endMin vha. setIntervalLength
		setIntervalLength(intervalLength);;
	}
	// end::startHour-methods[]

	public int getStartMinutes() {
		return start.getMinutes();
	}

	public void setStartMinutes(int minutes) {
		checkInt(minutes, 0, 60);
		// husk den gamle intervall-lengden
		int intervalLength = getIntervalLength();
		// sjekk om den nye kombinasjon av start og lengde er gyldig
		checkInt(intervalLength, 0, 24 * 60 - minutes(start.getHour(), minutes));

		start.setMinutes(minutes);
		// juster endHour og endMin vha. setIntervalLength
		setIntervalLength(intervalLength);;
	}

	public int getEndHour() {
		return end.getHour();
	}

	public void setEndHour(int hour) {
		checkInt(hour, start.getHour(), 24);
		end.setHour(hour);
	}

	public int getEndMinutes() {
		return end.getMinutes();
	}

	private int minutes(int hour, int min) {
		return hour * 60 + min;
	}
	
	private int minutes(int startHour, int startMin, int endHour, int endMin) {
		return minutes(endHour, endMin) - minutes(startHour, startMin);
	}

	public void setEndMinutes(int minutes) {
		checkInt(minutes(start.getHour(), start.getMinutes(), end.getHour(), minutes), 0, 24 * 60);
		end.setMinutes(minutes);
	}

	public int getIntervalLength() {
		return minutes(start.getHour(), start.getMinutes(), end.getHour(), end.getMinutes());
	}

	public void setIntervalLength(int minutes) {
		// sjekk om den nye kombinasjon av start og lengde er gyldig
		checkInt(minutes, 0, 24 * 60 - minutes(start.getHour(), start.getMinutes()));
		end.setHour(start.getHour() + (start.getMinutes() + minutes) / 60);
		end.setMinutes((start.getMinutes() + minutes) % 60);		
	}

	//

	// tag::main-method[]
	public static void main(String[] args) {
		TimeInterval ti = new TimeInterval(12, 15, 14, 0);
		System.out.println(ti);
		ti.setStartHour(14);
		System.out.println(ti);
		ti.setStartMinutes(0);
		System.out.println(ti);
		try {
			ti.setStartHour(23);
			System.out.println("Forventet feil ble ikke fanget opp");
		} catch (IllegalArgumentException e) {
			System.out.println("Forventet feil ble fanget opp");
		}
		System.out.println(ti);
	}
	// end::main-method[]
}
