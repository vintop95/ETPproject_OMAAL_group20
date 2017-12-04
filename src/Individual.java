import java.util.*;
class Individual {
	private int numOfGenes; 
	private int numOfAlleles;
	private int[] genes;
	private double fitness = 0.0;
	private Random rand = new Random();
	private Vector<HashSet<Integer>> timeslots;
	public Individual(Problem p) {
		numOfGenes = p.getExams();
		numOfAlleles = p.getTimeslots();
		genes = new int[numOfGenes];
		timeslots = new Vector<HashSet<Integer>>(numOfAlleles);
		for(int i=0; i<numOfAlleles; i++) {
			timeslots.add(new HashSet<Integer>());
		}
	}
	public void generateIndividual() {
		for(int e=0; e<genes.length; e++) {
			int period = rand.nextInt(numOfAlleles);
			genes[e] = period;
		}
			
	}
	public int getGene(int i) {
		return genes[i];
	}
	public void setGene(int i, int allele) {
		genes[i] = allele;
	}
	public int size() {
		return genes.length;
	}
	public double getFitness() {
		if(fitness == 0) {
			fitness = FitnessFunct.evaluate(this);
		}
		return fitness;
	}
	@Override
	public String toString() {
		String chromosome = "";
		for(int i=0; i<size(); i++) {
			chromosome += (i + 1) + " " + (genes[i] + 1) + "\n";
		}
		if(isLegal())
			chromosome += "LEGAL\n";
		else
			chromosome += "ILLEGAL\n";
		chromosome += "Penalty: " + getCost();
		return chromosome;
	}
	protected double getCost() {
		return FitnessFunct.getCost(this);
	}
	private boolean isLegal() {
		return FitnessFunct.isLegal(this);
	}
}
