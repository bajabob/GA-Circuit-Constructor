import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Spliterator;
import java.util.Stack;

public class Circuit {
	
	/**
	 * Total number of individual gates in this circuit
	 */
	private int counterAnd;
	private int counterOr;
	private int counterNot;
	private int counterNone;
	
	/**
	 * Collection of gates in linear order
	 */
	private Stack<LogicBase> gates;
	
	public Circuit()
	{
		gates = new Stack<LogicBase>();
	}
	
	/**
	 * Load a circuit from disk
	 * @param filename String
	 */
	public Circuit(String filename){
		gates = new Stack<LogicBase>();
		this.load( filename );
	}
	
	
	/**
	 * Add a gate to the front of this circuit
	 * @param gate
	 */
	void addGateFront(LogicBase gate)
	{
		gates.push(gate);
		this.countGate(gate);
	}
	
	
	/**
	 * Increment the local gate count
	 * @param gate
	 */
	private void countGate(LogicBase gate){
		if(gate.getGate() == LogicBase.GATE_AND)
		{
			counterAnd++;
		}
		
		if(gate.getGate() == LogicBase.GATE_OR)
		{
			counterOr++;
		}
		
		if(gate.getGate() == LogicBase.GATE_NOT)
		{
			counterNot++;
		}
		
		if(gate.getGate() == LogicBase.GATE_NONE)
		{
			counterNone++;
		}
	}
	
	/**
	 * Get this circuit's current fitness score
	 * @return int
	 */
	int getFitnessScore()
	{
		return counterNot * 10000 + 10 * (counterAnd + counterOr);
		
	}
	
