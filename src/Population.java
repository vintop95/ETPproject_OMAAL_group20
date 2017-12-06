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
		if(initialize){
			
			for(int i=0; i<size(); i++) {
				saveIndividual(i, new Individual(p));
				getIndividual(i).generateFeasibleIndividual();
			}
		}
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
