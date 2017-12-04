import java.util.*;
class GeneticAlgorithm {
	private static final double uniformRate = 0.5;
	private static final double mutationRate = 0.15;
	private static final int tournamentSize = 5;
	private static final boolean elitism = true;
	private static Problem problem;
	private static final Random rand = new Random();
	private static int nonImprovingIterationsCount = 0;
	
	public static void setProblem(Problem p) {
		GeneticAlgorithm.problem = p;
		FitnessFunct.setProblem(p);
	}
	public static int getNonImprovingIterationsCount(){
		return nonImprovingIterationsCount;
	}
	
	//methods:
	
	public static Population evolvePopulation(Population pop) {
		//create a new uninitialized population
		Population newPopulation = new Population(pop.size(), problem, false);
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
		
		for (int i = elitismOffset; i < pop.size(); i++) {
			//choose the survivors of the tournament
			Individual indiv1 = tournamentSelection(pop);
			Individual indiv2 = tournamentSelection(pop);
			Individual newIndiv = crossover(indiv1, indiv2);
			newPopulation.saveIndividual(i, newIndiv);
		}
		
		// Mutate population
		for (int i = elitismOffset; i < newPopulation.size(); i++) {
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
	
	private static Individual crossover(Individual indiv1, Individual indiv2) {
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
	
	/*private static Individual crossover(Individual indiv1, Individual indiv2) {
		Individual newSol = new Individual(problem);
		for (int i = 0; i < indiv1.size(); i++) {
			if (Math.random() <= uniformRate) {
				newSol.setGene(i, indiv1.getGene(i));
			} else {
				newSol.setGene(i, indiv2.getGene(i));
			}
		}
		return newSol;
	}*/
	
	
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
}
