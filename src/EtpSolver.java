
<<<<<<< HEAD
//testtt

=======
//ttt
>>>>>>> 9e559257575d3703490a9e6ae1ea20cb1fd6e75e

//TODO: CREATE DEDICATED CLASSES FOR EVERY PROCESS OF THE ALGORITHM



//TODO: gestire il tempo in questo modo 
/*long startTime = System.currentTimeMillis();

...
...
// operazioni ...
...
...

long endTime = System.currentTimeMillis();
long seconds = (endTime - startTime) / 1000;
*/
public class EtpSolver {
	
	//SHOULD BE OK
	
	public static void main(String[] args) {
		//usage: java -jar EtpSolver.jar instancename -t timelimit
		System.out.println("program is working..");
		if(args.length != 3) {
			System.out.println("usage: java -jar EtpSolver.jar instancename -t timelimit");
			System.exit(-1);
		}
		final int popSize = 50;
		final int maxNonImprovingIterations = 1000;
		final String instanceName = args[0];
		final double timeLimit = Integer.parseInt(args[2]);
		long startTime= System.currentTimeMillis();
		
		//Conflict matrix
		//TODO: USE ANOTHER DATA STRUCTURE INSTEAD OF MATRIX FOR CONFLICT MATRIX?
		Problem p = new Problem(instanceName);
		GeneticAlgorithm.setProblem(p);
		Population myPop = new Population(popSize, p, true);
		double timeElapsed = updateTimeElapsed(startTime);
		while(timeElapsed < timeLimit /* && 
			GeneticAlgorithm.getNonImprovingIterationsCount() < maxNonImprovingIterations*/){
			myPop = GeneticAlgorithm.evolvePopulation(myPop);
			timeElapsed = updateTimeElapsed(startTime);
			
		}
		System.out.println("Best solution found in " + timeElapsed + " seconds:");
		System.out.println(myPop.getFittest());
		//generatePopulation(population, p)
		//TODO: FUNCTION TO CALCULATE OBJECIVE FUNCT VALUE OF A S
		
	}
	
	private static double updateTimeElapsed(long startTime){
		long currentTime = System.currentTimeMillis();
		double elapsedTime = (currentTime - startTime)/1000;
		return elapsedTime;
	}

}//end of class
