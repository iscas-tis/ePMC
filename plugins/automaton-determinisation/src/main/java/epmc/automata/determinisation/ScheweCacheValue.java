package epmc.automata.determinisation;

final class ScheweCacheValue {
    final AutomatonScheweState state;
    final AutomatonScheweLabeling labeling;
    
    ScheweCacheValue(AutomatonScheweState state, AutomatonScheweLabeling labeling) {
        this.state = state;
        this.labeling = labeling;
    }
    
}
