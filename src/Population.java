class Population {
	//A POPULATION OF populationSize IS COMPOSED BY individuals (FEASIBLE SOLUTIONS)
	private int populationSize;
	private Individual[] individuals;
	private Individual fittest; //We keep the best individual of the population in this variable
	
	//CONSTRUCTOR
	//boolean initialize: if true, population is filled with random solutions
	//					  otherwise it remains empty
	public Population(int size, Problem p, boolean initialize) {
		//this is the fraction of total computational time we want to dedicate,
		//in worst cases, to find the first generation
		double timeLimitRate = 0.33;
		
		//this is the minimum number of solutions we need in our population
		int minSolutionToGenerate = 1;
				
		populationSize = size;
		individuals = new Individual[populationSize];
		
		
		if(! initialize )	
			return;
		
		long startTime= System.currentTimeMillis();
		
		double timeElapsed = EtpSolver.updateTimeElapsed(startTime);
		boolean timeLimitCondition = timeElapsed < (EtpSolver.timeLimit * timeLimitRate);

		//this will save the current population size
		int i=0;
		
		//if there is already a feasible solution in the .sol file
		//we put it in our new population	
		Individual firstInd = p.readOldFile();
		if (firstInd != null && firstInd.isLegal() ){
			saveIndividual(i, firstInd);
			System.out.println(i + ": " + getIndividual(i).getCost());
			i++;
		}
				

		//we should generate maxPop exams, but in a time limit 
		//and the population must have at least 'minSolutionToGenerate' individuals
		
		while( i<size() && (i< minSolutionToGenerate || timeLimitCondition ) ) {
			
			Individual newInd = new Individual(p);
			
			//this loop iterates until a feasible solution is found, but it also should stop
			//if we already overcame the minimum threshold of generated Individuals 
			//and we exceeded the time limit
			boolean feasibleSolFound = false;
			do{
				newInd.reinitialize();
				feasibleSolFound = newInd.generateFeasibleIndividual();
				
				timeElapsed = EtpSolver.updateTimeElapsed(startTime);
				timeLimitCondition = timeElapsed < (EtpSolver.timeLimit * timeLimitRate);
			}while(! feasibleSolFound && (i< minSolutionToGenerate || timeLimitCondition ));
			
			
			//if we exited the loop with a feasible solution
			//we should save it in the population
			if(feasibleSolFound){
				saveIndividual(i, newInd);
				i++;
			}
			
			
			timeElapsed = EtpSolver.updateTimeElapsed(startTime);
		}
		
		//save the effective size of the population
		populationSize = i;
		
		//update the best individual found until now
		p.updateBestInd( getFittest() );
	}
	
	//return the solution with the maximum fit
	public Individual getFittest() {
		return fittest;
	}
		
	
	//to save an individual 'indiv' in the population in position 'index'
	public void saveIndividual(int index, Individual indiv) {
		double newCost = indiv.getCost();
		
		//we update the fittest individual of the population
		if(fittest == null || (fittest != null && newCost < fittest.getCost()) ){
			fittest = indiv;
		}
		
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
