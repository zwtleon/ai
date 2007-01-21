package aima.probability.reasoning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import aima.probability.RandomVariable;
import aima.util.Table;

public class HiddenMarkovModel {

	Table<String, String, Double> transitionModel, sensorModel;

	RandomVariable priorDistribution, belief;

	public HiddenMarkovModel(List<String> states, List<String> perceptions,
			List<String> actions) {
		priorDistribution = new RandomVariable("HiddenState", states);
		belief = priorDistribution.duplicate();
		createEmptyTransitionTable(states, actions);
		sensorModel = new Table<String, String, Double>(states, perceptions);
	}

	public HiddenMarkovModel(List<String> states, List<String> perceptions) {
		// no actions possible thus the only "action" is to "wait" till the next perception is observed		
		this(states,perceptions,Arrays.asList(new String[] {HmmConstants.DO_NOTHING}));

	}

	private void createEmptyTransitionTable(List<String> states,
			List<String> actions) {
		List<String> state_actions = new ArrayList<String>();
		for (String state : states) {
			for (String action : actions) {
				state_actions.add(state.concat(action));
			}
		}
		transitionModel = new Table<String, String, Double>(state_actions,
				states);

	}

	public RandomVariable prior() {
		return priorDistribution;
	}

	public RandomVariable belief() {
		return belief;
	}

	public void setTransitionModelValue(String startState, String action,
			String endState, Double probability) {
		String start_state_plus_action = startState.concat(action);
		transitionModel.set(start_state_plus_action, endState, probability);
	}
	
	public void setTransitionModelValue(String startState,
			String endState, Double probability) {
		String start_state_plus_action = startState.concat(HmmConstants.DO_NOTHING);
		transitionModel.set(start_state_plus_action, endState, probability);
	}

	public void setSensorModelValue(String state, String perception,
			double probability) {
		sensorModel.set(state, perception, probability);

	}

	public void act(String action) {

		belief = predict(belief,action);
	}
	
	public RandomVariable predict(RandomVariable aBelief,String action){
		RandomVariable newBelief = aBelief.duplicate();
		for (String newState : aBelief.states()) {
			double total = 0;
			for (String oldState : aBelief.states()) {
				total += partialProbabilityOfTransition(oldState, action,newState);
			}
			newBelief.setProbabilityOf(newState, total);
		}
		return newBelief;
	}
	
	public void waitForPerception() {
		act(HmmConstants.DO_NOTHING);
	}
	
	public void perceptionUpDate(String perception) {
		RandomVariable newBelief = belief.duplicate();
		for (String state : belief.states()){
			double probabilityOfPerception= sensorModel.get(state,perception);
			newBelief.setProbabilityOf(state,probabilityOfPerception * belief.getProbabilityOf(state));
		}
		newBelief.normalize();
		belief = newBelief;
		
	}

	private double partialProbabilityOfTransition(String oldState, String action,
			String newState) {
		String old_state_action = oldState.concat(action);
		double transitionProbabilityFromOldStateToNewState = transitionModel
				.get(old_state_action, newState);
		return transitionProbabilityFromOldStateToNewState
				* belief.getProbabilityOf(oldState);
	}

}