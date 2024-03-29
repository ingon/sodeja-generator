package org.sodeja.generator.impl;

import org.sodeja.generator.java.JavaClass;
import org.sodeja.generator.java.JavaField;
import org.sodeja.generator.java.JavaMember;
import org.sodeja.generator.java.JavaPackage;
import org.sodeja.generator.uml.UmlAggregationType;
import org.sodeja.generator.uml.UmlAssociation;
import org.sodeja.generator.uml.UmlAssociationEnd;
import org.sodeja.generator.uml.UmlAttribute;
import org.sodeja.generator.uml.UmlClass;
import org.sodeja.generator.uml.UmlDependency;
import org.sodeja.generator.uml.UmlEnumeration;
import org.sodeja.generator.uml.UmlModel;
import org.sodeja.generator.uml.UmlOperation;
import org.sodeja.generator.uml.UmlType;
import org.sodeja.lang.StringUtils;

public class HibernateClassGenerator extends ClassGenerator {
	
	private static final JavaClass LONG = new JavaClass(null, "Long");
	
	private static final JavaPackage PERSISTANCE_PACKAGE = JavaPackage.createFromDots("javax.persistence");
	private static final JavaClass PERSISTANCE_BASIC = new JavaClass(PERSISTANCE_PACKAGE, "Basic");
	private static final JavaClass PERSISTANCE_CASCADE_TYPE = new JavaClass(PERSISTANCE_PACKAGE, "CascadeType");
	private static final JavaClass PERSISTANCE_EMBEDDED = new JavaClass(PERSISTANCE_PACKAGE, "Embedded");
	private static final JavaClass PERSISTANCE_EMBEDDABLE = new JavaClass(PERSISTANCE_PACKAGE, "Embeddable");
	private static final JavaClass PERSISTANCE_ENTITY = new JavaClass(PERSISTANCE_PACKAGE, "Entity");
	private static final JavaClass PERSISTANCE_ENUMERATED = new JavaClass(PERSISTANCE_PACKAGE, "Enumerated");
	private static final JavaClass PERSISTANCE_ENUM_TYPE = new JavaClass(PERSISTANCE_PACKAGE, "EnumType");
//	private static final JavaClass PERSISTANCE_FETCH_TYPE = new JavaClass(PERSISTANCE_PACKAGE, "FetchType");
	private static final JavaClass PERSISTANCE_GENERATED_VALUE = new JavaClass(PERSISTANCE_PACKAGE, "GeneratedValue");
	private static final JavaClass PERSISTANCE_GENERATION_TYPE = new JavaClass(PERSISTANCE_PACKAGE, "GenerationType");
	private static final JavaClass PERSISTANCE_ID = new JavaClass(PERSISTANCE_PACKAGE, "Id");
	private static final JavaClass PERSISTANCE_INHERITANCE = new JavaClass(PERSISTANCE_PACKAGE, "Inheritance");
	private static final JavaClass PERSISTANCE_INHERITANCE_TYPE = new JavaClass(PERSISTANCE_PACKAGE, "InheritanceType");
	private static final JavaClass PERSISTANCE_JOIN_COLUMN = new JavaClass(PERSISTANCE_PACKAGE, "JoinColumn");
	private static final JavaClass PERSISTANCE_JOIN_TABLE = new JavaClass(PERSISTANCE_PACKAGE, "JoinTable");
	private static final JavaClass PERSISTANCE_MAPPED_SUPERCLASS = new JavaClass(PERSISTANCE_PACKAGE, "MappedSuperclass");
	private static final JavaClass PERSISTANCE_ONE_TO_MANY = new JavaClass(PERSISTANCE_PACKAGE, "OneToMany");
	private static final JavaClass PERSISTANCE_ONE_TO_ONE = new JavaClass(PERSISTANCE_PACKAGE, "OneToOne");
	private static final JavaClass PERSISTANCE_MANY_TO_MANY = new JavaClass(PERSISTANCE_PACKAGE, "ManyToMany");
	private static final JavaClass PERSISTANCE_MANY_TO_ONE = new JavaClass(PERSISTANCE_PACKAGE, "ManyToOne");
	private static final JavaClass PERSISTANCE_SEQUENCE_GENERATOR = new JavaClass(PERSISTANCE_PACKAGE, "SequenceGenerator");
	private static final JavaClass PERSISTANCE_TABLE = new JavaClass(PERSISTANCE_PACKAGE, "Table");
	
