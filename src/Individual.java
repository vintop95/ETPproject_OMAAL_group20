import java.util.*;
//todo: comparatore di soluzioni



class Individual {
	//EXPLANATION OF THE CLASS
	//0) GENERICS:
	//individual = chromosome: a solution, a timetable, a set of timeslots composed by exams
	//gene: a component of the solution, in this case an exam
	//allele: the value of the gene, in this case a timeslot relative to an exam
	//fitness: how good is a solution. It depends from the objective function value
	
	//1) ATTRIBUTES
	//rand: useful to get random numbers
	private Random rand = new Random();
	//numOfGenes: number of exams
	//numOfAlleles: number of the possible timeslots in which an exam could be scheduled
	private int numOfGenes; 
	private int numOfAlleles;
	//WE REPRESENT THE SOLUTION IN TWO FORMS
	//a) genes: it's the first way we can codify the solution
	//		 each gene (exam) contains an allele (a timeslot)
	//b) timeslots: it's the second possible way to represent the solution:
	//			 a vector in which each element is a timeslot
	//			 hashset<Integer> is a set of exams in that timeslot
	private Vector<Integer> genes;
	
	//fitness: value of how much a solution is good, in this case the objective function value
	private double fitness = 0.0;
	Problem p;
	
	//this is the set of exams TO SCHEDULATE
	PriorityQueue<SortedExam> examToSchedule;
	
	
	
	//2) METHODS
	//CONSTRUCTORS
	public Individual(Individual ind){
		this.p = ind.p;
		this.numOfGenes = p.getExams();
		this.numOfAlleles = p.getTimeslots();	
		examToSchedule = new PriorityQueue<SortedExam>(p.getSortedExams());
		
		this.genes = new Vector<Integer>(ind.genes);
	}
	
	//change the timeslot of exam in newTimeSlot
	public Individual(Individual ind,int exam, int newTimeSlot){
		//call the other constructor
		this(ind);
		genes.set(exam, newTimeSlot);
	}
	
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
	
	//END CONSTRUCTORS
	
