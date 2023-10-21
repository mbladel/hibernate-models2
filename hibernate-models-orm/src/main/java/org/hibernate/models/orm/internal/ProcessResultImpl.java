/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.internal;

import java.util.Map;
import java.util.Set;

import org.hibernate.models.orm.spi.EntityHierarchy;
import org.hibernate.models.orm.spi.GlobalRegistrations;
import org.hibernate.models.orm.spi.ProcessResult;
import org.hibernate.models.source.spi.ClassDetails;

/**
 * @author Steve Ebersole
 */
public class ProcessResultImpl implements ProcessResult {
	private final Set<EntityHierarchy> entityHierarchies;
	private final Map<String, ClassDetails> mappedSuperclasses;
	private final Map<String, ClassDetails> embeddables;
	private final GlobalRegistrations globalRegistrations;

	public ProcessResultImpl(
			Set<EntityHierarchy> entityHierarchies,
			Map<String, ClassDetails> mappedSuperclasses,
			Map<String, ClassDetails> embeddables,
			GlobalRegistrations globalRegistrations) {
		this.entityHierarchies = entityHierarchies;
		this.mappedSuperclasses = mappedSuperclasses;
		this.embeddables = embeddables;
		this.globalRegistrations = globalRegistrations;
	}

	@Override
	public Set<EntityHierarchy> getEntityHierarchies() {
		return entityHierarchies;
	}

	public Map<String, ClassDetails> getMappedSuperclasses() {
		return mappedSuperclasses;
	}

	@Override
	public Map<String, ClassDetails> getEmbeddables() {
		return embeddables;
	}

	@Override
	public GlobalRegistrations getGlobalRegistrations() {
		return globalRegistrations;
	}
}
