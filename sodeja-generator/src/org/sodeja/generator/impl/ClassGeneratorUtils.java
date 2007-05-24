package org.sodeja.generator.impl;

import org.sodeja.generator.java.JavaClass;
import org.sodeja.generator.java.JavaMethod;
import org.sodeja.generator.java.JavaPackage;
import org.sodeja.generator.java.JavaParameter;
import org.sodeja.generator.java.JavaType;
import org.sodeja.generator.uml.UmlClass;
import org.sodeja.generator.uml.UmlDataType;
import org.sodeja.generator.uml.UmlOperation;
import org.sodeja.generator.uml.UmlParameter;
import org.sodeja.generator.uml.UmlReference;
import org.sodeja.generator.uml.UmlType;

public class ClassGeneratorUtils {
	protected static JavaMethod createMethod(JavaClass domainClass, UmlOperation modelOperation) {
		JavaType resultType = ClassGeneratorUtils.getJavaType(modelOperation.getResult().getType());
		JavaMethod method = new JavaMethod(resultType, modelOperation.getName());
		method.setCustom(modelOperation.getId());
		
		for(UmlParameter modelParam : modelOperation.getParameters()) {
			JavaType paramType = ClassGeneratorUtils.getJavaType(modelParam.getType());
			method.addParameter(new JavaParameter(paramType, modelParam.getName()));
		}
		
		return method;
	}

	protected static JavaType getJavaType(UmlReference<? extends UmlType> modelReference) {
		return new JavaType(getJavaClass(modelReference));
	}
	
	protected static JavaClass getJavaClass(UmlReference<? extends UmlType> modelReference) {
		return getJavaClass(modelReference.getReferent());
	}
	
	protected static JavaType getJavaType(UmlType modelType) {
		return new JavaType(getJavaClass(modelType));
	}
	
	protected static JavaClass getJavaClass(UmlType modelType) {
		if(modelType instanceof UmlDataType) {
			return new JavaClass(null, modelType.getName());
		} else if(modelType instanceof UmlClass) {
			return getJavaClass((UmlClass) modelType);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	protected static JavaClass getJavaClass(UmlClass modelClass) {
		JavaPackage pack = JavaPackage.createFromDots(modelClass.getParentPackage().getFullName());
		return new JavaClass(pack, modelClass.getName());
	}
}
