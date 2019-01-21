# Time interval

Dette eksemplet handler om ulike utforming av API og ulike representasjoner.

## API-design

Vi skal utforme et objekt som skal holde rede på et tidsintervall, f.eks. til bruk i en avtale-app.
Vi forenkler det litt ved at tidsintervallet skal være innenfor en og samme dag, slik at vi bare trenger å holde rede på timer og minutter.
Et tidsintervall defineres av start- og slutt-tidspunkt, alternativt av et start-tidspunkt og en intervall-lengde, hvor et tidspunkt er klokkeslettet i form timer og minutter.

Vi skal kunne spørre om og endre start- og slutt-timen og -minuttet, i tillegg til antall minutter i intervallet:
**int getStartHour()** og **int setStartHour(int hour)**
**int getStartMinutes()** og **int setStartMinutes(int minutes)**

**int getEndHour()** og **int setEndHour(int hour)** 
**int getEndMinutes()** og **int setEndMinutes(int minutes)**

**int getIntervalLength()** og **int setIntervalLength(int minutes)**

Start- og slutt-tidspunktet og lengden på intervallet er koblede data, så vi må bestemme hva som skjer når hver av dem endres.
Vi velger logikk som gjør det enkelt å forskyve en avtale i tid:
- Ved endring av start-tidspunkt (time eller minutt) så skal intervall-lengden holdes fast, så slutt-tidspunktet må justeres. 
- Ved endring av slutt-tidspunkt så skal start-tidspunktet holdes fast, så intervall-lengden må justeres.
- Ved endring av intervall-lengden så skal start-tidspunktet holdes fast, så slutt-tidspunktet må justeres.

Merk at denne logikken er uavhengig av hvilke variabler vi bruker for å representere tidsintervallet.
Vi skal nedenfor prøve tre alternative representasjoner:
- **TimeInterval1**: heltallsvariabler for start- og slutt-time og -minutt, som intervall-lengden beregnes fra 
- **TimeInterval2**: heltallsvariabler for start-time og -minutt og intervall-lengden, som slutt-time og -minutt beregnes fra 
- **TimeInterval3**: som **TimeInterval1**, men istedet for timer og minutter, så brukes antall minutter siden midnatt for å representere start- og slutt-tidspunktene 

## TimeInterval1

### Koden

I TimeInterval1-varianten så representeres tidsintervallet med fire heltallsvariabler for start- og slutt-time og -minutt:

```java
private int startHour;
private int startMin;
private int endHour;
private int endMin;
```

Det meste av kompleksiteten i koden ligger i **set**-metodene og har med validering av en (potensiell) endring å gjøre. F.eks. skal jo interval-lengden holdes fast, når start-tidspunktet endres, så da må en både sjekke om start-tidspunktet alene er greit og om intervallet er mindre enn gjenværende del av dagen.

```java
public int getStartHour() {
   return startHour;
}

// hjelpemetode for å sjekke om et tall er i riktig intervall
private void checkInt(int i, int min, int max) {
   if (i < min || i >= max) {
      throw new IllegalArgumentException(String.format("%d isn't between %d (inclusive) and %d (exclusive)", i, min, max));
   }
}

public void setStartHour(int hour) {
   checkInt(hour, 0, 24);
   // husk den gamle intervall-lengden
   int intervalLength = getIntervalLength();
   startHour = hour;
   // juster endHour og endMin vha. setIntervalLength
   setIntervalLength(intervalLength);
}

public int getStartMinutes() {
   return startMin;
}
public void setStartMinutes(int minutes) {
   checkInt(minutes, 0, 60);
   // husk den gamle intervall-lengden
   int intervalLength = getIntervalLength();
   // sjekk om den nye kombinasjon av start og lengde er gyldig
   checkInt(intervalLength, 0, 24 * 60 - minutes(startHour, minutes));

   startMin = minutes;
   // juster endHour og endMin vha. setIntervalLength
   setIntervalLength(intervalLength);;
}

public int getEndHour() {
   return endHour;
}
public void setEndHour(int hour) {
   checkInt(hour, startHour, 24);
   endHour = hour;
}

public int getEndMinutes() {
   return endMin;
}

private int minutes(int hour, int min) {
   return hour * 60 + min;
}
	
private int minutes(int startHour, int startMin, int endHour, int endMin) {
   return minutes(endHour, endMin) - minutes(startHour, startMin);
}

public void setEndMinutes(int minutes) {
   checkInt(minutes(startHour, startMin, endHour, endMin), 0, 24 * 60);
   endMin = minutes;
}

public int getIntervalLength() {
   return minutes(startHour, startMin, endHour, endMin);
}

public void setIntervalLength(int minutes) {
   checkInt(minutes, 0, 24 * 60 - minutes(startHour, startMin));
   endHour = startHour + (startMin + minutes) / 60;
   endMin = (startMin + minutes) % 60;		
}
```

