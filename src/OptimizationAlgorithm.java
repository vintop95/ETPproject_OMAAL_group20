import java.util.*;

//we use this structure to sort individuals by their cost
class CostAndIndividual implements Comparable<CostAndIndividual>{
	private double key; //cost
	private Individual val; //individual
	public CostAndIndividual(double cost, Individual indiv){
		key = cost;
		val = indiv;
	}
	
	public double getKey(){
		return key;
	}
	public Individual getVal(){
		return val;
	}
	
	//this method is used to define an ordering relationship
	//between objects of class Couple
	@Override
	public int compareTo(CostAndIndividual c2){
		if (getKey() < c2.getKey())
			return -1;
		else if (getKey() > c2.getKey())
			return 1;
		else
			return 0;
	}
}

public class OptimizationAlgorithm {
	private static final Random rand = new Random();
	
	private static final double elitismRate = 0.2;
	private static final double maxDeschedulationRate = 1;
	private static int nonImprovingIterationsCount = 0;
	
	private static Problem problem;
	private static PriorityQueue<CostAndIndividual> individualsSortedByCost = new PriorityQueue<CostAndIndividual>();
	

	//This method returns a new population evolved from 'pop'
	public static Population evolvePopulation(Population pop) {		
		
		//sort individual by their cost to choose the elite ones
		for(int i=0; i<pop.size(); i++){
			individualsSortedByCost.add(new CostAndIndividual(pop.getIndividual(i).getCost(), pop.getIndividual(i)));
		}
		
		//create a new uninitialized population
		Population newPopulation = new Population(pop.size(), problem);
		
		//calculate how many elite individuals should be saved
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
		
		//generating the remaining individuals of the new population
		//from the existing ones
		for(int i=elitePopSize; i< pop.size(); i++){
			Individual ind1 = randomIndividual(pop);
			
			Individual newInd;
			
			//probability to mutate the solution starts from 25%
			//and eventually increases until 50% if population
			//hasn't been improving for more than 'maxNonImprovingIterations' iterations
			int maxNonImprovingIterations = 100;
			double prob = 0.25 + 0.25 * 
					Math.min(nonImprovingIterationsCount, maxNonImprovingIterations) / maxNonImprovingIterations;
		
			//we extract a random number between 0 and 1
			if( rand.nextDouble() < prob ){
				//in this case we mutate an existing individual
				double deschedulingRate = rand.nextDouble() * maxDeschedulationRate;
				newInd = deschedulingOperator(ind1, deschedulingRate);
			}else{
				//in this case we try to optimize an existing individual
				if( rand.nextDouble() < 0.5 )
					newInd = localSearchMoveExamsOperator(ind1);
				else
					newInd = localSearchSwapTimeslotsOperator(ind1);
			}
			
			newPopulation.saveIndividual(i, newInd);
		}
		
	
		//we use a counter to check if we are stuck in a local minimum
		if(pop.getFittest().getCost() == newPopulation.getFittest().getCost()) {
			nonImprovingIterationsCount++;
		}
		else {
			nonImprovingIterationsCount = 0;
		}
		
		
		//we need to update the best individual for the entire instance
		problem.updateBestInd( newPopulation.getFittest() );
		
		return newPopulation;
	}
	
	
	
	//++++++OPERATORS

	//used to mutate feasible sol to another feasible sol
	private static Individual deschedulingOperator(Individual indiv, double deschedulationRate){
		Individual newInd = new Individual(indiv);
		//this method deschedulate random exams from solution 'indiv'
		newInd.descheduleRandomExams(deschedulationRate);

		//it tries to generate a solution for 'maxIterations' iterations
		boolean feasibleSolFound = false;
		int maxIterationsForDescheduling = 1000;
		for(int i=0; i <maxIterationsForDescheduling && ! feasibleSolFound; i++){
			feasibleSolFound = newInd.generateFeasibleIndividual();
		}

		//if the solution found is not feasible, we return the old individual 'indiv'
		if(! feasibleSolFound)
			newInd = indiv;
		
		return newInd;
	}
	
	//LOCAL SEARCH OPERATORS
	//used to get the best solution in the neighborhood
	
	//BY MOVING EXAMS IN OTHER TIMESLOTS WITH NO CONFLICTING EXAMS
	private static Individual localSearchMoveExamsOperator(Individual old){
		Individual newInd = new Individual(old);
		newInd.localSearchMoveExams();
		return newInd;
	}
	
	//by swapping timeslots
	private static Individual localSearchSwapTimeslotsOperator(Individual old){
		Individual newInd = new Individual(old);
		newInd.localSearchSwapTimeslots();
		return newInd;
	}
	
	//++++++AUXILIARY
	
	public static void setProblem(Problem p) {
		problem = p;
		FitnessFunct.setProblem(p);
	}
	
	//to get a random individual
	private static Individual randomIndividual(Population pop){
		int indexRandomInd = rand.nextInt(pop.size());
		return pop.getIndividual(indexRandomInd);
	}
	
	
}
