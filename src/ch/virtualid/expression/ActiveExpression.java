package ch.virtualid.expression;

/**
 * Description.
 * 
 * @invariant code : "Text.";
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public class ActiveExpression extends PassiveExpression {
    
    /**
     * Asserts that the class invariant still holds.
     */
    protected void invariant() {
        assert true: "Text.";
    }
    
    public ActiveExpression(String string) {
        super(string);
    }
    
}
