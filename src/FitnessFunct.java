//THIS CLASS HANDLES THE OBJECTIVE FUNCTION
class FitnessFunct {
	private static Problem p;
	private static double w[] = {0, 16.0, 8.0, 4.0, 2.0, 1.0};
	

	//IT CALCULATES THE OBJECTIVE FUNCTION VALUE OF AN INDIVIDUAL
	protected static double getCost(Individual timetable) {
		double cost= 0.0;
		int numExams= p.getNumOfExams();
		
		for(int exam1=0; exam1<numExams; exam1++) {
			int period1 = timetable.getTimeslotOfExam(exam1);
			for(int exam2=exam1+1; exam2<numExams; exam2++) {
				int period2= timetable.getTimeslotOfExam(exam2);
				int d = Math.abs(period1 - period2);
				if(d <= 5)
					cost += w[d]*p.getConflicts(exam1, exam2);
			}
		}
		return cost/p.getNumOfStudents();
	}
	
	//it calculates the costWeight of the exam1
	//i.e. the impact of exam1 on the total objective function
	protected static double getCostWeight(Individual timetable, int exam1) {
		double costWeight= 0;
		int numExams= p.getNumOfExams();
		int period1= timetable.getTimeslotOfExam(exam1);
		
		for(int exam2=0; exam2<numExams; exam2++) {
			if(exam1!=exam2) {
				int period2=timetable.getTimeslotOfExam(exam2);
				int d = Math.abs(period1 - period2);
				if(d <= 5)
					costWeight += w[d]*p.getConflicts(exam1, exam2);
			}
		}
		return (costWeight/p.getNumOfStudents()/2);
		// divide by two because we consider twice a conflict
	}
	
	//IT CHECKS IF THE GIVEN SOLUTION IS LEGAL
	protected static boolean isLegal(Individual ind) {
		int numExams= p.getNumOfExams();
		
		for(int exam1=0; exam1<numExams; exam1++) {
			int period1 = ind.getTimeslotOfExam(exam1);
			for(int exam2=exam1+1; exam2<numExams; exam2++) {
				int period2 = ind.getTimeslotOfExam(exam2);
				if(period1 == period2) {
					if(p.areExamsInConflicts(exam1, exam2))
						return false; 
				}
				if(period1 == -1 || period2 == -1){
					return false; 
				}
			}
		}
		return true;
	}
	
	
	//OTHER METHODS
	public static void setProblem(Problem p) {
		FitnessFunct.p = p;
	}
	
	//it returns the penalty due to have exam1 in timeslot1 and exam2 in timeslot2
	//we assume that exam1 and exam2 are in conflict
	protected static double getPenalty(int exam1, int timeslot1, int exam2, int timeslot2){
		int d = Math.abs(timeslot1 - timeslot2);
		double penalty = 0;
		if(d <=5){
			penalty = w[d]*p.getConflicts(exam1, exam2);
		}
		return penalty;
	}
}
