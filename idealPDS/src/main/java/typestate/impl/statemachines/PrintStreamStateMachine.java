/*******************************************************************************
 * Copyright (c) 2018 Fraunhofer IEM, Paderborn, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Johannes Spaeth - initial API and implementation
 *******************************************************************************/
package typestate.impl.statemachines;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import boomerang.WeightedForwardQuery;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import typestate.TransitionFunction;
import typestate.finiteautomata.TypeStateMachineWeightFunctions;
import typestate.finiteautomata.MatcherTransition;
import typestate.finiteautomata.MatcherTransition.Parameter;
import typestate.finiteautomata.MatcherTransition.Type;
import typestate.finiteautomata.State;

public class PrintStreamStateMachine extends TypeStateMachineWeightFunctions{


	public static enum States implements State {
		NONE, CLOSED, ERROR;

		@Override
		public boolean isErrorState() {
			return this == ERROR;
		}

		@Override
		public boolean isInitialState() {
			return false;
		}

		@Override
		public boolean isAccepting() {
			return false;
		}
	}

	public PrintStreamStateMachine() {
		addTransition(
				new MatcherTransition(States.CLOSED, closeMethods(), Parameter.This, States.CLOSED, Type.OnReturn));
		addTransition(new MatcherTransition(States.CLOSED, readMethods(), Parameter.This, States.ERROR, Type.OnReturn));
	}

	private Set<SootMethod> closeMethods() {
		return selectMethodByName(getSubclassesOf("java.io.PrintStream"), "close");
	}

	private Set<SootMethod> readMethods() {
		List<SootClass> subclasses = getSubclassesOf("java.io.PrintStream");
		Set<SootMethod> closeMethods = closeMethods();
		Set<SootMethod> out = new HashSet<>();
		for (SootClass c : subclasses) {
			for (SootMethod m : c.getMethods())
				if (m.isPublic() && !closeMethods.contains(m) && !m.isStatic())
					out.add(m);
		}
		return out;
	}

	@Override
	public Collection<WeightedForwardQuery<TransitionFunction>> generateSeed(SootMethod m, Unit unit, Collection<SootMethod> calledMethod) {
		return this.generateThisAtAnyCallSitesOf(m, unit, calledMethod, closeMethods());
	}

	@Override
	protected State initialState() {
		return States.CLOSED;
	}
}