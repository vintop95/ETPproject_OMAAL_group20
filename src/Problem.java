import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Vector;

class Problem {
	private int N_EXAMS;
	private int N_STUDENTS;
	private int N_TIMESLOTS;
	private int[][] conflictMatrix;
	
	public Problem(String instanceName) {
		if(instanceName != null) {
			N_EXAMS = readNExams(instanceName);
			N_TIMESLOTS = readNTimeslots(instanceName);
			generateConflicts(instanceName);
		}
	}
	//
	
	private int readNTimeslots(String instanceName){
		int n=0;

		try{
			Scanner in = new Scanner(new File(instanceName + ".slo"));
			n = in.nextInt();
			in.close();
		} catch(FileNotFoundException e) {
            System.out.println("File not found: "+ instanceName + ".slo");
            System.exit(-1);
        } catch(NoSuchElementException e){
        	System.out.println("Empty file: " + instanceName + ".slo");
        	System.exit(-1);
        }
		
		return n;//ritorna n
	}

	private int readNExams(String instanceName){
		//WE COUNT THE ROWS OF THE FILE .exm
		//WE ASSUME THAT EXAMS GO FROM 0 TO N_EXAMS-1
		int nExams=0;
		
		try{
			//counting rows of the file. it works, trust me!
			LineNumberReader count = new LineNumberReader(new FileReader("./" + instanceName + ".exm"));
			while (count.skip(Long.MAX_VALUE) > 0);
			nExams = count.getLineNumber() + 1; // +1 because line index starts at 0  
			count.close();
		} catch(FileNotFoundException e) {
            System.out.println("File not found: " + instanceName + ".exm");
            System.exit(-1);
        } catch(IOException e){
        	System.out.println("IOException in file: " + instanceName + ".exm");
        	System.exit(-1);
        }
		
		return nExams;
	}
	private void generateConflicts(String instanceName){
		//WE ASSUME THAT STUDENTS GO FROM 0 TO N_STUDENTS-1
		
			//a vector of students that includes in each element
			//a list of exams in which the student is enrolled
			//Vector allows us to insert numbers in a dynamic array
			Vector<Vector<Integer>> studentList = new Vector<Vector<Integer>>();
			conflictMatrix = new int[N_EXAMS][N_EXAMS];
			try {
			//reading exams for each student
				Scanner in = new Scanner(new File(instanceName + ".stu"));
			
				while (in.hasNextLine()){	
					//READ SID WITHOUT INITIAL 's'
					StringBuilder sb = new StringBuilder(in.next());
					sb.deleteCharAt(0);
					int sId = Integer.parseInt(sb.toString()) - 1;//we start from studentId 0
																//READ exam FOR THAT sId
					String s = in.next();
					s = s.replaceFirst("^0+(?!$)", ""); //regex that delete leading zeros
					int exam = Integer.parseInt(s) - 1;//due to Vector index convention, we start
													//counting from exam 0
			
					//if the student has never enrolled until now
					if (sId >= studentList.size()){
						studentList.add(new Vector<Integer>());
					}
					//add the exam in the list
					studentList.get(sId).add(exam);
				}
				in.close();
			}catch(FileNotFoundException e) {
	            System.out.println("File not found: " + instanceName + ".stu");
	            System.exit(-1);
	        }
			N_STUDENTS = studentList.size();
			//POPULATE CONFLICT MATRIX n
			for(int s=0; s<N_STUDENTS; s++) {
				//for each student
				//we iterate through all the possible couples of exam in which he's enrolled
				for(int i=0; i<(studentList.get(s).size() - 1); i++) {
					for(int j=i+1; j<studentList.get(s).size(); j++) {
						int e1, e2; //the two exams involved
						e1= studentList.get(s).get(i); 
						e2= studentList.get(s).get(j);
						conflictMatrix[e1][e2]++;//increment the corresponding value in the conflict matrix
						conflictMatrix[e2][e1]++;//in both cases
						
					}
				}
			} 
	}
	public int getConflicts(int e1, int e2) {
		return conflictMatrix[e1][e2];
	}
	public boolean areExamsInConflicts(int e1, int e2) {
		if(getConflicts(e1, e2) > 0)
			return true;
		return false;
	}
	public int getExams() {return N_EXAMS;}
	public int getStudents() {return N_STUDENTS;}
	public int getTimeslots() {return N_TIMESLOTS;}
}// end of class
