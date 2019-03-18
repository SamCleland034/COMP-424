package student_player;

import java.util.List;

import boardgame.Move;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

public class Node {

	public PentagoMove move;
	public List<Node> next;
	public PentagoBoardState state;
	public int wins;
	public int games;
	public int nsa;
	public Move getMove() {
		return move;
	}
	public void setMove(PentagoMove move) {
		this.move = move;
	}
	public List<Node>getNode() {
		return next;
	}
	public void setNode(List<Node> node) {
		this.next = node;
	}

	public Node(PentagoMove move, PentagoBoardState state, List<Node> next) {
		this.move = move;
		this.next = next;
		this.state = state;
	}


}
