package sagiyehezkel.matchappserver.games;

import java.util.ArrayList;
import java.util.Arrays;

import sagiyehezkel.matchappserver.Util;

public class ConnectFour extends Game {
	// Number of connected discs to look for in order to win
	final private int NUM_OF_CONNECTED_TO_WIN = 4;
    final private int NUM_OF_ROWS = 6;
    final private int NUM_OF_COLUMNS = 7;
    
	ArrayList<String> moves;
	
	public ConnectFour() {
		super(GamesFactory.CONNECT_FOUR);
	}

	@Override
	public void updateStatusFromString(String player, String newStatus) {
		if (newStatus != null) {
			moves = Util.fromStringToList(newStatus);
			
			if ((player != null) && checkIfGameCompleted()) {
				mWinnerPlayer = player;
			}
		}
	}

	@Override
	public boolean checkIfGameCompleted() {
	    Integer[][] gameBoard = new Integer[NUM_OF_ROWS][NUM_OF_COLUMNS];
	    for (int i = 0; i < NUM_OF_ROWS; ++i)
	    	Arrays.fill(gameBoard[i], 0);
        
        int lastCellEditedRow = 0;
        int lastCellEditedCol = 0;
        int playerNum = 0;
        for (String cell : moves) {
        	int lastCellIndex = Integer.parseInt(cell);
        	lastCellEditedRow = lastCellIndex / NUM_OF_COLUMNS;
        	lastCellEditedCol = lastCellIndex % NUM_OF_COLUMNS;
			gameBoard[lastCellEditedRow][lastCellEditedCol] = playerNum + 1;
			playerNum = (playerNum + 1) % 2;
		}
        
        int lastPlayerCode = gameBoard[lastCellEditedRow][lastCellEditedCol];
        
        boolean foundConnectFour = false;
        
        // Check Down
        if (!foundConnectFour && 
        	// Verify the row is "high" enough so it's possible to have enough connected disks down 
        	(lastCellEditedRow <= NUM_OF_ROWS - NUM_OF_CONNECTED_TO_WIN)) {
        	
        	foundConnectFour = true;
        	for (int i = 1; foundConnectFour && i < NUM_OF_CONNECTED_TO_WIN; ++i) {
        		int nextLocationCode = gameBoard[lastCellEditedRow + i][lastCellEditedCol];
        		foundConnectFour = (lastPlayerCode == nextLocationCode);
        	}
        }
        
        // Check Row
        if (!foundConnectFour) {
            int foundConnected = 0;
        	for (int i = 1; !foundConnectFour && i < NUM_OF_COLUMNS; ++i) {
        		int nextLocationCode = gameBoard[lastCellEditedRow][i];
        		
        		if (nextLocationCode == lastPlayerCode)
        			++foundConnected;
        		else
        			foundConnected = 0;
        		
        		foundConnectFour = (foundConnected == NUM_OF_CONNECTED_TO_WIN);
            }
        }
        
        int curScanRow;
        int curScanCol;
        
        // Check main diagonal
        if (!foundConnectFour) {
        	curScanRow = lastCellEditedRow - NUM_OF_CONNECTED_TO_WIN;
        	curScanCol = lastCellEditedCol - NUM_OF_CONNECTED_TO_WIN;
        	
        	// Move to valid indexes
        	while (curScanRow < 0 || curScanCol < 0) {
        		++curScanRow;
        		++curScanCol;
        	}
        	
        	int foundConnected = 0;
        	while (!foundConnectFour && 
        		curScanRow < NUM_OF_ROWS && 
        		curScanCol < NUM_OF_COLUMNS) {
        		
        		int nextLocationCode = gameBoard[curScanRow][curScanCol];
        		
        		if (nextLocationCode == lastPlayerCode)
        			++foundConnected;
        		else
        			foundConnected = 0;
        		
        		foundConnectFour = (foundConnected == NUM_OF_CONNECTED_TO_WIN);
        		
        		++curScanRow;
        		++curScanCol;
            }
        }
        
        // Check second diagonal
        if (!foundConnectFour) {
        	curScanRow = lastCellEditedRow + NUM_OF_CONNECTED_TO_WIN;
        	curScanCol = lastCellEditedCol - NUM_OF_CONNECTED_TO_WIN;
        	
        	// Move to valid indexes
        	while (curScanRow >= NUM_OF_ROWS || curScanCol < 0) {
        		--curScanRow;
        		++curScanCol;
        	}
        	
        	int foundConnected = 0;
        	while (!foundConnectFour && 
        		curScanRow >= 0 && 
        		curScanCol < NUM_OF_COLUMNS) {
        		
        		int nextLocationCode = gameBoard[curScanRow][curScanCol];
        		
        		if (nextLocationCode == lastPlayerCode)
        			++foundConnected;
        		else
        			foundConnected = 0;
        		
        		foundConnectFour = (foundConnected == NUM_OF_CONNECTED_TO_WIN);
        		
        		--curScanRow;
        		++curScanCol;
            }
        }
        
		return foundConnectFour;
	}
}
