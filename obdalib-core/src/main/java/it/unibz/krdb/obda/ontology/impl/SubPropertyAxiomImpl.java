package it.unibz.krdb.obda.ontology.impl;

/*
 * #%L
 * ontop-obdalib-core
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

import it.unibz.krdb.obda.ontology.Property;

public class SubPropertyAxiomImpl extends AbstractSubDescriptionAxiom {

	private static final long serialVersionUID = -3020225654321319941L;

	SubPropertyAxiomImpl(Property included, Property including) {
		super(included, including);
	}

	public Property getSub() {
		return (Property) included;
	}

	public Property getSuper() {
		return (Property) including;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof SubPropertyAxiomImpl)) {
			return false;
		}
		SubPropertyAxiomImpl inc2 = (SubPropertyAxiomImpl) obj;
		if (!including.equals(inc2.including)) {
			return false;
		}
		return (included.equals(inc2.included));
	}
}