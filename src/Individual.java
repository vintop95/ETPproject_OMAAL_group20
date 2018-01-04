import java.util.*;
//todo: comparatore di soluzioni

class CoupleOfInt{
	public int a;
	public int b;
	
	public CoupleOfInt(int a, int b){
		this.a = a;
		this.b = b;
	}
	
	@Override
	public boolean equals(Object other){
		if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof CoupleOfInt))return false;
		CoupleOfInt c2 = (CoupleOfInt)other;
		return (c2.a == a && c2.b == b);
	}
	
	@Override
	public String toString() {
		Integer aa = (Integer) a;
		Integer bb = (Integer) b;
		return "(" + aa.toString() + ", " + bb.toString() + ")";
	}
}

class Individual {
	//EXPLANATION OF THE CLASS
	//0) GENERICS:
	//individual: a solution, a timetable, a set of timeslots composed by exams
	
	//1) ATTRIBUTES
	//rand: useful to get random numbers
	private Random rand = new Random();
	//we represent an individual as a vector which has the size of N_EXAMS
	//in each cell we will write the timeslot in which the exam is scheduled
	private Vector<Integer> timeslotOfExam;
	//the problem that the Individual refers
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
	//2a)CONSTRUCTORS
	public Individual(Problem p) {
		this.p = p;
		examToSchedule = new PriorityQueue<SortedExam>(p.getSortedExams());
		
		//set all timeslots to an undefined value
		timeslotOfExam = new Vector<Integer>();
		for(int i=0; i<p.getNumOfExams(); i++){
			timeslotOfExam.add(-1);
		}
	}
	
	public Individual(Individual ind){
		p = ind.p;	
		examToSchedule = new PriorityQueue<SortedExam>(p.getSortedExams());
		
		timeslotOfExam = new Vector<Integer>(ind.timeslotOfExam);
	}
	//END CONSTRUCTORS
	
	//++++++++++++++++++++++++
	//2b)AUXILIARY METHODS
	//TO REPRESENT A SOLUTION IN COMMAND LINE
	@Override
	public String toString() {
		String individual = "";
		for(int i=0; i<size(); i++) {
			individual += (i + 1) + " " + (getTimeslotOfExam(i) + 1) + "\n";
		}
		if(isLegal())
			individual += "LEGAL \n";
		else
			individual += "ILLEGAL \n";
		individual += "Penalty: " + getCost();
		return individual;
	}
		
	//this method is required to reset a solution if we weren't 
	//able to find a feasible solution
	public void reinitialize(){
		examToSchedule = new PriorityQueue<SortedExam>(p.getSortedExams());
		timeslotOfExam = new Vector<Integer>();
		for(int e=0; e<p.getNumOfExams(); e++){
			timeslotOfExam.add(-1);
		}
		legalityIsCalculated = false;
		costIsCalculated = false;
	}
		
