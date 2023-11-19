/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.orm.categorize.xml.internal.attr;

import org.hibernate.boot.internal.Target;
import org.hibernate.boot.jaxb.mapping.spi.JaxbEmbeddedImpl;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.models.internal.MutableAnnotationUsage;
import org.hibernate.models.internal.MutableClassDetails;
import org.hibernate.models.internal.MutableMemberDetails;
import org.hibernate.models.orm.categorize.xml.internal.XmlAnnotationHelper;
import org.hibernate.models.orm.categorize.xml.internal.XmlProcessingHelper;
import org.hibernate.models.orm.categorize.xml.spi.XmlDocumentContext;

import jakarta.persistence.AccessType;
import jakarta.persistence.Embedded;

import static org.hibernate.internal.util.NullnessHelper.coalesce;
import static org.hibernate.models.orm.categorize.xml.internal.XmlProcessingHelper.getOrMakeAnnotation;
import static org.hibernate.models.orm.categorize.xml.internal.attr.CommonAttributeProcessing.applyAttributeBasics;

/**
 * @author Steve Ebersole
 */
public class EmbeddedAttributeProcessing {
	public static MutableMemberDetails processEmbeddedAttribute(
			JaxbEmbeddedImpl jaxbEmbedded,
			MutableClassDetails declarer,
			AccessType classAccessType,
			XmlDocumentContext xmlDocumentContext) {
		final AccessType accessType = coalesce( jaxbEmbedded.getAccess(), classAccessType );
		final MutableMemberDetails memberDetails = XmlProcessingHelper.getAttributeMember(
				jaxbEmbedded.getName(),
				accessType,
				declarer
		);

		final MutableAnnotationUsage<Embedded> embeddedAnn = getOrMakeAnnotation( Embedded.class, memberDetails, xmlDocumentContext );

		if ( StringHelper.isNotEmpty( jaxbEmbedded.getTarget() ) ) {
			final MutableAnnotationUsage<Target> targetAnn = getOrMakeAnnotation( Target.class, memberDetails, xmlDocumentContext );
			targetAnn.setAttributeValue( "value", jaxbEmbedded.getTarget() );
		}

		applyAttributeBasics( jaxbEmbedded, memberDetails, embeddedAnn, accessType, xmlDocumentContext );
		XmlAnnotationHelper.applyAttributeOverrides( jaxbEmbedded.getAttributeOverrides(), memberDetails, xmlDocumentContext );
		XmlAnnotationHelper.applyAssociationOverrides( jaxbEmbedded.getAssociationOverrides(), memberDetails, xmlDocumentContext );

		return memberDetails;
	}
}