	private static final JavaPackage HIBERNATE_PACKAGE = JavaPackage.createFromDots("org.hibernate.annotations");
	private static final JavaClass HIBERNATE_ACCESS_TYPE = new JavaClass(HIBERNATE_PACKAGE, "AccessType");
	private static final JavaClass HIBERNATE_CASCADE = new JavaClass(HIBERNATE_PACKAGE, "Cascade");
//	private static final JavaClass HIBERNATE_COLLECTION_OF_ELEMENTS = new JavaClass(HIBERNATE_PACKAGE, "CollectionOfElements");
	private static final JavaClass HIBERNATE_INDEX_COLUMN = new JavaClass(HIBERNATE_PACKAGE, "IndexColumn");

	@Override
	protected JavaClass createJavaClass(JavaPackage domainPackage, UmlModel model, UmlClass modelType) {
		JavaClass clazz = super.createJavaClass(domainPackage, model, modelType);
		if(! (modelType instanceof UmlClass)) {
			return clazz;
		}
		
		UmlClass modelClass = (UmlClass) modelType;
		if(GeneratorUtils.isEmbedded(modelClass)) {
			addEmbeddedAnnotations(clazz);
		} else {
			addDomainAnnotations(model, modelClass, clazz);
		}
		return clazz;
	}
	
	@Override
	protected void createAttributes(JavaClass domainClass, UmlModel model, UmlClass modelClass) {
		if(! GeneratorUtils.isEmbedded(modelClass) && ! GeneratorUtils.isChild(modelClass, getStereotype())) {
			addDomainId(domainClass);
		}
		
		super.createAttributes(domainClass, model, modelClass);
	}

	@Override
	protected JavaField createField(JavaClass domainClass, UmlModel model, UmlAttribute attribute) {
		JavaField field = super.createField(domainClass, model, attribute);
		
		UmlType otherType = attribute.getType().getReferent();
		if(otherType instanceof UmlClass) {
			UmlClass otherClass = (UmlClass) otherType;
			if(GeneratorUtils.isEmbedded(otherClass)) { 
				field.addAnnotation(PERSISTANCE_EMBEDDED);
			} else {
				field.addAnnotation(PERSISTANCE_BASIC);
			}
		} else if(otherType instanceof UmlEnumeration) {
			domainClass.addImport(PERSISTANCE_ENUM_TYPE);
			field.addAnnotation(PERSISTANCE_ENUMERATED, "EnumType.STRING");
		} else {
			field.addAnnotation(PERSISTANCE_BASIC);
		}
		
		return field;
	}

	@Override
	protected JavaField createField(JavaClass domainClass, UmlModel model, UmlClass modelClass, UmlAssociation modelAssociation) {
		JavaField field = super.createField(domainClass, model, modelClass, modelAssociation);
		if(field == null) {
			return null;
		}
		
		UmlAssociationEnd thisModelEnd = modelAssociation.getThisEnd(modelClass);
		UmlAssociationEnd otherModelEnd = modelAssociation.getOtherEnd(modelClass);
		UmlClass otherModelClass = (UmlClass) otherModelEnd.getReferent().getReferent();
		
		if(GeneratorUtils.isEmbedded(otherModelClass)) {
			field.addAnnotation(PERSISTANCE_EMBEDDED);
		} else if(GeneratorUtils.isEnum(otherModelClass)) {
			throw new IllegalArgumentException("Implementation changed to use a UmlEnumeration type");
//			domainClass.addImport(PERSISTANCE_ENUM_TYPE);
//			field.addAnnotation(PERSISTANCE_ENUMERATED, "EnumType.STRING");
		} else if(otherModelEnd.getRange().isMulty()) {
			if(thisModelEnd.getType() == UmlAggregationType.COMPOSITE) {
				field.addAnnotation(PERSISTANCE_ONE_TO_MANY, getTargetEntryString(modelClass, otherModelClass));
				field.addAnnotation(HIBERNATE_INDEX_COLUMN, "name=\"number_in_row\"");
				field.addAnnotation(HIBERNATE_CASCADE, "value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN}");
			} else {
				field.addAnnotation(PERSISTANCE_MANY_TO_MANY, getTargetEntryString(modelClass, otherModelClass));
				field.addAnnotation(HIBERNATE_INDEX_COLUMN, "name=\"number_in_row\"");
			}
		} else if(thisModelEnd.getType() == UmlAggregationType.COMPOSITE) {
			domainClass.addImport(PERSISTANCE_CASCADE_TYPE);
			field.addAnnotation(PERSISTANCE_ONE_TO_ONE, "cascade=CascadeType.ALL" + getTargetEntryStringC(modelClass, otherModelClass));
		} else if(thisModelEnd.getRange().isMulty()) {
			field.addAnnotation(PERSISTANCE_MANY_TO_ONE);
			domainClass.addImport(PERSISTANCE_JOIN_COLUMN);
			field.addAnnotation(PERSISTANCE_JOIN_TABLE, String.format("name=\"%s\", " +
					"joinColumns = @JoinColumn(name = \"%s\"), " +
					"inverseJoinColumns = @JoinColumn(name = \"%s\")", 
					getJoinTableName(modelClass, otherModelClass), 
					getJoinColumns(thisModelEnd),
					getInverseJoinColumns(otherModelClass)));
		} else {
			field.addAnnotation(PERSISTANCE_ONE_TO_ONE, getTargetEntryString(modelClass, otherModelClass));
		}
		return field;
	}

