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
	PriorityQueue<SortedExam> examToScheduleCostWeight;
	
	
	
	//2) METHODS
	//CONSTRUCTOR
	public Individual(Individual ind){
		this.numOfGenes = ind.numOfGenes;
		this.numOfAlleles = ind.numOfAlleles;
		this.p = ind.p;
		this.genes = new Vector<Integer>(ind.genes);
		examToSchedule = new PriorityQueue<SortedExam>(p.getSortedExams());
		examToScheduleCostWeight = new PriorityQueue<SortedExam>(p.getSortedExamsCostWeight());
	}
	//change the timeslot of exam in newTimeSlot
	public Individual(Individual ind,int exam, int newTimeSlot){
		this.numOfGenes = ind.numOfGenes;
		this.numOfAlleles = ind.numOfAlleles;
		this.p = ind.p;
		this.genes = new Vector<Integer>(ind.genes);
		genes.set(exam, newTimeSlot);
		examToSchedule = new PriorityQueue<SortedExam>(p.getSortedExams());
	}
	public Individual(Problem p) {
		this.p = p;
		numOfGenes = p.getExams();
		numOfAlleles = p.getTimeslots();
		//set all timeslots to an undefined value
		genes = new Vector<Integer>();
		
		for(int i=0; i<numOfGenes; i++){
			genes.add(-1);
		}
		
		examToSchedule = new PriorityQueue<SortedExam>(p.getSortedExams());
	}
	
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
	protected int getCostWeight(int exam1) {
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
			SortedExam se = new SortedExam(p.getSortedExam(exId));
			examToSchedule.add(se);		
		}
	}
	
	//TODO: rimuovi questa intestazione
	//+++inseriti da vincenzo 6-12 after revelations+++
	
	
	
	//TODO: limit number of iteration OF generateFeasibleIndividual
	public void generateFeasibleIndividual(){
		
		//examToSchedule contains the exams to schedule
		
		HashSet<Integer> T = p.getTimeslotSet();
		
		while(! examToSchedule.isEmpty() ){			
			//pick the most 'greedy' exam
			SortedExam e1 = examToSchedule.poll();//not a copy, the same e1
			
			HashSet<Integer> e1TimeslotSet = generateE1TimeslotSet(e1, T);	
			
			if( ! e1TimeslotSet.isEmpty()){
				//assign e1 in a random timeslot
				
				Iterator<Integer> it = e1TimeslotSet.iterator();
				for (int t = rand.nextInt(e1TimeslotSet.size());
						t > 0; t--){
					it.next();
				}
				
				int pickedTimeslot = it.next();
				
				setGene(e1.id, pickedTimeslot);
				
			} else {
				//deschedule exams e2 and reinsert e1 and all e2 in E1
				examToSchedule.add(e1);
				for(int e2 = 0; e2 < p.getExams(); e2++){
					if(p.areExamsInConflicts(e1.id, e2)){
						setGene(e2, -1);
						//get sorted exam with id=e2
						SortedExam se = new SortedExam(p.getSortedExam(e2));
						//and reinsert it to the list of exams to schedule
						examToSchedule.add(se);
					}
				}
				
			}
		} //end while
		
		//System.out.println(this.toString());
	}
	
	public Individual localSearch(Individual startSol){
		//if don't find a better timeslot stay in the same
		Individual newInd=this;
		HashSet<Integer> T = p.getTimeslotSet();
		
		while(! examToScheduleCostWeight.isEmpty() ){ //for each element or the queue
			SortedExam e1 = examToScheduleCostWeight.poll();
			HashSet<Integer> e1TimeslotSet = generateE1TimeslotSet(e1, T);
			int exam1=e1.getId();
			int bestTimeSlot=-1;
			int bestVariation=0;
			if( ! e1TimeslotSet.isEmpty()){
				for(Integer timeSlot: e1TimeslotSet){
					int var=this.costVariation(exam1, timeSlot);
					if(var<bestVariation){
						bestVariation=var;
						bestTimeSlot=timeSlot;
					}
				}
				if(bestTimeSlot!=-1){
					newInd=new Individual(this, exam1, bestTimeSlot);
				}
			}
		} //end while
		return newInd;
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
	int costVariation(int examToMove, int newTimeSlot) {
		int costVar=0;
		Individual newInd=new Individual(this, examToMove, newTimeSlot);
		double costWeightOld=this.getCostWeight(examToMove);
		double costWeightNew=newInd.getCostWeight(examToMove);
		costVar=(int)(costWeightNew - costWeightOld);
		return costVar;
	}
}
