import java.util.*;
//todo: comparatore di soluzioni



class Individual {
	//EXPLANATION OF THE CLASS
	//0) GENERICS:
	//individual = chromosome: a solution, a timetable, a set of timeslots composed by exams
	//gene: a component of the solution, in this case an exam
	//allele: the value of the gene, in this case a timeslot relative to an exam
	//fitness: the objective function value
	
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
	private int[] genes;
	private Vector<HashSet<Integer>> timeslots;
	//fitness: value of how much a solution is good, in this case the objective function value
	private double fitness = 0.0;
	Problem p;
	
	
	
	//2) METHODS
	//CONSTRUCTOR
	public Individual(Problem p) {
		this.p = p;
		numOfGenes = p.getExams();
		numOfAlleles = p.getTimeslots();
		genes = new int[numOfGenes];
		Arrays.fill(genes, -1); //set all timeslots to an undefined value
		
		//INITIALIZING AN EMPTY SET OF TIMESLOTS
		timeslots = new Vector<HashSet<Integer>>(numOfAlleles);
		for(int i=0; i<numOfAlleles; i++) {
			timeslots.add(new HashSet<Integer>());
		}
	}
	
	//TO GENERATE THE SOLUTION RANDOMLY (only in the form a)
	public void generateIndividual() {
		for(int e=0; e<genes.length; e++) {
			//we assign the random timeslot chosen for the exam e of the solution
			genes[e] = rand.nextInt(numOfAlleles);
			
		}
		System.out.println("Num of confl: " + FitnessFunct.nOfConflicts(this));
		this.toString();
	}
	
	
	//TO REPRESENT A SOLUTION IN COMMAND LINE
	@Override
	public String toString() {
		String chromosome = "";
		for(int i=0; i<size(); i++) {
			chromosome += (i + 1) + " " + (genes[i] + 1) + "\n";
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
		return genes.length;
	}
	
	public int getGene(int i) {
		return genes[i];
	}
	
	public void setGene(int i, int allele) {
		genes[i] = allele;
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
	protected boolean isLegal() {
		return FitnessFunct.isLegal(this);
	}
	
	
	
	//TODO: rimuovi questa intestazione
	//+++inseriti da vincenzo 6-12 after revelations+++
	
	
	public void generateFeasibleIndividual(){
		
		PriorityQueue<SortedExam> E1 = new PriorityQueue<SortedExam>(p.getSortedExams());
		HashSet<Integer> T = p.getTimeslotSet();
		
		while(! E1.isEmpty() ){			
			//pick the most 'greedy' exam
			SortedExam e1 = E1.poll();//not a copy, the same e1
			
			HashSet<Integer> e1TimeslotSet = generateE1TimeslotSet(e1, T);	
			
			if( ! e1TimeslotSet.isEmpty()){
				//assign e1 in a random timeslot
				Iterator<Integer> it = e1TimeslotSet.iterator();
				for (int t = rand.nextInt(e1TimeslotSet.size());
						t > 0; t--){
					it.next();
				}
				genes[e1.id] = it.next();
				
			} else {
				//deschedule exams e2 and reinsert e1 and all e2 in E1
				E1.add(e1);
				for(int e2 = 0; e2 < p.getExams(); e2++){
					if(p.areExamsInConflicts(e1.id, e2)){
						genes[e2] = -2;
						SortedExam se = new SortedExam(p.getSortedExam(e2));
						E1.add(se);
					}
				}
				
			}
		} //end while
		
		System.out.println(this.toString());
	}
	
	//it returns the set of timeslots complementary to:
	//timeslots in which are scheduled all e2 (in conflict with e1)
	private HashSet<Integer> generateE1TimeslotSet(SortedExam e1, HashSet<Integer> timeslotSet){
		//I clone the timeslot set complete, in order to remove bad timeslots from there
		HashSet<Integer> e1TimeslotSet = new HashSet<Integer>(timeslotSet);
		
		for(int e2 = 0; e2 < p.getExams(); e2++){
			if(p.areExamsInConflicts(e1.id, e2) && genes[e2] >= 0){
				//we should remove the timeslot because it's not
				//available anymore for e1
				e1TimeslotSet.remove(genes[e2]);
			}
		}
		
		//TODO: check if variable nSlotsFree is shared
		//among all structures in the algorithm
		//TODO; nSlotsFree is potentially useless
		e1.nSlotsFree = e1TimeslotSet.size();
		
		return e1TimeslotSet;
	}
}
