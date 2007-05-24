package org.sodeja.generator.java;

import java.util.ArrayList;
import java.util.List;

public class JavaField implements Annotateable, AccessModifiable {
	protected JavaAccessModifier accessModifier = JavaAccessModifier.PRIVATE;
	protected JavaType type;
	protected String name;
	protected List<JavaAnnotation> annotations;
	
	public JavaField(JavaType type, String name) {
		this.type = type;
		this.name = name;
		this.annotations = new ArrayList<JavaAnnotation>();
	}

	public JavaType getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public List<JavaAnnotation> getAnnotations() {
		return annotations;
	}

	public void addAnnotation(JavaAnnotation annotation) {
		annotations.add(annotation);
	}
	
	public void addAnnotation(JavaClass clazz) {
		addAnnotation(new JavaAnnotation(clazz));
	}
	
	public void addAnnotation(JavaClass clazz, String value) {
		addAnnotation(new JavaAnnotation(clazz, value));
	}
	
	public JavaAccessModifier getAccessModifier() {
		return accessModifier;
	}
}
