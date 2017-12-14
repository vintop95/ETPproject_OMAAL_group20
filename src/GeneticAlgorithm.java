import java.util.*;

class Couple implements Comparable<Couple>{
	private double key; //cost
	private Individual val; //individual
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
	
	@Override
	public int compareTo(Couple c2){
		//it's inverted for having in the head
		//of the PriorityQueue the biggest key
		if (getKey() < c2.getKey())
			return -1;
		else if (getKey() > c2.getKey())
			return 1;
		else
			return 0;
	}
}

class GeneticAlgorithm {
	private static final double elitismRate = 0.2;
	private static final double maxDeschedulationRate = 1;
	private static Problem problem;
	private static final Random rand = new Random();
	private static int nonImprovingIterationsCount = 0;
	
	private static PriorityQueue<Couple> individualsSortedByCost = new PriorityQueue<Couple>();
	
	
	public static void setProblem(Problem p) {
		GeneticAlgorithm.problem = p;
		FitnessFunct.setProblem(p);
	}

	
	
	public static Population evolvePopulation(Population pop) {

		System.out.println("non impr cont:" + nonImprovingIterationsCount);
		
		
		//sort individual by fitness for choosing elite
		for(int i=0; i<pop.size(); i++){
			individualsSortedByCost.add(new Couple(pop.getIndividual(i).getCost(), pop.getIndividual(i)));
		}
		
		//create a new uninitialized population
		Population newPopulation = new Population(pop.size(), problem, false);
		
		//calculate how many elite to save
		int elitePopSize = (int) (pop.size() * elitismRate);
		
		//if there is more than one individual, you have to save at least one elite individual.
		//If there is only 1 individual in the pop, you cannot take it into the new population
		//which will contains, in fact, only one individual
		if(pop.size() > 1)
			elitePopSize = Math.max(elitePopSize, 1);
		
		//saving elite
		for(int i=0; i< elitePopSize; i++){
			Individual eliteInd = individualsSortedByCost.poll().getVal();
			newPopulation.saveIndividual(i, eliteInd);
		}
		
		//generating new individuals from the existing ones
		for(int i=elitePopSize; i< pop.size(); i++){
			Individual ind1 = randomIndividual(pop);
			
			Individual newInd;
			
			//0.25 is good for instance01
			//probability to mutate the solution starts from 25%
			//and eventually increases until 50% if population
			//hasn't been improving for more than 'maxNonImprovingIterations' iterations
			int maxNonImprovingIterations = 100;
			double prob = 0.25 + 0.25 * 
					Math.min(nonImprovingIterationsCount, maxNonImprovingIterations) / maxNonImprovingIterations;
			
			if( rand.nextDouble() < prob ){
				double deschedulationRate = rand.nextDouble() * maxDeschedulationRate; //
				newInd = deschedulingOperator(ind1, deschedulationRate);
			}else{
				newInd = localSearchOperator(ind1);
			}
			
			
			newPopulation.saveIndividual(i, newInd);
		}
		
	
		//to check if we are in a local minimum
		
		if(pop.getFittest().getCost() == newPopulation.getFittest().getCost()) {
			nonImprovingIterationsCount++;
		}
		else {
			nonImprovingIterationsCount = 0;
		}
		
		
		problem.updateBestInd( newPopulation.getFittest() );
		
		
		return newPopulation;
	}
	
	
	
	
	//used to mutate feasible sol to other feasible sol
	private static Individual deschedulingOperator(Individual indiv, double deschedulationRate){
		Individual newInd = new Individual(indiv);
		newInd.deschedulateRandomExams(deschedulationRate);

		//it tries to generate a solution for 'maxIterations' iterations
		boolean feasibleSolFound = false;
		int maxIterations = 1000;
		for(int i=0; i <maxIterations && ! feasibleSolFound; i++){
			feasibleSolFound = newInd.generateFeasibleIndividual();
		}

		//if the solution found is not feasible, we return the old individual 'indiv'
		if(! feasibleSolFound)
			newInd = indiv;
		
		return newInd;
	}
	
	//used to get the best sol in the neighborhood
	private static Individual localSearchOperator(Individual indiv){
		Individual newInd = new Individual(indiv);
		newInd.localSearch();
		return newInd;
	}
	
	
	private static Individual randomIndividual(Population pop){
		int indexRandomInd = rand.nextInt(pop.size());
		return pop.getIndividual(indexRandomInd);
	}
	
}
