/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.xml;

import java.util.List;

import org.hibernate.boot.jaxb.mapping.JaxbEntityMappings;
import org.hibernate.models.orm.internal.ProcessResultCollector;
import org.hibernate.models.orm.xml.internal.XmlDocumentImpl;
import org.hibernate.models.orm.xml.spi.XmlResources;
import org.hibernate.models.orm.xml.spi.PersistenceUnitMetadata;
import org.hibernate.models.source.SourceModelTestHelper;
import org.hibernate.models.source.internal.StringTypeDescriptor;
import org.hibernate.models.source.spi.SourceModelBuildingContext;
import org.hibernate.type.descriptor.jdbc.ClobJdbcType;

import org.junit.jupiter.api.Test;

import static jakarta.persistence.AccessType.FIELD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.annotations.CascadeType.LOCK;
import static org.hibernate.annotations.CascadeType.PERSIST;
import static org.hibernate.annotations.CascadeType.REMOVE;
import static org.hibernate.models.internal.SimpleClassLoading.SIMPLE_CLASS_LOADING;
import static org.hibernate.models.orm.XmlHelper.loadMapping;

/**
 * @author Steve Ebersole
 */
public class XmlProcessingSmokeTests {
	@Test
	void testPersistenceUnitDefaults1() {
		final XmlResources collector = new XmlResources();

		final JaxbEntityMappings simple1 = loadMapping( "mappings/simple1.xml", SIMPLE_CLASS_LOADING );
		final JaxbEntityMappings simple2 = loadMapping( "mappings/simple2.xml", SIMPLE_CLASS_LOADING );
		collector.addDocument( simple1 );
		collector.addDocument( simple2);

		final PersistenceUnitMetadata metadata = collector.getPersistenceUnitMetadata();
		// xml-mappings-complete is a gated flag - once we see a true, it should always be considered true
		assertThat( metadata.areXmlMappingsComplete() ).isTrue();
		// same for quoted identifiers
		assertThat( metadata.useQuotedIdentifiers() ).isTrue();

		// default cascades are additive
		assertThat( metadata.getDefaultCascadeTypes() ).containsAll( List.of( PERSIST, REMOVE, LOCK ) );

		// simple2.xml should take precedence
		assertThat( metadata.getDefaultCatalog() ).isEqualTo( "catalog2" );
		assertThat( metadata.getDefaultSchema() ).isEqualTo( "schema2" );
		assertThat( metadata.getAccessType() ).isEqualTo( FIELD );
		assertThat( metadata.getDefaultAccessStrategyName() ).isEqualTo( "MIXED" );
	}

	@Test
	void testPersistenceUnitDefaults2() {
		final XmlResources collector = new XmlResources();

		collector.addDocument( loadMapping( "mappings/simple2.xml", SIMPLE_CLASS_LOADING ) );
		collector.addDocument( loadMapping( "mappings/simple1.xml", SIMPLE_CLASS_LOADING ) );

		final PersistenceUnitMetadata metadata = collector.getPersistenceUnitMetadata();
		// xml-mappings-complete is a gated flag - once we see a true, it should always be considered true
		assertThat( metadata.areXmlMappingsComplete() ).isTrue();
		// same for quoted identifiers
		assertThat( metadata.useQuotedIdentifiers() ).isTrue();

		// default cascades are additive
		assertThat( metadata.getDefaultCascadeTypes() ).containsAll( List.of( PERSIST, REMOVE, LOCK ) );

		// simple1.xml should take precedence
		assertThat( metadata.getDefaultCatalog() ).isEqualTo( "catalog1" );
		assertThat( metadata.getDefaultSchema() ).isEqualTo( "schema1" );
		assertThat( metadata.getAccessType() ).isEqualTo( FIELD );
		assertThat( metadata.getDefaultAccessStrategyName() ).isEqualTo( "MIXED" );
	}

