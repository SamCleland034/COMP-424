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

	double BIAS = 0;
	int player;
    int stopTime = 1800;
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

    public List<PentagoMove> getAllLegalMoves(PentagoBoardState state) {
    	List<PentagoMove> results = new ArrayList<>();
    	for(PentagoMove move : allMoves) {
    		if(state.isLegal(move)) {
    			results.add(move);
    		}
    	}

    	return results;
    }
    public List<PentagoMove> getAllMoves(PentagoBoardState boardState) {
    	ArrayList<PentagoMove> moves = new ArrayList<>();
		Piece piece;
		if(player == 0) {
			piece = Piece.BLACK;
		} else {
			piece = Piece.WHITE;
		}

    	for(PentagoMove move : allMoves) {
    		if(boardState.isLegal(move)) {
    			if(boardState.getTurnNumber() < 2 && player == 1) {
    				if(adjacentTo(move.getMoveCoord(), boardState, Piece.WHITE)) {
    					moves.add(move);
    				}
    			} else {
    				if(adjacentTo(move.getMoveCoord(), boardState, piece)) {
    					priorityNodes.add(move);
    				}

        			moves.add(move);
    			}
    		}
    	}

    	return moves;
    }

    public void setMoves(PentagoBoardState boardState) {
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 6; j++) {
				allMoves.add(new PentagoMove(i, j, Quadrant.BL, Quadrant.BR, player));
				allMoves.add(new PentagoMove(i, j, Quadrant.BL, Quadrant.TL, player));
				allMoves.add(new PentagoMove(i, j, Quadrant.BL, Quadrant.TR, player));
				allMoves.add(new PentagoMove(i, j, Quadrant.TL, Quadrant.BR, player));
				allMoves.add(new PentagoMove(i, j, Quadrant.TL, Quadrant.TR, player));
				allMoves.add(new PentagoMove(i, j, Quadrant.TR, Quadrant.BR, player));
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
    	int turnNumber = boardState.getTurnNumber();
    	long startTime = System.currentTimeMillis();
    	if(boardState.getTurnNumber() == 0) {
        	player = boardState.getTurnPlayer();
    		setMoves(boardState);
    	}

    	List<Node> tempNodes = new ArrayList<>();
    	List<PentagoMove> moves = getAllMoves(boardState);
    	if(moves.isEmpty()) {
    		moves = getAllLegalMoves(boardState);
    	}

    	if(turnNumber > 4) {
    		for(PentagoMove move : moves) {
        		PentagoBoardState copy = (PentagoBoardState) boardState.clone();
        		copy.processMove(move);
        		if(copy.gameOver() && copy.getWinner() == player) {
        			return move;
        		}
    		}
    	}

    	Piece piece = player == 0? Piece.WHITE : Piece.BLACK;
		for(PentagoMove move : moves) {
        	Node tempNode = new Node(move, null);
        	tempNodes.add(tempNode);
        	if(turnNumber > 2 && adjacentTo(tempNode.move.getMoveCoord(), boardState, piece)) {
        		priorityNodes.add(move);
        	}
		}

        PentagoBoardState copy = null;
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

       priorityNodes.clear();

        // Return your move to be processed by the server.
        System.out.println("Best move = " + bestMove.wins / (double) bestMove.games + " or " + bestMove.wins + " / " + bestMove.games);
        return bestMove.move;
    }

    private boolean adjacentTo(PentagoCoord moveCoord, PentagoBoardState boardState, Piece piece) {
    	int x = moveCoord.getX();
    	int y = moveCoord.getY();

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

    	if((boardState.getPieceAt(x, lowerY) == piece && (y % 3) == (lowerY) % 3)|| (boardState.getPieceAt(x, upperY) == piece
    			&& (y % 3) == (upperY % 3)) ||
    			(boardState.getPieceAt(lowerX, y) == piece && (x % 3) == (lowerX % 3)) || (boardState.getPieceAt(upperX, y) == piece && (x % 3) == (upperX % 3))
    			|| (boardState.getPieceAt(upperX, upperY) == piece && (x % 3) == (upperX % 3)
    			&& (y % 3) == (upperY % 3)) || (boardState.getPieceAt(upperX, lowerY) == piece && (x % 3) == (upperX % 3) && (y % 3) == (lowerY % 3))
    			|| (boardState.getPieceAt(lowerX, upperY) == piece && (x % 3) == (lowerX % 3) && (y % 3) == (upperY % 3)
    			) ||
    			(boardState.getPieceAt(lowerX, lowerY) == piece && (x % 3) == (lowerX % 3) && (y % 3) == (lowerY % 3))) {
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

    	double score = (node.wins / (double) node.games) * Math.log(node.games) / 10.0;
    	if(priorityNodes.contains(node.move)) {
    		score *= (1 + BIAS);
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

		copy.processMove(promisingNode.move);
		if(promisingNode.next == null || promisingNode.next.isEmpty()) {
			promisingNode.next = new ArrayList<>();
			for(PentagoMove move : getAllMoves(copy)) {
				promisingNode.next.add(new Node(move, new ArrayList<>()));
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

		return node.wins / (double) node.games + MCTSValues.getC() * Math.sqrt(300 / node.nsa);
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