	//TO GENERATE THE SOLUTION RANDOMLY (only in the form a)
	public void generateIndividual() {
		for(int e=0; e<size(); e++) {
			//we assign the random timeslot chosen for the exam e of the solution
			setGene(e, rand.nextInt(numOfAlleles));
		}
		System.out.println("Num of confl: " + FitnessFunct.nOfConflicts(this));
		this.toString();
	}
	
	
	//TO REPRESENT A SOLUTION IN COMMAND LINE
	@Override
	public String toString() {
		String chromosome = "";
		for(int i=0; i<size(); i++) {
			chromosome += (i + 1) + " " + (getGene(i) + 1) + "\n";
		}
		if(isLegal())
			chromosome += "LEGAL (n of confl: " + FitnessFunct.nOfConflicts(this) + ")\n";
		else
			chromosome += "ILLEGAL (n of confl: " + FitnessFunct.nOfConflicts(this) + ")\n";
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
	
	public double getFitness() {
		if(fitness == 0) {
			fitness = FitnessFunct.evaluate(this);
		}
		return fitness;
	}
	
	protected double getCost() {
		return FitnessFunct.getCost(this);
	}
	protected double getCostWeight(int exam1) {
		return FitnessFunct.getCostWeight(this, exam1);
	}
	protected Problem getProblem() {
		return this.p;
	}
	protected boolean isLegal() {
		return FitnessFunct.isLegal(this);
	}
	
	public void deschedulateRandomExams(double percOfExamsToDesched){
		
		int nOfExamsToDesched = (int) (percOfExamsToDesched * size());
		
		List<Integer> examsToDeschedulate = new ArrayList<Integer>(p.getExamSet());
		
		//it shuffles the list
		Collections.shuffle(examsToDeschedulate);
		
		Iterator<Integer> it = examsToDeschedulate.iterator();
		for(int i=0; i<nOfExamsToDesched; i++){
			int exId = it.next();
			setGene( exId, -1);
			//set exams to schedule for the feasible generator
			SortedExam se = p.getSortedExam(exId);
			examToSchedule.add(se);		
		}
	}
	
	
	private void assignExamInRandomTimeslot(int exam, HashSet<Integer> availableTimeslot){
		
		Iterator<Integer> it = availableTimeslot.iterator();
		for (int t = rand.nextInt(availableTimeslot.size());
				t > 0; t--){
			it.next();
		}
		
		int pickedTimeslot = it.next();
		
		setGene(exam, pickedTimeslot);
	}
	
	//TODO: limit number of iteration OF generateFeasibleIndividual
	//TODO: DA COMPLETARE, ANCORA NON BUONA PER LA INSTANCE2
	
	public void generateFeasibleIndividual(){
		
		boolean legal = isLegal();
		//provo se conviene ricominciare da capo se si blocca
		do{

			//needed as a second auxiliary queue for collecting exams
			PriorityQueue<SortedExam> deschedulatedExamToSchedule =
			new PriorityQueue<SortedExam>(11, new CostWeightComparator());
			
			while(! examToSchedule.isEmpty() ){			
				//pick the most 'greedy' exam
				SortedExam e1 = examToSchedule.poll();
				
				HashSet<Integer> availableTimeslot = generateE1TimeslotSet(e1, p.getTimeslotSet());	
				
				//if there is at least one timeslot free to assign e1
				if( ! availableTimeslot.isEmpty() ){
					//assign e1 in a random timeslot
					assignExamInRandomTimeslot(e1.id, availableTimeslot);
				} else {
					//add e1 in the new queue for schedule it
					deschedulatedExamToSchedule.add(e1);
				}
			}
			
			
			int nOfIteration = 0;
			int nOfIterationMax = p.getExams()*2;
			while(! deschedulatedExamToSchedule.isEmpty() && nOfIteration < nOfIterationMax){
				nOfIteration++;
				//pick the most 'greedy' exam
				SortedExam e1 = deschedulatedExamToSchedule.poll();
				
				//this vector is used to store for each timeslot
				//how many exams in conflict with e1 are there
				int[] occurrency = new int[p.getTimeslots()];
				int selectedTimeslot = 0;
				
				for(int e2 = 0; e2 < p.getExams(); e2++){
					boolean e2IsInConflictWithE1 = p.areExamsInConflicts(e1.id, e2);
					boolean e2IsAlreadyScheduled = (getGene(e2) != -1);
					
					if(e2IsInConflictWithE1 && e2IsAlreadyScheduled){
						//counting the exams in conflict in that timeslot
						occurrency[ getGene(e2) ] ++;					
					}
				}
				
				for(int t=0; t<p.getTimeslots(); t++){
					//choose the timeslot with the minimum number
					//of exams in conflict for e1
					
					//TODO: si potrebbe pensare di usare minore o uguale per
					//diversificare in caso di pareggio?
					if(occurrency[t]< occurrency[selectedTimeslot])
						selectedTimeslot = t;
				}
				
				Vector<Integer> optimalTimeslots = new Vector<Integer>();
				for(int t=0; t<p.getTimeslots(); t++){
					if(occurrency[t] == occurrency[selectedTimeslot]){
						optimalTimeslots.add(t);
					}
				}
				
				//we get a random optimal timeslot
				selectedTimeslot = optimalTimeslots.get(rand.nextInt(optimalTimeslots.size()));
				//descheduliamo gli esami in conflitto con e1 che sono schedulati in
				//selectedTimeslot, ammesso che ce ne siano
				if(occurrency[selectedTimeslot] > 0){
					for(int e2=0; e2<p.getExams(); e2++){
						boolean e2IsInConflictWithE1 = p.areExamsInConflicts(e1.id, e2);
						boolean e2IsInTheSelectedTimeslot = (getGene(e2) == selectedTimeslot);
						
						if (e2IsInConflictWithE1 && e2IsInTheSelectedTimeslot){
							setGene(e2, -1);
							//inseriamo gli esami deschedulati nella stessa lista 
							deschedulatedExamToSchedule.add(p.getSortedExam(e2));
						}
					}
				}
				
				//exam1 is finally positioned
				setGene(e1.id, selectedTimeslot);
				
				
				//nota che nella lista deschedulatedExamToSchedule vanno a finire:
				// - Gli esami che non siamo riusciti a posizionare perché
				// non vi era nessun timeslot disponibile
				// - Gli esami che sono stati deschedulati poiché in
				// conflitto con quelli definiti sopra
				//Nel caso della seconda categoria potrebbe anche verificarsi
				//che occurrency[selectedTimeslot] valga 0 e che quindi,
				//per poterli posizionare, non si debba deschedulare nessuno
			}
		
			
			legal = isLegal();
			//se non è ancora legale reinizializzo tutto
			if(! legal ){
				
				examToSchedule = new PriorityQueue<SortedExam>(p.getSortedExams());
				genes = new Vector<Integer>();
				for(int i=0; i<numOfGenes; i++){
					genes.add(-1);
				}
			}
		}while(!legal);
	}
	
	//versione non ancora funzionante
	/*public void generateFeasibleIndividual(){
		
		//examToSchedule contains the exams to schedule
		
		HashSet<Integer> T = p.getTimeslotSet();
		
		while(! examToSchedule.isEmpty() ){			
			//pick the most 'greedy' exam
			SortedExam e1 = examToSchedule.poll();//not a copy, the same e1
			
			HashSet<Integer> e1TimeslotSet = generateE1TimeslotSet(e1, T);	
			
			int numOfTimeslotToRestore = (int) 0.2 * p.getTimeslots();
			
			while( e1TimeslotSet.isEmpty() ){
				for(int i=0; i<numOfTimeslotToRestore;i++){
					int period;
					do{
						period = rand.nextInt(p.getTimeslots());
					}while(e1TimeslotSet.contains(period));
					
					e1TimeslotSet.add(period);
				}
				
				//deschedule exams e2 that are in a random timeslot picked
				//and reinsert e1 and all e2 there in E1
				
				//examToSchedule.add(e1);
				
				//we pick a random timeslot and we try to make it 
				//available for the exam e1
				
				for(int e2 = 0; e2 < p.getExams(); e2++){
					//if e2 is in conflict with e1 and is placed in one of the restored timeslots
					//we have to deschedulate it
					if(p.areExamsInConflicts(e1.id, e2) && e1TimeslotSet.contains(getGene(e2)) ){
						setGene(e2, -1);
						//get sorted exam with id=e2
						SortedExam se = p.getSortedExam(e2);
						//and reinsert it to the list of exams to schedule
						if(! examToSchedule.contains(se))
							examToSchedule.add(se);
					}
				}
				
			}
			
			
			
			//assign e1 in a random timeslot
			
			Iterator<Integer> it = e1TimeslotSet.iterator();
			for (int t = rand.nextInt(e1TimeslotSet.size());
					t > 0; t--){
				it.next();
			}
			
			int pickedTimeslot = it.next();
			
			setGene(e1.id, pickedTimeslot);
			
			//end assign e1 in a random timeslot

			
		} //end while
		
		
		
	}*/
	
	private PriorityQueue<SortedExam> calculateExamToScheduleCostWeight(){
		PriorityQueue<SortedExam> examToScheduleCostWeight = new PriorityQueue<SortedExam>(11, new CostWeightComparator());
		
		for(int e = 0; e < numOfGenes; e++){
			p.getSortedExam(e).costWeight = getCostWeight(e);
			examToScheduleCostWeight.add(p.getSortedExam(e));
		}
		return examToScheduleCostWeight;
	}
	
	//THIS METHOD MOVES THE SOLUTION INTO THE BEST OF THE NEIGHBORHOOD
	public void localSearch(){
		//if you don't find a better timeslot, stay in the same
		
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
					double var = this.costVariation(e1.getId(), timeslot);
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
		System.out.println("new cost after local search: " + getCost());
	}
	
	//it returns the set of timeslots complementary to:
	//timeslots in which are scheduled all e2 (in conflict with e1)
	private HashSet<Integer> generateE1TimeslotSet(SortedExam e1, HashSet<Integer> timeslotSet){
		//I clone the timeslot set complete, in order to remove bad timeslots from there
		HashSet<Integer> e1TimeslotSet = new HashSet<Integer>(timeslotSet);
		
		for(int e2 = 0; e2 < p.getExams(); e2++){
			if(p.areExamsInConflicts(e1.id, e2) && getGene(e2) >= 0){
				//we should remove the timeslot because it's not
				//available anymore for e1
				e1TimeslotSet.remove(getGene(e2));
			}
		}
		
		//TODO: check if variable nSlotsFree is shared
		//among all structures in the algorithm
		//TODO; nSlotsFree is potentially useless
		e1.nSlotsFree = e1TimeslotSet.size();
		
		return e1TimeslotSet;
	}
	
	//Calculates the advantage of moving an examToMove in the newTimeSlot
	double costVariation(int examToMove, int newTimeslot) {
		double costVar=0;
		//we create a new temporary solution
		//in which the examToMove is moved to the newTimeslot
		Individual newInd = new Individual(this, examToMove, newTimeslot);
		double costWeightOld = this.getCostWeight(examToMove);
		double costWeightNew = newInd.getCostWeight(examToMove);
		costVar = (costWeightNew - costWeightOld);
		return costVar;
	}
}
