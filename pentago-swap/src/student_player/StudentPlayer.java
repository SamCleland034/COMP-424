package student_player;

import java.util.ArrayList;
import java.util.List;

import boardgame.Board;
import boardgame.Move;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoBoardState.Piece;
import pentago_swap.PentagoBoardState.Quadrant;
import pentago_swap.PentagoCoord;
import pentago_swap.PentagoMove;
import pentago_swap.PentagoPlayer;

/** A player file submitted by a student. */
public class StudentPlayer extends PentagoPlayer {

	static final double BIAS = 3;
	int player;
	private List<PentagoMove> allMoves = new ArrayList<>();
	List<PentagoMove> priorityNodes = new ArrayList<>();


    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260675996");
    }

    public List<PentagoMove> getAllMoves(PentagoBoardState boardState) {
    	ArrayList<PentagoMove> moves = new ArrayList<>();
    	for(PentagoMove move : allMoves) {
    		if(boardState.isLegal(move)) {
    			if(boardState.getTurnNumber() < 2 && player == 1) {
    				if(adjacentTo(move.getMoveCoord(), boardState)) {
    					moves.add(move);
    				}
    			} else {
    				if(adjacentTo(move.getMoveCoord(), boardState)) {
    					priorityNodes.add(move);
    				}

        			moves.add(move);
    			}
    		}
    	}

    	return moves;
    }

    public void setMoves(PentagoBoardState boardState) {
    	for(int p = 0; p < 2; p++) {
			for(int i = 0; i < 6; i++) {
				for(int j = 0; j < 6; j++) {
					allMoves.add(new PentagoMove(i, j, Quadrant.BL, Quadrant.BR, p));
					allMoves.add(new PentagoMove(i, j, Quadrant.BL, Quadrant.TL, p));
					allMoves.add(new PentagoMove(i, j, Quadrant.BL, Quadrant.TR, p));
					allMoves.add(new PentagoMove(i, j, Quadrant.TL, Quadrant.BR, p));
					allMoves.add(new PentagoMove(i, j, Quadrant.TL, Quadrant.TR, p));
					allMoves.add(new PentagoMove(i, j, Quadrant.TR, Quadrant.BR, p));
				}
			}
    	}
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
    	if(boardState.getTurnNumber() == 0) {
    		setMoves(boardState);
    	}

    	player = boardState.getTurnPlayer();
    	List<Node> tempNodes = new ArrayList<>();
    	List<PentagoMove> moves = getAllMoves(boardState);
    	if(boardState.getTurnNumber() > 4) {
    		for(PentagoMove move : moves) {
        		PentagoBoardState copy = (PentagoBoardState) boardState.clone();
        		copy.processMove(move);
        		if(copy.gameOver() && copy.getWinner() == player) {
        			return move;
        		}
    		}
    	}

		for(PentagoMove move : moves) {
        	Node tempNode = new Node(move, boardState, null);
        	tempNodes.add(tempNode);
        	if(adjacentTo(tempNode.move.getMoveCoord(), boardState)) {
        		priorityNodes.add(move);
        	}
		}

    	/*Iterator<Node> it = nodes.iterator();
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
        }*/

        PentagoBoardState copy = null;
        int stopTime = boardState.getTurnNumber() == 0 ? 25000 : 1000;
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
        	} else if(nodeVal == bestNodeVal) {
        		if(node.games > bestMove.games) {
        			bestMove = node;
        		}
        	}
        }


       MCTSValues.statesVisited.remove(MCTSValues.getState(boardState));
       priorityNodes.clear();

        // Return your move to be processed by the server.
        System.out.println("Best move = " + bestMove.wins / (double) bestMove.games + " or " + bestMove.wins + " / " + bestMove.games);
        return bestMove.move;
    }

    private boolean adjacentTo(PentagoCoord moveCoord, PentagoBoardState boardState) {
    	int x = moveCoord.getX();
    	int y = moveCoord.getY();

    	Piece piece = Piece.WHITE;
    	if(player == 0) {
    		piece = Piece.BLACK;
    	}

    	int lowerX = x - 1;
    	if(lowerX < 0) {
    		lowerX = 0;
    	}

    	int lowerY = y - 1;
    	if(lowerY < 0) {
    		lowerY = 0;
    	}

    	int upperX = x + 1;
    	if(upperX > 5) {
    		upperX = 5;
    	}

    	int upperY = y + 1;
    	if(upperY > 5) {
    		upperY = 5;
    	}

    	if(boardState.getPieceAt(x, lowerY) == piece || boardState.getPieceAt(x, upperY) == piece ||
    			boardState.getPieceAt(lowerX, y) == piece || boardState.getPieceAt(upperX, y) == piece
    			|| boardState.getPieceAt(upperX, upperY) == piece || boardState.getPieceAt(upperX, upperY) == piece
    			|| boardState.getPieceAt(lowerX, upperY) == piece || boardState.getPieceAt(lowerX, lowerY) == piece) {
    		return true;
    	}

    	return false;
	}

	private double calculateBestMove(Node node) {
    	if(node == null) {
    		return -1;
    	}

    	if(node.games == 0) {
    		return -1;
    	}

    	double score = (node.wins / (double) node.games) * (Math.log10(node.games) / 3.0);
    	if(priorityNodes.contains(node.move)) {
    		score *= BIAS;
    	}

    	return score;
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
		if(promisingNode.next == null || promisingNode.next.isEmpty()) {
			PentagoBoardState pbs = MCTSValues.getState(copy);
			if(pbs == null) {
				pbs = (PentagoBoardState) copy.clone();
				MCTSValues.addStateVisited(pbs);
			}

			promisingNode.next = new ArrayList<>();
			for(PentagoMove move : getAllMoves(pbs)) {
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