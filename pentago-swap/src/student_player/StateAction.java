package student_player;

import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

public class StateAction {
	PentagoMove move;
	PentagoBoardState state;
	public StateAction(PentagoMove move, PentagoBoardState state) {
		this.move = move;
		this.state = state;
	}
}
