//THIS CLASS HANDLES THE OBJECTIVE FUNCTION
class FitnessFunct {
	private static Problem p;
	private static final int MAX_COEFF = 100;
	private static final int MAX_FIT = 2; //greater than 1
	private static final int STEP = 1; 
	private static double w[] = {MAX_COEFF, 16.0, 8.0, 4.0, 2.0, 1.0};
	
	//IT RETURNS THE FITNESS OF A SOLUTION (THAT IS DIFFERENT FROM THE OBJ FUNCT VALUE)
	public static double evaluate(Individual timetable) {
		//WE SET A MAXIMUM FITNESS VALUE:
		//IF THE OBJ FUNCT VALUE IT'S LOWER THAN THE MAXIMUM FIT
		//THE HIGHER THE OBJ FUNCT VALUE, THE LOWER IS THE FITNESS
		//OTHERWISE (THE OBJ FUNCT IS VERY HIGH) WE CONSIDER IT VERY FIT
		double fitness;
		double cost = getCost(timetable);
		if(cost != 0){
			//it's for sure less than 1
			fitness = 1/cost; 
		}else{
			fitness = MAX_FIT - STEP;
		}

		//A LEGAL SOLUTION MUST HAVE A HIGHER FIT TO NEVER SELECT NOT LEGAL SOLUTIONS
		if(isLegal(timetable))
			fitness = fitness + STEP;
		
		return fitness;
	}
	

	//IT CALCULATES THE OBJECTIVE FUNCTION VALUE
	protected static double getCost(Individual timetable) {
		double cost= 0.0;
		int numExams= p.getExams();
		
		for(int exam1=0; exam1<numExams; exam1++) {
			int period1 = timetable.getGene(exam1);
			for(int exam2=exam1+1; exam2<numExams; exam2++) {
				int period2= timetable.getGene(exam2);
				int d = Math.abs(period1 - period2);
				if(d <= 5)
					cost += w[d]*p.getConflicts(exam1, exam2);
			}
		}
		return cost/p.getStudents();
	}
	
	//it calculates the costWeight of the exam1
	protected static int getCostWeight(Individual timetable, int exam1) {
		int costWeight= 0;
		int numExams= p.getExams();
		int period1= timetable.getGene(exam1);
		
		for(int exam2=0; exam2<numExams; exam2++) {
			if(exam1!=exam2) {
				int period2=timetable.getGene(exam2);
				int d = Math.abs(period1 - period2);
				if(d <= 5)
					costWeight += w[d]*p.getConflicts(exam1, exam2);
			}
		}
		return (int) (costWeight/p.getStudents());
	}
	
	//IT CHECKS IF THE GIVEN SOLUTION IS LEGAL
	protected static boolean isLegal(Individual timetable) {
		int numExams= p.getExams();
		
		for(int exam1=0; exam1<numExams; exam1++) {
			int period1 = timetable.getGene(exam1);
			for(int exam2=exam1+1; exam2<numExams; exam2++) {
				int period2 = timetable.getGene(exam2);
				if(period1 == period2) {
					if(p.areExamsInConflicts(exam1, exam2))
						return false; 
				}
			}
		}
		return true;
	}
	
	//it counts the number of conflicts in the same timeslot
	protected static int nOfConflicts(Individual timetable) {
		int numExams= p.getExams();
		int count = 0;
		
		for(int exam1=0; exam1<numExams; exam1++) {
			int period1 = timetable.getGene(exam1);
			for(int exam2=exam1+1; exam2<numExams; exam2++) {
				int period2 = timetable.getGene(exam2);
				if(period1 == period2) {
					if(p.areExamsInConflicts(exam1, exam2))
						count++; 
				}
			}
		}
		
		return count;
	}
	
	//OTHER METHODS
	public static void setProblem(Problem p) {
		FitnessFunct.p = p;
	}
}