	@Test
	void testSimpleXmlDocumentBuilding() {
		final XmlResources collector = new XmlResources();

		final JaxbEntityMappings simple1 = loadMapping( "mappings/simple1.xml", SIMPLE_CLASS_LOADING );
		final JaxbEntityMappings simple2 = loadMapping( "mappings/simple2.xml", SIMPLE_CLASS_LOADING );
		collector.addDocument( simple1 );
		collector.addDocument( simple2 );

		final PersistenceUnitMetadata metadata = collector.getPersistenceUnitMetadata();

		final XmlDocumentImpl simple1Doc = XmlDocumentImpl.consume( simple1, metadata );
		assertThat( simple1Doc.getDefaults().getPackage() ).isEqualTo( "org.hibernate.models.orm.xml" );
		assertThat( simple1Doc.getEntityMappings() ).hasSize( 1 );
		assertThat( simple1Doc.getEntityMappings().get( 0 ).getClazz() ).isEqualTo( "SimpleEntity" );
		assertThat( simple1Doc.getEntityMappings().get( 0 ).getName() ).isNull();
		assertThat( simple1Doc.getEntityMappings().get( 0 ).isMetadataComplete() ).isNull();

		final XmlDocumentImpl simple2Doc = XmlDocumentImpl.consume( simple2, metadata );
		assertThat( simple2Doc.getDefaults().getPackage() ).isNull();
		assertThat( simple2Doc.getEntityMappings() ).hasSize( 1 );
		assertThat( simple2Doc.getEntityMappings().get( 0 ).getClazz() ).isNull();
		assertThat( simple2Doc.getEntityMappings().get( 0 ).getName() ).isEqualTo( "DynamicEntity" );
		assertThat( simple2Doc.getEntityMappings().get( 0 ).isMetadataComplete() ).isTrue();
	}

	@Test
	void testSimpleGlobalXmlProcessing() {
		final SourceModelBuildingContext buildingContext = SourceModelTestHelper.createBuildingContext();
		final XmlResources collectedXmlResources = new XmlResources();

		final JaxbEntityMappings xmlMapping = loadMapping( "mappings/globals.xml", SIMPLE_CLASS_LOADING );
		collectedXmlResources.addDocument( xmlMapping );

		final ProcessResultCollector processResultCollector = new ProcessResultCollector( buildingContext.getClassDetailsRegistry() );
		collectedXmlResources.getDocuments().forEach( (jaxbRoot) -> {
			processResultCollector.collectJavaTypeRegistrations( jaxbRoot.getJavaTypeRegistrations() );
			processResultCollector.collectJdbcTypeRegistrations( jaxbRoot.getJdbcTypeRegistrations() );
			processResultCollector.collectConverterRegistrations( jaxbRoot.getConverterRegistrations() );
			processResultCollector.collectUserTypeRegistrations( jaxbRoot.getUserTypeRegistrations() );
			processResultCollector.collectCompositeUserTypeRegistrations( jaxbRoot.getCompositeUserTypeRegistrations() );
			processResultCollector.collectCollectionTypeRegistrations( jaxbRoot.getCollectionUserTypeRegistrations() );
			processResultCollector.collectEmbeddableInstantiatorRegistrations( jaxbRoot.getEmbeddableInstantiatorRegistrations() );
		} );

		assertThat( processResultCollector.getJavaTypeRegistrations() ).hasSize( 1 );
		assertThat( processResultCollector.getJavaTypeRegistrations().get(0).getDescriptor().getClassName() )
				.isEqualTo( StringTypeDescriptor.class.getName() );

		assertThat( processResultCollector.getJdbcTypeRegistrations() ).hasSize( 1 );
		assertThat( processResultCollector.getJdbcTypeRegistrations().get(0).getDescriptor().getClassName() )
				.isEqualTo( ClobJdbcType.class.getName() );

		assertThat( processResultCollector.getUserTypeRegistrations() ).hasSize( 1 );
		assertThat( processResultCollector.getUserTypeRegistrations().get(0).getUserTypeClass().getClassName() )
				.isEqualTo( MyUserType.class.getName() );

		assertThat( processResultCollector.getConverterRegistrations() ).hasSize( 1 );
		assertThat( processResultCollector.getConverterRegistrations().get(0).getConverterType().getClassName() )
				.isEqualTo( org.hibernate.type.YesNoConverter.class.getName() );
	}
}
