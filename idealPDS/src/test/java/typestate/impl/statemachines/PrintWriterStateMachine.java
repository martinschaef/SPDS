package typestate.impl.statemachines;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import boomerang.jimple.Val;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import typestate.finiteautomata.MatcherStateMachine;
import typestate.finiteautomata.MatcherTransition;
import typestate.finiteautomata.MatcherTransition.Parameter;
import typestate.finiteautomata.MatcherTransition.Type;
import typestate.finiteautomata.State;

public class PrintWriterStateMachine extends MatcherStateMachine{

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

	}

	public PrintWriterStateMachine() {
		addTransition(new MatcherTransition(States.NONE, closeMethods(), Parameter.This, States.CLOSED, Type.OnReturn));
		addTransition(
				new MatcherTransition(States.CLOSED, closeMethods(), Parameter.This, States.CLOSED, Type.OnReturn));
		addTransition(new MatcherTransition(States.CLOSED, readMethods(), Parameter.This, States.ERROR, Type.OnReturn));
		addTransition(new MatcherTransition(States.ERROR, readMethods(), Parameter.This, States.ERROR, Type.OnReturn));

	}

	private Set<SootMethod> closeMethods() {
		return selectMethodByName(getSubclassesOf("java.io.PrintWriter"), "close");
	}

	private Set<SootMethod> readMethods() {
		List<SootClass> subclasses = getSubclassesOf("java.io.PrintWriter");
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
	public Collection<Val> generateSeed(SootMethod m, Unit unit,
			Collection<SootMethod> calledMethod) {
		return generateThisAtAnyCallSitesOf(m, unit, calledMethod, closeMethods());
	}

}