package epmc.propertysolverltlfg.automaton;

public interface Acceptance {
	int getFin();
	int getInf(int index);
	int getInfSize();
	boolean isTrue();
	boolean isFalse();
}
