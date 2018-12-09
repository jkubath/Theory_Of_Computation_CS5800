package jonahKubath_A5;

public class Node {
	int index = 0;
	boolean isFinal = false;
	
	public Node() {
		
	}
	
	public Node(int index, boolean isFinal) {
		this.index = index;
		this.isFinal = isFinal;
	}
	
	public Node(int index) {
		this.index = index;
		isFinal = false;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isFinal() {
		return isFinal;
	}

	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}
	
}
