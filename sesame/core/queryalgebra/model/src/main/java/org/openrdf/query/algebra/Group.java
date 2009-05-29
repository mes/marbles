/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import info.aduna.collections.iterators.Iterators;

/**
 * A tuple operator that groups tuples that have a specific set of equivalent
 * variable bindings, and that can apply aggregate functions on the grouped
 * results.
 * 
 * @author David Huynh
 * @author Arjohn Kampman
 */
public class Group extends UnaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Set<String> groupBindings = new LinkedHashSet<String>();

	private List<GroupElem> groupElements = new ArrayList<GroupElem>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Group(TupleExpr arg) {
		super(arg);
	}

	public Group(TupleExpr arg, Iterable<String> groupBindingNames) {
		this(arg);
		setGroupBindingNames(groupBindingNames);
	}

	public Group(TupleExpr arg, Iterable<String> groupBindingNames, Iterable<GroupElem> groupElements) {
		this(arg, groupBindingNames);
		setGroupElements(groupElements);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Set<String> getGroupBindingNames() {
		return Collections.unmodifiableSet(groupBindings);
	}

	public void addGroupBindingName(String bindingName) {
		groupBindings.add(bindingName);
	}

	public void setGroupBindingNames(Iterable<String> bindingNames) {
		groupBindings.clear();
		Iterators.addAll(bindingNames.iterator(), groupBindings);
	}

	public List<GroupElem> getGroupElements() {
		return Collections.unmodifiableList(groupElements);
	}

	public void addGroupElement(GroupElem groupElem) {
		groupElements.add(groupElem);
	}

	public void setGroupElements(Iterable<GroupElem> elements) {
		this.groupElements.clear();
		Iterators.addAll(elements.iterator(), this.groupElements);
	}

	public Set<String> getAggregateBindingNames() {
		Set<String> bindings = new HashSet<String>();

		for (GroupElem binding : groupElements) {
			bindings.add(binding.getName());
		}

		return bindings;
	}

	@Override
	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>();

		bindingNames.addAll(getGroupBindingNames());
		bindingNames.addAll(getAggregateBindingNames());

		return bindingNames;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		super.visitChildren(visitor);

		for (GroupElem ge : groupElements) {
			ge.visit(visitor);
		}
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement)
	{
		int index = groupElements.indexOf(current);
		if (index >= 0) {
			groupElements.set(index, (GroupElem)replacement);
			replacement.setParentNode(this);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public Group clone() {
		Group clone = (Group)super.clone();

		clone.setGroupBindingNames(getGroupBindingNames());

		List<GroupElem> elementsClone = new ArrayList<GroupElem>(getGroupElements().size());
		for (GroupElem ge : getGroupElements()) {
			elementsClone.add(ge.clone());
		}

		clone.setGroupElements(elementsClone);

		return clone;
	}
}
