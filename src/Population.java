class Population {
	//A POPULATION OF populationSize IS COMPOSED BY individuals (POSSIBLE SOLUTIONS)
	private int populationSize;
	private Individual[] individuals;
	
	//CONSTRUCTOR
	//initialize: if true, population is filled with random solutions
	//				otherwise it remains empty
	public Population(int size, Problem p, boolean initialize) {
		populationSize = size;
		individuals = new Individual[populationSize];
		
		if(! initialize)	
			return;
		
		
		long startTime= System.currentTimeMillis();
		
		
		
		System.out.println("Generating first population: ");
		double timeElapsed = EtpSolver.updateTimeElapsed(startTime);
		
		boolean timeLimitCondition = timeElapsed < (EtpSolver.timeLimit / 2);
		
		
		int i;
		for( i=0; i<size() && timeLimitCondition; i++) {
			Individual newInd = new Individual(p);
			
			boolean feasibleSolFound = false;
			do{
				newInd.reinitialize();
				feasibleSolFound = newInd.generateFeasibleIndividual();
				
				timeElapsed = EtpSolver.updateTimeElapsed(startTime);
				timeLimitCondition = timeElapsed < (EtpSolver.timeLimit / 2);
			}while(! feasibleSolFound && (i==0 || timeLimitCondition ));
			//do that while a feasible solution is not found, but also
			//if this is not the first solution you should stop after a timeLimit
			
			saveIndividual(i, newInd);
			System.out.println(i + ": " + getIndividual(i).getCost() + getIndividual(i).isLegal());
			
			timeElapsed = EtpSolver.updateTimeElapsed(startTime);
		}
		
		//save the effective size of the population
		populationSize = i;
	}
	
	//find the solution with the maximum fit
	public Individual getFittest() {
		Individual fittest = individuals[0];
		// Loop through individuals to find fittest
		for (int i = 1; i < size(); i++) {
			if (fittest.getFitness() <= getIndividual(i).getFitness()) {
				fittest = getIndividual(i);
			}
		}
		return fittest;
	}
		
	
	//TO ACCESS TO AN INDIVIDUAL
	public void saveIndividual(int index, Individual indiv) {
		individuals[index] = indiv;
	}
	public Individual getIndividual(int index) {
		return individuals[index];
	}
		
	//OTHER METHODS
	public int size() {
		return populationSize;
	}
	
}
