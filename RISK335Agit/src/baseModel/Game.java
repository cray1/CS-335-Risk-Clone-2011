/**
 * @author Chris Ray
 * Created on 8:15:11 PM Nov 26, 2011
 *
 */
package baseModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Chris Ray Created on 8:15:11 PM Nov 26, 2011
 */
public class Game extends CommandInterface {
	private CardDeck deck;
	private int unitMultiplier;
	private Map map;
	private Boolean firstTerritory;
	private Move move;
	private LinkedList<Player> players;
	private ArrayList<Die> defendDice;
	private ArrayList<Die> attackDice;

	/**
	 * 
	 * @author Chris Ray Created on 8:16:57 PM Nov 26, 2011
	 * 
	 */
	public Game() {
		deck = new CardDeck();
		unitMultiplier = 4;
		setMap(new Map());
		firstTerritory = true;
		move = new Move();
		setPlayers(new LinkedList<Player>());

		// defend Dice
		defendDice = new ArrayList<Die>();
		// attack Dice
		attackDice = new ArrayList<Die>();
		players = new LinkedList<Player>();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see baseModel.CommandInterface#drawCard(baseModel.Player)
	 * 
	 * @author Chris Ray Created on 8:27:35 PM Nov 26, 2011
	 */
	@Override
	public boolean drawCard(Player p) {
		try {
			p.getCards().add(deck.drawCard());
			this.notifyObservers(p);
			return true;
		} catch (Exception e) {
			System.out.println("Draw card was unsuccessful");
			System.out.println(e.getMessage());
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see baseModel.CommandInterface#turnInCards(baseModel.Player)
	 * 
	 * @author Chris Ray Created on 8:27:35 PM Nov 26, 2011
	 */
	@Override
	public boolean turnInCards(Player p, ArrayList<TerritoryCard> cardsTurningIn) {
		if ((cardsTurningIn.size() == 3) && isValidTurnin(cardsTurningIn)) {
			p.getCards().removeAll(cardsTurningIn);

			// Check to see if any of the cards territories are owned by the
			// player
			// if so, award the player two more units
			if (p.getTerritoriesOwned().contains(
					cardsTurningIn.get(0).getCardTerritory())
					|| p.getTerritoriesOwned().contains(
							cardsTurningIn.get(1).getCardTerritory())
					|| p.getTerritoriesOwned().contains(
							cardsTurningIn.get(2).getCardTerritory()))
				// Special award: award two more units to the player
				p.setNewUnits(p.getNewUnits() + 2);

			// Normal award: award unitMultiplier units to player
			p.setNewUnits(p.getNewUnits() + unitMultiplier);

			// move the unitMultiplier to the next position
			unitMultiplierUp();
			this.notifyObservers(p);
			return true;
		} else
			return true;
	}

	private boolean isValidTurnin(ArrayList<TerritoryCard> cards) {
		// 2 and a wildcard or 1 and 2 wildcards
		if (cards.contains(CardType.WILDCARD) && (cards.size() == 3))
			return true;
		else if ((cards.get(0).getCardType() == cards.get(1).getCardType())
				&& (cards.get(1).getCardType() == cards.get(2).getCardType()))
			return true;
		// 1 of each
		else if (cards.contains(CardType.CANNON)
				&& cards.contains(CardType.HORSE)
				&& cards.contains(CardType.SOLDIER))
			return true;

		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see baseModel.CommandInterface#placeUnitOnTerritory(baseModel.Player,
	 * java.lang.String, baseModel.Unit)
	 * 
	 * @author Chris Ray Created on 8:27:35 PM Nov 26, 2011
	 */
	@Override
	public boolean placeOneUnitOnTerritory(Player p, Territory territory) {
		if (territory.getUnitsOnTerritory() == 0) {
			territory.setUnitsOnTerritory(territory.getUnitsOnTerritory() + 1);
			territory.setOwner(p.getTeam());
			p.getTerritoriesOwned().add(territory);
			this.notifyObservers(p);
			this.notifyObservers(territory);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see baseModel.CommandInterface#attackTerritory(baseModel.Player,
	 * java.lang.String, java.lang.String)
	 * 
	 * @author Stephen Brown at 6:17pm 11/29/11, Christopher Ray (Dice changes
	 * only) 11/30/11 7:26 PM
	 */
	@Override
	public boolean attackTerritory(Player p, Territory orig, Territory dest,
			int numOfAttackingDice) {
		// Check to see if the two territories are neighbors first, if not,
		// nothing happens, Attack Fails.
		if (orig.getNeighbors().contains(dest)) {

			int defenders = dest.getUnitsOnTerritory();
			if (defenders > 2)
				defenders = 2;

			// the attacking territory must be the player's, and there has to be
			// at least one unit left on the original territory.
			// Also max of 3 attacking Dice
			if ((orig.getOwner() == p.getTeam())
					&& (numOfAttackingDice < orig.getUnitsOnTerritory())
					&& (numOfAttackingDice < 4) && (numOfAttackingDice >= 1)) {
				// records the beginning and ending number of attacking units
				int remainingArmy = numOfAttackingDice;

				rollAttackDice(numOfAttackingDice);
				// Sorts in descending order
				Collections.sort(this.getAttackDice(),
						Collections.reverseOrder());

				rollDefendDice();
				// Sorts in descending order
				Collections.sort(this.getDefendDice(),
						Collections.reverseOrder());

				// iterators for attacking and defending dice, resp.
				Iterator<Die> attackingDice = this.getAttackDice().iterator();
				Iterator<Die> defendingDice = this.getDefendDice().iterator();

				// Only goes for the lesser number of dice.
				while ((attackingDice.hasNext()) && (defendingDice.hasNext())) {
					Die attackingDie = attackingDice.next();
					Die defendingDie = defendingDice.next();

					// Die is now comparable...thus no need to compare their
					// rolls, you can just compare themselves directly
					if (attackingDie.compareTo(defendingDie) > 0)
						dest.removeUnits(1);
					else
						orig.removeUnits(1);
				}
				// capture:
				if (dest.getUnitsOnTerritory() == 0) {

					// find destination's owner player
					Iterator<Player> playersItr = players.iterator();
					Player current = new Player(null);
					while (playersItr.hasNext()) {
						current = playersItr.next();
						if (current.getTeam() == dest.getOwner())
							break;
					}
					current.getTerritoriesOwned().remove(dest);
					// check if current has no territories
					if (current.getNumberOfTerritories() == 0) {
						// give his or her cards to player p
						p.getCards().addAll(current.getCards());

						// remove current from players List
						players.remove(current);
						/*
						 * while(p.getCards().size()>4) { //request turn in
						 * cards from GUI }
						 */
						this.notifyObservers(players);

					}
					if (firstTerritory) {
						// add card
						drawCard(p);
						firstTerritory = false;
					}
					move(p, orig, dest, remainingArmy);
					updateMove(p, orig, dest);
				}

				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see baseModel.CommandInterface#move(baseModel.Player, java.lang.String,
	 * java.lang.String, int)
	 * 
	 * @author Chris Ray Created on 1:54:39 AM Nov 27, 2011
	 */
	@Override
	public boolean move(Player p, Territory orig, Territory dest,
			int numOfUnitsToMove) {
		try {
			// Check to see if the two territories are neighbors first, if not,
			// nothing happens, Move Fails.
			if (orig.getNeighbors().contains(dest))
				if ((orig.getOwner() == p.getTeam())
						&& ((orig.getUnitsOnTerritory() > 1) && (numOfUnitsToMove < orig
								.getUnitsOnTerritory())))
					if ((dest.getUnitsOnTerritory() <= 0)
							|| (dest.getOwner() == p.getTeam())) {

						// move is valid...proceed.

						// if numberOfUnitsToMove > orig.getUnitsOnTerritory()
						// move orig.getUnitsOnTerritory()-1 units
						if (numOfUnitsToMove > orig.getUnitsOnTerritory()) {
							orig.setUnitsOnTerritory(1);
							dest.setUnitsOnTerritory(orig.getUnitsOnTerritory() - 1);
						} else {
							orig.setUnitsOnTerritory(orig.getUnitsOnTerritory()
									- numOfUnitsToMove);
							dest.setUnitsOnTerritory(dest.getUnitsOnTerritory()
									+ numOfUnitsToMove);
						}
						this.notifyObservers(orig);
						this.notifyObservers(dest);
						updateMove(p, orig, dest);
						return true;
					}
			return false;
		} catch (Exception e) {

			return false;
		}
	}

	/**
	 * 
	 * @author Chris Ray Created on 9:12:29 PM Nov 29, 2011
	 * 
	 */
	private void updateMove(Player p, Territory orig, Territory dest) {
		move.setP(p);
		move.setDest(dest);
		move.setOrig(orig);

	}

	/**
	 * 
	 * @author Chris Ray Created on 6:42:19 PM Nov 30, 2011
	 * 
	 */
	private void unitMultiplierUp() {
		if (unitMultiplier < 10)
			unitMultiplier += 2;
		else
			unitMultiplier += 5;
	}

	/**
	 * 
	 * @author Chris Ray Created on 6:42:24 PM Nov 30, 2011
	 * 
	 */
	private void rollAttackDice(int numOfDiceToRoll) {
		ArrayList<Die> returnDice = new ArrayList<Die>();
		if ((numOfDiceToRoll <= 3) && (numOfDiceToRoll >= 1))
			for (int i = 0; i < numOfDiceToRoll; i++) {
				attackDice.get(i).initiateRoll();
				returnDice.add(attackDice.get(i));
			}
		this.attackDice = returnDice;
	}

	/**
	 * 
	 * @author Chris Ray Created on 6:41:38 PM Nov 30, 2011 <br />
	 *         Assumes always will be rolling 2 dice
	 * 
	 */
	private void rollDefendDice() {
		for (Die d : defendDice)
			d.initiateRoll();
	}

	/**
	 * @return the map
	 * @author Chris Ray Created on 3:39:52 PM Nov 27, 2011
	 */
	public Map getMap() {
		return map;
	}

	/**
	 * @param map
	 *            the map to set
	 * @author Chris Ray Created on 3:39:52 PM Nov 27, 2011
	 */
	public void setMap(Map map) {
		this.map = map;
	}

	/**
	 * @return the move
	 * @author Chris Ray Created on 9:07:24 PM Nov 29, 2011
	 */
	public Move getMove() {
		return move;
	}

	/**
	 * @param move
	 *            the move to set
	 * @author Chris Ray Created on 9:07:24 PM Nov 29, 2011
	 */
	public void setMove(Move move) {
		this.move = move;
	}

	/**
	 * @return the players
	 * @author Chris Ray Created on 9:23:49 PM Nov 29, 2011
	 */
	public LinkedList<Player> getPlayers() {
		return players;
	}

	/**
	 * @param players
	 *            the players to set
	 * @author Chris Ray Created on 9:23:49 PM Nov 29, 2011
	 */
	public void setPlayers(LinkedList<Player> players) {
		this.players = players;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see baseModel.CommandInterface#getDefendDice()
	 * 
	 * @author Chris Ray Created on 5:36:45 PM Nov 30, 2011
	 */
	@Override
	public ArrayList<Die> getDefendDice() {
		return this.defendDice;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see baseModel.CommandInterface#getAttackDice()
	 * 
	 * @author Chris Ray Created on 5:36:45 PM Nov 30, 2011
	 */
	@Override
	public ArrayList<Die> getAttackDice() {
		return this.attackDice;
	}

	/**
	 * @return the unitMultiplier
	 * @author Chris Ray Created on 7:24:23 PM Nov 30, 2011
	 */
	public int getUnitMultiplier() {
		return unitMultiplier;
	}

	/**
	 * @param unitMultiplier
	 *            the unitMultiplier to set
	 * @author Chris Ray Created on 7:24:23 PM Nov 30, 2011
	 */
	public void resetUnitMultiplier() {
		this.unitMultiplier = 4;
	}

}
