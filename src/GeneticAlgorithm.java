import java.util.*;

class Couple{
	private double key; //fitness
	private Individual val;
	public Couple(double k, Individual v){
		key = k;
		val = v;
	}
	
	public double getKey(){
		return key;
	}
	
	public Individual getVal(){
		return val;
	}
}
class CoupleComparator implements Comparator<Couple>{
	public int compare(Couple c1, Couple c2){
		//it's inverted for having in the head
		//of the PriorityQueue the biggest key
		if (c1.getKey() < c2.getKey())
			return 1;
		else if (c1.getKey() > c2.getKey())
			return -1;
		else
			return 0;
	}
}

class GeneticAlgorithm {
	private static final double elitismRate = 0.5;
	private static final double uniformRate = 0.5;
	private static final double mutationRate = 0.15;
	private static final boolean elitism = true;
	private static Problem problem;
	private static final Random rand = new Random();
	private static int nonImprovingIterationsCount = 0;
	private static Comparator<Couple> comp = new CoupleComparator();
	private static PriorityQueue<Couple> individualsSortedByFitness = new PriorityQueue<Couple>(11, comp);
	
	
	public static void setProblem(Problem p) {
		GeneticAlgorithm.problem = p;
		FitnessFunct.setProblem(p);
	}
	public static int getNonImprovingIterationsCount(){
		return nonImprovingIterationsCount;
	}
	
	//methods:
	
	public static double getSummary(Population pop){
		double nLegals = 0;
		for(int i=0; i< pop.size();i++){
			if (pop.getIndividual(i).isLegal())
				nLegals++;
		}
		return nLegals / pop.size();
	}
	
	static int nOfCycles = 0;
	public static Population evolvePopulation(Population pop) {
		nOfCycles++;
		System.out.println(nOfCycles + ")Perc of legal sol: " + getSummary(pop));
		
		for(int i=0; i<pop.size(); i++){
			individualsSortedByFitness.add(new Couple(pop.getIndividual(i).getFitness(), pop.getIndividual(i)));
		}
		
		//create a new uninitialized population
		Population newPopulation = new Population(pop.size(), problem, false);
		
		int elitePopSize = (int) (pop.size() * elitismRate);
		
		for(int i=0; i< elitePopSize; i++){
			Individual eliteInd = individualsSortedByFitness.poll().getVal();
			newPopulation.saveIndividual(i, eliteInd);
		}
		
		for(int i=elitePopSize; i< pop.size(); i++){
			Individual ind1 = randomIndividual(pop);
			Individual ind2 = randomIndividual(pop);
			Individual newInd = crossover(ind1, ind2);
			newPopulation.saveIndividual(i, newInd);
		}
		
		/* we don't use this now
		if(elitism) {
			//take the fittest to the next population
			newPopulation.saveIndividual(0, pop.getFittest());
		}
		
		
		//elitismOffset: for not choosing the fittest for the next tournament
		//what, a tournament?
		int elitismOffset;
		if (elitism) {
			elitismOffset = 1;
		} else {
			elitismOffset = 0;
		}
		
		
		for (int i = 0; i < pop.size(); i++) {
			//choose the survivors of the tournament
			Individual indiv1 = tournamentSelection(pop);
			Individual indiv2 = tournamentSelection(pop);
			Individual newIndiv = crossover(indiv1, indiv2);
			newPopulation.saveIndividual(i, newIndiv);
		}
		*/
		
		// Mutate population
		for (int i = 0; i < newPopulation.size(); i++) {
			mutate(newPopulation.getIndividual(i));
		}
		
		//to check if we are in a local minimum
		if(pop.getFittest().getCost() == newPopulation.getFittest().getCost()) {
			nonImprovingIterationsCount++;
		}
		else {
			nonImprovingIterationsCount = 0;
		}
		return newPopulation;
	}
	
	// Crossover individuals
	
	/*private static Individual crossover(Individual indiv1, Individual indiv2) {
		Individual newSol = new Individual(problem);
		//select two cutting point
		int src = rand.nextInt(problem.getExams());
		int dest= rand.nextInt(problem.getExams());
		if(src > dest) {
			int temp = src;
			src = dest;
			dest= temp;
		}
		for(int i=0; i<indiv1.size(); i++) {
			if(i < src || i > dest) {
				//outside of the selected region we take alleles from indiv1
				newSol.setGene(i, indiv1.getGene(i));
			}
			else {
				//inside the selected region we take alleles from indiv2
				newSol.setGene(i, indiv2.getGene(i));
			}
		}
		return newSol;
	}
	*/
	
	private static Individual crossover(Individual indiv1, Individual indiv2) {
		Individual newSol = new Individual(problem);
		for (int i = 0; i < indiv1.size(); i++) {
			if (Math.random() <= uniformRate) {
				newSol.setGene(i, indiv1.getGene(i));
			} else {
				newSol.setGene(i, indiv2.getGene(i));
			}
		}
		return newSol;
	}
	
	
	// Mutate an individual
	private static void mutate(Individual indiv) {
		// Loop through genes
		for (int i = 0; i < indiv.size(); i++) {
			if (Math.random() <= mutationRate) {
				// Create random allele
				int allele = rand.nextInt(problem.getTimeslots());
				indiv.setGene(i, allele);
			}
		}
	}
	
	// Select individuals for crossover
	// Tournament is a nice term to describe the Hunger Games of life
	private static Individual tournamentSelection(Population pop) {
		// Create a tournament population
		final int tournamentSize = pop.size() / 10;
		Population tournament = new Population(tournamentSize, problem,  false);
		
		// For each place in the tournament get a random individual 
		for (int i = 0; i < tournamentSize; i++) {
			//we select a random individual from previous population
			int randomIndividual = rand.nextInt(pop.size());
			//and we put it in the tournament
			tournament.saveIndividual(i, pop.getIndividual(randomIndividual));
		}
		// Get the fittest
		Individual fittest = tournament.getFittest();
		return fittest;
	}
	
	
	
	private static Individual randomIndividual(Population pop){
		int indexRandomInd = rand.nextInt(pop.size());
		return pop.getIndividual(indexRandomInd);
	}
	
	
	
	
	
	//+++inseriti da vincenzo 6-12 after revelations+++

	
	
	
	
}
