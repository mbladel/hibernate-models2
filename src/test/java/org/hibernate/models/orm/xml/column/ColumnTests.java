/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.xml.column;

import org.hibernate.boot.internal.BootstrapContextImpl;
import org.hibernate.boot.internal.MetadataBuilderImpl;
import org.hibernate.boot.model.process.spi.ManagedResources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.models.orm.categorize.spi.CategorizedDomainModel;
import org.hibernate.models.orm.categorize.spi.EntityHierarchy;
import org.hibernate.models.orm.categorize.spi.EntityTypeMetadata;
import org.hibernate.models.orm.process.ManagedResourcesImpl;
import org.hibernate.models.spi.AnnotationUsage;
import org.hibernate.models.spi.FieldDetails;

import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.models.orm.categorize.spi.ManagedResourcesProcessor.processManagedResources;

/**
 * @author Steve Ebersole
 */
@ServiceRegistry
public class ColumnTests {
	@Test
	void testCompleteColumn(ServiceRegistryScope scope) {
		final ManagedResources managedResources = new ManagedResourcesImpl.Builder()
				.addXmlMappings( "mappings/column/complete.xml" )
				.build();
		final StandardServiceRegistry serviceRegistry = scope.getRegistry();
		final BootstrapContextImpl bootstrapContext = new BootstrapContextImpl(
				serviceRegistry,
				new MetadataBuilderImpl.MetadataBuildingOptionsImpl( serviceRegistry )
		);
		final CategorizedDomainModel categorizedDomainModel = processManagedResources(
				managedResources,
				bootstrapContext
		);

		assertThat( categorizedDomainModel.getEntityHierarchies() ).hasSize( 1 );

		final EntityHierarchy hierarchy = categorizedDomainModel.getEntityHierarchies().iterator().next();
		final EntityTypeMetadata root = hierarchy.getRoot();
		assertThat( root.getClassDetails().getClassName() ).isEqualTo( AnEntity.class.getName() );
		assertThat( root.getNumberOfAttributes() ).isEqualTo( 2 );
		final FieldDetails nameField = root.getClassDetails().findFieldByName( "name" );
		assertThat( nameField ).isNotNull();
		final AnnotationUsage<Column> annotationUsage = nameField.getAnnotationUsage( Column.class );
		assertThat( annotationUsage.getString( "name" ) ).isEqualTo( "nombre" );
		assertThat( annotationUsage.getInteger( "length" ) ).isEqualTo( 256 );
		assertThat( annotationUsage.getString( "comment" ) ).isEqualTo( "The name column" );
		assertThat( annotationUsage.getString( "table" ) ).isEqualTo( "tbl" );
		assertThat( annotationUsage.getString( "options" ) ).isEqualTo( "the options" );
		assertThat( annotationUsage.getList( "check" ) ).isNotEmpty();
	}

	@Test
	void testOverrideColumn(ServiceRegistryScope scope) {
		final ManagedResources managedResources = new ManagedResourcesImpl.Builder()
				.addXmlMappings( "mappings/column/override.xml" )
				.build();

		final StandardServiceRegistry serviceRegistry = scope.getRegistry();
		final BootstrapContextImpl bootstrapContext = new BootstrapContextImpl(
				serviceRegistry,
				new MetadataBuilderImpl.MetadataBuildingOptionsImpl( serviceRegistry )
		);
		final CategorizedDomainModel categorizedDomainModel = processManagedResources(
				managedResources,
				bootstrapContext
		);

		assertThat( categorizedDomainModel.getEntityHierarchies() ).hasSize( 1 );

		final EntityHierarchy hierarchy = categorizedDomainModel.getEntityHierarchies().iterator().next();
		final EntityTypeMetadata root = hierarchy.getRoot();
		assertThat( root.getClassDetails().getClassName() ).isEqualTo( AnEntity.class.getName() );

		assertThat( root.getNumberOfAttributes() ).isEqualTo( 2 );
		final FieldDetails nameField = root.getClassDetails().findFieldByName( "name" );
		assertThat( nameField ).isNotNull();
		final AnnotationUsage<Column> columnAnn = nameField.getAnnotationUsage( Column.class );
		assertThat( columnAnn ).isNotNull();
		assertThat( columnAnn.getString( "name" ) ).isEqualTo( "nombre" );
	}
}
