package it.unibz.inf.ontop.owlrefplatform.core;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.inf.ontop.answering.input.*;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.answering.reformulation.OntopQueryReformulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;


/**
 * Abstract class for QuestStatement.
 *
 * TODO: rename it (not now) AbstractQuestStatement.
 */
public abstract class QuestStatement implements OntopStatement {

	private final OntopQueryReformulator engine;

	private QueryExecutionThread executionThread;
	private boolean canceled = false;


	private static final Logger log = LoggerFactory.getLogger(QuestStatement.class);


	public QuestStatement(OntopQueryReformulator queryProcessor) {
		this.engine = queryProcessor;
	}

	/**
	 * TODO: explain
	 */
	@FunctionalInterface
	private interface Evaluator<R extends OBDAResultSet, Q extends InputQuery<R>> {

		R evaluate(Q inputQuery, ExecutableQuery executableQuery)
				throws OntopQueryEvaluationException, OntopResultConversionException, OntopConnectionException;
	}

	/**
	 * Execution thread
	 */
	private class QueryExecutionThread<R extends OBDAResultSet, Q extends InputQuery<R>> extends Thread {

		private final Q inputQuery;
		private final QuestStatement.Evaluator<R, Q> evaluator;
		private final CountDownLatch monitor;
		private final ExecutableQuery executableQuery;

		private R resultSet;	  // only for SELECT and ASK queries
		private Exception exception;
		private boolean executingTargetQuery;

		QueryExecutionThread(Q inputQuery, ExecutableQuery executableQuery, Evaluator<R,Q> evaluator,
							 CountDownLatch monitor) {
			this.executableQuery = executableQuery;
			this.inputQuery = inputQuery;
			this.evaluator = evaluator;
			this.monitor = monitor;
			this.exception = null;
			this.executingTargetQuery = false;
		}

		public boolean errorStatus() {
			return exception != null;
		}

		public Exception getException() {
			return exception;
		}

		public R getResultSet() {
			return resultSet;
		}

		public void cancel() throws OntopQueryEvaluationException {
			canceled = true;
			if (!executingTargetQuery) {
				this.stop();
			} else {
				cancelExecution();
			}
		}

		@Override
		public void run() {
			//                        FOR debugging H2 in-memory database
//			try {
//				org.h2.tools.Server.startWebServer(((QuestConnection)conn).getSQLConnection());
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
			try {
				/**
				 * Executes the target query.
				 */
				log.debug("Executing the query and get the result...");
				executingTargetQuery = true;
				resultSet = evaluator.evaluate(inputQuery, executableQuery);
				log.debug("Execution finished.\n");
				/*
				 * TODO: re-handle the timeout exception.
				 */
			} catch (Exception e) {
				e.printStackTrace();
				exception = e;
				log.error(e.getMessage(), e);
			} finally {
				monitor.countDown();
			}
		}
	}


	/**
	 * TODO: describe
	 */
	protected abstract TupleResultSet executeSelectQuery(SelectQuery inputQuery, ExecutableQuery executableQuery)
			throws OntopQueryEvaluationException;

	/**
	 * TODO: describe
	 */
	protected abstract BooleanResultSet executeBooleanQuery(AskQuery inputQuery, ExecutableQuery executableQuery)
			throws OntopQueryEvaluationException;

	/**
	 * TODO: describe
	 */
	private GraphResultSet executeDescribeConstructQuery(ConstructQuery constructQuery, ExecutableQuery executableQuery)
			throws OntopQueryEvaluationException, OntopResultConversionException, OntopConnectionException {
		return executeGraphQuery(constructQuery, executableQuery, true);
	}

	/**
	 * TODO: describe
	 */
	private GraphResultSet executeConstructQuery(ConstructQuery constructQuery, ExecutableQuery executableQuery)
			throws OntopQueryEvaluationException, OntopResultConversionException, OntopConnectionException {
		return executeGraphQuery(constructQuery, executableQuery, false);
	}

	/**
	 * TODO: refactor
	 */
	protected abstract GraphResultSet executeGraphQuery(ConstructQuery query, ExecutableQuery executableQuery,
														boolean collectResults)
			throws OntopQueryEvaluationException, OntopResultConversionException, OntopConnectionException;

	/**
	 * Cancel the processing of the target query.
	 */
	protected abstract void cancelExecution() throws OntopQueryEvaluationException;

