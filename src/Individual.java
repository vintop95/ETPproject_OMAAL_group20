import java.util.*;
//todo: comparatore di soluzioni



class Individual {
	//EXPLANATION OF THE CLASS
	//0) GENERICS:
	//individual = chromosome: a solution, a timetable, a set of timeslots composed by exams
	//gene: a component of the solution, in this case an exam
	//allele: the value of a gene, in this case a timeslot relative to an exam
	
	//1) ATTRIBUTES
	//rand: useful to get random numbers
	private Random rand = new Random();
	//numOfGenes: number of exams
	//numOfAlleles: number of the possible timeslots in which an exam can be scheduled
	private int numOfGenes; 
	private int numOfAlleles;
	//we represent an individual/solution as a vector wich has the size of N_EXAMS
	//in each cell we will write the timeslot in wich the exam is scheduled
	private Vector<Integer> genes;
	
	private Problem p;
		
	//this is the set of exams TO SCHEDULE
	PriorityQueue<SortedExam> examToSchedule;
	
	//we use this support variable to avoid to calculate 
	//objective function and feasibility more than once
	private boolean costIsCalculated = false;
	private double cost;
	private boolean legalityIsCalculated = false;
	private boolean legal;
	
	//2) METHODS
	//CONSTRUCTORS
	public Individual(Problem p) {
		this.p = p;
		numOfGenes = p.getExams();
		numOfAlleles = p.getTimeslots();
		examToSchedule = new PriorityQueue<SortedExam>(p.getSortedExams());
		
		//set all timeslots to an undefined value
		genes = new Vector<Integer>();
		for(int i=0; i<numOfGenes; i++){
			genes.add(-1);
		}
	}
	
	public Individual(Individual ind){
		p = ind.p;
		numOfGenes = p.getExams();
		numOfAlleles = p.getTimeslots();	
		examToSchedule = new PriorityQueue<SortedExam>(p.getSortedExams());
		
		genes = new Vector<Integer>(ind.genes);
	}
	
	//change the timeslot of an exam with newTimeSlot
	public Individual(Individual ind,int exam, int newTimeSlot){
		//call the other constructor
		this(ind);
		genes.set(exam, newTimeSlot);
	}
	
	
	
	//END CONSTRUCTORS
	
	
	//TO REPRESENT A SOLUTION IN COMMAND LINE
	@Override
	public String toString() {
		String chromosome = "";
		for(int i=0; i<size(); i++) {
			chromosome += (i + 1) + " " + (getGene(i) + 1) + "\n";
		}
		if(isLegal())
			chromosome += "LEGAL \n";
		else
			chromosome += "ILLEGAL \n";
		chromosome += "Penalty: " + getCost();
		return chromosome;
	}
		
	//OTHER METHODS
	public int size() {
		return genes.size();
	}
	
	public int getGene(int i) {
		return genes.get(i);
	}
	
	public void setGene(int i, int allele) {
		genes.set(i, allele);
	}
		
	protected double getCost() {
		if(! costIsCalculated ){
			cost = FitnessFunct.getCost(this);
			costIsCalculated = true;
		}
		
		return cost;
	}
	
	protected double getCostWeight(int exam1) {
		return FitnessFunct.getCostWeight(this, exam1);
	}
	protected Problem getProblem() {
		return this.p;
	}
	protected boolean isLegal() {
		if(!legalityIsCalculated){
			legal = FitnessFunct.isLegal(this);
			legalityIsCalculated = true;
		}
		return legal;
	}
	//this method actually mutate a solution, descheduling a fraction of exams
	//equal to 'percOfExamsToDesched'
	public void descheduleRandomExams(double percOfExamsToDesched){
		
		int nOfExamsToDesched = (int) (percOfExamsToDesched * size());
		
		List<Integer> examsToDeschedulate = new ArrayList<Integer>(p.getExamSet());
		
		//it shuffles the complete list of exam
		Collections.shuffle(examsToDeschedulate);
		//and select only a fraction of it
		Iterator<Integer> it = examsToDeschedulate.iterator();
		for(int i=0; i<nOfExamsToDesched; i++){
			int exId = it.next();
			setGene( exId, -1);
			//add the selected exam to the ones that have to be re-scheduled
			SortedExam se = p.getSortedExam(exId);
			examToSchedule.add(se);		
		}
	}
	