	private String getJoinTableName(UmlClass modelClass, UmlClass otherModelClass) {
		return NamingUtils.getTableName(otherModelClass.getName()) + "_" + NamingUtils.getTableName(modelClass.getName());
	}
	
	private String getJoinColumns(UmlAssociationEnd thisModelEnd) {
		return thisModelEnd.getName() + "_id";
	}
	
	private String getInverseJoinColumns(UmlClass otherModelClass) {
		return NamingUtils.getTableName(otherModelClass.getName()) + "_id";
	}
	
	private String getTargetEntryStringC(UmlClass modelClass, UmlClass otherModelClass) {
		String value = getTargetEntryString(modelClass, otherModelClass);
		if(StringUtils.isTrimmedEmpty(value)) {
			return "";
		}
		return ", " + value;
	}
	
	private String getTargetEntryString(UmlClass modelClass, UmlClass otherModelClass) {
		UmlDependency dependency = getDependency(modelClass, otherModelClass);
		if(dependency == null) {
			return "";
		}
		return "targetEntity=" + ClassGeneratorUtils.getJavaClass(otherModelClass).getName() + ".class";
	}
	
	@Override
	protected JavaMember createMethod(JavaClass domainClass, UmlModel model, UmlOperation modelOperation) {
		if(GeneratorUtils.isDao(modelOperation)) {
			return null;
		}
		
		return super.createMethod(domainClass, model, modelOperation);
	}

	private void addEmbeddedAnnotations(JavaClass clazz) {
		clazz.addAnnotation(PERSISTANCE_EMBEDDABLE);
		clazz.addAnnotation(HIBERNATE_ACCESS_TYPE, "\"field\"");
	}
	
	private void addDomainAnnotations(UmlModel model, UmlClass modelClass, JavaClass clazz) {
		if(GeneratorUtils.isNotRootParent(model, modelClass)) {
			clazz.addAnnotation(PERSISTANCE_MAPPED_SUPERCLASS);
			return;
		}
		
		addDomainAnnotations(clazz);
		
		if(GeneratorUtils.isParent(model, modelClass)) {
			clazz.addImport(PERSISTANCE_INHERITANCE_TYPE);
			clazz.addAnnotation(PERSISTANCE_INHERITANCE, "strategy=InheritanceType.JOINED");
		}
	}
	
	private void addDomainAnnotations(JavaClass clazz) {
		String mappingName = NamingUtils.getTableName(clazz.getName());
		
		clazz.addAnnotation(PERSISTANCE_ENTITY);
		clazz.addAnnotation(HIBERNATE_ACCESS_TYPE, "\"field\"");
		clazz.addAnnotation(PERSISTANCE_TABLE, "name=\"" + mappingName + "\"");
		clazz.addAnnotation(PERSISTANCE_SEQUENCE_GENERATOR, "name=\"SEQ_GENERATOR\", sequenceName=\"" + mappingName + "_id_seq\"");
	}

	private void addDomainId(JavaClass clazz) {
		clazz.addImport(PERSISTANCE_GENERATION_TYPE);
		
		JavaField idField = new JavaField(LONG, "id");
		idField.addAnnotation(PERSISTANCE_ID);
		idField.addAnnotation(PERSISTANCE_GENERATED_VALUE, "strategy=GenerationType.SEQUENCE, generator=\"SEQ_GENERATOR\"");
		clazz.addField(idField);
		
		clazz.addMethod(createGetter("id", idField.getType()));
	}
}
