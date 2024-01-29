package net.dirtydetector.agent;

/**
 *
 * @author jbertinetti
 */
public class TDDAgentInitializationException extends RuntimeException {
    
    public TDDAgentInitializationException(Throwable cause) {
        super("Error initializing TDD Agent", cause);
    }
    
}