Her har vi valgt å definere noen hjelpemetoder for sjekk av heltallsverdier (**checkInt**) og beregning av minutter (**minutes**). Slike metoder gjør koden forøvrig lettere å lese og skrive. Vi har faktisk to **minutes**-metoder, hvor den ene beregner tid fra midnatt til et tidspunkt og den andre tid mellom to tidspunkt. Java tillater at en klasse har flere metoder med samme navn, så lenge de har parameterlister som gjør dem distinkte (ulike lengder og/eller typer).

### Testing med main-metoden

Når man tester med **main**-metoden så kan det være greit med en (eller flere) praktisk(e) konstruktører. I koden under har vi laget en som gjør det lett å initialisere tidspunktet. Uten den er det faktisk nokså fiklete å få satt både start- og slutt-tidspunktet uten at det utløses unntak.

Vi har også lagt til en **toString**-metode uten parametre, som brukes implisitt ved utskrift av en objekt-referanse. Når vi i **main**-metoden skriver **System.out.println(ti)**, så vil objektet som **ti**-variablen refererer til bli "konvertert" til en **String** som skrives ut, og denne konverteringen gjøres ved at **toString**-metoden kalles.
Her har vi valgt å bruke [**String.format**-metoden](https://docs.oracle.com/javase/8/docs/api/java/lang/String.html#format-java.lang.String-java.lang.Object...-), som gjør det enkelt å lage en **String** basert på en mal hvor verdier skal skytes inn. Første argument er malen, hvor såkalte format-*direktiver* sier hvor verdier skal skytes inn og hvordan de skal formateres, og resten av argumentene er verdiene som skal skytes inn. Direktivet **%02d** sier at argumentet må være et heltall og at det fylles ut med **0** foran så det blir minst **2** sifre, slik at vi får **08:00** i stedet for **8:0**.

```java
public TimeInterval1(int startHour, int startMin, int endHour, int endMin) {
   checkInt(startHour, 0, 24);
   checkInt(startMin, 0, 60);
   checkInt(minutes(startHour, startMin, endHour, endMin), 0, 24 * 60);
   this.startHour = startHour;
   this.startMin = startMin;
   this.endHour = endHour;
   this.endMin = endMin;
}

@Override
public String toString() {
   return String.format("[TimeInterval1 %02d:%02d-%02d:%02d]", getStartHour(), getStartMinutes(), getEndHour(), getEndMinutes());
}

public static void main(String[] args) {
   TimeInterval1 ti = new TimeInterval1(12, 15, 14, 0);
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
```

Selve testen av TimeInterval1-logikken gjøres ved at vi rigger opp et gyldig TimeInterval1-objekt og endrer start-tidspunktet vha. kall til setStartHour og setStartMinutes. Utskriften kan vi sammenligne med hva vi forventer, og som nevnt over, skal slutt-tidspunktet forskyves slik at lengden på tidsintervallet forblir det samme.

![Testing av TimeInterval1-logikk med main-metode](TimeInterval1-object-states.png)

Vi tester også at unntak blir utløst når det skal, siden det er en vesentlig del av logikken. Her sjekker vi at kallet til ti.setStartHour(23) utløser et unntak, fordi det gjør at slutt-tidspunktet vil haven over midnatt. Her brukes **try/catch** med samme unntakstype (**IllegalArgumentException**) som vi forventer og utskrift som indikerer om det gikk grei eller galt.

## TimeInterval2

### Koden

I TimeInterval2-varianten så representeres tidsintervallet med tre heltallsvariabler for start-time og -minutt og intervall-lengden:


```java
private int startHour;
private int startMin;
private int intervalLength;
```

Metodene er i stor grad de samme, bortsett fra de som direkte leser og setter slutt-tidspunktet og intervall-lengden. Formlene som brukes i **getEndHour** og **getEndMinutes** er de samme som ble brukt i setIntervalLength i **TimeInterval1**-koden:

```java
public int getEndHour() {
   return startHour + (startMin + intervalLength) / 60;
}

public void setEndHour(int hour) {
   setIntervalLength(minutes(startHour, startMin, hour, getEndMinutes()));
}

public int getEndMinutes() {
   return (startMin + intervalLength) % 60;
}
public void setEndMinutes(int minutes) {
   setIntervalLength(minutes(startHour, startMin, getEndHour(), minutes));
}

public int getIntervalLength() {
   return intervalLength;
}

public void setIntervalLength(int minutes) {
   // sjekk om den nye kombinasjon av start og lengde er gyldig
   checkInt(minutes, 0, 24 * 60 - minutes(startHour, startMin));
   intervalLength = minutes;
}
```

### Testing med main-metoden

```java
public TimeInterval2(int startHour, int startMin, int endHour, int endMin) {
   checkInt(startHour, 0, 24);
   checkInt(startMin, 0, 60);
   checkInt(minutes(startHour, startMin, endHour, endMin), 0, 24 * 60);
   this.startHour = startHour;
   this.startMin = startMin;
   this.intervalLength = minutes(startHour, startMin, endHour, endMin);
}
```

Test-koden er lik, bortsett fra at konstruktøren setter **intervalLength**-variablen i stedet for **endHour** og **endMin**, og at **main**-metoden lager en instans av **TimeInterval2**. Siden metodene og oppførselen er ment å være den samme, så bør den samme testkoden virke greit! Strukturen på tilstandsdiagrammet blir lik, men tilstandsvariablene er jo endret:

![Testing av TimeInterval2-logikk med main-metode](TimeInterval2-object-states.png)

## TimeInterval3

### Koden

I **TimeInterval3** representeres start- og slutt-tidspunktene som minutter siden midnatt: 

```java
private int start;
private int end;
```

Her må koden i større grad endres, siden ingen av variablene er felles med tidligere løsninger. Ser en på [detaljene](TimeInterval3.java) så er dette egentlig den enkleste løsningen.

### Testing med main-metoden

Også her må konstruktøren skrives om, mens testkoden er den samme.

## Sluttkommentar om API-design og validering

Ved utforming av API-et for tidsintervall-klassene, så har vi valgt å ha metoder for sette enkeltverdier for start- og slutt-time og -minutt, selv om verdiene henger tett sammen. Dette skaper lett problemer når flere av dem må endres for å oppå ønsket effekt, og de må validere hvert for seg.

Anta f.eks. at en har en tom konstruktør som initialiserer tidsintervallet til 00:00-00:00 og vi ønsker å endre tilstanden til 12:00-14:00. Hvis vi starter med kalle setEndHour(14) så får vi et intervall fra midnatt på 14 timer, og da går det galt når vi siden forskyver start-tidspunktet til kl. 12, fordi slutt-tidspunktet havner over midnatt. Akkurat her er det greit å starte med å endre start-tidspunktet, men ofte er det ikke så opplagt hvilken sekvens av enkeltendringer som bringer objektet til ønsket tilstand.

Alternativet er å la endringsmetodene sette en større del av tilstanden om gangen, f.eks. ha en **set**-metode som tar inn de samme argumentene som konstruktøren og endrer hele tilstanden. Dette vil gjøre objektene både enklere og sikrere å bruke. Diagrammet under illustrerer problemet og løsningen:

![Endring av tilstand med ulike metodekall](TimeIntervalN-object-states.png)