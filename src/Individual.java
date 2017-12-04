import java.util.*;
//todo: comparatore di soluzioni

class Individual {
	//EXPLANATION OF THE CLASS
	//0) GENERICS:
	//individual = chromosome: a solution, a timetable, a set of timeslots composed by exams
	//gene: a component of the solution, in this case an exam
	//allele: the value of the gene, in this case a timeslot relative to an exam
	//fitness: the objective function value
	
	//1) ATTRIBUTES
	//rand: useful to get random numbers
	private Random rand = new Random();
	//numOfGenes: number of exams
	//numOfAlleles: number of the possible timeslots in which an exam could be scheduled
	private int numOfGenes; 
	private int numOfAlleles;
	//WE REPRESENT THE SOLUTION IN TWO FORMS
	//a) genes: it's the first way we can codify the solution
	//		 each gene (exam) contains an allele (a timeslot)
	//b) timeslots: it's the second possible way to represent the solution:
	//			 a vector in which each element is a timeslot
	//			 hashset<Integer> is a set of exams in that timeslot
	private int[] genes;
	private Vector<HashSet<Integer>> timeslots;
	//fitness: value of how much a solution is good, in this case the objective function value
	private double fitness = 0.0;
	
	
	//2) METHODS
	//CONSTRUCTOR
	public Individual(Problem p) {
		numOfGenes = p.getExams();
		numOfAlleles = p.getTimeslots();
		genes = new int[numOfGenes];
		//INITIALIZING AN EMPTY SET OF TIMESLOTS
		timeslots = new Vector<HashSet<Integer>>(numOfAlleles);
		for(int i=0; i<numOfAlleles; i++) {
			timeslots.add(new HashSet<Integer>());
		}
	}
	
	//TO GENERATE THE SOLUTION RANDOMLY (only in the form a)
	public void generateIndividual() {
		for(int e=0; e<genes.length; e++) {
			//we assign the random timeslot chosen for the exam e of the solution
			genes[e] = rand.nextInt(numOfAlleles);
		}
	}
	
	//TO REPRESENT A SOLUTION IN COMMAND LINE
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
		
	//OTHER METHODS
	public int size() {
		return genes.length;
	}
	
	public int getGene(int i) {
		return genes[i];
	}
	
	public void setGene(int i, int allele) {
		genes[i] = allele;
	}
	
	public double getFitness() {
		if(fitness == 0) {
			fitness = FitnessFunct.evaluate(this);
		}
		return fitness;
	}
	
	protected double getCost() {
		return FitnessFunct.getCost(this);
	}
	private boolean isLegal() {
		return FitnessFunct.isLegal(this);
	}
}