	/**
	 * Save this circuit to disk
	 * @param filename String
	 */
	void save(String filename){
		PrintWriter writer;
		try
		{
			writer = new PrintWriter(filename+".txt", "UTF-8");
			for(int i = gates.size() - 1; i >= 0; --i ) {
				writer.println(gates.get( i ).toFileFormat());
			}
			writer.close();
		} catch ( FileNotFoundException | UnsupportedEncodingException e )
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Load a circuit from disk
	 * @param filename String
	 */
	void load(String filename){
		BufferedReader br = null;
		try {
			String currentLine;
 
			br = new BufferedReader(new FileReader(filename+".txt"));
 
			ArrayList<LogicBase> list =  new ArrayList<LogicBase>();
			while ((currentLine = br.readLine()) != null) {
				String[] split = currentLine.split("\t");
				
				if(split[1].equals("NONE"))
				{
					LogicBase gate = new LogicBase(LogicBase.GATE_NONE, Integer.valueOf(split[2]),
							Integer.valueOf(split[0]));
					list.add( gate );
				} else if(split[1].equals("NOT"))
				{
					LogicBase gate = new LogicBase(LogicBase.GATE_NOT, Integer.valueOf( split[2]), 
							Integer.valueOf(split[0]) );
					list.add( gate );
				} else if(split[1].equals("AND"))
				{
					LogicBase gate = new LogicBase(LogicBase.GATE_AND, Integer.valueOf(split[2]),
							Integer.valueOf(split[3]), Integer.valueOf(split[0]));
					list.add( gate );
				} else if(split[1].equals("OR"))
				{
					LogicBase gate = new LogicBase(LogicBase.GATE_OR, Integer.valueOf( split[2]), 
							Integer.valueOf(split[3]), Integer.valueOf(split[0]) );
					list.add( gate );
				} else
				{
					System.err.println("Not a gate..");
				}
			}
 
			for(int i = list.size() - 1; i >= 0 ; i--){
				this.addGateFront( list.get( i ) );
			}
			
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	
	/**
	 * Get the current number of gates in this circuit
	 * @return int
	 */
	int getGateCount()
	{
		return gates.size();
	}
	
	/**
	 * Shuffle the inputs of this circuit
	 */
	void shuffleInputs(long l){
		Random rand = new Random(l);
		
		for(int i = gates.size()-1; i >= 0 ; i--){
			if(gates.get( i ).getGate() == LogicBase.GATE_NONE){
				continue;
			}
			int inA = rand.nextInt(gates.get( i ).output);
			int inB = rand.nextInt(gates.get( i ).output);
			
			gates.get( i ).setInputs( inA, inB );
		}
	
	}
	
	
	/**
	 * Evaluate this circuit based off an array of inputs. The total number of 
	 *  input should equal the total number of NONE gates at the beginning of 
	 *  this circuit.
	 * @param tt TruthTable
	 * @return boolean - is this a valid circuit?
	 */
	boolean evaluate( TruthTable tt )
	{
		if(tt.getTableWidth() != counterNone){
			System.err.println("Circuit.evaluate :: Total number of NONE gates in circuit "
					+ "does not equal number of inputs.");
			return false;
		}
		
		int pass = 0;
		
		ArrayList<Boolean> testResults = new ArrayList<Boolean>();
		
		String out = "";
		
		for(int test = 0; test < tt.getRowCount(); test++ ){
			// clear any old results
			testResults.clear();
		
			// add inputs to beginning of test
			for( int i = 0; i < tt.getTableWidth(); i++ )
			{
				testResults.add(tt.getInput(test, i));
			}
			
			// evaluate the circuit
			for( int i = gates.size()-1; i >= 0; i-- )
			{
				gates.get(i).evaluate(testResults);
			}
						
			/**
			 * Did the test pass? - then keep going!
			 * 
			 * Did our circuit pass for this row in the truth table?
			 * Compare test results for this row with the value in 
			 *  the truth table
			 */
			if(testResults.get(testResults.size()-1) == tt.getOutput(test)){
				pass++;
				out += "PASSED\n";
			}else{
				out += "FAILED\n";
				return false;
			}
			out += "Output\tGate\n";
			for(int i = gates.size() - 1; i >= 0; i--){
				out += testResults.get((testResults.size()-1)-i) + "\t" + gates.get( i ) + "\n";
			}
		}
		// debug output
		
		if(pass > 5){
			//System.out.println("passed: "+pass);
			//System.out.println( out );
			//System.out.println( tt );
			//return true;
		}
		return true;
	}
	
	@Override
	public int hashCode(){
		return this.gates.size() + counterAnd + counterNot + 
				counterOr + getFitnessScore();
	}
	
	@Override
	public String toString()
	{
		String response = "";
		for(int i = gates.size() - 1; i >= 0; i--)
		{
			response += gates.get(i) + "\n";
		}
		
		return response;
	}
	
	public static void main(String[] args)
	{
		Circuit c = new Circuit();
		c.addGateFront(new LogicBase(LogicBase.GATE_OR, 4, 5, 6));
		c.addGateFront(new LogicBase(LogicBase.GATE_AND, 1, 2, 5));
		c.addGateFront(new LogicBase(LogicBase.GATE_AND, 0, 3, 4));
		c.addGateFront(new LogicBase(LogicBase.GATE_NOT, 1, 3));
		c.addGateFront(new LogicBase(LogicBase.GATE_NOT, 0, 2));
		c.addGateFront(new LogicBase(LogicBase.GATE_NONE, 1, 1));
		c.addGateFront(new LogicBase(LogicBase.GATE_NONE, 0, 0));
		
		System.out.println(c);
		System.out.println(c.hashCode());
		System.out.println("Testing Fitness: " + c.getFitnessScore());
		
		boolean[] expOuts = {false, true, true, false};
		TruthTable tt = new TruthTable("Carry-Out", 2, expOuts);
		boolean[] badOuts = {true, true, true, false};
		TruthTable badtt = new TruthTable("Bad", 2, badOuts);
		
		System.out.println("Testing evaluate with good tt: " + c.evaluate(tt));
		System.out.println("Testing evaluate with bad tt: " + c.evaluate(badtt));
		System.out.println("Testing Get Gate Count: " + c.getGateCount());
		
		c.save( "testFileIO" );
		System.out.println("Saved Circuit:");
		System.out.println(c);
				
		// now attempt to load that same file into a circuit obj
		Circuit cLoad = new Circuit("testFileIO");
		System.out.println("Loaded Circuit:");
		System.out.println(cLoad);
	}
}
