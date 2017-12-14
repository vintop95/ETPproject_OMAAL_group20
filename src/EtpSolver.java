//CLASS THAT CONTAINS THE MAIN

public class EtpSolver {
	
	static public double timeLimit;
	//SHOULD BE OK
	
	public static void main(String[] args) {
		//usage: java -jar EtpSolver.jar instancename -t timelimit
		System.out.println("program is working..");
		if(args.length != 3) {
			System.out.println("usage: java -jar EtpSolver.jar instancename -t timelimit");
			System.exit(-1);
		}
		
		final int popSize = 15;
		final String instanceName = args[0];
		timeLimit = Integer.parseInt(args[2]);
		//we start counting time
		long startTime= System.currentTimeMillis();
		
		
		//WE DEFINE A PROBLEM AND WE INSERT IT IN THE ALGORITHM (STATIC CLASS)
		Problem p = new Problem(instanceName);
		GeneticAlgorithm.setProblem(p);
		Population myPop = new Population(popSize, p, true);
		
		double timeElapsed = updateTimeElapsed(startTime);
		while(timeElapsed < timeLimit){
			
			myPop = GeneticAlgorithm.evolvePopulation(myPop);
			
			System.out.println("best solution now: " + p.getBestInd().getCost());

			timeElapsed = updateTimeElapsed(startTime);
			
		}
		System.out.println("Best solution found in " + timeElapsed + " seconds:");
		System.out.println(p.getBestInd());
		p.checkOutputFile(p.getBestInd());
	}
	
	public static double updateTimeElapsed(long startTime){
		long currentTime = System.currentTimeMillis();
		double elapsedTime = (currentTime - startTime)/1000;
		return elapsedTime;
	}

}//end of class