	/**
	 * Calls the necessary tuple or graph query execution Implements describe
	 * uri or var logic Returns the result set for the given query
	 */
	@Override
	public <R extends OBDAResultSet> R execute(InputQuery<R> inputQuery) throws OntopConnectionException,
			OntopReformulationException, OntopQueryEvaluationException, OntopResultConversionException {
		if (inputQuery instanceof SelectQuery) {
			return (R) executeInThread((SelectQuery) inputQuery, this::executeSelectQuery);
		}
		else if (inputQuery instanceof AskQuery) {
			return (R) executeInThread((AskQuery) inputQuery, this::executeBooleanQuery);
		}
		else if (inputQuery instanceof ConstructQuery) {
			return (R) executeInThread((ConstructQuery) inputQuery, this::executeConstructQuery);
		}
		else if (inputQuery instanceof DescribeQuery) {
			throw new RuntimeException("TODO: fix Describe queries");

//			// create list of URI constants we want to describe
//			List<String> constants = new ArrayList<>();
//			if (SPARQLQueryUtility.isVarDescribe(inputQueryString)) {
//				// if describe ?var, we have to do select distinct ?var first
//				String sel = SPARQLQueryUtility.getSelectVarDescribe(inputQueryString);
//				OBDAResultSet resultSet = executeTupleQuery(sel, engine.parseQuery(sel), QueryType.SELECT);
//				if (resultSet instanceof EmptyTupleResultSet)
//					return null;
//				else if (resultSet instanceof QuestTupleResultSet) {
//					QuestTupleResultSet res = (QuestTupleResultSet) resultSet;
//					while (res.nextRow()) {
//						Constant constant = res.getConstant(1);
//						if (constant instanceof URIConstant) {
//							// collect constants in list
//							constants.add(((URIConstant)constant).getURI());
//						}
//					}
//				}
//			}
//			else if (SPARQLQueryUtility.isURIDescribe(inputQueryString)) {
//				// DESCRIBE <uri> gives direct results, so we put the
//				// <uri> constant directly in the list of constants
//				try {
//					constants.add(SPARQLQueryUtility.getDescribeURI(inputQueryString));
//				} catch (MalformedQueryException e) {
//					e.printStackTrace();
//				}
//			}
//
//			GraphResultSet describeResultSet = null;
//			// execute describe <uriconst> in subject position
//			for (String constant : constants) {
//				// for each constant we execute a construct with
//				// the uri as subject, and collect the results
//				String str = SPARQLQueryUtility.getConstructSubjQuery(constant);
//				GraphResultSet set = (GraphResultSet) executeGraphQuery(str, QueryType.DESCRIBE);
//				if (describeResultSet == null) { // just for the first time
//					describeResultSet = set;
//				}
//				else if (set != null) {
//					// 2nd and manyth times execute, but collect result into one object
//					while (set.hasNext())
//						describeResultSet.addNewResultSet(set.next());
//				}
//			}
//			// execute describe <uriconst> in object position
//			for (String constant : constants) {
//				String str = SPARQLQueryUtility.getConstructObjQuery(constant);
//				GraphResultSet set = (GraphResultSet) executeGraphQuery(str, QueryType.DESCRIBE);
//				if (describeResultSet == null) { // just for the first time
//					describeResultSet = set;
//				}
//				else if (set != null) {
//					while (set.hasNext())
//						describeResultSet.addNewResultSet(set.next());
//				}
//			}
//			return describeResultSet;
		}
		else {
			throw new OntopUnsupportedInputQueryException("Unsupported query type: " + inputQuery);
		}
	}

	/**
	 * Internal method to start a new query execution thread type defines the
	 * query type SELECT, ASK, CONSTRUCT, or DESCRIBE
	 */
	private <R extends OBDAResultSet, Q extends InputQuery<R>> R executeInThread(Q inputQuery, Evaluator<R, Q> evaluator)
			throws OntopReformulationException, OntopQueryEvaluationException {

		log.debug("Executing SPARQL query: \n{}", inputQuery);

		CountDownLatch monitor = new CountDownLatch(1);
		ExecutableQuery executableQuery = engine.translateIntoNativeQuery(inputQuery);

		QueryExecutionThread<R, Q> executionthread = new QueryExecutionThread<>(inputQuery, executableQuery, evaluator,
				monitor);

		this.executionThread = executionthread;
		executionthread.start();
		try {
			monitor.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (executionthread.errorStatus()) {
			Exception ex = executionthread.getException();
			if (ex instanceof OntopReformulationException) {
				throw (OntopReformulationException) ex;
			}
			else if (ex instanceof OntopQueryEvaluationException) {
				throw (OntopQueryEvaluationException) ex;
			}
			else {
				throw new OntopQueryEvaluationException(ex);
			}
		}

		if (canceled) {
			canceled = false;
			throw new OntopQueryEvaluationException("Query execution was cancelled");
		}
		return executionthread.getResultSet();
	}

	
	@Override
	public void cancel() throws OntopConnectionException {
		canceled = true;
		try {
			QuestStatement.this.executionThread.cancel();
		} catch (Exception e) {
			throw new OntopConnectionException(e);
		}
	}

	/**
	 * Called to check whether the statement was cancelled on purpose
	 */
	public boolean isCanceled(){
		return canceled;
	}

	@Override
	public String getRewritingRendering(InputQuery query) throws OntopReformulationException, OntopInvalidInputQueryException {
		return engine.getRewritingRendering(query);
	}


	@Override
	public ExecutableQuery getExecutableQuery(InputQuery inputQuery) throws OntopReformulationException {
			return engine.translateIntoNativeQuery(inputQuery);
	}

	protected boolean hasDistinctResultSet() {
		return engine.hasDistinctResultSet();
	}

}
