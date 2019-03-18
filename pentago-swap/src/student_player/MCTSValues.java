package student_player;

import java.util.HashMap;

import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoCoord;
import pentago_swap.PentagoMove;

public class MCTSValues {
	private static HashMap<StateAction, Statistics> statistics = new HashMap<>();
	private static double c = 0.5;
	private static HashMap<PentagoBoardState, Integer> statesVisited = new HashMap<>();

	public static boolean IsMoveEqual(PentagoMove pm1, PentagoMove pm2) {
		if(pm1.getASwap() != pm2.getASwap()) {
			return false;
		}

		if(pm1.getBSwap() != pm2.getBSwap()) {
			return false;
		}

		PentagoCoord pmc1 = pm1.getMoveCoord();
		PentagoCoord pmc2 = pm2.getMoveCoord();
		if(pmc1.getX() != pmc2.getX()) {
			return false;
		}

		if(pmc1.getY() != pmc2.getY()) {
			return false;
		}

		if(pm1.getPlayerID() != pm2.getPlayerID()) {
			return false;
		}

		return true;
	}

	public static boolean isStateEqual(PentagoBoardState pbs1, PentagoBoardState pbs2) {
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 6; j++) {
				if(pbs1.getPieceAt(i, j) != pbs2.getPieceAt(i, j)) {
					return false;
				}
			}
		}

		return true;
	}
	public static double getC() {
		return c;
	}

	public static void setC(double c) {
		MCTSValues.c = c;
	}


	public static double getStatesVisited(PentagoBoardState state) {
		for(PentagoBoardState pbs : statesVisited.keySet()) {
			if(isStateEqual(pbs, state)) {
				return statesVisited.get(pbs);
			}
		}

		statesVisited.put(state, 0);
		return 0;
	}

	public static void addStateVisited(PentagoBoardState copy) {
		PentagoBoardState clone = (PentagoBoardState) copy.clone();
		Integer value = null;
		for(PentagoBoardState pbs : statesVisited.keySet()) {
			if(isStateEqual(copy, pbs)) {
				value = statesVisited.get(pbs);
				if(value == null) {
					value = 0;
				}

				statesVisited.put(pbs, value + 1);
				return;
			}
		}

		statesVisited.put(clone, 1);
	}

	public static StateAction getStateAction(PentagoMove move, PentagoBoardState state) {
		for(StateAction sa : statistics.keySet()) {
			if(IsMoveEqual(move, sa.move) && isStateEqual(sa.state, state)) {
				return sa;
			}
		}

		return null;
	}

	public static void addStateAction(StateAction sa) {
		statistics.put(sa, new Statistics());
	}
}
