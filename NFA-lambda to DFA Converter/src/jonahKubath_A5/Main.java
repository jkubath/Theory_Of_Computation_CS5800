 package jonahKubath_A5;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
	//Create a HashMap to hold the start variables
	static HashMap<String, Node> map = new HashMap<String, Node>(); //Maps a node the index in the table
	static HashMap<String, Node> nMap = new HashMap<String, Node>(); //Maps a node the index in the table
	/* HashMap indexed by current node
	 * Returns a HashMap
	 * 	HashMap indexed by transition value (a, b, lambda)
	 * 	Returns an ArrayList of possible transitions
	 */	
	static HashMap<Integer, HashMap<Integer, ArrayList<String>>> table = new HashMap<Integer, HashMap<Integer, ArrayList<String>>>();
	static HashMap<Integer, HashMap<Integer, ArrayList<String>>> nTable = new HashMap<Integer, HashMap<Integer, ArrayList<String>>>();
	static HashMap<String, Integer> values = new HashMap<String, Integer>(); // Transition values
	static String finalStates[] = null;
	static String input = ""; //Input sentence
	static String start = "q0"; //Starting position
	
	
	public static void main(String[] args) throws FileNotFoundException {
			//System.out.println("Running DFA");
			String f = "";
			int fileCount = 0; //Which file to read data from
			if(fileCount == 0) {
				f = "./a5_input/ex_5.6.1.txt";
			}
			else if(fileCount == 1) {
				f = "./a5_input/ex_5.6.2.txt";
			}
			else if(fileCount == 2) {
				f = "./a5_input/ex_5.6.3.txt";
			}
			else {
				f = "./a5_input/test.txt";
			}
			
			if(args.length > 0) {
				f = args[0];
			}
			
			readData(f);
			//printData();
			
			convertDFA();
			
			//printNewData();
			
			printTable();
	}
	
	/**
	 * The data help in the original table and map global variables.  This NFA-lambda is then
	 * converted into a DFA where all transitions are defined and only go to one node.
	 * The final map is held in the nTable for transitions and nMap for node names and values
	 */
	public static void convertDFA() {
		int count = 0; // Total number of nodes in the new DFA
		
		//Add any nodes that needs to be defined to this list
		ArrayList<String> nodes = new ArrayList<String>();
		
		//Initialize q0' to start + any nodes on lambda transitions
		ArrayList<String> lambdaTrans = getValues(start, "lambda");
		
		//Create the new node
		String nStart = makeNode(start, lambdaTrans);
		
		//System.out.println("q0': " + nStart);
		nodes.add(nStart);
		
		String current = "";
		//Convert all nodes to DFA
		while(nodes.size() > 0) {
			current = nodes.remove(0);
			//System.out.println("Current Build: " + current);
			
			//Nodes doesn't already exist
			if(nMap.get(current) == null) {
				//Break the current node into the individual nodes
				String split[] = current.split(",");
				//Are any of the individual nodes final states?
				boolean isFinal = false;
				int index = 0;
				while(!isFinal && index < split.length) {
					if(searchFinalStates(finalStates, split[index])) {
						isFinal = true;
					}
					index++;
				}
				
				//Add the node the the new HashMap
				nMap.put(current, new Node(count, isFinal));
				
				
				//Determine where all the transitions can go
				for (HashMap.Entry<String, Integer> entry : values.entrySet()) {
					//Don't add lambda states
					if(entry.getKey().compareTo("lambda") == 0) {
						continue;
					}
					
					int transition = entry.getValue();
					ArrayList<String> transList = new ArrayList<String>();
					int curNode = 0; // Integer value of the node
					//Iterate through every node in the combined node
					for(int i = 0; i < split.length; i++) {
						curNode = map.get(split[i]).getIndex();
			
						// List of possible transitions of the current node
						ArrayList<String> curTrans = table.get(curNode).get(transition);
						//Add all lambda transitions
						ArrayList<String> lambda = table.get(curNode).get(values.get("lambda"));
						
						if(curTrans != null) {
							if(lambda != null)
								curTrans.addAll(lambda);
						}
						else if(lambda != null) {
							curTrans = lambda;
						}
						
						if(curTrans != null && curTrans.size() != 0) {
							// Add all the possible transitions to the list
							for(int j = 0; j < curTrans.size(); j++) {
								//If the node isn't already added to the list, add it
								if(!transList.contains(curTrans.get(j))) {
									transList.add(curTrans.get(j));
								}
							}
						}
						
					} // Done finding all possible transitions
					
					// Sort to prevent duplicate nodes in different orders
					transList.sort(null);
					/* In a DFA, all possible transitions will go to one
					 * node that contains those nodes.  This will be newNode
					 */
					String combinedNode = makeNode(null, transList);
					//System.out.println("Transition " + entry.getKey() + ": '" + combinedNode + "'");
					transList.clear();
					
					//If the current node goes to null, we need a trap state
					if(combinedNode == null) {
						//Go to trap state
						String trapName = "TRAP";
						//We need to create the trap state
						if(nMap.get(trapName) == null) {
							int trapIndex = -1;
							Node trap = new Node(trapIndex, false);
							nMap.put(trapName, trap);
							//HashMap to hold the trap state transitions
							HashMap<Integer, ArrayList<String>> map = new HashMap<Integer, ArrayList<String>>();
							//Every transition in the trap state goes to itself
							for (HashMap.Entry<String, Integer> inputValue : values.entrySet()) {
								//Don't add lambda transitions
								if(inputValue.getKey().compareTo("lambda") == 0) {
									continue;
								}
								ArrayList<String> list = new ArrayList<String>();
								list.add(trapName);
								map.put(inputValue.getValue(), list);
							}
							
							nTable.put(trapIndex, map);
						}
						
						transList.add(trapName);
						
					}
					else {
						transList.add(combinedNode);
					}
					
					if(nTable.get(count) == null) {
						//Create the HashMap
						HashMap<Integer, ArrayList<String>> map = new HashMap<Integer, ArrayList<String>>();
						map.put(transition, transList);
						nTable.put(count, map);
					}
					else {
						//Add the transition list to the nodes HashMap
						nTable.get(count).put(transition, transList);
					}
					
					/* The node is not defined, we add it to the queue to be defined
					 * Set change to true so another iteration is taken
					 */
					if(combinedNode != null && nMap.get(combinedNode) == null) {
						nodes.add(combinedNode);
					}
					
					
				} // Done defining all transitions for the current node
				
				count++; // Iterate the number of total nodes
				
				
				
			} // done creating the current node
			
			
			
		} // All possible nodes have been created
		
	}
		
	/**
	 * Read the data from the file
	 * Line 0: number of nodes
	 * Line 1: Final states
	 * Line 2 - n nodes: current node and then nodes evaluated with each input
	 * 		comma separated for each input value and space separated for the list
	 * 		Example:
	 * 			q0,q1 q2,q3,q4 q5
	 * 		Node: q0
	 * 		Transition on 'a': q1 and q2
	 * 		Transition on 'b': q3
	 * 		Transition on 'lambda': q4 and q5
	 * 		
	 * Next Line: input values
	 * Final line: input string
	 * @param filename The filename to open and read
	 * @throws FileNotFoundException File wasn't found
	 */
	public static void readData(String filename) throws FileNotFoundException {
		File f = null;
		Scanner scan = null;
		try {
			f = new File(filename);
			scan = new Scanner(f);
		}
		catch(FileNotFoundException e) {
			System.out.println("File: " + filename + " was not found");
			System.exit(0);
		}
		
		boolean cont = true; // Is there more data to process
		boolean isFinal = false; //Is the node a final state
		String line = ""; // Temporary buffer
		String split[]; // buffer split by spaces
		int count = 0; // Count of the current node
		int total = 0; // Total number of nodes
		
		//Get the number of nodes from the first line
		line = scan.nextLine();
		total = Integer.parseInt(line);
		//Read the final states
		line = scan.nextLine();
		finalStates = line.split(",");
		
		//Read the table
		while(cont && count < total) {
			try {
				//Get the line
				line = scan.nextLine();
				//Break it up and if empty == ""
				split = line.split(",", -1);
				
				isFinal = searchFinalStates(finalStates, split[0]);
				
				map.put(split[0], new Node(count, isFinal));
				
				//Array to hold transitions split by ","
				String temp[] = null;
				//HashMap to hold all the possible transitions
				HashMap<Integer, ArrayList<String>> trans = new HashMap<Integer, ArrayList<String>>();
				for(int i = 0; i < split.length - 1; i++) {
					//No transitions for this input
					if(split[i + 1].compareTo("") == 0) {
						trans.put(i, null);
					}
					//Add all the transitions to one array list
					else {
						temp = split[i + 1].split(" ");
						//Hold the possible nodes to go to
						ArrayList<String> list = new ArrayList<String>();
						for(int j = 0; j < temp.length; j++) {
							list.add(temp[j]);
						}
						trans.put(i, list);
					}
				}
				table.put(count, trans);
				
				count++;
			}
			catch(NoSuchElementException e) {
				cont = false;
			}
		}
		
		//Read the input values
		line = scan.nextLine();
		split = line.split(",");
		int i = 0;
		for(i = 0; i < split.length; i++) {
			values.put(split[i], i);
		}
		//i++;
		values.put("lambda", i);
		
		input = scan.nextLine();
		input = input.trim();
		
		scan.close();
	}

	/**
	 * Search the array of final states for the given string
	 * @param states The array that contains all the final states
	 * @param name The name of the node to look for
	 * @return True if the node is a final state, false otherwise
	 */
	public static boolean searchFinalStates(String states[], String name) {
		boolean val = false;
		//Iterate through the final states
		for(int i = 0; i < states.length; i++) {
			if(states[i].compareTo(name) == 0) {
				val = true;
				break;
			}
		}
		
		return val;
	}
	
	/**
	 * HashMap iteration taken from:
	 * 	https://stackoverflow.com/questions/1066589/iterate-through-a-hashmap
	 * 
	 * Iterate over the nodes 
	 * Print: Node name, each transition, and whether or not it is a final state
	 */
	public static void printData() {
		System.out.println("Print Data");
		ArrayList<String> keys = new ArrayList<String>();
		
		//Print header information
		System.out.printf("%-5s", "Key");
		for(String key : values.keySet()) {
			System.out.printf("%-15s", key);
			keys.add(key);
		}
		System.out.println("isFinal");
		
		//Print table
		for (HashMap.Entry<String, Node> entry : map.entrySet()) {
		    String key = entry.getKey();
		    Node value = entry.getValue();
		    
		    System.out.printf("%-5s", key);
		    HashMap<Integer, ArrayList<String>> temp = table.get(value.getIndex());
		    //for(HashMap.Entry<Integer, ArrayList<String>> entry1 : temp.entrySet()) {
		    for(int i = 0; i < keys.size(); i++) {
		    	ArrayList<String> values1 = temp.get(values.get(keys.get(i)));
		    	String val = "";
		    	if(values1 != null) {
			    	for(int j = 0; j < values1.size(); j++) {
			    		val += values1.get(j) + " ";
			    	}
		    	}
		    	System.out.printf("%-15s", val);
		    	
		    }
		    System.out.println(value.isFinal());
		}
		
		
		System.out.println();
	}
	
	/**
	  * HashMap iteration taken from:
	  * 	https://stackoverflow.com/questions/1066589/iterate-through-a-hashmap
	  * 
	  * Iterate over the nodes 
	  * Print: Node name, each transition, and whether or not it is a final state
	  */
	public static void printNewData() {
		System.out.println("Print New DFA Data");
		ArrayList<String> keys = new ArrayList<String>();
		
		//Print header information
		System.out.printf("%-15s", "Key");
		for(String key : values.keySet()) {
			System.out.printf("%-15s", key);
			keys.add(key);
		}
		System.out.println("isFinal");
		
		//Print table
		for (HashMap.Entry<String, Node> entry : nMap.entrySet()) {
		    String key = entry.getKey();
		    Node value = entry.getValue();
		    
		    System.out.printf("%-15s", key);
		    HashMap<Integer, ArrayList<String>> temp = nTable.get(value.getIndex());
		    for(int i = 0; i < keys.size(); i++) {
		    	ArrayList<String> values1 = temp.get(values.get(keys.get(i)));
		    	String val = "";
		    	if(values1 != null) {
			    	for(int j = 0; j < values1.size(); j++) {
			    		val += values1.get(j) + " ";
			    	}
		    	}
		    	System.out.printf("%-15s", val);
		    	
		    }
		    System.out.println(value.isFinal());
		}
		
		
		System.out.println();
	}
	
	/**
	 * Return the ArrayList of possible nodes that the current node 
	 * can go to on the given transition
	 * @param cur Current Node
	 * @param trans Transition value
	 * @return ArrayList of possible nodes
	 */
	public static ArrayList<String> getValues(String cur, String trans) {
		if(map.get(cur) == null) {
			System.out.println("Starting node \"" + cur + "\" not found");
			System.out.println("This can be set in the global variable at the top of the program");
			System.exit(0);
		}
		//Get the index of the current node
		int index = map.get(cur).getIndex();
		
		//Get the possible transitions of the current node
		HashMap<Integer, ArrayList<String>> possibleTrans = table.get(index);
		
		//Get the index of the transition
		index = values.get(trans);
		
		//Return the list of the nodes that the current node could go to on the transition
		return possibleTrans.get(index);
	}
	
	/**
	 * Takes in the current node and an ArrayList of possible nodes.
	 * This function will combine them into one string separated by ','
	 * @param cur Current node - can be null
	 * @param lambdaTrans List of possible nodes
	 * @return String of comma separated nodes
	 */
	public static String makeNode(String cur, ArrayList<String> lambdaTrans) {
		ArrayList<String> newStart = new ArrayList<String>();
		
		if(cur != null)
			newStart.add(cur);
		
		if(lambdaTrans != null && lambdaTrans.size() != 0) {
			for(int i = 0; i < lambdaTrans.size(); i++) {
				String temp = lambdaTrans.get(i);
				if(!newStart.contains(temp)) {
					newStart.add(temp);
				}
			}
		}
		
		String returnString = null;
		for(int i = 0; i < newStart.size(); i++) {
			if( i == 0) {
				returnString = newStart.get(i);
			}
			else {
				returnString += "," + newStart.get(i);
			}
		}
		
		
		return returnString;
	}
	
	/**
	 * Print the table that can be used in the DFA simulator from Assignment 4
	 * Table format
	 * Line 1: Total number of nodes
	 * Line 2: Final states
	 * Line 3 - n: Nodes and their transitions separated by spaces
	 * 	Example:
	 * 	[q0] [q1] [q2]
	 * 	Explanation
	 * 	[q0] goes to [q1] on first transition
	 * 	[q0] goes to [q2] on second transition
	 * Second to last line: input values
	 * 	a b c
	 * Last Line: input string into the DFA
	 */
	public static void printTable() {
		int nodeCount = nMap.size();
		
		System.out.println(nodeCount);
		
		String finalStates = null;
		
		//Node is a final state
		for(HashMap.Entry<String, Node> entry : nMap.entrySet()) {
			if(entry.getValue().isFinal) {
				if(finalStates == null) {
					finalStates = "[" + entry.getKey() + "]";
				}
				else {
					finalStates += " [" + entry.getKey() + "]";
				}
			}
		}
		
		System.out.println(finalStates);
		
		//Array List of possible transitions
		ArrayList<String> keys = new ArrayList<String>();
		
		// Print the transition table
		String node = "";
		for(HashMap.Entry<String, Node> entry : nMap.entrySet()) {
			int curNode = entry.getValue().getIndex();
			node = "[" + entry.getKey() + "]"; // Add the current node the first part of the string
			//System.out.println(node);
			
			HashMap<Integer, ArrayList<String>> allTransitions = nTable.get(curNode);
			
			ArrayList<String> oneTransition;
			for(HashMap.Entry<String, Integer> value : values.entrySet()) {
				if(value.getKey().compareTo("lambda") == 0) {
					continue;
				}
				oneTransition = allTransitions.get(value.getValue());
				
				node += " ";
				if(oneTransition != null) {
					for(int i = 0; i < oneTransition.size(); i++) {
						if(i != 0) {
							node += " ";
						}
						node += "[" + oneTransition.get(i) + "]";
					
						// Add to the input keys so they are printed in the same order
						if(!keys.contains(value.getKey())) {
							keys.add(value.getKey());
						}
					}
				}
			}
			System.out.println(node);
			
		}
		
		
		//Print possible transition values
		String inputs = null;
		String testInput = "";
		for(int i = 0; i < keys.size(); i++) {
			if(inputs == null) {
				inputs = keys.get(i);
			}
			else {
				inputs += " " + keys.get(i);
			}
			
			testInput += keys.get(i);
		}
		
		// Possible input values
		System.out.println(inputs);
		
		// Test string
		System.out.println(testInput);
		
	}
 
}
