package student_player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import boardgame.Board;
import boardgame.Move;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;
import pentago_swap.PentagoPlayer;

/** A player file submitted by a student. */
public class StudentPlayer extends PentagoPlayer {

	public List<Node>nodes = new ArrayList<>();
	int player;

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260675996");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    @Override
	public Move chooseMove(PentagoBoardState boardState) {
        // You probably will make separate functions in MyTools.
        // For example, maybe you'll need to load some pre-processed best opening
        // strategies...
    	long startTime = System.currentTimeMillis();
    	player = boardState.getTurnPlayer();
    	List<Node> tempNodes = new ArrayList<>();


    	Iterator<Node> it = nodes.iterator();
    	secondLoop : while(it.hasNext()) {
    		Node node = it.next();
    		if(node == null) {
    			it.remove();
    			continue;
    		}
    		for(PentagoMove move : boardState.getAllLegalMoves()) {
    			if(MCTSValues.IsMoveEqual(node.move, move) && MCTSValues.isStateEqual(node.state, boardState)) {
    				continue secondLoop;
    			}
    		}

			it.remove();
    	}

        firstLoop : for(PentagoMove move : boardState.getAllLegalMoves()) {
        	for(Node node : nodes) {
        		if(MCTSValues.IsMoveEqual(move, node.move) && MCTSValues.isStateEqual(node.state, boardState)) {
        			tempNodes.add(node);
        			continue firstLoop;
        		}
        	}

        	Node tempNode = new Node(move, boardState, null);
        	tempNodes.add(tempNode);
        	nodes.add(tempNode);
        }

        PentagoBoardState copy = null;
        int stopTime = boardState.getTurnNumber() == 0 ? 29000 : 1800;
        while(System.currentTimeMillis() - startTime < stopTime) {
            copy = (PentagoBoardState) boardState.clone();
            startTreePolicy(copy, tempNodes);
        }

       Node bestMove = null;
       for(Node node : tempNodes) {
        	double nodeVal = calculateBestMove(node);
        	double bestNodeVal = calculateBestMove(bestMove);
        	if(bestMove == null || nodeVal > bestNodeVal) {
        		bestMove = node;
        	}
        }

        nodes.clear();
        nodes.addAll(bestMove.next);

        // Return your move to be processed by the server.
        System.out.println("Best move = " + bestMove.wins / (double) bestMove.games + " or " + bestMove.wins + " / " + bestMove.games);
        return bestMove.move;
    }

    private double calculateBestMove(Node node) {
    	if(node == null) {
    		return -1;
    	}

    	if(node.games == 0) {
    		return -1;
    	}

    	return node.wins / (double) node.games;
    }

	private int startTreePolicy(PentagoBoardState copy, List<Node> nodesLayer) {
		Node promisingNode = null;

		for (Node node : nodesLayer) {
			double nodeVal = calculateValue(node);
			double promisingNodeVal = calculateValue(promisingNode);
		    if (promisingNode == null || nodeVal > promisingNodeVal) {
		        promisingNode = node;
		    }
		}

		MCTSValues.addStateVisited(copy);
		copy.processMove(promisingNode.move);
		copy.processMove((PentagoMove) copy.getRandomMove());
		if(promisingNode.next == null || promisingNode.next.isEmpty()) {
			PentagoBoardState pbs = (PentagoBoardState) copy.clone();
			promisingNode.next = new ArrayList<>();
			for(PentagoMove move : copy.getAllLegalMoves()) {
				promisingNode.next.add(new Node(move, pbs, new ArrayList<>()));
			}
			int value = startDefaultPolicy(copy);
			return calculateMCTS(promisingNode, value);
		}


		return calculateMCTS(promisingNode, startTreePolicy(copy, promisingNode.next));
	}

	private int calculateMCTS(Node promisingNode, int value) {
		promisingNode.games++;
		if(value == 2) {
			promisingNode.wins += 2;
		} else if(value == 1) {
			promisingNode.wins++;
		}

		promisingNode.nsa++;
		return value;
	}


	private double calculateValue(Node node) {
		if(node == null) {
			return -1;
		}

		if(node.nsa == 0) {
			return Double.MAX_VALUE;
		}

		double statesVisited = MCTSValues.getStatesVisited(node.state);
		if(statesVisited == 0) {
			return Double.MAX_VALUE;
		}

		return node.wins / (double) node.games + MCTSValues.getC() * Math.sqrt(Math.log(statesVisited) / node.nsa);
	}

	private int startDefaultPolicy(PentagoBoardState copy) {
		while(!copy.gameOver()) {
			Move nextMove = copy.getRandomMove();
			copy.processMove((PentagoMove) nextMove);
		}

		if(copy.getWinner() == player) {
			return 2;
		}

		if(copy.getWinner() == Board.DRAW) {
			return 1;
		}

		return 0;
	}
}