	//this method assigns an exam to a feasible timeslot
	private void assignExamInRandomTimeslot(int exam, HashSet<Integer> availableTimeslot){
		
		Iterator<Integer> it = availableTimeslot.iterator();
		for (int t = rand.nextInt(availableTimeslot.size()); t > 0; t--){
			it.next();
		}
		
		int pickedTimeslot = it.next();
		
		setGene(exam, pickedTimeslot);
	}
	
	//this method could surrend and return false when it can't find a feasible solution
	public boolean generateFeasibleIndividual(){
		
		//needed as a second auxiliary list for collecting exams
		
		List<SortedExam> deschedulatedExamToSchedule = new ArrayList<SortedExam>();
		
		while(! examToSchedule.isEmpty() ){			
			//pick the most conflicting exam
			SortedExam e1 = examToSchedule.poll();

			HashSet<Integer> availableTimeslot = generateE1TimeslotSet(e1, p.getTimeslotSet());	
			
			//if there is at least one feasible timeslot to place exam e1
			if( ! availableTimeslot.isEmpty() ){
				//assign e1 in a random timeslot
				assignExamInRandomTimeslot(e1.id, availableTimeslot);
			} else {
				//otherwise add e1 in the new queue to schedule it later
				deschedulatedExamToSchedule.add(e1);
			}
		}
		
		
		int nOfIteration = 0;
		//the maximum number of iterations we are willing to do
		//in order to not remain stuck in this research - 300
		int nOfIterationMax = 300;

		//we iterate this loop until there are no more exams to schedule
		//or if we exceeded the given number of maximum iterations
		while(! deschedulatedExamToSchedule.isEmpty() && nOfIteration < nOfIterationMax){
			nOfIteration++;
				
			//pick a random exam from the list
			Collections.shuffle(deschedulatedExamToSchedule);
			SortedExam e1 = deschedulatedExamToSchedule.iterator().next();
			deschedulatedExamToSchedule.remove(e1);
			
			//this vector is used to store for each timeslot
			//how many exams in conflict with e1 are there
			int[] occurrency = new int[numOfAlleles];		
			for(int e2 = 0; e2 < p.getExams(); e2++){
				boolean e2IsInConflictWithE1 = p.areExamsInConflicts(e1.id, e2);
				boolean e2IsAlreadyScheduled = (getGene(e2) != -1);
				
				if(e2IsInConflictWithE1 && e2IsAlreadyScheduled){
					//update the number of exams in conflict in that timeslot
					occurrency[ getGene(e2) ] ++;					
				}
			}
			
			
			//choose the timeslot with the minimum number
			//of exams in conflict for e1
			int selectedTimeslot = 0;
			for(int t=0; t<numOfAlleles; t++){
				
				if(occurrency[t]< occurrency[selectedTimeslot])
					selectedTimeslot = t;
			}
						
			//we get a random optimal timeslot
			Vector<Integer> optimalTimeslots = new Vector<Integer>();
			for(int t=0; t<numOfAlleles; t++){
				if(occurrency[t] == occurrency[selectedTimeslot]){
					optimalTimeslots.add(t);
				}
			}
			
			selectedTimeslot = optimalTimeslots.get(rand.nextInt(optimalTimeslots.size()));
			
			//we deschedulate the exams in conflict with e1 that are scheduled
			//in selectedTimeslot, if there are any
			if(occurrency[selectedTimeslot] > 0){
				for(int i = 0; i < e1.conflictingExams.size(); i++){
					
					//id of conflicting exams
					int e2 = e1.conflictingExams.get(i);
					
					boolean e2IsInTheSelectedTimeslot = (getGene(e2) == selectedTimeslot);
					
					if (e2IsInTheSelectedTimeslot){
						setGene(e2, -1);
						//inseriamo gli esami deschedulati nella stessa lista 
						deschedulatedExamToSchedule.add(p.getSortedExam(e2));
					}
				}
			}
			
			//exam1 is finally positioned
			setGene(e1.id, selectedTimeslot);
			
			//note that the list deschedulatedExamToSchedule will be composed by:
			// - Exams that we could not place because
			// there were no timeslot available
			// - The exams that have been deschedulated because they are in
			// conflict with those defined before
			// In the second category it could also occur
			// that occurrency[selectedTimeslot] is 0 and then,
			// in order to position them, no one should be deschedulated
		}
		
		
		return isLegal();
	}
	
