/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.cfg.annotations.reflection;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityListeners;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQuery;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.TableGenerator;

import org.hibernate.annotations.common.reflection.AnnotationReader;
import org.hibernate.annotations.common.reflection.MetadataProvider;
import org.hibernate.annotations.common.reflection.java.JavaMetadataProvider;
import org.hibernate.boot.internal.ClassLoaderAccessImpl;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.registry.classloading.spi.ClassLoadingException;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.boot.spi.ClassLoaderAccess;
import org.hibernate.boot.spi.ClassLoaderAccessDelegateImpl;
import org.hibernate.boot.spi.MetadataBuildingOptions;

import org.dom4j.Element;

/**
 * MetadataProvider aware of the JPA Deployment descriptor
 *
 * @author Emmanuel Bernard
 */
@SuppressWarnings("unchecked")
public class JPAMetadataProvider implements MetadataProvider {
	private final MetadataProvider delegate = new JavaMetadataProvider();

	private final ClassLoaderAccess classLoaderAccess;
	private final XMLContext xmlContext;


	private Map<Object, Object> defaults;
	private Map<AnnotatedElement, AnnotationReader> cache = new HashMap<AnnotatedElement, AnnotationReader>(100);

	/**
	 * @deprecated Use {@link JPAMetadataProvider#JPAMetadataProvider(BootstrapContext)} instead.
	 */
	@Deprecated
	public JPAMetadataProvider(final MetadataBuildingOptions metadataBuildingOptions) {
		this( new ClassLoaderAccessDelegateImpl() {
			ClassLoaderAccess delegate;

			@Override
			protected ClassLoaderAccess getDelegate() {
				if ( delegate == null ) {
					delegate = new ClassLoaderAccessImpl(
							metadataBuildingOptions.getTempClassLoader(),
							metadataBuildingOptions.getServiceRegistry().getService( ClassLoaderService.class )
					);
				}
				return delegate;
			}
		} );
	}

	public JPAMetadataProvider(BootstrapContext bootstrapContext) {
		this( bootstrapContext.getClassLoaderAccess() );
	}

	JPAMetadataProvider(ClassLoaderAccess classLoaderAccess) {
		this.classLoaderAccess = classLoaderAccess;
		this.xmlContext = new XMLContext( classLoaderAccess );
		;
	}

	//all of the above can be safely rebuilt from XMLContext: only XMLContext this object is serialized
	@Override
	public AnnotationReader getAnnotationReader(AnnotatedElement annotatedElement) {
		AnnotationReader reader = cache.get( annotatedElement );
		if (reader == null) {
			if ( xmlContext.hasContext() ) {
				throw new UnsupportedOperationException( "This version does not support mapping overrides via XML" );
			}
			else {
				reader = delegate.getAnnotationReader( annotatedElement );
			}
			cache.put(annotatedElement, reader);
		}
		return reader;
	}
	@Override
	public Map<Object, Object> getDefaults() {
		return Collections.EMPTY_MAP;
	}

	public XMLContext getXMLContext() {
		return xmlContext;
	}
}