	public int size() {
		return timeslotOfExam.size();
	}
	public int getTimeslotOfExam(int e) {
		return timeslotOfExam.get(e);
	}
	public void setExamInTimeslot(int e, int t) {
		timeslotOfExam.set(e, t);
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
	//END 2b
	//++++++++++++++++++++++++++++++
	
	
	//++++++++++++++++++++++++++++++
	//2c) OPTIMIZATION METHODS
	
	//this method could surrend and return false when it can't find a feasible solution
	//also we use a tabulist
	public boolean generateFeasibleIndividual(){
		
		//needed as a second auxiliary list for collecting exams
		
		List<SortedExam> descheduledExamToSchedule = new ArrayList<SortedExam>();
		
		while(! examToSchedule.isEmpty() ){			
			//pick the most conflicting exam
			SortedExam e1 = examToSchedule.poll();

			HashSet<Integer> e1AvailableTimeslots = generateAvailableTimeslotSet(e1);	
			
			//if there is at least one feasible timeslot to place exam e1
			if( ! e1AvailableTimeslots.isEmpty() ){
				//assign e1 in a random timeslot
				assignExamInRandomTimeslot(e1.id, e1AvailableTimeslots);
			} else {
				//otherwise add e1 in the new queue to schedule it later
				descheduledExamToSchedule.add(e1);
			}
		}
		
		
		int nOfIteration = 0;
		//the maximum number of iterations we are willing to do
		//in order to not remain stuck in this research - 3000
        int maxIterationsGenerateFeasibleIndividual =
        		(int) (p.getNumOfExams() * 4);

		//TABU LIST: we need it to avoid to repeat same moves
		//CoupleOfInt: (exam, oldTimeslotNotToGo)
		Queue<CoupleOfInt> tabuList = new LinkedList<CoupleOfInt>();
		//parameter to adjust - 20
		int maxSizeTabuList = 20;//p.getTimeslots(); 

		//we iterate this loop until there are no more exams to schedule
		//or if we exceeded the given number of maximum iterations
		while(! descheduledExamToSchedule.isEmpty() && nOfIteration < maxIterationsGenerateFeasibleIndividual){
			nOfIteration++;
				
			//pick a random exam from the list
			Collections.shuffle(descheduledExamToSchedule);
			SortedExam e1 = descheduledExamToSchedule.iterator().next();
			descheduledExamToSchedule.remove(e1);
			
			//this vector is used to store for each timeslot
			//how many exams in conflict with e1 are there
			int[] occurrency = new int[p.getNumOfTimeslots()];
			//we scan exams in conflict with e1
			//to calculate the occurency vector
			for(int i = 0; i < e1.conflictingExams.size(); i++){
				int e2 = e1.conflictingExams.get(i);
				boolean e2IsAlreadyScheduled = (getTimeslotOfExam(e2) != -1);
				if(e2IsAlreadyScheduled){
					//update the number of exams in conflict in that timeslot
					occurrency[ getTimeslotOfExam(e2) ] ++;					
				}
			}
			
			
			//choose the timeslot with the minimum number of exams in conflict for e1
			//to get the minimum number possible of exams in conflict in a timeslot
			int selectedTimeslot = 0;
			for(int t=0; t<p.getNumOfTimeslots(); t++){
				boolean isTabuMove = tabuList.contains( new CoupleOfInt(e1.getId(), t) );
				
				if( !isTabuMove && occurrency[t]< occurrency[selectedTimeslot])
					selectedTimeslot = t;
			}
						
			//we get a random optimal timeslot
			Vector<Integer> optimalTimeslots = new Vector<Integer>();
			for(int t=0; t<p.getNumOfTimeslots(); t++){
				boolean isTabuMove = tabuList.contains( new CoupleOfInt(e1.getId(), t) );
				
				if( !isTabuMove && occurrency[t] == occurrency[selectedTimeslot]){
					optimalTimeslots.add(t);
				}
			}
			if( optimalTimeslots.size() > 0 )
				selectedTimeslot = optimalTimeslots.get(rand.nextInt(optimalTimeslots.size()));
			else
				selectedTimeslot = -1;
			
			
			
			//we deschedule the exams in conflict with e1 that are scheduled
			//in selectedTimeslot, if there are any
			if(selectedTimeslot >= 0 && occurrency[selectedTimeslot] > 0){
				for(int i = 0; i < e1.conflictingExams.size(); i++){
					//id of conflicting exams
					int e2 = e1.conflictingExams.get(i);
					
					boolean e2IsInTheSelectedTimeslot = (getTimeslotOfExam(e2) == selectedTimeslot);
					
					if (e2IsInTheSelectedTimeslot){
						
						//implementing FIFO for adding a move in the tabulist
						if(tabuList.size() == maxSizeTabuList)
							tabuList.remove();
						tabuList.add( new CoupleOfInt(e2, getTimeslotOfExam(e2)) );
	
						setExamInTimeslot(e2, -1);
						//we insert descheduled exam in the same list
						descheduledExamToSchedule.add(p.getSortedExam(e2));
					}
				}
			}
			
			//exam1 is finally positioned
			setExamInTimeslot(e1.id, selectedTimeslot);
			
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
			setExamInTimeslot( exId, -1);
			//add the selected exam to the ones that have to be re-scheduled
			SortedExam se = p.getSortedExam(exId);
			examToSchedule.add(se);		
		}
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
			HashSet<Integer> e1AvailableTimeslots = generateAvailableTimeslotSet(e1);
			
			//if there are feasible timeslot to schedule e1
			if(! e1AvailableTimeslots.isEmpty()){
				//look for the best timeslot in terms of best cost variation
				int bestTimeslot = -1;
				double bestVariation = 0;
				
				for(Integer timeslot: e1AvailableTimeslots){
					double var = this.costVariationMoveExam(e1.getId(), timeslot);
					if(var<bestVariation){
						bestVariation = var;
						bestTimeslot = timeslot;
					}
				}
				
				if(bestTimeslot != -1){
					setExamInTimeslot(e1.getId(), bestTimeslot);
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
		for(int t1=0; t1< p.getNumOfTimeslots() - 1; t1++){
			for(int t2=t1+1; t2<p.getNumOfTimeslots(); t2++){
				//calculate how much the cost will vary if we swap 
				//the exams in the two timeslots
				double currentVariation = costVariationSwapTimeslots(t1, t2);
				//we update if we found a best swap of timeslots
				if(currentVariation < bestVariation){
					timeslot1=t1;
					timeslot2=t2;
					bestVariation = currentVariation;
				}
			}
		}
		//we swap the timeslots only if we have a good variation
		if(bestVariation < 0 ){
			for(int e= 0; e<p.getNumOfExams(); e++){
				if(getTimeslotOfExam(e) == timeslot1){
					setExamInTimeslot(e, timeslot2);
				}
				else if(getTimeslotOfExam(e) == timeslot2){
					setExamInTimeslot(e, timeslot1);
				}
			}
		}
		//if there is not a good swap, newInd will remain the same
	}
	
	
	//+++++++++++++++++++++++
	//AUXILIARY OPTIMIZATION FUNCTIONS
	//+++++++++++++++++++++++
	
	//this method assigns an exam to a feasible timeslot
	private void assignExamInRandomTimeslot(int exam, HashSet<Integer> availableTimeslot){
		
		Iterator<Integer> it = availableTimeslot.iterator();
		for (int t = rand.nextInt(availableTimeslot.size()); t > 0; t--){
			it.next();
		}
		
		int pickedTimeslot = it.next();
		
		setExamInTimeslot(exam, pickedTimeslot);
	}
		
	//this method fills the queue in which exams are ordered by 
	//their impact on the objective function
	private PriorityQueue<SortedExam> calculateExamToScheduleCostWeight(){
		PriorityQueue<SortedExam> examToScheduleCostWeight = new PriorityQueue<SortedExam>(11, new CostWeightComparator());
		
		for(int e = 0; e < p.getNumOfExams(); e++){
			p.getSortedExam(e).costWeight = getCostWeight(e);
			examToScheduleCostWeight.add(p.getSortedExam(e));
		}
		return examToScheduleCostWeight;
	}
		
	//it returns the set of timeslots in which exam e1 can actually be scheduled
	private HashSet<Integer> generateAvailableTimeslotSet(SortedExam e1){
		//I clone the complete timeslot set, in order to remove infeasible timeslots from there
		HashSet<Integer> e1TimeslotSet = new HashSet<Integer>(p.getTimeslotSet());
		
		//we are iterating only through the exams in conflicts with e1
		for(int i = 0; i < e1.conflictingExams.size(); i++){
			
			//id of conflicting exams
			int e2 = e1.conflictingExams.get(i);
			
			boolean e2IsScheduled = (getTimeslotOfExam(e2) >= 0);
			if(e2IsScheduled){
				//we should remove the timeslot because it's not
				//available anymore for e1
				e1TimeslotSet.remove(getTimeslotOfExam(e2));
			}
		}

		return e1TimeslotSet;
	}
	
	//Calculates the advantage of moving an exam 'examToMove' in the 'newTimeSlot'
	private double costVariationMoveExam(int examToMove, int newTimeslot) {
		double costVar=0;
		//we create a new temporary solution
		//in which the examToMove is moved to the newTimeslot
		Individual newInd = new Individual(this);
		newInd.setExamInTimeslot(examToMove, newTimeslot);
		double costWeightOld = this.getCostWeight(examToMove);
		double costWeightNew = newInd.getCostWeight(examToMove);
		costVar = (costWeightNew - costWeightOld);
		return costVar;
	}
	
	//it returns the variation of the obj function if we swap the exams
	//in timeslot t1 and in timeslot t2
	private double costVariationSwapTimeslots(int t1, int t2){
		
		double variation = 0;
		HashSet<Integer> involvedExams = new HashSet<Integer>();
		
		for(int e=0; e<p.getNumOfExams(); e++){
			if(getTimeslotOfExam(e) == t1 || getTimeslotOfExam(e) == t2){
				involvedExams.add(e);
			}
		}
		
		Iterator<Integer> it = involvedExams.iterator();
		while( it.hasNext() ){
			//for each exam
			int e1 = it.next();
	
			//we scan all the exams e2 conflicting with e1
			for(int j=0; j<p.getSortedExam(e1).conflictingExams.size(); j++){
				
				int e2= p.getSortedExam(e1).conflictingExams.get(j);
				//we calculate variation only if e2 is not in the other timeslot,
				//because otherwise the cost would not change
				if(! involvedExams.contains(e2)){
					int currentTimeslot = getTimeslotOfExam(e1);
					int otherTimeslot = ((currentTimeslot == t1) ? t2 : t1);
					
					double currentPenalty = FitnessFunct.getPenalty(e1, currentTimeslot, e2, getTimeslotOfExam(e2));
					double newPenalty = FitnessFunct.getPenalty(e1, otherTimeslot, e2, getTimeslotOfExam(e2));
		
					variation += (newPenalty - currentPenalty);
				}
			}
			
		}
		return variation;
	}
	
	//++++++++++++++++++++++++
	//END 2C
}