	//this method is required to reset a solution if we weren't 
	//able to find a feasible solution
	public void reinitialize(){
		examToSchedule = new PriorityQueue<SortedExam>(p.getSortedExams());
		genes = new Vector<Integer>();
		for(int i=0; i<numOfGenes; i++){
			genes.add(-1);
		}
		legalityIsCalculated = false;
		costIsCalculated = false;
	}
	
	//this method fills the queue in wich exams are ordered by 
	//their impact on the objective function
	private PriorityQueue<SortedExam> calculateExamToScheduleCostWeight(){
		PriorityQueue<SortedExam> examToScheduleCostWeight = new PriorityQueue<SortedExam>(11, new CostWeightComparator());
		
		for(int e = 0; e < numOfGenes; e++){
			p.getSortedExam(e).costWeight = getCostWeight(e);
			examToScheduleCostWeight.add(p.getSortedExam(e));
		}
		return examToScheduleCostWeight;
	}
	
	
	//THIS METHOD MOVES THE SOLUTION INTO THE BEST OF THE NEIGHBORHOOD
	//BY MOVING EXAMS IN OTHER TIMESLOTS
	public void localSearchMoveExams(){
		//main idea: if you don't find a better timeslot, stay in the same
		
		
		//we need to recalculate the cost weight of each exam
		PriorityQueue<SortedExam> examToScheduleCostWeight = calculateExamToScheduleCostWeight();
		
		while(! examToScheduleCostWeight.isEmpty() ){ //for each element or the queue
			
			//pop an exam from the queue
			SortedExam e1 = examToScheduleCostWeight.poll();
			
			//generate feasible timeslot for scheduling exam e1
			HashSet<Integer> T = p.getTimeslotSet();
			HashSet<Integer> e1TimeslotSet = generateE1TimeslotSet(e1, T);
			
			//if there are feasible timeslot to schedule e1
			if(! e1TimeslotSet.isEmpty()){
				//look for the best timeslot in terms of best cost variation
				int bestTimeslot = -1;
				double bestVariation = 0;
				
				for(Integer timeslot: e1TimeslotSet){
					double var = this.costVariationMoveExam(e1.getId(), timeslot);
					if(var<bestVariation){
						bestVariation = var;
						bestTimeslot = timeslot;
					}
				}
				
				if(bestTimeslot != -1){
					setGene(e1.getId(), bestTimeslot);
				}
			}
			
		} //end while
		
		
		//System.out.println("localSearch - newVal: " + (getCost()));
	}
	
	
	//THIS METHOD MOVES THE SOLUTION INTO THE BEST OF THE NEIGHBORHOOD
	//BY SWAPPING TIMESLOTS
	public void localSearchSwapTimeslots(){
		int timeslot1 = -1;
		int timeslot2 = -1;
		double bestVariation = 0;
		//assicurarsi che i campi costIsCalculated e legalityIsCalculated vengano 
		//reimpostati a false, se da problemi potrebbe essere per quello
		for(int t1=0; t1< p.getTimeslots() - 1; t1++){
			for(int t2=t1+1; t2<p.getTimeslots(); t2++){
				//utilizziamo una funzione che ritorna la variazione della funzione obiettivo 
				//se dovessimo scambiare gli esami di t1 con quelli di t2
				double currentVariation = costVariationSwapTimeslots(t1, t2);
				//aggiorniamo i parametri per effettuare il migliore spostamento
				if(currentVariation < bestVariation){
					timeslot1=t1;
					timeslot2=t2;
					bestVariation = currentVariation;
				}
			}
		}
		//solo se effettivamente la funz. ob. migliora effettuiamo lo scambio
		if(bestVariation < 0 ){
			for(int e= 0; e<p.getExams(); e++){
				if(getGene(e) == timeslot1){
					setGene(e, timeslot2);
				}
				else if(getGene(e) == timeslot2){
					setGene(e, timeslot1);
				}
			}
		}
		//se non esiste uno scambio che porta benefici viene newInd rimarrà identico a this 
	}
	
	
	//++++AUXILIARY FUNCTIONS
	
