package it.unibz.inf.ontop.owlapi.resultset;

import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;

public interface OWLBinding {
    /**
     * Gets the name of the binding (e.g. the variable name).
     *
     * @return The name of the binding.
     */
    String getName();

    /**
     * Gets the value of the binding. The returned value is never equal to <tt>null</tt>, such a "binding" is
     * considered to be unbound.
     *
     * @return The value of the binding, never <tt>null</tt>.
     */
    OWLObject getValue() throws OWLException;
}
