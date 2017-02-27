package it.unibz.inf.ontop.iq;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.validation.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.sql.DBMetadataTestingTools;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.OntopModelTestingTools.*;


public class IQValidationTest {

    private final static AtomPredicate TABLE2_PREDICATE = DATA_FACTORY.getAtomPredicate("table1", 2);
    private final static AtomPredicate P3_PREDICATE = DATA_FACTORY.getAtomPredicate("p1", 3);
    private final static AtomPredicate ANS1_VAR1_PREDICATE = DATA_FACTORY.getAtomPredicate("ans1", 1);
    private final static Variable X = DATA_FACTORY.getVariable("x");
    private final static Variable Y = DATA_FACTORY.getVariable("y");
    private final static Variable Z = DATA_FACTORY.getVariable("z");
    private final static Variable A = DATA_FACTORY.getVariable("a");
    private final static Variable B = DATA_FACTORY.getVariable("b");
    private final static Variable C = DATA_FACTORY.getVariable("c");

    private final static ImmutableExpression EXPRESSION = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.EQ, X, Y);

    private final DBMetadata metadata;

    public IQValidationTest() {
        metadata = DBMetadataTestingTools.createDummyMetadata();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testConstructionNodeChild() {
        AtomPredicate TABLE_1 = DATA_FACTORY.getAtomPredicate("table1", 1);
        AtomPredicate TABLE_2 = DATA_FACTORY.getAtomPredicate("table2", 1);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A));
        ExtensionalDataNode table1DataNode = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(TABLE_1, A));
        ExtensionalDataNode table2DataNode = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(TABLE_2, A));

        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, A);

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(metadata, EXECUTOR_REGISTRY);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, table1DataNode);
        queryBuilder.addChild(constructionNode, table2DataNode);

        IntermediateQuery query = queryBuilder.build();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testUnionNodeChild() {
        AtomPredicate TABLE_1 = DATA_FACTORY.getAtomPredicate("table1", 2);
        AtomPredicate TABLE_4 = DATA_FACTORY.getAtomPredicate("table4", 2);
        AtomPredicate TABLE_5 = DATA_FACTORY.getAtomPredicate("table5", 3);

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(metadata, EXECUTOR_REGISTRY);

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables());

        UnionNode unionNode1  = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));

        ExtensionalDataNode table1DataNode = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(TABLE_1, A, B));
        ExtensionalDataNode table4DataNode = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(TABLE_4, A, C));
        ExtensionalDataNode table5DataNode = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(TABLE_5, A, B, C));

        queryBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        queryBuilder.addChild(rootConstructionNode, unionNode1);
        queryBuilder.addChild(unionNode1, joinNode);
        queryBuilder.addChild(unionNode1, table5DataNode);
        queryBuilder.addChild(joinNode, unionNode2);
        queryBuilder.addChild(joinNode, table4DataNode);
        queryBuilder.addChild(unionNode2, table1DataNode);

        IntermediateQuery query = queryBuilder.build();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testUnionNodeProjectedVariables() {
        AtomPredicate TABLE_1 = DATA_FACTORY.getAtomPredicate("table1", 2);
        AtomPredicate TABLE_2 = DATA_FACTORY.getAtomPredicate("table2", 2);
        AtomPredicate TABLE_3 = DATA_FACTORY.getAtomPredicate("table3", 2);
        AtomPredicate TABLE_4 = DATA_FACTORY.getAtomPredicate("table4", 2);
        AtomPredicate TABLE_5 = DATA_FACTORY.getAtomPredicate("table5", 3);

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(metadata, EXECUTOR_REGISTRY);

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables());

        UnionNode unionNode1  = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));

        ExtensionalDataNode table1DataNode = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(TABLE_1, A, B));
        ExtensionalDataNode table2DataNode = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(TABLE_2, A, B));
        ExtensionalDataNode table3DataNode = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(TABLE_3, A, B));
        ExtensionalDataNode table4DataNode = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(TABLE_4, A, C));
        ExtensionalDataNode table5DataNode = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(TABLE_5, A, B, C));

        queryBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        queryBuilder.addChild(rootConstructionNode, unionNode1);
        queryBuilder.addChild(unionNode1, joinNode);
        queryBuilder.addChild(unionNode1, table5DataNode);
        queryBuilder.addChild(joinNode, unionNode2);
        queryBuilder.addChild(joinNode, table4DataNode);
        queryBuilder.addChild(unionNode2, table1DataNode);
        queryBuilder.addChild(unionNode2, table2DataNode);
        queryBuilder.addChild(unionNode2, table3DataNode);

        IntermediateQuery query = queryBuilder.build();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testInnerJoinNodeChildren() {
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(metadata, EXECUTOR_REGISTRY);
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EXPRESSION);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, innerJoinNode);
        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder.addChild(innerJoinNode, dataNode);
        IntermediateQuery query = queryBuilder.build();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testLeftJoinNodeChildren() {
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(metadata, EXECUTOR_REGISTRY);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(EXPRESSION);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder.addChild(leftJoinNode, dataNode, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);
        IntermediateQuery query = queryBuilder.build();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testFilterNodeChild() {
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(metadata, EXECUTOR_REGISTRY);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(EXPRESSION);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, filterNode);
        IntermediateQuery query = queryBuilder.build();
    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testExtensionalDataNodeChildren() {
        AtomPredicate TABLE_1 = DATA_FACTORY.getAtomPredicate("table1", 1);
        AtomPredicate TABLE_2 = DATA_FACTORY.getAtomPredicate("table2", 1);
        AtomPredicate TABLE_3 = DATA_FACTORY.getAtomPredicate("table2", 1);

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(metadata, EXECUTOR_REGISTRY);
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EXPRESSION);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, A);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, innerJoinNode);
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(TABLE_1, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(TABLE_2, A));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(TABLE_3, A));
        queryBuilder.addChild(innerJoinNode, dataNode1);
        queryBuilder.addChild(innerJoinNode, dataNode2);
        queryBuilder.addChild(dataNode1, dataNode3);
        IntermediateQuery query = queryBuilder.build();
    }


    @Test(expected = InvalidIntermediateQueryException.class)
    public void testIntensionalDataNodeChildren() {
        AtomPredicate TABLE_1 = DATA_FACTORY.getAtomPredicate("table1", 1);
        AtomPredicate TABLE_2 = DATA_FACTORY.getAtomPredicate("table2", 1);
        AtomPredicate TABLE_3 = DATA_FACTORY.getAtomPredicate("table2", 1);

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(metadata, EXECUTOR_REGISTRY);
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EXPRESSION);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, A);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, innerJoinNode);
        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(DATA_FACTORY.getDataAtom(TABLE_1, A));
        IntensionalDataNode dataNode2 = IQ_FACTORY.createIntensionalDataNode(DATA_FACTORY.getDataAtom(TABLE_2, A));
        IntensionalDataNode dataNode3 = IQ_FACTORY.createIntensionalDataNode(DATA_FACTORY.getDataAtom(TABLE_3, A));
        queryBuilder.addChild(innerJoinNode, dataNode1);
        queryBuilder.addChild(innerJoinNode, dataNode2);
        queryBuilder.addChild(dataNode1, dataNode3);
        IntermediateQuery query = queryBuilder.build();
    }

//    @Test(expected = InvalidIntermediateQueryException.class)
//    public void testGroupNodeChildren() {
//        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
//        GroupNode groupNode = new GroupNodeImpl(ImmutableList.of(Z));
//        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
//        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
//        queryBuilder.init(projectionAtom, constructionNode);
//        queryBuilder.addChild(constructionNode, groupNode);
//        IntermediateQuery query = queryBuilder.build();
//    }

    @Test(expected = InvalidIntermediateQueryException.class)
    public void testEmptyNodeChildren() {
        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(metadata, EXECUTOR_REGISTRY);
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(Z));
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, Z);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, emptyNode);
        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        queryBuilder.addChild(emptyNode, dataNode);
        IntermediateQuery query = queryBuilder.build();
    }
}