	//it returns the set of timeslots in which exam e1 can actually be scheduled
	private HashSet<Integer> generateE1TimeslotSet(SortedExam e1, HashSet<Integer> timeslotSet){
		//I clone the complete timeslot set, in order to remove infeasible timeslots from there
		HashSet<Integer> e1TimeslotSet = new HashSet<Integer>(timeslotSet);
		
		//we are iterating only through the exams in conflicts with e1
		for(int i = 0; i < e1.conflictingExams.size(); i++){
			
			//id of conflicting exams
			int e2 = e1.conflictingExams.get(i);
			
			boolean e2IsScheduled = (getGene(e2) >= 0);
			if(e2IsScheduled){
				//we should remove the timeslot because it's not
				//available anymore for e1
				e1TimeslotSet.remove(getGene(e2));
			}
		}

		return e1TimeslotSet;
	}
	
	
	//Calculates the advantage of moving an exam 'examToMove' in the 'newTimeSlot'
	double costVariationMoveExam(int examToMove, int newTimeslot) {
		double costVar=0;
		//we create a new temporary solution
		//in which the examToMove is moved to the newTimeslot
		Individual newInd = new Individual(this, examToMove, newTimeslot);
		double costWeightOld = this.getCostWeight(examToMove);
		double costWeightNew = newInd.getCostWeight(examToMove);
		costVar = (costWeightNew - costWeightOld);
		return costVar;
	}
	
	
	//una funzione che ritorna la variazione della funz. ob. che si avrebbe
	//se si scambiasse il contenuto del timeslot t1 con quello del timeslot t2
	public double costVariationSwapTimeslots(int t1, int t2){
		
		double variation = 0;
		HashSet<Integer> involvedExams = new HashSet<Integer>();
		
		for(int e=0; e<p.getExams(); e++){
			if(getGene(e) == t1 || getGene(e) == t2){
				//riempiamo le due strutture parallelamente
				involvedExams.add(e);
			}
		}
		
		Iterator<Integer> it = involvedExams.iterator();
		//per ogni esame da muovere e1 calcoliamo la variazione nella funzione obiettivo
		while( it.hasNext() ){
			
			int e1 = it.next();
	
			//scorriamo tutti gli esami e2 in conflitto con e1
			for(int j=0; j<p.getSortedExam(e1).conflictingExams.size(); j++){
				
				int e2= p.getSortedExam(e1).conflictingExams.get(j);
				//solo se l'esame in conflitto non fa parte di quelli dell'altro timeslot
				//calcoliamo la variazione
				if(! involvedExams.contains(e2)){
					int currentTimeslot = getGene(e1);
					int otherTimeslot = ((currentTimeslot == t1) ? t2 : t1);
					
					//penalità attuale
					double currentPenalty = FitnessFunct.getPenalty(e1, currentTimeslot, e2, getGene(e2));
					//penalità se effettuiamo lo scambio
					double newPenalty = FitnessFunct.getPenalty(e1, otherTimeslot, e2, getGene(e2));
		
					variation += (newPenalty - currentPenalty);
				}
			}
			
		}
		return variation;
	}
	
}
