class Population {
	//A POPULATION OF populationSize IS COMPOSED BY individuals (FEASIBLE SOLUTIONS)
	private int populationSize;
	private Individual[] individuals;
	private Individual fittest; //We keep the best individual of the population in this variable
	private Problem p;
	
	//CONSTRUCTOR
	public Population(int size, Problem p) {	
		populationSize = size;
		individuals = new Individual[populationSize];
		this.p = p;
	}
	
	public void initialize(){
		//this is the fraction of total computational time we want to dedicate,
		//in worst cases, to find the first generation
		double timeLimitRate = 0.33;
		
		//this is the minimum number of solutions we need in our population
		int minSolutionToGenerate = 1;
				
		long startTime = System.currentTimeMillis();
		
		double timeElapsed = EtpSolver.updateTimeElapsed(startTime);
		boolean timeLimitCondition = timeElapsed < (EtpSolver.timeLimit * timeLimitRate);

		//this will save the current population size
		int currPopSize = 0;
		
		//if there is already a feasible solution in the .sol file
		//we put it in our new population	
		Individual firstInd = p.readOldIndividual();
		if (firstInd != null && firstInd.isLegal() ){
			saveIndividual(currPopSize, firstInd);
			System.out.println(currPopSize + ": " + getIndividual(currPopSize).getCost());
			currPopSize++;
		}
				

		//we should generate maxPop exams, but in a time limit 
		//and the population must have at least 'minSolutionToGenerate' individuals
		
		while( currPopSize<size() && (currPopSize< minSolutionToGenerate || timeLimitCondition ) ) {
			
			Individual newInd = new Individual(p);
			
			//this loop iterates until a feasible solution is found, but it also should stop
			//if we already overcame the minimum threshold of generated Individuals 
			//and we exceeded the time limit
			int countReinit = 0;
			
			boolean feasibleSolFound = false;
			do{
				newInd.reinitialize();
				countReinit++;
				
				feasibleSolFound = newInd.generateFeasibleIndividual();
				
				timeElapsed = EtpSolver.updateTimeElapsed(startTime);
				timeLimitCondition = timeElapsed < (EtpSolver.timeLimit * timeLimitRate);
			}while(! feasibleSolFound && (currPopSize< minSolutionToGenerate || timeLimitCondition ));
			
			
			//if we exited the loop with a feasible solution
			//we should save it in the population
			if(feasibleSolFound){
				saveIndividual(currPopSize, newInd);
				System.out.println("reinitialized " + countReinit + " times");
				System.out.println(currPopSize + ": " + getIndividual(currPopSize).getCost() + " in " + timeElapsed + "s") ;
				currPopSize++;
			}
			
			
			timeElapsed = EtpSolver.updateTimeElapsed(startTime);
		}
		
		//save the effective size of the population
		populationSize = currPopSize;
		
		//update the best individual found until now
		p.updateBestInd( getFittest() );
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
		
	//return the solution with the maximum fit
	public Individual getFittest() {
		return fittest;
	}
		
	//OTHER METHODS
	public int size() {
		return populationSize;
	}
	
}
