import java.util.*;
import java.io.*;

//TODO: CREATE DEDICATED CLASSES FOR EVERY PROCESS OF THE ALGORITHM
public class EtpSolver {
	
	//SHOULD BE OK
	static int readNTimeslots(String istanceName){
		int n=0;

		try{
			Scanner in = new Scanner(new File(istanceName + ".slo"));
			n = in.nextInt();
			in.close();
		} catch(FileNotFoundException e) {
            System.out.println("FileNotFound");
        } catch(NoSuchElementException e){
        	System.out.println("Empty exm file.");
        }
		
		return n;
	}
	
	
	//TODO: CAN WE JUST ASSUME THAT EXAMS STARTS FROM 0 TO N_EXAMS-1?
	static int readNExams(String istanceName){
		//WE COUNT THE ROWS OF THE FILE .stu
		//WE ASSUME THAT EXAMS GO FROM 0 TO N_EXAMS-1
		int nExams=0;
		
		try{
			//counting rows of the file. it works, trust me!
			LineNumberReader count = new LineNumberReader(new FileReader("./" + istanceName + ".exm"));
			while (count.skip(Long.MAX_VALUE) > 0);
			nExams = count.getLineNumber() + 1; // +1 because line index starts at 0  
			count.close();
		} catch(FileNotFoundException e) {
            System.out.println("FileNotFound");
        } catch(IOException e){
        	System.out.println("IOException.");
        }
		
		return nExams;
	}

	static void generateConflicts(String istanceName, int[][] n){
		//WE ASSUME THAT STUDENTS GO FROM 0 TO N_EXAMS-1
		try{
			//a vector of students that includes in each element
			//an automatically ordered list of exams in which the student is enrolled
			//treeset allows us to insert numbers in an automatically sorted list
			Vector<TreeSet<Integer>> studentList = new Vector<TreeSet<Integer>>();	
			
			//reading exams for each student
			Scanner in = new Scanner(new File(istanceName + ".stu"));

			while (in.hasNextLine()){	
				
				//READ SID WITHOUT INITIAL 's'
				StringBuilder sb = new StringBuilder(in.next());
				sb.deleteCharAt(0);
				int sId = Integer.parseInt(sb.toString()) - 1;//we start from studentId 0
				
				//READ exam FOR THAT sId
				String s = in.next();
				s = s.replaceFirst("^0+(?!$)", ""); //regex that delete leading zeros
				int exam = Integer.parseInt(s);
			
				//if the student has never enrolled until now
				if (sId >= studentList.size()){
					studentList.add(new TreeSet<Integer>());
				}
				//add the exam in the list
				studentList.get(sId).add(exam);
			}
			
			//TODO: POPULATE CONFLICT MATRIX n
			
			in.close();	
		} catch(FileNotFoundException e) {
            System.out.println("FileNotFound");
        }
	}
	
	public static void main(String[] args) {
		
		System.out.println("program is working..");
		
		if (args[0]==null){
			System.out.println(args[0] + "instance name not inserted");
			System.exit(-1);
		}
		if(args[2]==null){
			System.out.println("timelimit not inserted");
			System.exit(-1);
		}
		
		final String instanceName = args[0];
		final double timeLimit = Integer.parseInt(args[2]);
		
		final int N_EXAMS = readNExams(instanceName);
		
		final int N_TIMESL = readNTimeslots(instanceName);
		
		
		//Conflict matrix
		//TODO: USE ANOTHER DATA STRUCTURE INSTEAD OF MATRIX FOR CONFLICT MATRIX?
		int[][] n = new int[N_EXAMS][N_EXAMS];
		generateConflicts(instanceName, n);
		

	}

}
