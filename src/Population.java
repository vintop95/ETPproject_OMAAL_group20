class Population {
	private Individual[] individuals;
	private int populationSize;
	public Population(int size, Problem p, boolean initialize) {
		populationSize = size;
		individuals = new Individual[populationSize];
		if(initialize){
			for(int i=0; i<size(); i++) {
				Individual newIndividual = new Individual(p);
				newIndividual.generateIndividual();
				saveIndividual(i, newIndividual);
			}
		}
	}
	
	public int size() {
		return populationSize;
	}
	public void saveIndividual(int index, Individual indiv) {
		individuals[index] = indiv;
	}
	public Individual getIndividual(int index) {
		return individuals[index];
	}
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
}
