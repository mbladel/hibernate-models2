/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.xml.dynamic;

import java.util.Set;

import org.hibernate.annotations.JavaType;
import org.hibernate.boot.internal.Target;
import org.hibernate.models.orm.internal.ManagedResourcesImpl;
import org.hibernate.models.orm.spi.EntityHierarchy;
import org.hibernate.models.orm.spi.EntityTypeMetadata;
import org.hibernate.models.orm.spi.ManagedResources;
import org.hibernate.models.orm.spi.ProcessResult;
import org.hibernate.models.orm.spi.Processor;
import org.hibernate.models.orm.xml.SimpleEntity;
import org.hibernate.models.source.SourceModelTestHelper;
import org.hibernate.models.source.internal.SourceModelBuildingContextImpl;
import org.hibernate.models.source.spi.FieldDetails;

import org.junit.jupiter.api.Test;

import org.jboss.jandex.Index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.models.internal.SimpleClassLoading.SIMPLE_CLASS_LOADING;

/**
 * @author Steve Ebersole
 */
public class DynamicModelTests {
	@Test
	void testSimpleDynamicModel() {
		final ManagedResources managedResources = new ManagedResourcesImpl.Builder()
				.addXmlMappings( "mappings/dynamic/dynamic-simple.xml" )
				.build();
		final Index jandexIndex = SourceModelTestHelper.buildJandexIndex( SIMPLE_CLASS_LOADING );
		final SourceModelBuildingContextImpl buildingContext = SourceModelTestHelper.createBuildingContext(
				jandexIndex,
				SIMPLE_CLASS_LOADING
		);

		final ProcessResult processResult = Processor.process(
				managedResources,
				null,
				new Processor.Options() {
					@Override
					public boolean areGeneratorsGlobal() {
						return false;
					}

					@Override
					public boolean shouldIgnoreUnlistedClasses() {
						return false;
					}
				},
				buildingContext
		);

		assertThat( processResult.getEntityHierarchies() ).hasSize( 1 );
		final EntityHierarchy hierarchy = processResult.getEntityHierarchies().iterator().next();
		final EntityTypeMetadata rootEntity = hierarchy.getRoot();
		assertThat( rootEntity.getClassDetails().getClassName() ).isNull();
		assertThat( rootEntity.getClassDetails().getName() ).isEqualTo( "SimpleEntity" );

		final FieldDetails idField = rootEntity.getClassDetails().findFieldByName( "id" );
		assertThat( idField.getType().getClassName() ).isEqualTo( Integer.class.getName() );

		final FieldDetails nameField = rootEntity.getClassDetails().findFieldByName( "name" );
		assertThat( nameField.getType().getClassName() ).isEqualTo( Object.class.getName() );
		assertThat( nameField.getAnnotationUsage( JavaType.class ) ).isNotNull();

		final FieldDetails qtyField = rootEntity.getClassDetails().findFieldByName( "quantity" );
		assertThat( qtyField.getType().getClassName() ).isEqualTo( int.class.getName() );
	}

	@Test
	void testSemiSimpleDynamicModel() {
		final ManagedResources managedResources = new ManagedResourcesImpl.Builder()
				.addXmlMappings( "mappings/dynamic/dynamic-semi-simple.xml" )
				.build();
		final Index jandexIndex = SourceModelTestHelper.buildJandexIndex( SIMPLE_CLASS_LOADING );
		final SourceModelBuildingContextImpl buildingContext = SourceModelTestHelper.createBuildingContext(
				jandexIndex,
				SIMPLE_CLASS_LOADING
		);

		final ProcessResult processResult = Processor.process(
				managedResources,
				null,
				new Processor.Options() {
					@Override
					public boolean areGeneratorsGlobal() {
						return false;
					}

					@Override
					public boolean shouldIgnoreUnlistedClasses() {
						return false;
					}
				},
				buildingContext
		);

		assertThat( processResult.getEntityHierarchies() ).hasSize( 1 );
		final EntityHierarchy hierarchy = processResult.getEntityHierarchies().iterator().next();
		final EntityTypeMetadata rootEntity = hierarchy.getRoot();
		assertThat( rootEntity.getClassDetails().getClassName() ).isNull();
		assertThat( rootEntity.getClassDetails().getName() ).isEqualTo( "Contact" );

		final FieldDetails idField = rootEntity.getClassDetails().findFieldByName( "id" );
		assertThat( idField.getType().getClassName() ).isEqualTo( Integer.class.getName() );

		final FieldDetails nameField = rootEntity.getClassDetails().findFieldByName( "name" );
		assertThat( nameField.getType().getClassName() ).isNull();
		assertThat( nameField.getType().getName() ).isEqualTo( "Name" );
		assertThat( nameField.getAnnotationUsage( Target.class ) ).isNotNull();
		assertThat( nameField.getAnnotationUsage( Target.class ).getString( "value" ) ).isEqualTo( "Name" );

		assertThat( nameField.getType().getFields() ).hasSize( 2 );

		final FieldDetails labelsField = rootEntity.getClassDetails().findFieldByName( "labels" );
		assertThat( labelsField.getType().getClassName() ).isEqualTo( Set.class.getName() );
	}

}
