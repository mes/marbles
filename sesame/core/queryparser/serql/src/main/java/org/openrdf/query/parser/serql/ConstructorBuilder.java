/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.query.algebra.BNodeGenerator;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;

class ConstructorBuilder {

	public TupleExpr buildConstructor(TupleExpr bodyExpr, TupleExpr constructExpr, boolean distinct) {
		return buildConstructor(bodyExpr, constructExpr, true, distinct);
	}

	public TupleExpr buildConstructor(TupleExpr bodyExpr, boolean distinct) {
		return buildConstructor(bodyExpr, bodyExpr, false, distinct);
	}

	private TupleExpr buildConstructor(TupleExpr bodyExpr, TupleExpr constructExpr, boolean explicit,
			boolean distinct)
	{
		TupleExpr result = bodyExpr;

		// Retrieve all StatementPattern's from the construct expression
		List<StatementPattern> statementPatterns = StatementPatternCollector.process(constructExpr);

		Set<Var> constructVars = getConstructVars(statementPatterns);

		// Note: duplicate elimination is a two-step process. The first step
		// removes duplicates from the set of constructor variables. After this
		// step, any bnodes that need to be generated are added to each solution
		// and these solutions are projected to subject-predicate-object bindings.
		// Finally, the spo-bindings are again filtered for duplicates.
		if (distinct) {
			// Create projection that removes all bindings that are not used in the
			// constructor
			ProjectionElemList projElemList = new ProjectionElemList();

			for (Var var : constructVars) {
				// Ignore anonymous and constant vars, these will be handled after
				// the distinct
				if (!var.isAnonymous() && !var.hasValue()) {
					projElemList.addElement(new ProjectionElem(var.getName()));
				}
			}

			result = new Projection(result, projElemList);

			// Filter the duplicates from these projected bindings
			result = new Distinct(result);
		}

		// Create BNodeGenerator's for all anonymous variables
		Map<Var, ExtensionElem> extElemMap = new HashMap<Var, ExtensionElem>();

		for (Var var : constructVars) {
			if (var.isAnonymous() && !extElemMap.containsKey(var)) {
				ValueExpr valueExpr = null;

				if (var.hasValue()) {
					valueExpr = new ValueConstant(var.getValue());
				}
				else if (explicit) {
					// only generate bnodes in case of an explicit constructor
					valueExpr = new BNodeGenerator();
				}

				if (valueExpr != null) {
					extElemMap.put(var, new ExtensionElem(valueExpr, var.getName()));
				}
			}
		}

		if (!extElemMap.isEmpty()) {
			result = new Extension(result, extElemMap.values());
		}

		// Create a Projection for each StatementPattern in the constructor
		List<ProjectionElemList> projections = new ArrayList<ProjectionElemList>();

		for (StatementPattern sp : statementPatterns) {
			ProjectionElemList projElemList = new ProjectionElemList();

			projElemList.addElement(new ProjectionElem(sp.getSubjectVar().getName(), "subject"));
			projElemList.addElement(new ProjectionElem(sp.getPredicateVar().getName(), "predicate"));
			projElemList.addElement(new ProjectionElem(sp.getObjectVar().getName(), "object"));

			projections.add(projElemList);
		}

		if (projections.size() == 1) {
			result = new Projection(result, projections.get(0));

			// Note: no need to apply the second duplicate elimination step if
			// there's just one projection
		}
		else if (projections.size() > 1) {
			result = new MultiProjection(result, projections);

			if (distinct) {
				// Add another distinct to filter duplicate statements
				result = new Distinct(result);
			}
		}
		else {
			// Empty constructor
			result = new EmptySet();
		}

		return result;
	}

	/**
	 * Gets the set of variables that are relevant for the constructor. This
	 * method accumulates all subject, predicate and object variables from the
	 * supplied statement patterns, but ignores any context variables.
	 */
	private Set<Var> getConstructVars(Collection<StatementPattern> statementPatterns) {
		Set<Var> vars = new LinkedHashSet<Var>(statementPatterns.size() * 2);

		for (StatementPattern sp : statementPatterns) {
			vars.add(sp.getSubjectVar());
			vars.add(sp.getPredicateVar());
			vars.add(sp.getObjectVar());
		}

		return vars;
	}
}
