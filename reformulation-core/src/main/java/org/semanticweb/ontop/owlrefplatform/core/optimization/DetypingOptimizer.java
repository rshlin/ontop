package org.semanticweb.ontop.owlrefplatform.core.optimization;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.DetypingProposalImpl;

/**
 * TODO: explain
 *
 * TODO: make it more specific and find another name?
 * TODO: with the generalization of the use of ProjectionNodes should not be needed.
 */
@Deprecated
public class DetypingOptimizer implements QueryOptimizer {

    private final IntermediateQuery queryToOptimize;

    public DetypingOptimizer(IntermediateQuery queryToOptimize) {
        this.queryToOptimize = queryToOptimize;
    }


    @Override
    public Optional<LocalOptimizationProposal> makeProposal(InnerJoinNode node) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public Optional<LocalOptimizationProposal> makeProposal(SimpleFilterNode filterNode) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public Optional<LocalOptimizationProposal> makeProposal(ProjectionNode projectionNode) {
        ImmutableMap<Function,Variable> typesToReplace = proposeReplacement(extractTypes(projectionNode.getHeadAtom()));
        return buildProposal(projectionNode, typesToReplace);
    }

    /**
     * TODO: explain
     */
    private ImmutableMap<Function, Variable> proposeReplacement(ImmutableList<Function> typedFunctionalTerms) {
        ImmutableMap.Builder<Function, Variable> mapBuilder = ImmutableMap.builder();
        for (Function functionalTerm : typedFunctionalTerms) {
            mapBuilder.put(functionalTerm, queryToOptimize.createNewVariable());
        }
        return mapBuilder.build();
    }

    /**
     * TODO: explain
     *
     * TODO: see how to behave with aggregates.
     */
    private ImmutableList<Function> extractTypes(PureDataAtom headAtom) {
        ImmutableList.Builder<Function> typeBuilder = ImmutableList.builder();
        for (Term term : headAtom.getTerms()) {
            if (term instanceof Function) {
                typeBuilder.add((Function) term);
            }
        }
        return typeBuilder.build();
    }

    @Override
    public Optional<LocalOptimizationProposal> makeProposal(UnionNode unionNode) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public Optional<LocalOptimizationProposal> makeProposal(OrdinaryDataNode ordinaryDataNode) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public Optional<LocalOptimizationProposal> makeProposal(TableNode tableNode) {
        throw new RuntimeException("TODO: implement it");
    }

    /**
     * TODO: explain
     */
    private Optional<LocalOptimizationProposal> buildProposal(QueryNode nodeToDetype,
                                                              ImmutableMap<Function, Variable> typesToReplace) {
        if (typesToReplace.isEmpty()) {
            return Optional.absent();
        }
        LocalOptimizationProposal detypingProposal = new DetypingProposalImpl(nodeToDetype, queryToOptimize,
                typesToReplace);
        return Optional.of(detypingProposal);
    }
}